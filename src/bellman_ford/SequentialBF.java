package bellman_ford;

import java.util.LinkedList;
import java.util.Stack;

import auxillary_data_structures.Edge;
import auxillary_data_structures.Graph;

/*
 * Adapted from: http://algs4.cs.princeton.edu/44sp/BellmanFordSP.java.html
 * Used here to evaluate correctness of parallel algorithms
 */

public class SequentialBF extends BellmanFord {
	
    public SequentialBF(Graph graph, int source) {
        distTo  = new double[graph.n_nodes];
        edgeTo  = new Edge[graph.n_nodes];
        nodesOnQueue = new boolean[graph.n_nodes];
        for (int v = 0; v < graph.n_nodes; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[source] = 0.0;

        // Bellman-Ford algorithm
        nodesToRelax = new LinkedList<Integer>();
        nodesToRelax.add(source);
        nodesOnQueue[source] = true;
        while (!nodesToRelax.isEmpty() && !hasNegativeCycle()) {
            int v = nodesToRelax.remove();
            nodesOnQueue[v] = false;
            relax(graph, v);
        }
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
            if (cost++ % graph.n_nodes == 0)
                findNegativeCycle();
        }
    }

    /**
     * Is there a negative cycle reachable from the source vertex <tt>s</tt>?
     * @return <tt>true</tt> if there is a negative cycle reachable from the
     *    source vertex <tt>s</tt>, and <tt>false</tt> otherwise
     */
    public boolean hasNegativeCycle() {
        return cycle != null;
    }

    /**
     * Returns a negative cycle reachable from the source vertex <tt>s</tt>, or <tt>null</tt>
     * if there is no such cycle.
     * @return a negative cycle reachable from the source vertex <tt>s</tt> 
     *    as an iterable of edges, and <tt>null</tt> if there is no such cycle
     */
    public Iterable<Edge> negativeCycle() {
        return cycle;
    }

    // by finding a cycle in predecessor graph
    private void findNegativeCycle() {
        int V = edgeTo.length;
        Graph spt = new Graph(V);
        for (int v = 0; v < V; v++)
            if (edgeTo[v] != null)
                spt.add_edge(edgeTo[v]);

        EdgeWeightedDirectedCycle finder = new EdgeWeightedDirectedCycle(spt);
        cycle = finder.cycle();
    }

    /**
     * Returns the length of a shortest path from the source vertex <tt>s</tt> to vertex <tt>v</tt>.
     * @param v the destination vertex
     * @return the length of a shortest path from the source vertex <tt>s</tt> to vertex <tt>v</tt>;
     *    <tt>Double.POSITIVE_INFINITY</tt> if no such path
     * @throws UnsupportedOperationException if there is a negative cost cycle reachable
     *    from the source vertex <tt>s</tt>
     */
    public double distTo(int v) {
        if (hasNegativeCycle())
            throw new UnsupportedOperationException("Negative cost cycle exists");
        return distTo[v];
    }

    /**
     * Is there a path from the source <tt>s</tt> to vertex <tt>v</tt>?
     * @param v the destination vertex
     * @return <tt>true</tt> if there is a path from the source vertex
     *    <tt>s</tt> to vertex <tt>v</tt>, and <tt>false</tt> otherwise
     */
    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    /**
     * Returns a shortest path from the source <tt>s</tt> to vertex <tt>v</tt>.
     * @param v the destination vertex
     * @return a shortest path from the source <tt>s</tt> to vertex <tt>v</tt>
     *    as an iterable of edges, and <tt>null</tt> if no such path
     * @throws UnsupportedOperationException if there is a negative cost cycle reachable
     *    from the source vertex <tt>s</tt>
     */
    public Iterable<Edge> pathTo(int v) {
        if (hasNegativeCycle())
            throw new UnsupportedOperationException("Negative cost cycle exists");
        if (!hasPathTo(v)) return null;
        Stack<Edge> path = new Stack<Edge>();
        for (Edge e = edgeTo[v]; e != null; e = edgeTo[e.source]) {
            path.push(e);
        }
        return path;
    }

    // check optimality conditions: either 
    // (i) there exists a negative cycle reachable from s
    //     or 
    // (ii)  for all edges e = v->w:            distTo[w] <= distTo[v] + e.weight()
    // (ii') for all edges e = v->w on the SPT: distTo[w] == distTo[v] + e.weight()
    private boolean check(Graph G, int s) {

        // has a negative cycle
        if (hasNegativeCycle()) {
            double weight = 0.0;
            for (Edge e : negativeCycle()) {
                weight += e.weight;
            }
            if (weight >= 0.0) {
                System.err.println("error: weight of negative cycle = " + weight);
                return false;
            }
        }

        // no negative cycle reachable from source
        else {

            // check that distTo[v] and edgeTo[v] are consistent
            if (distTo[s] != 0.0 || edgeTo[s] != null) {
                System.err.println("distanceTo[s] and edgeTo[s] inconsistent");
                return false;
            }
            for (int v = 0; v < G.n_nodes; v++) {
                if (v == s) continue;
                if (edgeTo[v] == null && distTo[v] != Double.POSITIVE_INFINITY) {
                    System.err.println("distTo[] and edgeTo[] inconsistent");
                    return false;
                }
            }

            // check that all edges e = v->w satisfy distTo[w] <= distTo[v] + e.weight()
            for (int v = 0; v < G.n_nodes; v++) {
                for (Edge e : G.adjacencyList.get(v)) {
                    int w = e.destination;
                    if (distTo[v] + e.weight < distTo[w]) {
                        System.err.println("edge " + e + " not relaxed");
                        return false;
                    }
                }
            }

            // check that all edges e = v->w on SPT satisfy distTo[w] == distTo[v] + e.weight()
            for (int w = 0; w < G.n_nodes; w++) {
                if (edgeTo[w] == null) continue;
                Edge e = edgeTo[w];
                int v = e.source;
                if (w != e.destination) return false;
                if (distTo[v] + e.weight != distTo[w]) {
                    System.err.println("edge " + e + " on shortest path not tight");
                    return false;
                }
            }
        }

        System.out.println("Satisfies optimality conditions\n");
        return true;
    }

    /**
     * Unit tests the <tt>BellmanFordSP</tt> data type.
     */
//    public static void main(String[] args) {
//        In in = new In(args[0]);
//        int s = Integer.parseInt(args[1]);
//        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
//
//        BellmanFordSP sp = new BellmanFordSP(G, s);
//
//        // print negative cycle
//        if (sp.hasNegativeCycle()) {
//            for (DirectedEdge e : sp.negativeCycle())
//                StdOut.println(e);
//        }
//
//        // print shortest paths
//        else {
//            for (int v = 0; v < G.V(); v++) {
//                if (sp.hasPathTo(v)) {
//                    StdOut.printf("%d to %d (%5.2f)  ", s, v, sp.distTo(v));
//                    for (DirectedEdge e : sp.pathTo(v)) {
//                        StdOut.print(e + "   ");
//                    }
//                    StdOut.println();
//                }
//                else {
//                    StdOut.printf("%d to %d           no path\n", s, v);
//                }
//            }
//        }
//
//    }

}
