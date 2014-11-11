package BreadthFirstSearch;

import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		long t = System.nanoTime();
		Graph g = new Graph(0.5, 15000);
		double t_graph = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time to build graph = " + t_graph + " seconds");
		
		BreadthFirstSearch bfs_seq = new SequentialBFS(g);
		t = System.nanoTime();
		int[] shortest_hops_seq = bfs_seq.search(0);
		double t_seq = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time for sequential search = " + t_seq + " seconds");
		
		if (evaluate_search(shortest_hops_seq, "lock-free", g, 0)) {
			System.out.println("match!");
		} else {
			System.out.println("Bug!! Not a match");
		}
	}
	
	public static boolean evaluate_search(int[] shortest_hops, String type, Graph g, int source) {
		System.out.println("Evaluating " + type + " BFS");
		BreadthFirstSearch bfs = null;
		if (type.equals("lock-free")) {
			bfs = new LockFreeBFS(g);
		}
		long t = System.nanoTime();
		int[] shortest_hops_parallel = bfs.search(source);
		double t_par = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time for parallel search = " + t_par + " seconds");
		return (Arrays.equals(shortest_hops, shortest_hops_parallel));
	}

}
