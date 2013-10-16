package analizator;

import java.io.Serializable;


/**
 * A basic implementation of an immutable pair. Holds two immutable values.
 * @author Luka Skukan
 *
 * @param <X> Generic first argument in immutable pair.
 * @param <Y> Generic second argument in immutable pair.
 */
public class Pair<X, Y> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6081823517209889511L;

	public final X x;
	
	public final Y y;
	
	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}
	
	
	@Override
	public String toString() {
		String strX = x == null ? null : x.toString();
		String strY = y == null ? null : y.toString();
		return "(" + strX + ", " + strY + ")";
	}
}
