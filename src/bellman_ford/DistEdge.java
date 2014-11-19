package bellman_ford;

import auxillary_data_structures.Edge;

public class DistEdge {
	double distance;
	Edge edge;
	
	public DistEdge(double distance, Edge edge) {
		this.distance = distance;
		this.edge = edge;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DistEdge other = (DistEdge) obj;
		if (Double.doubleToLongBits(distance) != Double
				.doubleToLongBits(other.distance))
			return false;
		if (edge == null) {
			if (other.edge != null)
				return false;
		} else if (!edge.equals(other.edge))
			return false;
		return true;
	}
}
