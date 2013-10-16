import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analizator.Pair;

/**
 * A rule parser which takes a standardised text from stdin and parses it into a defined set of rules and states.
 * For such a file, which defines a set of states, lexical units and lexical rules defined as sets of
 * regular expressions for states and describe actions to perform if something matches the expression, 
 * a Map is created.
 * 
 * The form of the Map is (StateName => (RegularExpression, ActionList))
 * Regular expressions and lists of actions are grouped together in a {@link Pair} structure.
 * 
 * @author Luka Skukan
 * @see Pair
 */
public class RuleParser {
	
	/**
	 * Used to map temporary definitions of form {name} = regex
	 */
	private static Map<String,String> definitions = new HashMap<String,String>();
	
	/**
	 * Parses input from stdin, creating a map of lists of pairs (regular expression => actions to perform)
	 * per state.
	 * @return Created map
	 * @throws IOException If stdin does not exist - never
	 */
	public static Map<String, List<Pair<String, List<String>>>> parse() throws IOException{
		List<String> states = new ArrayList<String>();
		List<String> lexUnits = new ArrayList<String>();
		Map<String, List<Pair<String, List<String>>>> stateQueues =
				new LinkedHashMap<String, List<Pair<String, List<String>>>>();
		BufferedReader fread = new BufferedReader(new InputStreamReader(System.in));
		
		String line;
		ParserState state = ParserState.REGULAR_DEFINITIONS;
		boolean insideFunction = false;
		String stateName = null;
		String regex = null;
		List<String> commandQueue = new ArrayList<String>();
		
		while((line = fread.readLine()) != null) {
			if(line.isEmpty()) continue;
			
			switch (state) {
				case REGULAR_DEFINITIONS:
					if(parseRegularDefinition(line)) {
						break;
					}
					state = ParserState.STATES;
					
				case STATES:
					state = ParserState.LEX_UNITS;
					if(line.matches("^%X.*$")) {
						splitInto(line, states);
						break;
					}
					
				case LEX_UNITS:
					state = ParserState.LEX_RULES;
					if(line.matches("^%L.*$")) {
						splitInto(line, lexUnits);
						break;
					}
					
				case LEX_RULES:
					//Pattern matching opening line of definition: <state>regex
					final String pattern = "^<(\\w+)>(.+)$";
					
					//If first line, memorise it
					if(line.matches(pattern)) {
						stateName = line.replaceAll(pattern, "$1");
						regex = replaceGroups(line.replaceAll(pattern, "$2"));
					//If start of declaration, set flag
					} else if(line.matches("\\s*\\{\\s*") && insideFunction == false) {
						insideFunction = true;
						commandQueue = new ArrayList<String>();
					//If end of declaration, unset flag and export inner functions
					} else if(line.matches("\\s*\\}\\s*") && insideFunction == true) {
						insideFunction = false;
						List<Pair<String, List<String>>> temp;
						Pair<String, List<String>> pair = new Pair<String,List<String>> (regex, commandQueue);
						
						if(stateQueues.containsKey(stateName)) {
							temp = stateQueues.get(stateName);
						} else {
							temp = new ArrayList<Pair<String, List<String>>>();
						}

						temp.add(pair);
						stateQueues.put(stateName, temp);
						
					//Must be a function! Mark it down, unless it's NOP
					} else {
						if(line.matches("^\\s*\\-\\s*$")) {
							continue;
						}
						
						commandQueue.add(line.trim());
					}
				default:
					break;
			}
		}
		
		fread.close();
		
		return stateQueues;
	}
	
	/**
	 * Parses a regular definition which adheres to format "{name} regex" and stores
	 * it internally.
	 * @param line Regular definition
	 * @return Boolean indicating success of the operation
	 */
	private static boolean parseRegularDefinition(String line) {
		if(line.charAt(0) == '%') {
			return false;
		}
		
		String name = line.replaceAll("^\\s*\\{(\\w+)\\}.*$", "$1");
		String rule = line.replaceAll("^.*?\\}\\s+(.+)$", "$1");
		
		rule = replaceGroups(rule);
		if(name == null || rule == null) return false;
		definitions.put(name, rule);
		return true;
	}
	
	/**
	 * Replaces groups within regular expressions of form "{definitionName}" with the definition
	 * of the same name.
	 * @param rule Rule to perform search-and-replace on.
	 * @return Rule with replaced groups.
	 */
	private static String replaceGroups(String rule) {
		Pattern p = Pattern.compile("\\{([A-Za-z_]+)\\}");
		Matcher m = p.matcher(rule);
		while(m.find()) {
			String replacement = "(" + definitions.get(m.group(1)) + ")";
			rule = rule.replace("{" + m.group(1) + "}", replacement);
		}

		return rule;
	}
	
	/**
	 * Splits a given line by whitespace and puts all but the first element into a given container.
	 * @param line Line to split
	 * @param container List of strings to fill
	 */
	private static void splitInto(String line, List<String> container) {
		String[] parts = line.split("\\s+");
		for(int i = 1; i < parts.length; i++) {
			if(!parts[i].isEmpty()) { //Bad empty element! NO!
				container.add(parts[i]);
			}
		}
	}
	
	/**
	 * Parser states the parser has to go internally while at work.
	 * Marks the current step of the parsing process.
	 * @author Luka Skukan
	 *
	 */
	private static enum ParserState {
		REGULAR_DEFINITIONS,
		STATES,
		LEX_UNITS,
		LEX_RULES
	}
}
