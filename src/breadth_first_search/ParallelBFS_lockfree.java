package breadth_first_search;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBFS_lockfree implements BreadthFirstSearch {

	private Graph graph;
	int[] shortest_hops;
	Queue<Integer> current;
	Queue<Integer> next;
	private int n_threads;
	AtomicInteger n_threads_waiting;
	ReentrantLock waitingLock = new ReentrantLock();
	Condition allThreadsWaiting = waitingLock.newCondition();
	
	public ParallelBFS_lockfree(Graph graph, int num_Threads) {
		this.graph = graph;
		this.n_threads = num_Threads;
		shortest_hops = new int[graph.n_nodes];
		current = new ConcurrentLinkedQueue<Integer>();
		next = new ConcurrentLinkedQueue<Integer>();
		n_threads_waiting = new AtomicInteger();
	}

	@Override
	public int[] search(int source) {
		// Algorithm from Ole Miss paper (Algorithm 1):
		// http://cs.olemiss.edu/heroes/papers/bfs.pdf

		for (int i = 0; i < graph.n_nodes; i++) {
			shortest_hops[i] = Integer.MAX_VALUE;
		}
		shortest_hops[source] = 0;
		current.add(source);
		
		CyclicBarrier barrier = new CyclicBarrier(n_threads, 
				new Runnable() {
					public void run() {
						n_threads_waiting.set(0);
					}
		});
		
		BFSThread[] threads = new BFSThread[n_threads];
		for (int i = 0; i < n_threads; i++) {
			threads[i] = new BFSThread(current, next, barrier, n_threads, n_threads_waiting, waitingLock, allThreadsWaiting);
		}
//		System.out.println("Starting threads");
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
		
		return shortest_hops;
	}

	public class BFSThread extends Thread {

		Queue<Integer> current;
		Queue<Integer> next;
		CyclicBarrier barrier;
		private int n_threads;
		AtomicInteger n_threads_waiting;
		ReentrantLock waitingLock;
		Condition allThreadsWaiting;

		public BFSThread(Queue<Integer> current, Queue<Integer> next, CyclicBarrier barrier, int n_threads, AtomicInteger n_threads_waiting, ReentrantLock waitingLock, Condition allThreadsWaiting) {
			this.current = current;
			this.next = next;
			this.barrier = barrier;
			this.n_threads = n_threads;
			this.n_threads_waiting = n_threads_waiting;
			this.waitingLock = waitingLock;
			this.allThreadsWaiting = allThreadsWaiting;
		}

		@Override
		public void run() {
			int level = 0;
			boolean isFirstCheck = true;
			while (true) {
//				System.out.println("size of current = " + current.size());
				Integer node = current.poll();
				if (node == null) { // this queue is empty - no more nodes to evaluate at this level
					if (isFirstCheck) { // this is the first time this thread is trying this queue
						if (n_threads_waiting.incrementAndGet() == n_threads) { // announce that this thread is waiting
//							System.out.println("all threads waiting at level " + level);
							waitingLock.lock();
							try {
								allThreadsWaiting.signal();
							} finally {
								waitingLock.unlock();
							}
							return;
						}
					}
					try {
//						System.out.println("A thread is waiting at level " + level);
						barrier.await(); // wait until all threads are also waiting at this level
					} catch (InterruptedException ex) { // if I get interrupted while waiting, then all threads found this queue empty on the first try
						return;
					} catch (BrokenBarrierException ex) {
						return;
					}
					Queue<Integer> temp = current;
					current = next;
					next = temp;
					isFirstCheck = true; // if I pass the barrier, time to go onto next level
					level++;
					continue;
				}
				isFirstCheck = false; // I got a node from the queue, so next time I trie the queue at this level, it won't be my first check
				Set<Edge> out_edges = graph.adjacencyList.get(node);
				for (Edge e : out_edges) {// for each neighbor of this node
					if (shortest_hops[e.destination] == Integer.MAX_VALUE) {// if we haven't discovered this node yet
						shortest_hops[e.destination] = level+1; // update its shortest_hops
//						System.out.println("adding a node to next queue");
						next.add(e.destination); // add it to the queue of nodes to investigate - note: one node may be added several times. this will slow down code but not affect correctness
					}
				}
			}
		}

	}
}
