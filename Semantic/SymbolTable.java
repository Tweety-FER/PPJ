import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A table of symbols used by a compiler.<br>
 * The table creates a tree-like structure to represent scope, with the root being global scope.
 * Symbols are represented by a map of name -> Symbol.
 * The table supports adding symbols, getting symbols (which is done recursively - if a variable cannot
 * be found in current scope, the check carries on to the parent scope), making <s>love</s> children and
 * parent-locating.
 * @author Luka Skukan
 *
 */
public class SymbolTable {

	/**
	 * Parent table (wider scope)
	 */
	private SymbolTable parent;
	
	/**
	 * Children tables (narrower scope)
	 */
	private List<SymbolTable> children;
	
	/**
	 * Dem naughty symbols
	 */
	private Map<String, Symbol> symbols;
	
	/**
	 * Constructor. Creates a childless, symbol-less, alone, miserable table.
	 * @param parent Parent table
	 */
	public SymbolTable(SymbolTable parent) {
		this.parent = parent;
		this.children = new ArrayList<SymbolTable>();
		this.symbols = new HashMap<String, Symbol>();
	}
	
	/**
	 * Gets the symbol which has that name. If there is no such symbol in the current scope,
	 * it carries on to the parent.
	 * @param name Symbol name
	 * @return Symbol or null if no symbol is found
	 */
	public Symbol getSymbol(String name) {
		if(this.symbols.containsKey(name)) {
			return this.symbols.get(name);
		}
		
		if(this.parent == null) {
			return null;
		}
		
		return this.parent.getSymbol(name);
	}
	
	/**
	 * Adds a symbol to the symbol table
	 * @param symbol Symbol
	 */
	public void putSymbol(Symbol symbol) {
		this.symbols.put(symbol.name, symbol);
	}
	
	/**
	 * Creates a child table and returns a reference to it.
	 * @return
	 */
	public SymbolTable makeChild() {
		SymbolTable child = new SymbolTable(this);
		this.children.add(child);
		return child;
	}
	
	/**
	 * Gets an adult.
	 * @return Adult, or, if it is an adult, null.
	 */
	public SymbolTable getParent() {
		return this.parent;
	}
}
