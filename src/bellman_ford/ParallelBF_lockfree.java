package bellman_ford;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF_lockfree extends BellmanFord {
	private int n_threads;
	Queue<Integer> nodesToRelax;
	AtomicReferenceArray<DistEdge> distEdges;
	AtomicIntegerArray nodesOnQueue;
	AtomicInteger n_threads_waiting;
	ReentrantLock waitingLock = new ReentrantLock(); // used only for main thread
	Condition allThreadsWaiting = waitingLock.newCondition();
	
	public ParallelBF_lockfree(Graph graph, int n_threads) {
		super(graph);
        nodesToRelax = new ConcurrentLinkedQueue<Integer>();
        this.n_threads = n_threads;
        distEdges = new AtomicReferenceArray<DistEdge>(graph.n_nodes);
        nodesOnQueue = new AtomicIntegerArray(graph.n_nodes);
        n_threads_waiting = new AtomicInteger();
        for (int v = 0; v < graph.n_nodes; v++) {
        	distEdges.set(v, new DistEdge(Double.POSITIVE_INFINITY, null));
        }
	}

    public void run_bf(int source) {
        distEdges.set(source, new DistEdge(0.0, null));
        BFThread[] threads = new BFThread[n_threads];
        for (int i = 0; i < n_threads; i++) {
        	threads[i] = new BFThread(nodesToRelax, n_threads_waiting, n_threads);
        }
        // Fill nodesToRelax queue with some nodes we know must be relaxed
        nodesToRelax.add(source);
        nodesOnQueue.set(source, 1);
//        for (Edge e : graph.adjacencyList.get(source)) {
//        	nodesToRelax.add(e.destination);
//        	nodesOnQueue.set(e.destination, 1);
//        }
        // Start threads
        for (int i = 0; i < n_threads; i++) {
        	threads[i].start();
        }
        // Wait until all threads are found to be waiting
        waitingLock.lock();
        try {
        	while (n_threads_waiting.get() != n_threads) {
        		try {
					allThreadsWaiting.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        } finally {
        	waitingLock.unlock();
        }
        
        // Interrupt all threads
        for (int i = 0; i < n_threads; i++) {
        	threads[i].interrupt();
        }
    }
    
    public class BFThread extends Thread {
    	private volatile Queue<Integer> q;
    	private int n_threads;
    	private AtomicInteger n_threads_waiting;
    	
    	public BFThread(Queue<Integer> q, AtomicInteger n_threadsWaiting, int n_threads) {
    		this.q = q;
    		this.n_threads_waiting = n_threadsWaiting;
    		this.n_threads = n_threads;
    	}
    	
    	public void run() {
    		outer : while (true) {
    			Integer u = q.poll();
    			if (u == null) {
    				int n = n_threads_waiting.incrementAndGet();
    				if (n == n_threads) {
    					waitingLock.lock();
    					try {
    						allThreadsWaiting.signal();
    					} finally {
    						waitingLock.unlock();
    					}
    					break;
    				}
    				while (u == null) {
						if (Thread.currentThread().isInterrupted()) {
    						break outer;
    					}
    					// spin for some time, to not hog CPU
    					for (int i = 0; i < 1000; i++) { }
    					u = q.poll();
    				}
    				n_threads_waiting.getAndDecrement();
    			}
    			nodesOnQueue.set(u, 0);

    			for (Edge e : graph.adjacencyList.get(u)) {
    				int v = e.destination;
    				while (true) {
        				DistEdge de = distEdges.get(v);
        				double old_dist = de.distance;
        				double dist_to_u = distEdges.get(u).distance;
        				if (old_dist > dist_to_u + e.weight) {
        					if (distEdges.compareAndSet(v, de, new DistEdge(dist_to_u+e.weight, e))) {
        						if (nodesOnQueue.compareAndSet(v,0,1)) {
        							q.add(v);
        						}
        						break;
        					}
        				}
        				else { break; }
    				}
    			} 
    		}
    	}
    }

	@Override
	public double[] getDistances() {
		double[] distances = new double[graph.n_nodes];
		for (int i = 0; i < graph.n_nodes; i++) {
			distances[i] = distEdges.get(i).distance;
		}
		return distances;
	}

	@Override
	public Edge[] getEdges() {
		Edge[] edges = new Edge[graph.n_nodes];
		for (int i = 0; i < graph.n_nodes; i++) {
			edges[i] = distEdges.get(i).edge;
		}
		return edges;
	}
}
