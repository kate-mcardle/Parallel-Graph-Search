package BreadthFirstSearch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Density: (# nonzeros in adjacency matrix) / V*(V-1)
 * Diagonal of adjacency matrix should be 0s (no self-loops)
 */

public class Graph {
	private double[][] adjacency_matrix;
	private int n_nodes;
	private int n_edges;
	
	public Graph(double density, int n_nodes) {
		this.n_nodes = n_nodes;
		adjacency_matrix = new double[n_nodes][n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			for (int j = 0; j < n_nodes; j++) {
				adjacency_matrix[i][j] = 0.0;
			}
		}
		n_edges = (int) (density*n_nodes*(n_nodes-1));
		int count = 0;
		int i, j;
		Random rgen = new Random();
		while (count < n_edges) {
			i = rgen.nextInt(this.n_nodes);
			j = rgen.nextInt(this.n_nodes);
			if ((i == j) || (adjacency_matrix[i][j] != 0)) {
				continue;
			}
			this.add_edge(Math.random(), i, j);
			count++;
		}		
	}
	
	public void add_edge(Double weight, int node_1, int node_2) {
		adjacency_matrix[node_1][node_2] = weight;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < n_nodes; i++) {
			for (int j = 0; j < n_nodes; j++) {
				s.append(String.format("%.2f\t", adjacency_matrix[i][j]));
			}
			s.append("\n");
		}
		return s.toString();
	}
	
	public int[] sequential_BFS(int source) {
		// Algorithm from CSAIL paper: http://supertech.csail.mit.edu/papers/pbfs.pdf
		int[] shortest_hops = new int[n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			shortest_hops[i] = Integer.MAX_VALUE;
		}
		shortest_hops[source] = 0;
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(source);
		while (!q.isEmpty()) {
			int u = q.remove();
			for (int v = 0; v < n_nodes; v++) {
				if (adjacency_matrix[u][v] != 0) {
					if (shortest_hops[v] == Integer.MAX_VALUE) {
						shortest_hops[v] = shortest_hops[u] + 1;
						q.add(v);
					}
				}
			}
		}
		return shortest_hops;
	}
	
	public int[] wait_free_BFS(int source) {
		// Algorithm from Ole Miss paper: http://cs.olemiss.edu/heroes/papers/bfs.pdf
		int[] shortest_hops = new int[n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			shortest_hops[i] = Integer.MAX_VALUE;
		}
		shortest_hops[source] = 0;
		Queue<Integer> q = new ConcurrentLinkedQueue<Integer>();
		q.add(source);
		while (!q.isEmpty()) {
			int u = q.remove();
			for (int v = 0; v < n_nodes; v++) {
				if (adjacency_matrix[u][v] != 0) {
					if (shortest_hops[v] == Integer.MAX_VALUE) {
						shortest_hops[v] = shortest_hops[u] + 1;
						q.add(v);
					}
				}
			}
		}
		return shortest_hops;
	}
}
