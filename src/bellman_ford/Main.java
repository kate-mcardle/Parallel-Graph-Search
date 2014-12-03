package bellman_ford;

/*
 *  Assumes no negative weight graphs
 */

import java.util.Arrays;

import auxillary_data_structures.Graph;


public class Main {
	public static void main(String[] args) {
		demo();
//		evaluate_all();
	}	
	
	public static void demo() {
//		double demo_time = System.nanoTime();
		String[] search_types = { "lock-based", "lock-free" };
		int source_node = 2;
		
		/** Change for LRC machines! **/
		int max_threads = 7;
		int max_reps = 1;
		int n_graphs_to_evaluate = 1;
		String[] paths = {"src/data/as-skitter.txt"};
		/** End change for LRC machines! **/
		
		int[] graph_sizes ={1696415};
		boolean[] isZeroIndexed = {true};
		
		for(int i = 0; i < n_graphs_to_evaluate;i++){
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			long t;
			double t_graph;
			Graph g = null;
			System.out.println("Evaluating graph: " + paths[i] + " ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			t = System.nanoTime();
			g = new Graph(paths[i],isZeroIndexed[i],graph_sizes[i]);
			t_graph = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			System.out.println("Time to build graph = " + t_graph + " seconds");
			
			BellmanFord bf_seq = null;
			BellmanFord bf_par = null;
			
			// Executing Sequential Algorithm
			double t_seq = 0.0;
			int n_reps;
			for (n_reps = 0; n_reps < max_reps; n_reps++) {
				bf_seq = new SequentialBF(g);
				t = System.nanoTime();
				bf_seq.run_bf(source_node);
				t_seq += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			}
			System.out.println("Time for sequential Bellman Ford = " + t_seq + " seconds");
			
			// Parallel Algorithm Execution
			System.out.println("Parallel Executions: ~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Data structure: " + type);

				for (int n_threads = 1; n_threads <= max_threads; n_threads++) {// Number of threads
					double t_par = 0.0;
					for (n_reps = 0; n_reps < max_reps; n_reps++) {
						if (type.equals("lock-based")) {
							bf_par = new ParallelBF_locking(g, n_threads);
						}
						else if (type.equals("lock-free")) {
							bf_par = new ParallelBF_lockfree(g, n_threads);
						}
						else {
							System.out.println("Not a valid implementation!");
							System.exit(-1);
						}
						long t_par_start = System.nanoTime();
						bf_par.run_bf(source_node);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println(n_threads + " thread time = " + t_par / n_reps + " seconds");

					if(!(Arrays.equals(bf_par.getDistances(), bf_seq.getDistances()))){
						System.out.println("Bug!! Not a match");
					}
				}	
				System.out.println(" ---------------------------------------------------");
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//			demo_time = (System.nanoTime() - demo_time + 0.0) / (Math.pow(10,9));
//			System.out.println("Demo time = " + demo_time + " seconds");
		}	
	}
	
	public static void evaluate_all() {
		String[] search_types = { "lock-based", "lock-free" };
		double[] graph_density = { 0.00015, 0.000015 };
		int[] num_nodes = { 500000, 1000000 };
		int source_node = 2;
		
		/** Change for LRC machines! **/
		int max_threads = 7;
		int max_reps = 1;
		int n_graphs_to_evaluate = 5;
		String[] paths = {"src/data/soc-pokec-relationships.txt","src/data/wiki-Talk.txt","src/data/as-skitter.txt"};
		/** End change for LRC machines! **/
		
		int[] graph_sizes ={1632803,2394385,1696415};
		boolean[] isZeroIndexed = {false, true,true};
		
		for(int i = 0; i < n_graphs_to_evaluate;i++){
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			long t;
			double t_graph;
			Graph g = null;
			if (i < paths.length) {
				System.out.println("Evaluating graph: " + paths[i] + " ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				t = System.nanoTime();
				g = new Graph(paths[i],isZeroIndexed[i],graph_sizes[i]);
				t_graph = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			} else {
				t = System.nanoTime();
				g = new Graph(graph_density[i-paths.length], num_nodes[i-paths.length]);
				t_graph = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
				System.out.println("Evaluating randomly generated graph of size " + g.n_nodes + " and # edges " + g.n_edges + " ~~~~~~~~~~~~~~");
			}
			System.out.println("Time to build graph = " + t_graph + " seconds");
			
			BellmanFord bf_seq = null;
			BellmanFord bf_par = null;
			
			// Executing Sequential Algorithm 10 times
			double t_seq = 0.0;
			int n_reps;
			for (n_reps = 0; n_reps < max_reps; n_reps++) {
				bf_seq = new SequentialBF(g);
				t = System.nanoTime();
				bf_seq.run_bf(source_node);
				t_seq += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			}
			double t_seq_ave = t_seq / n_reps;
			System.out.println("Average time for sequential Bellman Ford = " + t_seq_ave + " seconds");
			
			// Parallel Algorithm Execution
			System.out.println("Parallel Executions: ~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Data structure: " + type);

				for (int n_threads = 1; n_threads <= max_threads; n_threads++) {// Number of threads
					double t_par = 0.0;
					for (n_reps = 0; n_reps < max_reps; n_reps++) {
						if (type.equals("lock-based")) {
							bf_par = new ParallelBF_locking(g, n_threads);
						}
						else if (type.equals("lock-free")) {
							bf_par = new ParallelBF_lockfree(g, n_threads);
						}
						else {
							System.out.println("Not a valid implementation!");
							System.exit(-1);
						}
						long t_par_start = System.nanoTime();
						bf_par.run_bf(source_node);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println(n_threads + " thread time = " + t_par / n_reps + " seconds");

					if(!(Arrays.equals(bf_par.getDistances(), bf_seq.getDistances()))){
						System.out.println("Bug!! Not a match");
					}
				}	
				System.out.println(" ---------------------------------------------------");
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}	
	}
}






