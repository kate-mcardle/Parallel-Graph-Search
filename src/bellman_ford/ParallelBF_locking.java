package bellman_ford;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBF_locking extends BellmanFord {
	private int n_threads;
	MyBlockingQueue<Integer> nodesToRelax;
	double[] distTo;
	Edge[] edgeTo;
	boolean[] nodesOnQueue;
	ReentrantLock[] locks;
	ReentrantLock waitingLock = new ReentrantLock();
	Condition allThreadsWaiting = waitingLock.newCondition();
	
	public ParallelBF_locking(Graph graph, int n_threads) {
		super(graph);
        nodesToRelax = new MyBlockingQueue<Integer>(graph.n_nodes, n_threads, waitingLock, allThreadsWaiting);
        this.n_threads = n_threads;
	    distTo  = new double[graph.n_nodes];
	    edgeTo  = new Edge[graph.n_nodes];
	    nodesOnQueue = new boolean[graph.n_nodes];
	    for (int v = 0; v < graph.n_nodes; v++) {
	        distTo[v] = Double.POSITIVE_INFINITY;
	    }
        this.locks = new ReentrantLock[graph.n_nodes];
        for (int i = 0; i < graph.n_nodes; i++) {
        	locks[i] = new ReentrantLock();
        }
	}

    public void run_bf(int source) {
        distTo[source] = 0.0;
        BFThread[] threads = new BFThread[n_threads];
        for (int i = 0; i < n_threads; i++) {
        	threads[i] = new BFThread(nodesToRelax);
        }
        // Fill nodesToRelax queue with some nodes we know must be relaxed
        nodesToRelax.add(source);
        nodesOnQueue[source] = true;
//        for (Edge e : graph.adjacencyList.get(source)) {
//        	nodesToRelax.add(e.destination);
//        	nodesOnQueue[e.destination] = true;
//        }
        // Start threads
        for (int i = 0; i < n_threads; i++) {
        	threads[i].start();
        }
        // Wait until all threads are found to be waiting
        waitingLock.lock();
        try {
        	while (nodesToRelax.n_threads_waiting != n_threads) {
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
    	private volatile BlockingQueue<Integer> q;
    	
    	public BFThread(BlockingQueue<Integer> q) {
    		this.q = q;
    	}
    	
    	public void run() {
    		while (true) {
        		Integer u = null;
				try {
					u = q.take();
				} catch (InterruptedException e1) {
					// main thread will interrupt threads when it receives signal that all threads are waiting on empty queue
					break;
				}
				locks[u].lock();
				try {
					nodesOnQueue[u] = false;
				} finally {
					locks[u].unlock();
				}
        		for (Edge e : graph.adjacencyList.get(u)) {
        			int v = e.destination;
        			locks[v].lock();
        			try {
            			if (distTo[v] > distTo[u] + e.weight) {
            				distTo[v] = distTo[u] + e.weight;
            				edgeTo[v] = e;
            				if (!nodesOnQueue[v]) {
            					q.add(v);
            					nodesOnQueue[v] = true;
            				}	            				
            			}
        			} finally {
        				locks[v].unlock();
        			}
			
        		}
    		}
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
