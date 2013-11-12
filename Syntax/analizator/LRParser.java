import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.naming.InitialContext;
import javax.swing.filechooser.FileSystemView;


public class LRParser {

	private Stack<String> stack;
	
	private Table action;
	
	private Table newState;
	
	private String initialState;
	
	private int index;
	
	private Stack<GenerativeTreeNode> tree;
	
	private List<TerminalCharacter> terminals;
	
	public LRParser(String initialState, List<TerminalCharacter> terminals, Table action, Table newState) {
		this.terminals = terminals;
		this.action = action;
		this.newState = newState;
		this.initialState = initialState;
	}
	
	public GenerativeTreeNode parse(List<LRInputRow> input) {
		input.add(new LRInputRow("#", 0, ""));
		this.index = 0;
		this.stack = new Stack<String>();
		this.stack.push(this.initialState);
		this.tree = new Stack<GenerativeTreeNode>();
		
		String state, LRAction;
		LRInputRow character;
		
		while(true) {
			state = stack.peek();
			character = input.get(index);
			LRAction = this.action.get(state, character.symbol);
			
			System.out.println("State " + state + ", Character " + character + " action " + LRAction);
			if(LRAction.matches("^Pomakni\\(.+?\\)$")) {
				//Sagradi novi list, PRETPOSTAVLJAM OD character, i pushaj ga na stack tree
				String to = LRAction.replaceAll("^Pomakni\\((.+?)\\)$", "$1");
				stack.push(character.toString());
				stack.push(to);
				
				GenerativeTreeNode newNode = new GenerativeTreeNode(character.toString());
				tree.push(newNode);
				
				index++;
			} else if(LRAction.matches("^Reduciraj\\(.+?\\)$")) {
				//Sagradi novi cvor od left
				//Popaj sa stoga tree len/2 cvorova i sve ih stavi kao djecu novom cvoru
				//(mozda u obrnutom redoslijedu)
				//Pushaj novi cvor
				String left = LRAction.replaceAll("^Reduciraj\\((.+?)->.+?\\)$", "$1");
				String[] right = LRAction.replaceAll("^Reduciraj\\(.+?->(.+?)\\)$", "$1").split("\\s");
				int len;
				
				if(right.equals("$")) {
					len = 0;
				} else {
					len = 2 * right.length;
				}
				
				GenerativeTreeNode newNode = new GenerativeTreeNode(left);
				
				if(len == 0) {
					GenerativeTreeNode child = new GenerativeTreeNode("$");
					newNode.addChild(child);
				}
				
				for(int i = 0; i < len; i++) {
					stack.pop();
					if(i % 2 == 0) {
						GenerativeTreeNode child = tree.pop();
						newNode.addChild(child);
					}
				}
				
				tree.push(newNode);
				
				state = stack.peek();
				stack.push(left);
				stack.push(newState.get(state, left));
				
			} else if(LRAction.matches("^Prihvati\\(\\)")) {
				return tree.pop();
			} else { //Odbaci()
				//ispis pogreske:
				//za stanje i sve znakove dodaj u Set znakova za koje prijelaz nije Odbaci()
				//ispisi line dobri znakovi znak koji si dobio, sadrzaj znaka
				Set<String> acceptable = new HashSet<String>();
				for(String c : action.getColSet()) {
					String cAction = action.get(state, c);
					if(cAction != null && !cAction.equals("Odbaci()")) {
						acceptable.add(c);
					}
				}
				
				System.err.println(
						"Error - line " + character.line + ": expected one of " + acceptable + " got "
						+ character.symbol + " as " + character.contents
						);
				
				while(true) {
					if(index == input.size()) {
						System.err.println("Error recovery process failed");
						return null;
					}
					
					character = input.get(index);
					System.out.println("Trying for " + character);
					if(isSync(character.symbol)) break;
					index++;
				}
				
				while(true) {
					if(stack.isEmpty()) {
						System.err.println("Error recovery process failed.");
						return null;
					}
					
					state = stack.peek();
					System.out.println("Trying for state " + state);
					LRAction = action.get(state, character.symbol);
					if(LRAction != null && !LRAction.equals("Odbaci()")) break;
					stack.pop();
				}
			}
		}
		
	}
	
	private boolean isSync(String c) {
		for(TerminalCharacter tc : terminals) {
			if(tc.isSync && tc.symbol.equals(c)) {
				return true;
			}
		}
		return false;
	}
}
