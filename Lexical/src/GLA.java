
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import analizator.Automaton;
import analizator.LA;
import analizator.LexState;
import analizator.Pair;

/**
 * Lexical Analysis machine generator. Taking a standardised file from stdin, generates and serializes
 * data structures to be used by a generic Lexical Analysis Machine. The file should have the following format:
 * <br>
 * <p>
 * {regular_definition_name} = regular_definition //0 or more lines like this
 * %X Lexical_Unit1 ... LexicalUnit_n //alphanumeric and _ are allowed<br>
 * %L Lexical_Rule1 ... LexicalRule_n //ditto<br>
 * &lt;LexicalUnit&gt; regularExpression<br>
 * {<br>
 * 	action1<br>
 * 	action2 <br>
 * 	...	//0 or more<br>
 * } // 0 or more of these<br>
 * </p>
 * Where allowed actions are - (NOP), VRATI_SE \d+, UDJI_U_STANJE stateName, lexicalRule, NOVI_RED
 * @author Luka Skukan
 * @see LA
 *
 */
public class GLA {
	
	/**
	 * Reads rules from stdin, parses them, creates wrapper LexStates and serializes them into
	 * analizator/states.set
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, List<Pair<String, List<String>>>> stateRules = null;
		List<LexState> lexStates =
				new ArrayList<LexState>();
		
		try {
			stateRules = RuleParser.parse();
		} catch (IOException e) {
			System.err.println("Error : could not read rules from stdin."); //This should never happen
			System.exit(-20);
		}
		
		Set<String> states = stateRules.keySet();
		
		for(String state : states) {
			char name = 'A';
			List<Pair<String, List<String>>> functions = stateRules.get(state);
			List<Automaton> automatons = new ArrayList<Automaton>();
			Map<String, List<String>> funForRegex = 
					new HashMap<String, List<String>>();
			
			for(Pair<String, List<String>> function : functions) {
				Automaton temp = AutomatonGenerator.generateAutomaton(
						String.valueOf(name), 
						function.x
						);
				automatons.add(temp);
				funForRegex.put(String.valueOf(name++), function.y);
			}

			lexStates.add(new LexState(AutomatonGenerator.combine(automatons, state), funForRegex));
		}

		serializeList(lexStates);
	}
	
	/**
	 * Serializes a given list into file analizator/states.ser
	 * @param states List of LexStates to serialize
	 * @return Boolean indicating success
	 */
	private static boolean serializeList(List<LexState> states) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;
		
		try {
			fileOut= new FileOutputStream("analizator/states.ser");
			objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(states);
			objOut.close();
			fileOut.close();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
}
