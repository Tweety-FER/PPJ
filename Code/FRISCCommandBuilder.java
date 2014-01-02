import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FRISCCommandBuilder {
	
	/**
	 * Global variables with initial values (? maybe should be set to 0 always)
	 */
	private Map<String, Integer> variables;
	
	/**
	 *  String builder for output, if all goes well the output is created.
	 */
	private StringBuilder printer;
	
	/**
	 * Local variable container
	 */
	private StackContainer locals;
	
	private int lexprCounter = 0;
	
	private int branchCounter = 0;
	
	/**
	 * Initial strings and variable init
	 */
	public void init() {
		this.printer = new StringBuilder();
		this.variables = new HashMap<String, Integer>();
		this.locals = new StackContainer(null);
		printer.append("\tMOVE 40000, R7\n\tCALL F_MAIN\n\tHALT\n");
	}
	
	/**
	 * Add builtin functions, globals and print
	 */
	public void finalize() {
		//MULT, DIV and MOD currently only work for positive numbers, I will improve them later
		printer.append("MULT_BUILTIN\n");
		printer.append("\tPOP R1\n");
		printer.append("\tPOP R0\n");
		printer.append("\tMOVE 0, R6\n");
		printer.append("MULT_BUILTIN_LOOP\n");
		printer.append("\tCMP R1, 0\n");
		printer.append("\tJP_EQ MULT_BUILTIN_OUT\n");
		printer.append("\tSUB R1, 1, R1\n");
		printer.append("\tADD R6, R0, R6\n");
		printer.append("\tJP MULT_BUILTIN_LOOP\n");
		printer.append("MULT_BUILTIN_OUT\n");
		printer.append("\tRET\n");
		
		printer.append("DIV_BUILTIN\n");
		printer.append("\tPOP R1\n");
		printer.append("\tPOP R0\n");
		printer.append("\tMOVE 0, R6\n");
		printer.append("DIV_BUILTIN_LOOP\n");
		printer.append("\tCMP R0, R1\n");
		printer.append("\tJP_SLT DIV_BUILTIN_OUT\n");
		printer.append("\tSUB R0, R1, R0\n");
		printer.append("\tADD R6, 1, R6\n");
		printer.append("\tJP DIV_BUILTIN_LOOP\n");
		printer.append("DIV_BUILTIN_OUT\n");
		printer.append("\tRET\n");
		
		printer.append("MOD_BUILTIN\n");
		printer.append("\tPOP R1\n");
		printer.append("\tPOP R0\n");
		printer.append("\tMOVE 0, R6\n");
		printer.append("MOD_BUILTIN_LOOP\n");
		printer.append("\tCMP R0, R1\n");
		printer.append("\tJP_SLT MOD_BUILTIN_OUT\n");
		printer.append("\tSUB R0, R1, R0\n");
		printer.append("\tJP MOD_BUILTIN_LOOP\n");
		printer.append("MOD_BUILTIN_OUT\n");
		printer.append("\tADD R0, 0, R7\n");
		printer.append("\tRET\n");
		
		
		for(String variable : variables.keySet()) {
			Integer varData = variables.get(variable);
			printer.append("G_" + variable + " DW %D " + varData + "\n");
		}
		
		printer.append("\t`END");
		System.out.println(printer.toString());
	}
	
	/**
	 * Declares a variable in global or local scope (should know which one it is)
	 * @param name Variable name
	 */
	public void declareVariable(String name) {
		if(locals.isGlobal()) {
			variables.put(name, 0);
		} else {
			locals.push(name);
		}
	}
	
	/**
	 * Initializes a variable, should know whether local or global.
	 * @param name Variable name
	 * @param value Value
	 */
	public void initializeVariable(String name, Integer value) {
		String constant = "0" + Integer.toHexString(value);
		if(locals.isGlobal()) {
			variables.put(name, value);
			printer.append("\tMOVE " + constant + ", R0\n");
			printer.append("\tSTORE R0, (" + name + ")\n");
		} else {
			locals.push(name);
			printer.append("\tMOVE " + constant + ", (R7 + " + locals.getStrOffset(name) + ")\n");
		}
	}
	
	/**
	 * Used to number labels in logical expressions.
	 * @return Number of next logical expression.
	 */
	public int getNextLogicalExpressionNumber() {
		return this.lexprCounter++;
	}
	
	/**
	 * Used to number labels in branching expression
	 * @return Number of next branching expression
	 */
	public int getNextBranchNumber() {
		return this.branchCounter++;
	}
	
	/**
	 * Function call. Resets local variables and adds function parameters to them.
	 * @param name Function name, must be globally unique (use '?)
	 * @param variables
	 */
	public void enterFunction(String name, List<String> variables) {
		this.locals = locals.makeLove();
		for(String variable : variables) {
			locals.push(variable);
		}
		locals.putReturnDelimiter();
		
		printer.append("\tCALL F_" + name.toUpperCase() + "\n");
	}
	
	/**
	 * Enters a function block, resetting local variables.
	 */
	public void enterBlock() {
		this.locals = locals.makeLove();
	}
	
	/**
	 * Goes back one level on local variables
	 */
	public void exitFunctionOrBlock() {
		this.locals = locals.disappointParent();
	}
	
	/**
	 * Outputs whatever given
	 * @param any String
	 */
	public void freeStyle(String any) {
		printer.append(any + "\n");
	}
	
	/**
	 * Int -> Char
	 * @param i Int value
	 * @return Char value
	 */
	public int toChar(int i) {
		int prefix;
		if((i & 0x80000000) != 0) {
			prefix = 0x080;
		} else {
			prefix = 0;
		}
		return i & 0x7f | prefix;
	}
	
	/**
	 * Char -> Int
	 * @param i Char value
	 * @return Int value
	 */
	public int fromChar(int i) {
		int prefix;
		if((i & 0x080) != 0) {
			prefix = 0x800000;
		} else {
			prefix = 0;
		}
		
		return i & 0x7f | prefix;
	}
	
	/**
	 * Return from a function. Pops top stack value into R6 if not void function
	 * @param isVoid Is the function void
	 */
	public void doReturn(boolean isVoid) {
		if(!isVoid) printer.append("\tPOP R6\n");
		printer.append("\tRET\n");
	}
	
	/**
	 * Places a variable onto the stack by name. Should know whether it is a local or global one.
	 * Might not handle scope (tell me if so!)
	 * @param name Variable name
	 */
	public void putVariable(String name) {
		if(locals.local.contains(name)) {
			printer.append("\tLOAD (R7 + " + locals.getStrOffset(name) + "), R0\n");
		} else {
			printer.append("\tLOAD (" + name + "), R0");
		}
		printer.append("\tPUSH R0\n");
	}
	
	public void putConstant(int value) {
		String constant = "0" + Integer.toHexString(value);
		printer.append("\tMOVE " + constant + ", R0\n");
		printer.append("\tPUSH R0\n");
	}
	
	/**
	 * Trivial abstract binary operation
	 * @param op Operation name
	 * @param tag Tag or null if no tag
	 */
	public void doBinaryOp(String op, String tag) {
		if(tag != null) printer.append(tag + "\n");
		printer.append("\tPOP R1\n");
		printer.append("\tPOP R0\n");
		printer.append("\t" + op + " R0, R1, R0\n");
		printer.append("\tPUSH R0\n");
	}
	
	/**
	 * Local variable stack
	 * @author Luka Skukan
	 *
	 */
	private class StackContainer {
		/**
		 * Local variables by name
		 */
		private List<String> local;
		
		/**
		 * Parent stack
		 */
		private StackContainer parent;
		
		/**
		 * Child stack
		 */
		private StackContainer child;
		
		/**
		 * Given a parent constructs a new level
		 * @param parent Parent or null if top-level local variable stack
		 */
		public StackContainer(StackContainer parent) {
			this.parent = parent;
			this.child = null;
			this.local = new ArrayList<String>();
		}
		
		/**
		 * Is it a global variable stack? A global stack should never be used and should always
		 * reroute to global variable map when it tries to be used.
		 * @return Well, is it?
		 */
		public boolean isGlobal() {
			return this.parent == null;
		}
		
		/**
		 * Pushes a value
		 * @param var
		 */
		public void push(String var) {
			local.add(0, var);
		}
		
		/**
		 * Pops a value, but does not return it.
		 */
		public void pop() {
			local.remove(0);
		}
		
		/**
		 * Marks the space of a return variable. Just a delimiter for proper offset calculation.
		 */
		public void putReturnDelimiter() {
			this.push(":return");
		}
		
		/**
		 * Gets a hexadecimal representation of the offset for given variable in the local stack.
		 * To be used in conjecture with (R7 + offset). If there is no such variable, null is returned.
		 * @param var Variable name
		 * @return Offset or null if no such var
		 */
		public String getStrOffset(String var) {
			if(!local.contains(var)) return null;
			return "0" + Integer.toHexString(local.indexOf(var) * 4);
		}
		
		/**
		 * Produces a beautiful baby variable stack.
		 * @return Child variable stack.
		 */
		public StackContainer makeLove() {
			StackContainer child = new StackContainer(this);
			this.child = child;
			return child;
		}
		
		/**
		 * Returns one level, going back to its parent.
		 * @return Parent
		 */
		public StackContainer disappointParent() {
			StackContainer parent = this.parent;
			if(this.parent == null) return null;
			
			this.parent = null;
			parent.child = null;
			return parent;
		}
	}

}
