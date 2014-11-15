package bellman_ford;

import java.util.Queue;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public abstract class BellmanFord {
	Graph graph;
	double[] distTo;
	Edge[] edgeTo;
	boolean[] nodesOnQueue;
	Queue<Integer> nodesToRelax;
	
	public BellmanFord(Graph graph) {
		this.graph = graph;
	    distTo  = new double[graph.n_nodes];
	    edgeTo  = new Edge[graph.n_nodes];
	    nodesOnQueue = new boolean[graph.n_nodes];
	    for (int v = 0; v < graph.n_nodes; v++) {
	        distTo[v] = Double.POSITIVE_INFINITY;
	    }
	}
	
	public abstract void run_bf(int source);
	

	
//	int cost;
//	Iterable<Edge> cycle;
}
