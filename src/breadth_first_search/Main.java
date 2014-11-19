package breadth_first_search;

import java.util.Arrays;

import auxillary_data_structures.Graph;

public class Main {

	static int count;

	public static void main(String[] args) {
		String[] search_types = { "array-locked", "lock-free" };
		double[] graph_density = { 0.00015, 0.000015 };
		int[] num_nodes = { 500000, 1000000 };
		int source_node = 2;
		
		/** Change for LRC machines! **/
		int max_threads = 7;
		int max_reps = 1;
		String[] paths = {"src/data/soc-pokec-relationships.txt","src/data/wiki-Talk.txt","src/data/as-skitter.txt"};
		/** End change for LRC machines! **/
		
		int[] graph_sizes ={1632803,2394385,1696415};
		boolean[] isZeroIndexed = {false, true,true};
		
		for(int i = 0; i < 5;i++){
			long t = System.nanoTime();
			Graph g = null;
			if (i < paths.length) {
				g = new Graph(paths[i],isZeroIndexed[i],graph_sizes[i]);
				System.out.println("Evaluating graph: " + paths[i] + " ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			} else {
				g = new Graph(graph_density[i-paths.length], num_nodes[i-paths.length]);
				System.out.println("Evaluating randomly generated graph of size " + g.n_nodes + " and # edges " + g.n_edges + " ~~~~~~~~~~~~~~");
			}
			double t_graph = (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			double t_seq = 0.0;
			int[] shortest_hops_seq = null;
			int[] shortest_hops_parallel = null;

			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			//System.out.println("Graph density " + graph_density[h] + "  Graph Size " + num_nodes[h] );
			System.out.println("Time to build graph = " + t_graph + " seconds");

			// Executing Sequential Algorithm 10 times
			int n_reps;
			for (n_reps = 0; n_reps < max_reps; n_reps++) {
				// Sequential Algorithm Execution
				BreadthFirstSearch bfs_seq = new SequentialBFS(g);
				t = System.nanoTime();
				shortest_hops_seq = bfs_seq.search(source_node);
				t_seq += (System.nanoTime() - t + 0.0) / (Math.pow(10, 9));
			}
			System.out.println("Time for sequential search = " + t_seq / n_reps + " seconds");

			// Parallel Algorithm Execution
			System.out.println("Approach: Collecting Task results at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Data structure: " + type);

				for (int n_threads = 1; n_threads <= max_threads; n_threads++) {// Number of threads
					double t_par = 0.0;
					for (n_reps = 0; n_reps < max_reps; n_reps++) {
						BreadthFirstSearch bfs_parallel = new ParallelBFS_task(g, type, n_threads);
						long t_par_start = System.nanoTime();
						shortest_hops_parallel = bfs_parallel.search(source_node);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println(n_threads + " thread time = " + t_par / n_reps + " seconds");

					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
						System.out.println("Bug!! Not a match");
					}

				}
				System.out.println(" ---------------------------------------------------");
			}
			System.out.println("Approach: Restarting thread pool at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			// Parallel Algorithm Execution - old approach (restarting thread pool at each level)
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Data structure: " + type);

				for (int n_threads = 1; n_threads <= max_threads; n_threads++) {// Number of threads
					double t_par = 0.0;
					for (n_reps = 0; n_reps < max_reps; n_reps++) {
						BreadthFirstSearch bfs_parallel = new ParallelBFS(g, type, n_threads);
						long t_par_start = System.nanoTime();
						shortest_hops_parallel = bfs_parallel.search(source_node);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println(n_threads + " thread time = " + t_par / n_reps + " seconds");

					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
						System.out.println("Bug!! Not a match");
					}

				}
				System.out.println(" ---------------------------------------------------");
			}
			
			System.out.println("Approach: Using explicit threads, not thread pool: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			// Parallel Algorithm Execution - old approach (restarting thread pool at each level)
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Data structure: " + type);

				for (int n_threads = 1; n_threads <= max_threads; n_threads++) {// Number of threads
					double t_par = 0.0;
					for (n_reps = 0; n_reps < max_reps; n_reps++) {
						BreadthFirstSearch bfs_parallel = null;
						if (type.equals("array-locked")) {
							bfs_parallel = new ParallelBFS_lockbased(g, n_threads);
						}
						else if (type.equals("lock-free")) {
							bfs_parallel = new ParallelBFS_lockfree(g, n_threads);
						}
						else {
							System.out.println("not an implementation!");
							System.exit(-1);
						}
						long t_par_start = System.nanoTime();
						shortest_hops_parallel = bfs_parallel.search(source_node);
						t_par += (System.nanoTime() - t_par_start + 0.0) / (Math.pow(10, 9));
					}
					System.out.println(n_threads + " thread time = " + t_par / n_reps + " seconds");

					if (!Arrays.equals(shortest_hops_seq, shortest_hops_parallel)) {
						System.out.println("Bug!! Not a match");
					}

				}
				System.out.println(" ---------------------------------------------------");
			}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		}
		
	/*	
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

			// Parallel Algorithm Execution
			System.out.println("Collecting Task results at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Search Algorithm " + type);

				for (int j = 1; j <= 7; j++) {// Number of threads
					System.out.println("# Thread(s) " + j);
					double t_par = 0.0;

					for (int k = 0; k < 10; k++) { // Averaging the execution
													// time
						BreadthFirstSearch bfs_parallel = new ParallelBFS_task(g, type, j);
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
			System.out.println("Restarting thread pool at each level: ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			// Parallel Algorithm Execution - old approach (restarting thread pool at each level)
			for (String type : search_types) { // Algorithm types
				System.out.println(" ---------------------------------------------------");
				System.out.println("Search Algorithm " + type);

				for (int j = 1; j <= 7; j++) {// Number of threads
					System.out.println("# Thread(s) " + j);
					double t_par = 0.0;

					for (int k = 0; k < 10; k++) { // Averaging the execution
													// time
						BreadthFirstSearch bfs_parallel = new ParallelBFS(g, type, j);
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
*/
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
