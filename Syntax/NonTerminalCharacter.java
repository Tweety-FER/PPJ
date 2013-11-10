import java.util.List;

/**
 * Representation of a non-terminal character in formal grammar.
 * @author Luka Skukan
 *
 */
public class NonTerminalCharacter {

	/**
	 * The character itself.
	 */
	public final String symbol;
	
	/**
	 * List of its transitions.
	 */
	public final List<List<String>> transitions;
	
	/**
	 * Constructs a new non-terminal character represented by given symbol and with
	 * the listed transitions.
	 * @param symbol The non-terminal character
	 * @param transitions List of right sides of its transition (elements of the right side are represented
	 * by another list)
	 */
	public NonTerminalCharacter(String symbol, List<List<String>> transitions) {
		this.symbol = symbol;
		this.transitions = transitions;
	}
	
	@Override
	public String toString() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result
				+ ((transitions == null) ? 0 : transitions.hashCode());
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
		NonTerminalCharacter other = (NonTerminalCharacter) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (transitions == null) {
			if (other.transitions != null)
				return false;
		} else if (!transitions.equals(other.transitions))
			return false;
		return true;
	}
}
