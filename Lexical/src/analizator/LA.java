package analizator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Map;

public class LA {
	
	private static int last = 0;
	
	private static int init = 0;
	
	private static int line = 1;
	
	private static int index = 0;
	
	private static ILexicalAnalizatorState state;
	
	private static char[] text;
	
	private static Map<String, ILexicalAnalizatorState> states =
			new HashMap<String, ILexicalAnalizatorState>();

	public static void main(String[] args) {
		List<ILexicalAnalizatorState> tmpStates = deserialize();
		
		if(tmpStates.isEmpty()) {
			System.err.println("Cannot find parsed language data.");
			System.exit(-42);
		}
		
		for(ILexicalAnalizatorState st : tmpStates) {
			states.put(st.getName(), st);
		}
		
		try {
			analyze(tmpStates.get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<ILexicalAnalizatorState> deserialize() {
		List<ILexicalAnalizatorState> lStates = null;
		try {
			FileInputStream fileIn = new FileInputStream("analizator/states.ser");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			lStates = (List<ILexicalAnalizatorState>) objIn.readObject();
			objIn.close();
			fileIn.close();
			return lStates;
		} catch(Exception e) {
			return new ArrayList<>();
		}
	}
	
	private static void analyze(ILexicalAnalizatorState initial) throws IOException {
		text = readLines(System.in);
		state = initial;
		String expression = null;
		
		while(index < text.length) {
			if(index == text.length) break;
			while(index < text.length) {
				if(state.isEmpty()) {
					index--;
					break;
				} 
				
				if(state.isAcceptable()) {
					expression = String.valueOf(state.getAcceptedState().charAt(0));
					last = index;
				} 
				
				state.parseToken(text[index++]);
			}
				
			if(expression == null) {
				displayError();
				last = index = ++init;
			} else {
				execute(state.getMatchedActions(expression));
				expression = null;
			}

			state.reset();
		}	
	}
	
	
	private static void execute(List<String> actions) {
		int left = init, right = last, digit = 0;
		boolean print = false, change = false;
		String token = null;
		
		for(String action : actions) {
			if(action.matches("^VRATI_SE (\\d+)$")) {
				digit = Integer.valueOf(action.replaceAll(".*?(\\d+)", "$1"));
				change = true;
				
				
			} else if(action.matches("^UDJI_U_STANJE\\s+([A-Za-z_]+)\\s*$")) {
				String newState = action.replaceAll(
						"^UDJI_U_STANJE\\s+([a-zA-Z_]+)\\s*$", 
						"$1"
						);
				state = states.get(newState);
			} else if(action.equals("NOVI_REDAK")) {
				line++;
			} else {
				token = action;
				print = true;
			}
		}
		
		if(change) {
			init += digit;
			right = init;
			last = index = init;
		} else {
			init = last = index;
		}
		
		if(print) {
			StringBuilder expr = new StringBuilder();
			for(int i = left; i < right; i++) {
				expr.append(text[i]);
			}

			System.out.println(token + " " + line + " " + rtrim(expr.toString())); //TODO trim?
		}
	}
	
	private static void displayError() {
		System.err.println("Lexical error for state " + state.getName() + ", sign " + text[init] + " in " + line);
		
	}
	
	private static String rtrim(String s) {
		int i;
		for(i = s.length() - 1; i >= 0 && Character.isWhitespace(s.charAt(i));) {
			i--;
		}
		
		return s.substring(0, i+1);
	}

	private static char[] readLines(InputStream stream) throws IOException {
		StringBuffer data = new StringBuffer();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream)
				);
		
		char[] buffer = new char[1024];
		int numRead = 0;
		
		while((numRead = reader.read(buffer)) != -1) {
			data.append(String.valueOf(buffer, 0, numRead));
		}
		
		reader.close();
		return data.toString().toCharArray();
	}
}
