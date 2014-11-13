package bellman_ford;

import java.util.Stack;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

/*
 * Adapted from: http://algs4.cs.princeton.edu/44sp/EdgeWeightedDirectedCycle.java.html
 */

public class EdgeWeightedDirectedCycle {
	
    private boolean[] marked;             // marked[v] = has vertex v been marked?
    private Edge[] edgeTo;        // edgeTo[v] = previous edge on path to v
    private boolean[] onStack;            // onStack[v] = is vertex on the stack?
    private Stack<Edge> cycle;    // directed cycle (or null if no such cycle)

    /**
     * Determines whether the edge-weighted digraph <tt>G</tt> has a directed cycle and,
     * if so, finds such a cycle.
     * @param G the edge-weighted digraph
     */
    public EdgeWeightedDirectedCycle(Graph G) {
        marked  = new boolean[G.n_nodes];
        onStack = new boolean[G.n_nodes];
        edgeTo  = new Edge[G.n_nodes];
        for (int v = 0; v < G.n_nodes; v++){
            if (!marked[v]) dfs(G, v);
        }
    }

    // check that algorithm computes either the topological order or finds a directed cycle
    private void dfs(Graph G, int v) {
        onStack[v] = true;
        marked[v] = true;
        for (Edge e : G.adjacencyList.get(v)) {
            int w = e.destination;

            // short circuit if directed cycle found
            if (cycle != null) return;

            //found new vertex, so recur
            else if (!marked[w]) {
                edgeTo[w] = e;
                dfs(G, w);
            }

            // trace back directed cycle
            else if (onStack[w]) {
                cycle = new Stack<Edge>();
                while (e.source!= w) {
                    cycle.push(e);
                    e = edgeTo[e.source];
                }
                cycle.push(e);
            }
        }

        onStack[v] = false;
    }

    /**
     * Does the edge-weighted digraph have a directed cycle?
     * @return <tt>true</tt> if the edge-weighted digraph has a directed cycle,
     * <tt>false</tt> otherwise
     */
    public boolean hasCycle() {
        return cycle != null;
    }

    /**
     * Returns a directed cycle if the edge-weighted digraph has a directed cycle,
     * and <tt>null</tt> otherwise.
     * @return a directed cycle (as an iterable) if the edge-weighted digraph
     *    has a directed cycle, and <tt>null</tt> otherwise
     */
    public Iterable<Edge> cycle() {
        return cycle;
    }


//    // certify that digraph is either acyclic or has a directed cycle
//    private boolean check(EdgeWeightedDigraph G) {
//
//        // edge-weighted digraph is cyclic
//        if (hasCycle()) {
//            // verify cycle
//            DirectedEdge first = null, last = null;
//            for (DirectedEdge e : cycle()) {
//                if (first == null) first = e;
//                if (last != null) {
//                    if (last.to() != e.from()) {
//                        System.err.printf("cycle edges %s and %s not incident\n", last, e);
//                        return false;
//                    }
//                }
//                last = e;
//            }
//
//            if (last.to() != first.from()) {
//                System.err.printf("cycle edges %s and %s not incident\n", last, first);
//                return false;
//            }
//        }
//
//
//        return true;
//    }

}
