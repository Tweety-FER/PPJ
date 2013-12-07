import java.util.ArrayList;
import java.util.List;

/**
 * A node in a syntactic tree. Contains pointers to children and string contents of a node.
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
	 * Constructs a childless node
	 * @param contents String contents of a syntactic node
	 */
	public SyntacticTreeNode(String contents){
		this.contents = contents;
		this.children = new ArrayList<SyntacticTreeNode>();
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
	 * Gets the node's contents.
	 * @return Contents
	 */
	public String getContents(){
		return this.contents;
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