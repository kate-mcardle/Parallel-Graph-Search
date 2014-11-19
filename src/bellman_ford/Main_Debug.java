package bellman_ford;

/*
 *  Assumes no negative weight graphs
 */

import java.util.Arrays;
import auxillary_data_structures.Graph;

public class Main_Debug {
	public static void main_debug(String[] args) {
		double t_graph, t_seq;
		long t = System.nanoTime();
		Graph g = new Graph(0.00015, 100000);
		t_graph = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time to build graph = " + t_graph + " seconds");
		
		BellmanFord bf_seq = new SequentialBF(g);
		t = System.nanoTime();
		bf_seq.run_bf(0);
		t_seq = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
		System.out.println("Time for sequential Bellman Ford = " + t_seq + " seconds");	
		
		if (evaluate_search(bf_seq, "lock-based", g, 0, 7)) {
			System.out.println("match!");
		}
		else {
			System.out.println("Bug!! Not a match");
		}
		
		if (evaluate_search(bf_seq, "lock-free", g, 0, 4)) {
			System.out.println("match!");
		} else {
			System.out.println("Bug!! Not a match");
		}	
		
		// to thoroughly test correctness:
//		for (int i = 0; i < 100; i++) {
//			if (i%10 == 0) {
//				System.out.println("iteration " + i);
//			}
//			Graph g = new Graph(0.00015, 100000);			
//			BellmanFord bf_seq = new SequentialBF(g);
//			bf_seq.run_bf(0);
//			
//			if (!evaluate_search(bf_seq, "lock-based", g, 0, 7)) {
//				System.out.println("Bug in lock-based!!");
//			}
//			
//			if (!evaluate_search(bf_seq, "lock-free", g, 0, 4)) {
//				System.out.println("Bug in lock-free!");
//			}
//		}
	
		
		
//		// for small graphs:
//		long t = System.nanoTime();
//		Graph g = new Graph(0.25, 10);
//		double t_graph = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
//		System.out.println("Time to build graph = " + t_graph + " seconds");
//	
//		BellmanFord bf_seq = new SequentialBF(g);
//		t = System.nanoTime();
//		bf_seq.run_bf(0);
//		double t_seq = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
//		System.out.println("Time for sequential Bellman Ford = " + t_seq + " seconds");
//		
//		BellmanFord bf_par = new ParallelBF_lockfree_v2(g, 7);
//		t = System.nanoTime();
//		bf_par.run_bf(0);
//		double t_par = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
//		System.out.println("Time for lock-free Bellman Ford = " + t_par + " seconds");
//		
//		if (Arrays.equals(bf_par.getDistances(),  bf_seq.getDistances()) ) {
//			System.out.println("Match");
//		}
//		else {
//			System.out.println("Graph\n"+ g.adjacencyList);
//			System.out.println("Sequential Distances to nodes: " + Arrays.toString(bf_seq.getDistances()));
//			System.out.println("Parallel Distances to nodes: " + Arrays.toString(bf_par.getDistances()));
//			System.out.println("Sequential Edges: " + Arrays.toString(bf_seq.getEdges()));
//			System.out.println("Parallel Edges: " + Arrays.toString(bf_par.getEdges()));	
//		}
		
	}
	
	// for debugging
	public static boolean evaluate_search(BellmanFord bf_seq, String type, Graph g, int source, int num_Threads) {
//		System.out.println("Evaluating " + type + " Bellman Ford");
		BellmanFord bf_par = null;
		if (type.equals("lock-based")) {
			bf_par = new ParallelBF_locking(g, num_Threads);
		}
		else if (type.equals("lock-free")){
			bf_par = new ParallelBF_lockfree(g, num_Threads);
		}
		else {
			System.out.println("Not a valid implementation!");
			System.exit(-1);
		}
//		long t = System.nanoTime();
		bf_par.run_bf(source);
//		double t_par = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
//		System.out.println("Time for parallel " + type + " search = " + t_par + " seconds");
//		return (Arrays.equals(bf_par.distTo, bf_seq.distTo) && Arrays.equals(bf_par.edgeTo, bf_seq.edgeTo));
		return (Arrays.equals(bf_par.getDistances(), bf_seq.getDistances()));
	}
}
