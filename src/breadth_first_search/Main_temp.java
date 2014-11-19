package breadth_first_search;

import java.util.Arrays;

import auxillary_data_structures.Graph;

public class Main_temp {

	static int count;

	public static void main_temp(String[] args) {
		String[] search_types = { "array-locked", "lock-free" };
		// (graph densities = 0.000015 ,graph nodes = 10000, 100000,1000000 ),
		// (graph densities = 0.001, graph nodes = 10000, 100000)
		double[] graph_density = { 0.001, 0.00001 };
		int[] num_nodes = { 10000, 1000000 };

		for (int h = 1; h < 2; h++) {// each graph

			long t = System.nanoTime();
			Graph g = new Graph(graph_density[h], num_nodes[h]);
			double t_graph = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			double t_seq = 0.0;
			int[] shortest_hops_seq = null;
			int[] shortest_hops_parallel = null;

			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			System.out.println("Graph density " + graph_density[h] + "  Graph Size " + num_nodes[h] );
			System.out.println("Time to build graph = " + t_graph + " seconds");

			// Executing Sequential Algorithm 10 times
			for (int k = 0; k < 10; k++) {
				// Sequential Algorithm Execution
				BreadthFirstSearch bfs_seq = new SequentialBFS(g);
				t = System.nanoTime();
				shortest_hops_seq = bfs_seq.search(0);
				t_seq += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			}
			System.out.println("Time for sequential search = " + t_seq / 10 + " seconds");

//			// Parallel Algorithm Execution
//			System.out.println("Collecting Task results at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
//			for (String type : search_types) { // Algorithm types
//				System.out.println(" ---------------------------------------------------");
//				System.out.println("Search Algorithm " + type);
//
//				for (int j = 1; j <= 7; j++) {// Number of threads
//					System.out.println("# Thread(s) " + j);
//					double t_par = 0.0;
//
//					for (int k = 0; k < 10; k++) { // Averaging the execution
//													// time
//						BreadthFirstSearch bfs_parallel = new ParallelBFS_task(g, type, j);
//						long t_par_start = System.nanoTime();
//						shortest_hops_parallel = bfs_parallel.search(0);
//						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
//					}
//					System.out.println("Time = " + t_par / 10 + " seconds");
//
//					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
//						System.out.println("Bug!! Not a match");
//					}
//
//				}
//				System.out.println(" ---------------------------------------------------");
//			}
//			System.out.println("Restarting thread pool at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
//			// Parallel Algorithm Execution - old approach (restarting thread pool at each level)
//			for (String type : search_types) { // Algorithm types
//				System.out.println(" ---------------------------------------------------");
//				System.out.println("Search Algorithm " + type);
//
//				for (int j = 1; j <= 7; j++) {// Number of threads
//					System.out.println("# Thread(s) " + j);
//					double t_par = 0.0;
//
//					for (int k = 0; k < 10; k++) { // Averaging the execution
//													// time
//						BreadthFirstSearch bfs_parallel = new ParallelBFS(g, type, j);
//						long t_par_start = System.nanoTime();
//						shortest_hops_parallel = bfs_parallel.search(0);
//						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
//					}
//					System.out.println("Time = " + t_par / 10 + " seconds");
//
//					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
//						System.out.println("Bug!! Not a match");
//					}
//
//				}
//				System.out.println(" ---------------------------------------------------");
//			}
			System.out.println("Approach: Using explicit threads, not thread pool: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Search Algorithm " + type);

				for (int j = 1; j <= 7; j++) {// Number of threads
					System.out.println("# Thread(s) " + j);
					double t_par = 0.0;

					for (int k = 0; k < 10; k++) { // Averaging the execution time
						BreadthFirstSearch bfs_parallel = null;
						if (type.equals("array-locked")) {
							bfs_parallel = new ParallelBFS_lockbased(g, j);
						}
						else if (type.equals("lock-free")) {
							bfs_parallel = new ParallelBFS_lockfree(g, j);
						}
						else {
							System.out.println("not an implementation!");
							System.exit(-1);
						}
						long t_par_start = System.nanoTime();
						shortest_hops_parallel = bfs_parallel.search(0);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println("Time = " + t_par / 10 + " seconds");

					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
						System.out.println("Bug!! Not a match");
					}

				}
				System.out.println(" ---------------------------------------------------");
			}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}

	}

	// Not used in the main method need to remove it while submitting the code
	public static boolean evaluate_search(int[] shortest_hops, String type, Graph g, int source, int num_Threads) {
		System.out.println("Evaluating " + type + " BFS");
		BreadthFirstSearch bfs = null;
		bfs = new ParallelBFS_task(g, type, 6); // 6 threads
		long t = System.nanoTime();
		int[] shortest_hops_parallel = bfs.search(source);
		double t_par = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
		System.out.println("Time for parallel " + type + " search = " + t_par + " seconds");
		return (Arrays.equals(shortest_hops, shortest_hops_parallel));
	}
}
