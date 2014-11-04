package BreadthFirstSearch;

public class Main {
	public static void main (String[] args) {
		Graph g = new Graph(0.5, 5);
		System.out.println(g);
		int[] shortest_hops = g.sequential_BFS(0);
		System.out.println("BFS results:\n");
		for (int i = 0; i < shortest_hops.length; i++) {
			System.out.println(shortest_hops[i]);
		}
	}

}
