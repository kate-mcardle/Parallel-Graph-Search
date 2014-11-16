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
import java.util.concurrent.locks.ReentrantLock;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF_twoQueues extends BellmanFord {
	private ExecutorService threadPool;
	private int n_threads;
	private Queue<Integer> current;
	private Queue<Integer> next;
	private ReentrantLock[] locks;
	private String type;
	
	public ParallelBF_twoQueues(Graph graph, String type, int n_threads) {
		super(graph);
		this.type = type;
        if (type.equals("lock-free")) {
        	current = new ConcurrentLinkedQueue<Integer>();
        	next = new ConcurrentLinkedQueue<Integer>();
        }
        else if (type.equals("lock-based")) {
        	current = new ArrayBlockingQueue<Integer>(graph.n_nodes);
        	next = new ArrayBlockingQueue<Integer>(graph.n_nodes);
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
        next.add(source);
        int iter = 0;
        while (!next.isEmpty()) {
        	current = next;
            if (type.equals("lock-free")) {
            	next = new ConcurrentLinkedQueue<Integer>();
            }
            else if (type.equals("lock-based")) {
            	next = new ArrayBlockingQueue<Integer>(graph.n_nodes);
            }
            else {
            	System.out.println("Not an implementation!");
            	System.exit(-1);
            }
            int i = 0;
            List<Future<Boolean> > futures = new ArrayList<Future<Boolean> >();
            while (!current.isEmpty()) {
            	int v = current.remove();
            	for (Edge e : graph.adjacencyList.get(v)) {
	            	if (i >= tasks.size()) {
	            		tasks.add(new Task());
	            	}
	        		Task task = tasks.get(i++);
	        		task.e = e;
	        		task.v = v;
	        		futures.add(threadPool.submit(task));
            	}
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
            iter++;
        }
        
		try {
			threadPool.shutdown();
			// wait until we have finished launching searches of all nodes at this level
			threadPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("number of iterations = " + iter);
    }
    
    public class Task implements Callable<Boolean> {
    	int v;
    	Edge e;

		@Override
		public Boolean call() {
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
                    if (!next.contains(w)) {
                        next.add(w);
                    }
                }
            return true;
		}
    }
}
