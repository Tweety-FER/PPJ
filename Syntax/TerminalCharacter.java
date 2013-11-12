import java.io.Serializable;


/**
 * A terminal character of formal grammar. It contains the symbol itself and the information whether it is used
 * as a synchronisation character.
 * @author Luka Skukan
 *
 */
public class TerminalCharacter implements Serializable {

	private static final long serialVersionUID = -3218849804223181514L;

	/**
	 * The symbol
	 */
	final public String symbol;
	
	/**
	 * Whether it is used a synchronisation character if LR parsing
	 */
	final public boolean isSync;
	
	/**
	 * Constructs a new terminal character.
	 * @param symbol Character
	 * @param isSync Is it a synchronisation character?
	 */
	public TerminalCharacter(String symbol, boolean isSync) {
		this.symbol = symbol;
		this.isSync = isSync;
	}
	
	@Override
	public String toString() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isSync ? 1231 : 1237);
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof String) {
			return ((String)obj).equals(this.symbol);
		}
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TerminalCharacter other = (TerminalCharacter) obj;
		if (isSync != other.isSync)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
	
}
