package breadth_first_search;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public class ParallelBFS implements BreadthFirstSearch {
	private ExecutorService neighborThreadPool;

	private Graph graph;
	private int[] shortest_hops;
	private int level = 0;
	private Queue<Integer> current;
	private Queue<Integer> next;
	private int num_Threads;

	public ParallelBFS(Graph graph, String type, int num_Threads) {
		this.graph = graph;
		this.num_Threads = num_Threads;
		shortest_hops = new int[graph.n_nodes];
		if (type.equals("lock-free")) {
			this.current = new ConcurrentLinkedQueue<Integer>();
			this.next = new ConcurrentLinkedQueue<Integer>();
		} else if (type.equals("reentrant-locked")) {
			this.current = new LockBasedQueue<Integer>();
			this.next = new LockBasedQueue<Integer>();
		} else if (type.equals("array-locked")) {
			this.current = new ArrayBlockingQueue<Integer>(graph.n_nodes);
			this.next = new ArrayBlockingQueue<Integer>(graph.n_nodes);
		}
	}

	@Override
	public int[] search(int source) {
		// Algorithm from Ole Miss paper (Algorithm 1):
		// http://cs.olemiss.edu/heroes/papers/bfs.pdf

		for (int i = 0; i < graph.n_nodes; i++) {
			shortest_hops[i] = Integer.MAX_VALUE;
		}
		shortest_hops[source] = level;

		next.add(source);
		NeighborExecutor neighbors;
		while (!next.isEmpty()) {
			level++;
			current = next; // all nodes at this level
			next = new ConcurrentLinkedQueue<Integer>(); // all nodes at the
															// next level
			neighborThreadPool = Executors.newFixedThreadPool(num_Threads);
			while (!current.isEmpty()) { // while we still have nodes to process
											// at this level
				int node = current.remove(); // pop the next node at this level
				neighbors = new NeighborExecutor(node);
				neighborThreadPool.execute(neighbors); // find this node's
														// neighbors
			}
			try {
				neighborThreadPool.shutdown();
				neighborThreadPool.awaitTermination(1, TimeUnit.DAYS); // wait
																		// until
																		// we
																		// have
																		// finished
																		// launching
																		// searches
																		// of
																		// all
																		// nodes
																		// at
																		// this
																		// level
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return shortest_hops;
	}

	public class NeighborExecutor implements Runnable {

		int node = -1;

		public NeighborExecutor(int node) {
			this.node = node;
		}

		@Override
		public void run() {
			Set<Edge> out_edges = graph.adjacencyList.get(node);
			for (Edge e : out_edges) {// for each neighbor of this node
				if (shortest_hops[e.destination] == Integer.MAX_VALUE) {// if we
																		// haven't
																		// discovered
																		// this
																		// node
																		// yet
					shortest_hops[e.destination] = level;// add it to the queue
															// of nodes to
															// investigate
					next.add(e.destination);// update its shortest_hops
				}
			}
		}

	}
}
