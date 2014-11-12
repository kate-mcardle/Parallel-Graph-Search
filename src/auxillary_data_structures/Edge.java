package auxillary_data_structures;

public class Edge {
	public int source;
	public int destination;
	public double weight;

	public Edge(int source, int destination, double weight) {
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destination;
		result = prime * result + source;
		return result;
	}

	@Override
	public boolean equals(Object obj) { // Note: Does not compare the weights of the edges
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (destination != other.destination)
			return false;
		if (source != other.source)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return source +" -- ( "+((double)Math.round(weight * 100)/100.0) + ") -->" + destination;
	}
}
