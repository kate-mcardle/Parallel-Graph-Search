package auxillary_data_structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/*
 * Density: (# nonzeros in adjacency matrix) / V*(V-1)
 * Diagonal of adjacency matrix should be 0s (no self-loops)
 */

public class Graph {
	
	public Map<Integer, Set<Edge>> adjacencyList;  
	public int n_nodes;
	public int n_edges;
	
	public Graph(int n_nodes) {
		this.n_nodes = n_nodes;
		n_edges = 0;
		adjacencyList = new HashMap<Integer, Set<Edge>>();
		for (int i = 0; i < n_nodes; i++) {
			adjacencyList.put(i, new HashSet<Edge>());
		}
	}

	public Graph(double density, int n_nodes) {
		this.n_nodes = n_nodes;
		adjacencyList = new HashMap<Integer,Set<Edge>>();
		for (int i = 0; i < n_nodes; i++) {
			adjacencyList.put(i, new HashSet<Edge>());
		}
		n_edges = (int) (density * n_nodes * (n_nodes - 1));
		int count = 0;
		int i, j;
		Random rgen = new Random();
		while (count < n_edges) {
			i = rgen.nextInt(this.n_nodes);
			j = rgen.nextInt(this.n_nodes);
			if (i == j) {
				continue;
			}
			Edge e = new Edge(i,j,Math.random());
			boolean wasAdded = adjacencyList.get(i).add(e);
			if(wasAdded){
				count++;
			}
			
		}
	}
	
	public Graph(String graph_file) {
		// read data from file, store in adjacencyList
	}

	public boolean add_edge(Edge e) {
		return adjacencyList.get(e.source).add(e);
	}

	public static void main(String[] args) {
		Graph g = new Graph(0.4, 5);
		System.out.println(" Graph " + g.adjacencyList);
	}
}
