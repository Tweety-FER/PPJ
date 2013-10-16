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

public class RuleParser {
	
	private static Map<String,String> definitions = new HashMap<String,String>();
	
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
	
	private static String replaceGroups(String rule) {
		Pattern p = Pattern.compile("\\{([A-Za-z_]+)\\}");
		Matcher m = p.matcher(rule);
		while(m.find()) {
			String replacement = "(" + definitions.get(m.group(1)) + ")";
			rule = rule.replace("{" + m.group(1) + "}", replacement);
		}

		return rule;
	}
	
	private static void splitInto(String line, List<String> container) {
		String[] parts = line.split("\\s+");
		for(int i = 1; i < parts.length; i++) {
			if(!parts[i].isEmpty()) { //Bad empty element! NO!
				container.add(parts[i]);
			}
		}
	}
	
	private static enum ParserState {
		REGULAR_DEFINITIONS,
		STATES,
		LEX_UNITS,
		LEX_RULES
	}
}
