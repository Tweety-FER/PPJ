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

	/**
	 * First element.
	 */
	public final X x;
	
	/**
	 * Second element.
	 */
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
		if(x == null && y == null) return other.x == null && other.y == null;
		if(x == null) return other.x == null;
		if(y == null) return other.y == null;
		return (x.equals(other.x) && y.equals(other.y)) || (x.equals(other.y) && y.equals(other.x));
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
