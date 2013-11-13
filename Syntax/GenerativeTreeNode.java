import java.util.ArrayList;
import java.util.List;


public class GenerativeTreeNode {

	private String contents;
	
	private List<GenerativeTreeNode> children;
	
	public GenerativeTreeNode(String contents) {
		this.contents = contents;
		this.children = new ArrayList<GenerativeTreeNode>();
	}
	
	public void addChild(GenerativeTreeNode child) {
		this.children.add(child);
	}
	
	protected String print(int depth) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < depth; i++) {
			sb.append(" ");
		}
		
		sb.append(contents + "\n");
		for(GenerativeTreeNode child : children) {
			sb.append(child.print(depth + 1));
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		String output = this.print(0);
		return output.substring(0, output.length() - 1);
	}
}
