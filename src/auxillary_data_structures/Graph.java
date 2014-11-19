package auxillary_data_structures;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
		//n_edges = 0;
		adjacencyList = new HashMap<Integer, Set<Edge>>();
		for (int i = 0; i < n_nodes; i++) {
			adjacencyList.put(i, new HashSet<Edge>());
		}
	}

	public Graph(double density, int n_nodes) {
		this.n_nodes = n_nodes;
		adjacencyList = new HashMap<Integer, Set<Edge>>();
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
			Edge e = new Edge(i, j, Math.random());
			boolean wasAdded = adjacencyList.get(i).add(e);
			if (wasAdded) {
				count++;
			}

		}
	}

	/*
	 * BufferedReader br = new BufferedReader(new FileReader("file.txt")); try {
	 * StringBuilder sb = new StringBuilder(); String line = br.readLine();
	 * 
	 * while (line != null) { sb.append(line);
	 * sb.append(System.lineSeparator()); line = br.readLine(); } String
	 * everything = sb.toString(); } finally { br.close(); }
	 */
	public Graph(String graph_file, boolean isZeroBased,int size) {
		// read data from file, store in adjacencyList
		adjacencyList = new HashMap<Integer, Set<Edge>>();
		for(int i =0; i<size;i++){
			adjacencyList.put(i, new HashSet<Edge>());
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(graph_file));
		//	StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			long start = System.currentTimeMillis();
			int i =-1;
			int j =-1;
			while (line != null){
				String[] s = line.split("\t");
				if(isZeroBased){
					i = Integer.valueOf(s[0].trim());
				    j = Integer.valueOf(s[1].trim());
				}else{
					i = Integer.valueOf(s[0].trim())-1;
				    j = Integer.valueOf(s[1].trim())-1;
				}
				Edge e = new Edge(i,j,Math.random());
				/*if(!adjacencyList.containsKey(i)){
					adjacencyList.put(i, new HashSet<Edge>());
				}*/
				adjacencyList.get(i).add(e);
				line = br.readLine();
			}
			n_nodes = size;
			System.out.println(" Time in ms "+ String.valueOf(System.currentTimeMillis() - start));
			br.close();
			//System.out.println(sb.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean add_edge(Edge e) {
		return adjacencyList.get(e.source).add(e);
	}

	public static void main(String[] args) {
		// Graph g = new Graph(0.4, 5);
		Graph g = new Graph("src/data/soc-pokec-relationships.txt", false,1632803);
		System.out.println("size of soc-pokec "+g.adjacencyList.size());
		Graph g_p = new Graph("src/data/wiki-Talk.txt", true,2394385);
		System.out.println("size of wiki-talk "+g_p.adjacencyList.size());
		Graph g_s = new Graph("src/data/as-skitter.txt", true,1696415);
		System.out.println("size of skitter "+g_s.adjacencyList.size());
	/*	Graph g1 = new Graph("src/test.txt",false,40);
		 System.out.println(" Graph " + g1.adjacencyList);*/
	}
}
