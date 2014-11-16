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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF_locks extends BellmanFord {
	private ExecutorService threadPool;
	private int n_threads;
	private ReentrantLock[] locks;
	Queue<Integer> nodesToRelax;
	
	public ParallelBF_locks(Graph graph, String type, int n_threads) {
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
        this.locks = new ReentrantLock[graph.n_nodes];
        for (int i = 0; i < graph.n_nodes; i++) {
        	locks[i] = new ReentrantLock();
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
        while (!nodesToRelax.isEmpty() || ( ((ThreadPoolExecutor) threadPool).getActiveCount() > 0)) {
            while (nodesToRelax.isEmpty()) { }
        	int v = nodesToRelax.remove();
//            int i = 0;
            for (Edge e : graph.adjacencyList.get(v)) {
//            	if (i >= tasks.size()) {
//            		tasks.add(new Task());
//            	}
//            	Task task = tasks.get(i++);
            	Task task = new Task();
            	task.e = e;
            	task.v = v;
            	threadPool.execute(task);
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
    
    public class Task implements Runnable {
    	
    	Edge e;
    	int v;

		@Override
		public void run() {
            int w = e.destination;
            locks[w].lock();
            boolean flag = false;
            try {
                if (distTo[w] > distTo[v] + e.weight) {
                    distTo[w] = distTo[v] + e.weight;
                    edgeTo[w] = e;
                    flag = true;
                }
            } finally {
            	locks[w].unlock();
            }
            if (flag) {
                if (!nodesToRelax.contains(w)) {
                    nodesToRelax.add(w);
                }
            }
		}
    }
}
