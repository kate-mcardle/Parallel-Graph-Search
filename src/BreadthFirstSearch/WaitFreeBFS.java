package BreadthFirstSearch;

import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaitFreeBFS implements BreadthFirstSearch {

	private ExecutorService neigbhorThreadPool = Executors.newCachedThreadPool();
	private ExecutorService shortestHopThreadPool = Executors.newCachedThreadPool();
	private CyclicBarrier neigbhorBarrier;
	private CyclicBarrier shortestHopBarrier;
	private int[] nonZeroRowCount;

	private int[] shortest_hops;
	private Graph graph;
	private int level = 0;
	private Queue<Integer> current;
	private Queue<Integer> next;

	public WaitFreeBFS(Graph graph) {
		this.graph = graph;
		this.current = new ConcurrentLinkedQueue<Integer>();
		this.next = new ConcurrentLinkedQueue<Integer>();
		this.nonZeroRowCount = new int[graph.n_nodes]; // TODO : fill in the
														// array in parallel.
	}

	@Override
	public int[] search(int source) {
		// Algorithm from Ole Miss paper:
		// http://cs.olemiss.edu/heroes/papers/bfs.pdf
		//
		try {
			for (int i = 0; i < graph.n_nodes; i++) {
				shortest_hops[i] = Integer.MAX_VALUE;
			}
			shortest_hops[source] = level;

			next.add(source);
			while (!next.isEmpty()) {
				level++;
				current = next;
				next = new ConcurrentLinkedQueue<Integer>();
				neigbhorBarrier = new CyclicBarrier(current.size());
				while (!current.isEmpty()) {
					int node = current.remove();

					NeigborExecutor neigbhors = new NeigborExecutor(node);
					neigbhorThreadPool.execute(neigbhors);
				}
			}
			return shortest_hops;
		} finally {
			neigbhorThreadPool.shutdown();
			shortestHopThreadPool.shutdown();
		}
	}

	public class NeigborExecutor implements Runnable {

		int node = -1;

		public NeigborExecutor(int node) {
			this.node = node;
		}

		@Override
		public void run() {
			shortestHopBarrier = new CyclicBarrier(nonZeroRowCount[node]);

			for (int v = 0; v < graph.n_nodes; v++) {
				if (graph.adjacency_matrix[node][v] != 0) {
					ShortestHopExecutor shortestHop = new ShortestHopExecutor(v);
					shortestHopThreadPool.execute(shortestHop);
				}
			}
			try {
				neigbhorBarrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}

	}

	public class ShortestHopExecutor implements Runnable {
		int v = -1;

		public ShortestHopExecutor(int v) {
			this.v = v;
		}

		@Override
		public void run() {
			if (shortest_hops[v] == Integer.MAX_VALUE) {
				next.add(v);
				shortest_hops[v] = level;
			}

			try {
				shortestHopBarrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
