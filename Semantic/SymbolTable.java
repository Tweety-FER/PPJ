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
	 * Checks whether all declared functions are also defined (together with the declaration,
	 * or later in the code).
	 * @return Are all functions defined
	 */
	public boolean allFunctionsDefined() {
		SymbolTable root = getRoot();
		for(Symbol f : root.symbols.values()) {
			if(f.isFunction() && !f.defined) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Adds a symbol to the table if it doesn't already exist at this scope.
	 * @param symbol Symbol
	 * @return Whether the symbol was successfully added.
	 */
	public boolean putSymbol(Symbol symbol) {
		if(this.symbols.containsKey(symbol.name)) return false;
		this.symbols.put(symbol.name, symbol);
		return true;
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
	 * Checks whether a function is defined. Returns false for non-functions and functions which are not
	 * declared, as well.
	 * @param name Function name
	 * @return Whether a function exists and is defined
	 */
	public boolean isDefinedFunction(String name) {
		SymbolTable root = getRoot();
		Symbol s = root.getSymbol(name);
		return (s != null && s.isFunction() && !s.defined);
	}
	
	/**
	 * Creates a function definition and declaration in one fell swoop, all in global scope.
	 * This isn't javascript, you know.
	 * @param name Function name
	 * @param returnType Function return type
	 * @param paramTypes Function parameter types or empty list for void
	 * @return Indication of success
	 */
	public boolean defineFunction(String name, Type returnType, List<Type> paramTypes) {
		SymbolTable root = getRoot();
		Symbol s = root.getSymbol(name);
		if(s != null) {
			if(s.isFunction() && !s.defined && paramTypes.equals(s.signature.y) 
					&& returnType.equals(s.signature.x)) {
					s.defined = true;
			return true;
			} else {
				return false;
			}
		}
		
		s = new Symbol(name, null, new Pair<Type, List<Type>>(returnType, paramTypes));
		s.defined = true;
		this.putSymbol(s);
		return true;
	}
	
	/**
	 * Declares a function in global scope.
	 * @param name Function name
	 * @param returnType Function return type
	 * @param paramTypes Function parameter types or empty for void
	 * @return Indication of success
	 */
	public boolean declareFunction(String name, Type returnType, List<Type> paramTypes) {
		if(this.symbols.containsKey(name)) {
			Symbol oldDeclaration = this.symbols.get(name);
			if(oldDeclaration.isFunction() && oldDeclaration.signature.x.equals(returnType) &&
					oldDeclaration.signature.y.equals(paramTypes)) {
				return true;
			}
			
			return false;
		}
		
		Symbol newSymbol = new Symbol(name, null, new Pair<Type, List<Type>>(returnType, paramTypes));
		this.putSymbol(newSymbol);
		return true;
	}
	
	/**
	 * Gets to the root of things
	 * @return Returns the root, global table.
	 */
	protected SymbolTable getRoot() {
		if(this.parent == null) return this;
		return this.parent.getRoot();
	}
	
	/**
	 * Gets an adult.
	 * @return Adult, or, if it is an adult, null.
	 */
	public SymbolTable getParent() {
		return this.parent;
	}
}
