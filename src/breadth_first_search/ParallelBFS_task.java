package breadth_first_search;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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

public class ParallelBFS_task implements BreadthFirstSearch {
	private ExecutorService threadPool;

	private Graph graph;
	private int[] shortest_hops;
	private int level = 0;
	private Queue<Integer> current;
	private Queue<Integer> next;
	private int num_Threads;
	private String type;

	public ParallelBFS_task(Graph graph, String type, int num_Threads) {
		this.graph = graph;
		this.num_Threads = num_Threads;
		shortest_hops = new int[graph.n_nodes];
		this.type = type;
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
		else {
        	System.out.println("Not an implementation!");
        	System.exit(-1);
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
		
		List<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < 10; i++) {
			tasks.add(new Task());
		}
		threadPool = Executors.newFixedThreadPool(num_Threads);
		
		while (!next.isEmpty()) {
			level++;
			current = next; // all nodes at this level
			if (type.equals("lock-free")) {
				this.next = new ConcurrentLinkedQueue<Integer>();
			} else if (type.equals("reentrant-locked")) {
				this.next = new LockBasedQueue<Integer>();
			} else if (type.equals("array-locked")) {
				this.next = new ArrayBlockingQueue<Integer>(graph.n_nodes);
			}
			else {
	        	System.out.println("Not an implementation!");
	        	System.exit(-1);
			}
			
			int i = 0;
			List<Future<Boolean> > futures = new ArrayList<Future<Boolean> >();
			while (!current.isEmpty()) { // while we still have nodes to process at this level
				if (i >= tasks.size()) {
					tasks.add(new Task());
				}
				Task task = tasks.get(i++);
				task.node = current.remove(); // pop the next node at this level
				futures.add(threadPool.submit(task)); // find this node's neighbors
			}
			// "Barrier"
			for (int j = 0; j < i; j++) {
				try {
					futures.get(j).get();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
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
		return shortest_hops;
	}

	public class Task implements Callable<Boolean> {

		int node = -1;

		@Override
		public Boolean call() {
			Set<Edge> out_edges = graph.adjacencyList.get(node);
			for (Edge e : out_edges) {// for each neighbor of this node
				if (shortest_hops[e.destination] == Integer.MAX_VALUE) {// if we haven't discovered this node yet
					shortest_hops[e.destination] = level;// add it to the queue of nodes to investigate
					next.add(e.destination);// update its shortest_hops
				}
			}
			return true;
		}

	}
}
