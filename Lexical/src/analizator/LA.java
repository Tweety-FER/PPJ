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

/**
 * Lexical Analysis Engine which, provided it can read serialized data from analizator/states.ser,
 * reads a source file text from stdin and produces a list of lexical units in format "unitName line groupedText\n"
 * on stdout and a list of errors on stdout. Handles error recovery by dropping the first ungrouped
 * character and trying again from the next one.
 * @author Luka Skukan
 *
 */
public class LA {
	
	/**
	 * Index of last accepted character
	 */
	private static int last = 0;
	
	/**
	 * Index of character current step of analysis is starting from
	 */
	private static int init = 0;
	
	/**
	 * Current line index (1-based)
	 */
	private static int line = 1;
	
	/**
	 * Index of next character to be read
	 */
	private static int index = 0;
	
	/**
	 * Current lexical state
	 */
	private static LexState state;
	
	/**
	 * Text being parsed
	 */
	private static char[] text;
	
	/**
	 * Map of lexical state wrappers for lexical state name
	 */
	private static Map<String, LexState> states =
			new HashMap<String, LexState>();

	public static void main(String[] args) {
		List<LexState> tmpStates = deserialize();
		
		if(tmpStates.isEmpty()) {
			System.err.println("Cannot find parsed language data.");
			System.exit(-42);
		}
		
		for(LexState st : tmpStates) {
			states.put(st.getName(), st);
		}
		
		try {
			analyze(tmpStates.get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deserializes the list of lexical state wrappers required by the analysis engine from analizator/states.ser,
	 * returning an empty list if the deserialization fails.
	 * @return List of lexical state wrappers
	 */
	@SuppressWarnings("unchecked")
	private static List<LexState> deserialize() {
		List<LexState> lStates = null;
		try {
			FileInputStream fileIn = new FileInputStream("analizator/states.ser");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			lStates = (List<LexState>) objIn.readObject();
			objIn.close();
			fileIn.close();
			return lStates;
		} catch(Exception e) {
			return new ArrayList<>();
		}
	}
	
	/**
	 * Performs lexical analysis on text read (in bulk) from stdin, assuming the initial lexical
	 * state is the one for which the wrapper is provided.
	 * @param initial Initial lexical state wrapper
	 * @throws IOException If no stdin
	 */
	private static void analyze(LexState initial) throws IOException {
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
	
	/**
	 * Executes an action in set [VRATI_SE \d+, UDJI_U_STANJE stateName, NOVI_REDAK, lexUnitName]
	 * Actions, in the same order are:<br>
	 * <ol>
	 * <li>Grouping only first \d+ characters instead of all read as matching a regex rule</li>
	 * <li>Changing lexical state to one provided by name</li>
	 * <li>Incrementing line counter</li>
	 * <li>Printing the name of the lexical unit, line in which it occurred and grouped characters</li>
	 * </ol>
	 * @param actions
	 */
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

			System.out.println(token + " " + line + " " + rtrim(expr.toString()));
		}
	}
	
	/**
	 * Displays on stderr an error occured, for which character, state and line.
	 */
	private static void displayError() {
		System.err.println("Lexical error for state " + state.getName() + ", sign " + text[init] + " in " + line);
		
	}
	
	/**
	 * Right-trims a string
	 * @param s String
	 * @return Right-trimmed string
	 */
	private static String rtrim(String s) {
		int i;
		for(i = s.length() - 1; i >= 0 && Character.isWhitespace(s.charAt(i));) {
			i--;
		}
		
		return s.substring(0, i+1);
	}

	/**
	 * Reads lines in bulk from stream and splits them into a character array.
	 * @param stream S Stream
	 * @return Read characters as an array
	 * @throws IOException If stream cannot be read from
	 */
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
