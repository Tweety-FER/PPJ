import java.util.ArrayList;
import java.util.List;

/**
 * A node in a syntactic tree. Contains pointers to children and string contents of a node,
 * as well as syntactic properties, only some of which are defined.
 * @author Juraj Doncevic
 *
 */
public class SyntacticTreeNode {
	
	/**
	 * Contents of the node as a string
	 */
	private String contents;
	
	/**
	 * References to children nodes
	 */
	private List<SyntacticTreeNode> children;
	
	/**
	 * Syntactic type of the node, if any.
	 */
	private Type type;
	
	/**
	 * Multiple types of the node, if any (e.g. all characters for a string)
	 */
	private List<Type> types;
	
	/**
	 * Name of the node, if any (e.g. identifier)
	 */
	private String name;
	
	/**
	 * Multiple names of the node, if any (e.g. function parameters)
	 */
	private List<String> names;
	
	/**
	 * Return type of a function node, if any.
	 */
	private Type returnType;
	
	/**
	 * Inherited type in a list of declarations (e.g. int a = 2, b, c = -2;)
	 */
	private Type inheritedType;
	
	/**
	 * Argument types in a function node, if any.
	 */
	private List<Type> argTypes;
	
	/**
	 * Indicates whether the node is an l-expression.
	 */
	private boolean lExpression;
	
	/**
	 * Constructs a childless node
	 * @param contents String contents of a syntactic node
	 */
	public SyntacticTreeNode(String contents){
		this.contents = contents;
		this.children = new ArrayList<SyntacticTreeNode>();
		this.type = Type.None;
		this.types = new ArrayList<Type>();
		this.returnType = Type.None;
		this.argTypes = new ArrayList<Type>();
		this.lExpression = false;
		this.names = new ArrayList<String>();
		this.inheritedType = Type.None;
	}
	
	/**
	 * Adds a child to the node
	 * @param child Child node
	 */
	public void addChild(SyntacticTreeNode child) {
		this.children.add(child);
	}
	
	/**
	 * Gets the node's children
	 * @return Children
	 */
	public List<SyntacticTreeNode> getChildren(){
		return this.children;
	}
	
	/**
	 * Gets a child by index.
	 * @param i Index
	 * @return Child, if there is one by such an index.
	 */
	public SyntacticTreeNode getChild(int i) {
		return this.children.get(i);
	}
	
	/**
	 * Creates an information packet from the node.
	 * @return Information packet
	 */
	public SyntaxInformationPacket getInfoPacket() {
		return new SyntaxInformationPacket(this.contents);
	}
	
	/**
	 * Gets the node's contents.
	 * @return Contents
	 */
	public String getContents(){
		return this.contents;
	}	
	
	/**
	 * Sets the node's type
	 * @param type Type
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * Gets the node's type.
	 * @return Type
	 */
	public Type getType() {
	//	if(this.type.equals(Type.Function)) return this.returnType;
		return this.type;
	}
	
	/**
	 * Checks whether the node is a function
	 * @return Is the node a function
	 */
	public boolean isFunction() {
		return this.type.equals(Type.Function);
	}
	
	/**
	 * Declares the node to be a function and sets it signature.
	 * @param returnType Return type of the function
	 * @param argTypes Argument types of the function
	 */
	public void setFunctionSignature(Type returnType, List<Type> argTypes) {
		this.type = Type.Function;
		this.returnType = returnType;
		this.argTypes = argTypes;
	}
	
	/**
	 * Gets the return type of the function
	 * @return Return type
	 */
	public Type getReturnType() {
		if(!isFunction()) return Type.None;
		return this.returnType;
	}
	
	/**
	 * Gets the argument types of a function
	 * @return Argument types
	 */
	public List<Type> getArgumentTypes() {
		return this.argTypes;
	}
	
	/**
	 * Checks whether a node is a function and its function signature matches the given one.
	 * @param returnType Return type of a given function signature
	 * @param argTypes Parameter types (ordered) of a given function signature
	 * @return Whether it matches its own signature
	 */
	public boolean matchesSignature(Type returnType, List<Type> argTypes) {
		if(!isFunction()) return false;
		
		boolean isTrue = this.returnType.equals(returnType);
		isTrue = isTrue && (this.argTypes.size() == argTypes.size());
		
		if(!isTrue) return isTrue;
		
		for(int i = 0; i < argTypes.size(); i++) {
			isTrue = isTrue && (this.argTypes.get(i).equals(argTypes.get(i)));
		}
		
		return isTrue;
	}
	
	/**
	 * Sets whether the node is an l-expression
	 * @param isLExpression Is it?
	 */
	public void setLExpression(boolean isLExpression) {
		this.lExpression = isLExpression;
	}
	
	/**
	 * Checks whether the node is an l-expression
	 * @return It could be. You never know.
	 */
	public boolean isLExpression() {
		return this.lExpression;
	}
	
	/**
	 * Inherits a type from another node. If it is a function, it also inherits its signature.
	 * @param node Another node. Or the same node. But the Bible says it shouldn't be done.
	 */
	public void inheritType(SyntacticTreeNode node) {
		if(node.isFunction()) {
			this.setFunctionSignature(node.getReturnType(), node.getArgumentTypes());
		} else {
			this.setType(node.getType());
		}
	}
	
	/**
	 * Assigns the node a list of types
	 * @param types Types
	 */
	public void setTypes(List<Type> types) {
		this.types = types;
	}
	
	/**
	 * Adds a type to the node's list of types
	 * @param type Type
	 */
	public void addType(Type type) {
		this.types.add(type);
	}
	
	/**
	 * Gets the node's list of types.
	 * @return Types
	 */
	public List<Type> getTypes() {
		return this.types;
	}
	
	/**
	 * Sets the node's list of names, for example function argument names.
	 * @param names Names
	 */
	public void setNames(List<String> names) {
		this.names = names;
	}
	
	/**
	 * Adds a name to the node's list of names.
	 * @param name Name 
	 */
	public void addName(String name) {
		this.names.add(name);
	}
	
	/**
	 * Gets the node's list of names
	 * @return Names
	 */
	public List<String> getNames() {
		return this.names;
	}
	
	/**
	 * Sets the node's name (singular)
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the node's name
	 * @return Probably Steve
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Yeah, I have no idea.
	 * @param inheritedType Nope
	 */
	public void setInheritedType(Type inheritedType) {
		this.inheritedType = inheritedType;
	}
	
	/**
	 * Still no idea.
	 * @return Sorry.
	 */
	public Type getInheritedType() {
		return this.inheritedType;
	}
	
	/**
	 * Printing function
	 * @param level Initial depth
	 * @return String representation of a node and its children
	 */
	private String print(int level) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; i++) {
			sb.append(" ");
		}
		
		sb.append(this.contents + "\n");
		for(SyntacticTreeNode child : this.children) {
			sb.append(child.print(level + 1));
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return print(0);
	}
}