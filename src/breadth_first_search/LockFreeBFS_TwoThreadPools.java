package breadth_first_search;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import auxillary_data_structures.Graph_Adjacency_Matrix;

public class LockFreeBFS_TwoThreadPools implements BreadthFirstSearch {
	private ExecutorService neighborThreadPool;
	private ExecutorService shortestHopThreadPool;
	private int[] nonZeroRowCount;

	private Graph_Adjacency_Matrix graph;
	private int[] shortest_hops;
	private int level = 0;
	private Queue<Integer> current;
	private Queue<Integer> next;

	public LockFreeBFS_TwoThreadPools(Graph_Adjacency_Matrix graph) {
		this.graph = graph;
		shortest_hops = new int[graph.n_nodes];
		this.current = new ConcurrentLinkedQueue<Integer>();
		this.next = new ConcurrentLinkedQueue<Integer>();
		this.nonZeroRowCount = new int[graph.n_nodes]; 
		for (int i = 0; i < graph.n_nodes; i++) { // TODO : see if making this parallel improves performance
			int nnz = 0;
			for (int j = 0; j < graph.n_nodes; j++) {
				if (graph.adjacency_matrix[i][j] != 0) {
					nnz++;
				}
			}
			nonZeroRowCount[i] = nnz;
		}
	}

	@Override
	public int[] search(int source) {
		// Algorithm from Ole Miss paper:
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
			next = new ConcurrentLinkedQueue<Integer>(); // all nodes at the next level
			neighborThreadPool = Executors.newFixedThreadPool(4);
			while (!current.isEmpty()) { // while we still have nodes to process at this level
				int node = current.remove(); // pop the next node at this level
				neighbors = new NeighborExecutor(node);
				neighborThreadPool.execute(neighbors); // find this node's neighbors
			}
			try {
				neighborThreadPool.shutdown(); 
				neighborThreadPool.awaitTermination(1, TimeUnit.DAYS); // wait until we have finished launching searches of all nodes at this level
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return shortest_hops;
	}

	public int[] search(int source, String type) {// with two thread pools
		// Algorithm from Ole Miss paper:
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
			next = new ConcurrentLinkedQueue<Integer>(); // all nodes at the next level
			neighborThreadPool = Executors.newFixedThreadPool(4);
			shortestHopThreadPool = Executors.newFixedThreadPool(4);
			while (!current.isEmpty()) { // while we still have nodes to process at this level
				int node = current.remove(); // pop the next node at this level
				neighbors = new NeighborExecutor(node);
				neighborThreadPool.execute(neighbors); // find this node's neighbors
			}
			try {
				neighborThreadPool.shutdown(); 
				neighborThreadPool.awaitTermination(1, TimeUnit.DAYS); // wait until we have finished launching searches of all nodes at this level
				shortestHopThreadPool.shutdown();
				shortestHopThreadPool.awaitTermination(1, TimeUnit.DAYS); // wait until we have finished updating all the neighbors of all the nodes at this level
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
			for (int v = 0; v < graph.n_nodes; v++) {
				if (graph.adjacency_matrix[node][v] != 0) { // for each neighbor of this node
					if (shortest_hops[v] == Integer.MAX_VALUE) { // if we haven't discovered this node yet
						next.add(v); // add it to the queue of nodes to investigate
						shortest_hops[v] = level; // update its shortest_hops
					} // evaluate that neighbor's shortest hops from source
				}
			}
		}

	}
	
	/*public class NeighborExecutor implements Runnable {

		int node = -1;

		public NeighborExecutor(int node) {
			this.node = node;
		}

		@Override
		public void run() {
			ShortestHopExecutor shortestHop;
			for (int v = 0; v < graph.n_nodes; v++) {
				if (graph.adjacency_matrix[node][v] != 0) { // for each neighbor of this node
					shortestHop = new ShortestHopExecutor(v);
					shortestHopThreadPool.execute(shortestHop); // evaluate that neighbor's shortest hops from source
				}
			}
		}

	}*/

	public class ShortestHopExecutor implements Runnable {
		int v = -1;

		public ShortestHopExecutor(int v) {
			this.v = v;
		}

		@Override
		public void run() {
			if (shortest_hops[v] == Integer.MAX_VALUE) { // if we haven't discovered this node yet
				next.add(v); // add it to the queue of nodes to investigate
				shortest_hops[v] = level; // update its shortest_hops
			}
		}

	}

}
