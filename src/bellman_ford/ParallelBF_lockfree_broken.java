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

public class ParallelBF_lockfree_broken extends BellmanFord {
	private int n_threads;
	Queue<Integer> nodesToRelax;
	AtomicReferenceArray<Double> distTo;
	AtomicReferenceArray<Edge> edgeTo;
	AtomicIntegerArray nodesOnQueue;
	AtomicInteger n_threads_waiting;
	ReentrantLock waitingLock = new ReentrantLock(); // used only for main thread
	Condition allThreadsWaiting = waitingLock.newCondition();
	
	public ParallelBF_lockfree_broken(Graph graph, int n_threads) {
		super(graph);
        nodesToRelax = new ConcurrentLinkedQueue<Integer>();
        this.n_threads = n_threads;
        distTo = new AtomicReferenceArray<Double>(graph.n_nodes);
        edgeTo = new AtomicReferenceArray<Edge>(graph.n_nodes);
        nodesOnQueue = new AtomicIntegerArray(graph.n_nodes);
        n_threads_waiting = new AtomicInteger();
        for (int v = 0; v < graph.n_nodes; v++) {
        	distTo.set(v, Double.POSITIVE_INFINITY);
        }
	}

    public void run_bf(int source) {
        distTo.set(source, 0.0);
        BFThread[] threads = new BFThread[n_threads];
        for (int i = 0; i < n_threads; i++) {
        	threads[i] = new BFThread(nodesToRelax, n_threads_waiting, n_threads);
        }
        // Fill nodesToRelax queue with some nodes we know must be relaxed
        nodesToRelax.add(source);
        nodesOnQueue.set(source, 1);
        for (Edge e : graph.adjacencyList.get(source)) {
        	nodesToRelax.add(e.destination);
        	nodesOnQueue.set(e.destination, 1);
        }
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
        				Double old_dist = distTo.get(v);
        				Edge old_edge = edgeTo.get(v);
        				Double new_dist = distTo.get(u) + e.weight;
        				if (old_dist > new_dist) {
        					if (distTo.compareAndSet(v, old_dist, new_dist)) {
        						if (edgeTo.compareAndSet(v, old_edge, e)) {
            						if (nodesOnQueue.compareAndSet(v, 0, 1)) {
            							q.add(v);
            						}	
        						}
        						else {
        							distTo.compareAndSet(v, new_dist, old_dist);
        						}
    			
        					}
        				}
    				}

//    				if (old_dist > distTo.get(u) + e.weight) {
//    					distTo.set(v, distTo.get(u)+e.weight);
//    					edgeTo.set(v, e);
//						if (nodesOnQueue.compareAndSet(v, 0, 1)) {
//							q.add(v);
//						}	
//    				}
    			}
    		}
    	}
    }

	@Override
	public double[] getDistances() {
		double[] distances = new double[graph.n_nodes];
		for (int i = 0; i < graph.n_nodes; i++) {
			distances[i] = distTo.get(i);
		}
		return distances;
	}

	@Override
	public Edge[] getEdges() {
		Edge[] edges = new Edge[graph.n_nodes];
		for (int i = 0; i < graph.n_nodes; i++) {
			edges[i] = edgeTo.get(i);
		}
		return edges;
	}
}
