package bellman_ford;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

public abstract class BellmanFord {
	Graph graph;

	
	public BellmanFord(Graph graph) {
		this.graph = graph;
	}
	
	public abstract void run_bf(int source);
	
	public abstract double[] getDistances();
	public abstract Edge[] getEdges();

}
