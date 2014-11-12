package BreadthFirstSearch;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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
			Set<Edge> out_edges = graph.adjacencyList.get(u);
			for (Edge e : out_edges) {
				if (shortest_hops[e.destination] == Integer.MAX_VALUE) {
					shortest_hops[e.destination] = shortest_hops[u] + 1;
					q.add(e.destination);
				}
			}
		}
		return shortest_hops;
	}
	

}
