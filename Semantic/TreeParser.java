import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * Tree parser, creates a tree from its string representation.
 * @author Juraj Doncevic
 *
 */
public class TreeParser {
	
	/**
	 * A stack of pairs (Node, depth)
	 */
	private static Stack<Pair<SyntacticTreeNode, Integer>> stack;
	
	/**
	 * Reads a tree from its string representation and returns the root.
	 * @param in Input stream
	 * @return Root of a syntactic tree
	 * @throws IOException If stdin somehow fails to exist.
	 */
	public static SyntacticTreeNode read(InputStream in) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		int level = 0;
		stack = new Stack<Pair<SyntacticTreeNode,Integer>>();
		
		String line;
		
		while((line = reader.readLine()) != null) {
			String trimmedLine = line.replaceAll("^\\s*(\\S.*)$", "$1");
			level = line.length() - trimmedLine.length();
			
			if(trimmedLine.isEmpty()) continue;
			
			if(stack.isEmpty() || level >= stack.peek().y.intValue()) {
				stack.push(new Pair<SyntacticTreeNode, Integer>(
						new SyntacticTreeNode(trimmedLine), 
						level)
						);
			} else {
				reduce(level - 1);
				stack.peek().x.addChild(new SyntacticTreeNode(trimmedLine));
			}
		}
		reduce(0);
		return stack.pop().x;
	}
	
	/**
	 * Reduces the stack until the top is at a certain depth, adding elements to a tree.
	 * @param level Level to which the stack must be trimmed.
	 */
	public static void reduce(int level) {
		Stack<Pair<SyntacticTreeNode, Integer>> temp = new Stack<Pair<SyntacticTreeNode,Integer>>();
		int topLevel, tempLevel = -1;
		
		while(true) {
			topLevel = stack.peek().y.intValue();
			if(!temp.isEmpty()) {
				tempLevel = temp.peek().y;
			} else {
				tempLevel = -1;
			}
			
			if(topLevel < tempLevel) {
				while(!temp.isEmpty()) {
					stack.peek().x.addChild(temp.pop().x);
				}
			}
			
			if(topLevel == level) break;
			
			temp.add(stack.pop());
		}
	}
}
