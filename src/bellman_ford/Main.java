package bellman_ford;

import java.util.Arrays;

import auxillary_data_structures.Graph;

public class Main {
	public static void main(String[] args) {
		long t = System.nanoTime();
		Graph g = new Graph(0.000015, 1000000);
//		System.out.println("Graph\n"+ g.adjacencyList);
		double t_graph = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time to build graph = " + t_graph + " seconds");
	
		t = System.nanoTime();
		BellmanFord bf_seq = new SequentialBF(g, 0);
		double t_seq = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time for sequential Bellman Ford = " + t_seq + " seconds");
//		System.out.println("Distances to nodes: " + Arrays.toString(bf_seq.distTo));
//		System.out.println("Edges: " + Arrays.toString(bf_seq.edgeTo));
		
//		if (evaluate_search(shortest_hops_seq, "lock-based", g, 0)) {
//			System.out.println("match!");
//		} else {
//			System.out.println("Bug!! Not a match");
//		}
//		
//		if (evaluate_search(shortest_hops_seq, "lock-free", g, 0)) {
//			System.out.println("match!");
//		} else {
//			System.out.println("Bug!! Not a match");
//		}
		
		
	}
}
