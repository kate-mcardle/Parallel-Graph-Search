package bellman_ford;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

/*
 * Adapted from: http://algs4.cs.princeton.edu/44sp/BellmanFordSP.java.html
 * Used here to evaluate correctness of parallel algorithms
 */

public class SequentialBF extends BellmanFord {
	
	Queue<Integer> nodesToRelax;
	double[] distTo;
	Edge[] edgeTo;
	boolean[] nodesOnQueue;
	
    public SequentialBF(Graph graph) {
    	super(graph);
    	nodesToRelax = new LinkedList<Integer>();
	    distTo  = new double[graph.n_nodes];
	    edgeTo  = new Edge[graph.n_nodes];
	    nodesOnQueue = new boolean[graph.n_nodes];
	    for (int v = 0; v < graph.n_nodes; v++) {
	        distTo[v] = Double.POSITIVE_INFINITY;
	    }
    }
    
    public void run_bf(int source) {
        distTo[source] = 0.0;

        // Bellman-Ford algorithm
        nodesToRelax.add(source);
        nodesOnQueue[source] = true;
//        int iter = 0;
        while (!nodesToRelax.isEmpty()) {
            int v = nodesToRelax.remove();
            nodesOnQueue[v] = false;
            relax(graph, v);
//            iter++;
        }
//        System.out.println("# iterations = " + iter);
    }

    // relax vertex v and put other endpoints on queue if changed
    private void relax(Graph graph, int v) {
        for (Edge e : graph.adjacencyList.get(v)) {
            int w = e.destination;
            if (distTo[w] > distTo[v] + e.weight) {
                distTo[w] = distTo[v] + e.weight;
                edgeTo[w] = e;
                if (!nodesOnQueue[w]) {
                    nodesToRelax.add(w);
                    nodesOnQueue[w] = true;
                }
            }
        }
    }

    public double distTo(int v) {
        return distTo[v];
    }

    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    public Iterable<Edge> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        Stack<Edge> path = new Stack<Edge>();
        for (Edge e = edgeTo[v]; e != null; e = edgeTo[e.source]) {
            path.push(e);
        }
        return path;
    }

	@Override
	public double[] getDistances() {
		return distTo;
	}

	@Override
	public Edge[] getEdges() {
		return edgeTo;
	}
}
