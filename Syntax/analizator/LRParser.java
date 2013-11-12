import java.util.List;
import java.util.Stack;

import javax.naming.InitialContext;


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
		this.index = 0;
		this.stack = new Stack<String>();
		this.stack.push(this.initialState);
		
		String state, LRAction;
		LRInputRow character;
		
		while(true) {
			state = stack.peek();
			character = input.get(index);
			LRAction = this.action.get(state, character.symbol);
			
			if(LRAction.matches("^Pomakni\\(.+?\\)$")) {
				//Sagradi novi list, PRETPOSTAVLJAM OD character, i pushaj ga na stack tree
				String to = LRAction.replaceAll("^Pomakni\\((.+?)\\)$", "$1");
				stack.push(character.toString());
				stack.push(to);
				index++;
			} else if(LRAction.matches("^Reduciraj\\(.+?\\)$")) {
				//Sagradi novi cvor od left
				//Popaj sa stoga tree len/2 cvorova i sve ih stavi kao djecu novom cvoru
				//(mozda u obrnutom redoslijedu)
				//Pushaj novi cvor
				String left = LRAction.replaceAll("^Reduciraj\\((.+?)->.+?\\)$", "$1");
				String right = LRAction.replaceAll("^Reduciraj\\(.+?->(.+?)\\)$", "$1");
				int len;
				
				if(right.equals("$")) {
					len = 0;
				} else {
					len = 2 * right.length();
				}
				
				for(int i = 0; i < len; i++) {
					stack.pop();
				}
				
				state = stack.peek();
				stack.push(left);
				stack.push(newState.get(state, left));
				
			} else if(LRAction.matches("^Prihvati\\(\\)")) {
				return tree.pop();
			} else { //Odbaci()
				//ispis pogreske:
				//za stanje i sve znakove dodaj u Set znakova za koje prijelaz nije Odbaci()
				//ispisi line dobri znakovi znak koji si dobio, sadrzaj znaka
				
				//idemo kroz niz dok ne naidemo na sync
				//Ako smo na index == length nema oporavka, umri
				//nasli smo sync
				//dok(stog nije prazan)
				//uzmi stanje s vrha
				//ako nije odbaci Akcija[stanje, sync] -> uzmi, nekak izadi
				//inace dalje
				//ako si na kraju stoga nema oporavka
				return null;
			}
		}
	}
}
