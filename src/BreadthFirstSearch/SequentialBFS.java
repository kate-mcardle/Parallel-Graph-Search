package BreadthFirstSearch;

import java.util.LinkedList;
import java.util.Queue;

public class SequentialBFS implements BreadthFirstSearch {
	private int[] shortest_hops;
	private Graph graph;

	public SequentialBFS(Graph graph) {
		this.graph = graph;
		this.shortest_hops = new int[graph.n_nodes];
	}

	@Override
	public int[] search(int source) {
		// Algorithm from CSAIL paper:
		// http://supertech.csail.mit.edu/papers/pbfs.pdf
		for (int i = 0; i < graph.n_nodes; i++) {
			shortest_hops[i] = Integer.MAX_VALUE;
		}
		shortest_hops[source] = 0;
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(source);
		while (!q.isEmpty()) {
			int u = q.remove();
			for (int v = 0; v < graph.n_nodes; v++) {
				if (graph.adjacency_matrix[u][v] != 0) {
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
