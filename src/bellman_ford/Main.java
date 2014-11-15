package bellman_ford;

/*
 *  Assumes no negative weight graphs
 */

import java.util.Arrays;

import auxillary_data_structures.Graph;


public class Main {
	public static void main(String[] args) {
		String[] search_types = { "lock-based", "lock-free" };
		double[] graph_density = {0.00015, 0.000015, 0.0015  };
		int[] num_nodes = { 10000, 20000,30000 };
		// building graphs with different densities and nodes
		for (int h = 0; h < 3; h++) { 
			double t_graph = 0, t_seq = 0;
			long t = System.nanoTime();
			BellmanFord bf_parallel =null; 
			BellmanFord bf_seq = null;
			
			Graph g = new Graph(graph_density[h], num_nodes[h]);
			t_graph += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			System.out.println("Graph density " + graph_density[h] + "  Graph Size " + num_nodes[h] );
			System.out.println("Time to build graph = " + t_graph + " seconds");
			
			// Executing Sequential Algorithm 10 times
			for (int k = 0; k < 10; k++) {
				bf_seq = new SequentialBF(g);
				t = System.nanoTime();
				bf_seq.run_bf(0);
				t_seq += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			}
			double t_seq_ave = t_seq / 10.0;
			System.out.println("Average time for sequential Bellman Ford = " + t_seq_ave + " seconds");
			
			// Parallel Algorithm Execution
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Search Algorithm " + type);

				for (int j = 1; j <= 6; j++) {// Number of threads
					//System.out.println("# Thread(s) " + j);
					double t_par = 0.0;
					//Averaging the execution time
					for(int k=0; k<10;k++){
						bf_parallel = new ParallelBF(g,type, j);
						long t_par_start = System.nanoTime();
						bf_parallel.run_bf(0);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println("# Thread(s) " + j+" Time = " + t_par / 10 + " seconds");

					
					if(!(Arrays.equals(bf_parallel.distTo, bf_seq.distTo) && Arrays.equals(bf_parallel.edgeTo, bf_seq.edgeTo))){
						System.out.println("Bug!! Not a match");
					}
				}	
				System.out.println(" ---------------------------------------------------");
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
	
//		long t = System.nanoTime();
//		Graph g = new Graph(0.000015, 1000000);
////		System.out.println("Graph\n"+ g.adjacencyList);
//		double t_graph = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
//		System.out.println("Time to build graph = " + t_graph + " seconds");
	//
//		t = System.nanoTime();
//		BellmanFord bf_seq = new SequentialBF(g, 0);
//		double t_seq = (System.nanoTime() - t + 0.0)/(Math.pow(10,9));
//		System.out.println("Time for sequential Bellman Ford = " + t_seq + " seconds");
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






