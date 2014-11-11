package BreadthFirstSearch;

import java.util.ArrayList;
import java.util.List;

public class Node {
	int id;
	List<Node> out_edges;
	public Node(int value) {
		this.id = value;
		this.out_edges = new ArrayList<Node>();
		
	}
}
