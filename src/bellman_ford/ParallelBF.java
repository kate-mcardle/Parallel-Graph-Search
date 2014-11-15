package bellman_ford;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF extends BellmanFord {
	private ExecutorService threadPool;

    public ParallelBF(Graph graph, int source, String type, int n_threads) {
        distTo  = new double[graph.n_nodes];
        edgeTo  = new Edge[graph.n_nodes];
        for (int v = 0; v < graph.n_nodes; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[source] = 0.0;
        
        if (type.equals("lock-free")) {
        	nodesToRelax = new ConcurrentLinkedQueue<Integer>();
        }
        else {
        	nodesToRelax = new ArrayBlockingQueue<Integer>(graph.n_nodes);
        }
        
        List<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < 10; i++) {
        	tasks.add(new Task());
        }

        // Bellman-Ford algorithm
        nodesToRelax.add(source);
        while (!nodesToRelax.isEmpty()) {
            int v = nodesToRelax.remove();
            threadPool = Executors.newFixedThreadPool(n_threads);
            int i = 0;
            for (Edge e : graph.adjacencyList.get(v)) {
            	if (i >= tasks.size()) {
            		tasks.add(new Task());
            	}
            	Task task = tasks.get(i++);
            	task.e = e;
            	task.v = v;
            	threadPool.execute(task);
            }
        }
    }
    
    public class Task implements Runnable {
    	
    	Edge e;
    	int v;

		@Override
		public void run() {
            int w = e.destination;
            if (distTo[w] > distTo[v] + e.weight) {
                distTo[w] = distTo[v] + e.weight;
                edgeTo[w] = e;
                if (!nodesToRelax.contains(w)) {
                    nodesToRelax.add(w);
                }
            }
		}
    }
}
