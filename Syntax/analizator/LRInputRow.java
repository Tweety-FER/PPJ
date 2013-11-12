
public class LRInputRow {
	
	public final String symbol;
	
	public final int line;
	
	public final String contents;
	
	public LRInputRow(String symbol, int line, String contents) {
		this.symbol = symbol;
		this.line = line;
		this.contents = contents;
	}
	
	@Override
	public String toString() {
		return symbol + " " + line + " " + contents;
	}

}
