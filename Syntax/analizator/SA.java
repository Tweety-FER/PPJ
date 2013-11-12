import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class SA {

	public static void main(String[] args) {
		Pair<Table, Table> tables = deserializeTables();
		List<TerminalCharacter> terminals = deserializeTerminal();
		String initialState = deserializeInitial();
		if(tables == null || terminals == null || initialState == null) {
			System.err.println("Could not deserialize data. Check if it exists!");
			System.exit(20);
		}
		
		Table action = tables.x;
		Table newState = tables.y;
		
		LRParser parser = new LRParser(initialState, terminals, action, newState);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		List<LRInputRow> input = new ArrayList<LRInputRow>();
		String line;
		while(true) {
			try {
				line = reader.readLine();
				if(line == null) break;
				
				String symbol = line.replaceAll("^(.+?)\\s(\\d+)\\s(.+)$", "$1");
				int lineNum = Integer.valueOf(line.replaceAll("^(.+?)\\s(\\d+)\\s(.+)$", "$2"));
				String contents = line.replaceAll("^(.+?)\\s(\\d+)\\s(.+)$", "$3");
				input.add(new LRInputRow(symbol, lineNum, contents));
			} catch (IOException e) {
				System.err.println("Apparently there is no stdin. Huh");
				System.exit(9000 + 1);
			}
			
		}
		
		System.out.println(parser.parse(input));
	}
	
	@SuppressWarnings("unchecked")
	public static Pair<Table, Table> deserializeTables() {
		Pair<Table, Table> tables;
		try {
			FileInputStream fileIn = new FileInputStream("analizator/tables.ser");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			tables = (Pair<Table, Table>) objIn.readObject();
			objIn.close();
			fileIn.close();
			return tables;
		} catch(Exception e) {
			return null;
		}
	}
	
	public static String deserializeInitial() {
		String initial;
		try{
			FileInputStream fileIn = new FileInputStream("analizator/initialState.ser");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			initial = (String) objIn.readObject();
			objIn.close();
			fileIn.close();
			return initial;
		} catch(Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<TerminalCharacter> deserializeTerminal() {
		List<TerminalCharacter> terminals;
		try{
			FileInputStream fileIn = new FileInputStream("analizator/terminal.ser");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			terminals = (List<TerminalCharacter>) objIn.readObject();
			objIn.close();
			fileIn.close();
			return terminals;
		} catch(Exception e) {
			return null;
		}
	}
}
