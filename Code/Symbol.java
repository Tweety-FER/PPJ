import java.util.List;

/**
 * A representation of a symbol in a compiler's table of symbols.
 * A symbols has a name, type and signature. Variables will have types, but not signatures,
 * while functions will have signatures but not types.
 * 
 * The symbol was given proper schooling and knows whether it is a function or variable.
 * @author Luka Skukan
 *
 */
public class Symbol {

	/**
	 * Variable or function name
	 */
	final String name;
	
	/**
	 * Variable type, or null for functions.
	 */
	final Type type;
	
	/**
	 * Function signature as pair of (Return type, List of parameter types)
	 */
	final Pair<Type, List<Type>> signature;
	
	boolean defined;
	
	/**
	 * Constructor
	 * @param name Name of the function or variable
	 * @param type Variable type or null for functions
	 * @param signature Function signature or null for variables
	 */
	public Symbol(String name, Type type, Pair<Type, List<Type>> signature) {
		this.name = name;
		this.type = type;
		this.signature = signature;
		this.defined = false;
	}
	
	/**
	 * Checks whether a symbol is a function.
	 * @return Is symbol a function
	 */
	public boolean isFunction() {
		return (this.type == null && this.signature != null);
	}
	
	/**
	 * Checks whether a symbol is a variable
	 * @return Is symbol a variable
	 */
	public boolean isVariable() {
		return (this.type != null && this.signature == null);
	}
	
	/**
	 * Gets return type if a function or null if a variable
	 * @return Return type or null
	 */
	public Type getReturnType() {
		if(this.isVariable()) {
			return null;
		}
		
		return this.signature.x;
	}
	
	/**
	 * Gets parameter types (in order) if a function or null if a variable.
	 * @return Parameter types
	 */
	public List<Type> getParamTypes() {
		if(this.isVariable()) {
			return null;
		}
		
		return this.signature.y;
	}
}
