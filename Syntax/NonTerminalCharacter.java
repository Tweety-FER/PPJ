import java.util.List;

public class NonTerminalCharacter {

	public final String symbol;
	
	public final List<List<String>> transitions;
	
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
