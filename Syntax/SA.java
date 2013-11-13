import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class SA {

	private static String initialState;
	private static List<TerminalCharacter> tcs;
	private static Pair<Table, Table> tables;
	private static Table actionTable;
	private static Table newStateTable;
	private static GenerativeTreeNode start;
	private static List<String> syncChars;
	private static List<String> rows = new ArrayList<String>();
	private static int index = 0;
	
	private static Stack<GenerativeTreeNode> tree = new Stack<GenerativeTreeNode>();
	private static Stack<String> stack = new Stack<String>();
	
	public static void main(String[] args) {
		deserialize();
		stack.push(initialState);
		actionTable = tables.x;
		newStateTable = tables.y;
		syncChars = getSyncChars();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			while(true) {
				String s = reader.readLine();
				if(s == null || s.trim().isEmpty()) {
					break;
				}
				rows.add(s);
			}
			String s = "";
			while(true) {	
				if(index < rows.size()) {
					s = rows.get(index);
				} else {
					s = "#";
				}
				
				String symbol = "";
				LRInputRow row = null;
				
				if(s.equals("#")) {
					symbol = "#";
				} else {
					String[] parts = getParts(s);
					row = new LRInputRow(parts[0], Integer.parseInt(parts[1]), parts[2]);
					symbol = row.symbol;
				}
				String action = actionTable.get(stack.peek(), symbol);
				//System.out.println("Status report: " + stack.peek() + ", " + symbol + " : " + action);
				
				if(action.startsWith("Pomakni")) {
					actionPomakni(action, row);
					index++;
				} else if(action.startsWith("Reduciraj")) {
					actionReduciraj(action);
				} else if(action.startsWith("Prihvati")) {
					actionPrihvati();
					break;
				} else if(action.startsWith("Odbaci")) {
					actionOdbaci(row);
				} else {
					System.err.println("What the...? Something went horribly wrong! Your program is now dead! :(");
					System.exit(-5);
				}
			}
		} catch(IOException e) {
			System.err.println("Error happened during reading from stdin.");
			System.exit(-1);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println("Error happened during closing input stream.");
					System.exit(-2);
				}
			}
		}
		System.out.println(start.toString());
		
	}
	
	
	private static String[] getParts(String s) {
		char[] line = s.toCharArray();
		String symbol = null;
		String lineNumber = null;
		String contest;
		int i;
		for(i=0; i < line.length; i++) {
			if(line[i] == ' ') {
				symbol = s.substring(0, i);
				break;
			}
		}
		int j;
		for(j=i + 1; j < line.length; j++) {
			if(line[j] == ' ') {
				lineNumber = s.substring(i + 1, j);
				break;
			}
		}
		contest = s.substring(j+1);
		return new String[] {symbol, lineNumber, contest};
	}


	private static void actionOdbaci(LRInputRow row) {
		Set<String> goodOnes = new HashSet<String>();
		String state = stack.peek();
		for(String s : actionTable.getColSet()) {
			String act = actionTable.get(state, s);
			if(act != null && !act.startsWith("Odbaci")) {
				goodOnes.add(s);
			}
		}
		
		if(goodOnes.isEmpty()) goodOnes.add("$");
		
		System.err.println("Error in line " + row.line + ". " + "Expecting some of those: " 
					+ goodOnes + ". " + "Read: " + row.symbol + "->" + row.contents);
		
		String symbol = "";
		while(true) {
			
			if (index == rows.size()) {
				System.err.println("Error during recovery.");
				System.exit(-2);
			}
			String[] parts = getParts(rows.get(index));
			symbol = parts[0];
			if (syncChars.contains(symbol)) {
				break;
			}
			index++;
		}
		
		while(true) {
			state = stack.peek();
			String action = actionTable.get(state, symbol);
			if(action != null && !action.startsWith("Odbaci")) {
				break;
			}
			stack.pop();
		}
	}


	private static void actionPrihvati() {	
		start = tree.pop();
	}


	private static void actionReduciraj(String action) {
		String reduction = action.substring(action.indexOf("(") + 1, action.length() - 1);
		String[] sides = reduction.split("->");
		String left = sides[0];
		String[] right = sides[1].split("\\s+");
		int length;
		
		if(right[0].equals("$")) {
			length = 0;
		} else {
			length = 2 * right.length;
		}
		
		GenerativeTreeNode node = new GenerativeTreeNode(left);
		
		if(length == 0) {
			GenerativeTreeNode leaf = new GenerativeTreeNode("$");
			node.addChild(leaf);
		} else {
			Stack<GenerativeTreeNode> temp = new Stack<GenerativeTreeNode>();
			for(int i=0; i < length; i++) { //1, <=
				stack.pop();
				if(i % 2 == 0) {
					temp.push(tree.pop());
					//node.addChild(tree.pop());
				}
			}
			int size = temp.size();
			for(int i=0; i < size; i++) {
				node.addChild(temp.pop());
			}
		}
		
		tree.push(node);
		
		String state = newStateTable.get(stack.peek(), left);
		stack.push(left);
		stack.push(state.substring(state.indexOf("(") + 1, state.length() - 1));
	}


	private static void actionPomakni(String action, LRInputRow row) {
		String state = action.substring(action.indexOf("(") + 1, action.length() - 1);
		stack.push(row.symbol);
		stack.push(state);

		GenerativeTreeNode node = new GenerativeTreeNode(row.toString());
		tree.push(node);
	}


	private static List<String> getSyncChars() {
		List<String> syncChars = new ArrayList<String>();
		for(TerminalCharacter c : tcs) {
			if(c.isSync) {
				syncChars.add(c.symbol);
			}
		}
		return syncChars;
	}


	@SuppressWarnings("unchecked")
	private static boolean deserialize() {
        FileInputStream inFile = null;
        ObjectInputStream inObject = null;
       
        try {
                inFile = new FileInputStream("initialState.ser");
                inObject = new ObjectInputStream(inFile);
                initialState = (String) inObject.readObject();
                inFile.close();
                inObject.close();
               
                inFile = new FileInputStream("terminal.ser");
                inObject = new ObjectInputStream(inFile);
                tcs = (List<TerminalCharacter>) inObject.readObject();
                inFile.close();
                inObject.close();
               
                inFile = new FileInputStream("tables.ser");
                inObject = new ObjectInputStream(inFile);
                tables = (Pair<Table, Table>) inObject.readObject();
                inFile.close();
                inObject.close();
               
        } catch (IOException e) {
            return false;
        } catch (ClassNotFoundException e) {
			return false;
		} finally {
			try {
				if(inFile != null) {
					inFile.close();
				}
				if(inObject != null) {
					inObject.close();
				}
			} catch(IOException e) {
				System.err.println("Error during deserialization!");
			}
		}
        return true;
	}
}
