package bellman_ford;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF extends BellmanFord {
	private ExecutorService threadPool;
	private int n_threads;
	Queue<Integer> nodesToRelax;
	double[] distTo;
	Edge[] edgeTo;
	boolean[] nodesOnQueue;
	
	public ParallelBF(Graph graph, String type, int n_threads) {
		super(graph);
        if (type.equals("lock-free")) {
        	nodesToRelax = new ConcurrentLinkedQueue<Integer>();
        }
        else if (type.equals("lock-based")) {
        	nodesToRelax = new ArrayBlockingQueue<Integer>(graph.n_nodes);
        }
        else {
        	System.out.println("Not an implementation!");
        	System.exit(-1);
        }
        this.n_threads = n_threads;
	    distTo  = new double[graph.n_nodes];
	    edgeTo  = new Edge[graph.n_nodes];
	    nodesOnQueue = new boolean[graph.n_nodes];
	    for (int v = 0; v < graph.n_nodes; v++) {
	        distTo[v] = Double.POSITIVE_INFINITY;
	    }
	}

    public void run_bf(int source) {
        distTo[source] = 0.0;
        
        List<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < 10; i++) {
        	tasks.add(new Task());
        }
        threadPool = Executors.newFixedThreadPool(n_threads);

        // Bellman-Ford algorithm
        nodesToRelax.add(source);
        while (!nodesToRelax.isEmpty()) {
            int v = nodesToRelax.remove();
            int i = 0;
            List<Future<Double> > futures = new ArrayList<Future<Double> >();
            for (Edge e : graph.adjacencyList.get(v)) {
            	if (i >= tasks.size()) {
            		tasks.add(new Task());
            	}
            	Task task = tasks.get(i++);
            	task.e = e;
            	task.v = v;
            	futures.add(threadPool.submit(task));
            }
            // "Barrier"
            for (int j = 0; j < i; j++) {
            	try {
					futures.get(j).get();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
        
		try {
			threadPool.shutdown();
			// wait until we have finished launching searches of all nodes at this level
			threadPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public class Task implements Callable<Double> {
    	
    	Edge e;
    	int v;

		@Override
		public Double call() {
            int w = e.destination;
            if (distTo[w] > distTo[v] + e.weight) {
                distTo[w] = distTo[v] + e.weight;
                edgeTo[w] = e;
                if (!nodesToRelax.contains(w)) {
                    nodesToRelax.add(w);
                }
            }
            return distTo[w];
		}
    }

	@Override
	public double[] getDistances() {
		return distTo;
	}

	@Override
	public Edge[] getEdges() {
		return edgeTo;
	}
}
