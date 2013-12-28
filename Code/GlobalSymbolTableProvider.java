/**
 * Singleton provider for the global scope symbol table.
 * @author Luka Skukan
 *
 */
public class GlobalSymbolTableProvider {

	/**
	 * Global scope symbol table
	 */
	private static SymbolTable globalTable;
	
	/**
	 * Provides (always the same) global scope symbol table.
	 * @return Global scope symbol table
	 */
	public static SymbolTable instance() {
		if(globalTable == null) {
			globalTable = new SymbolTable(null);
		}
		
		return globalTable;
	}
}
