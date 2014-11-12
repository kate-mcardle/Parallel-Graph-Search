package auxillary_data_structures;

import java.util.Random;

/*
 * Density: (# nonzeros in adjacency matrix) / V*(V-1)
 * Diagonal of adjacency matrix should be 0s (no self-loops)
 */

public class Graph_Adjacency_Matrix {
	public double[][] adjacency_matrix;
	public int n_nodes;
	public int n_edges;

	public Graph_Adjacency_Matrix(double density, int n_nodes) {
		this.n_nodes = n_nodes;
		adjacency_matrix = new double[n_nodes][n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			for (int j = 0; j < n_nodes; j++) {
				adjacency_matrix[i][j] = 0.0;
			}
		}
		n_edges = (int) (density * n_nodes * (n_nodes - 1));
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
}
