
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import analizator.Automaton;
import analizator.ILexicalAnalizatorState;
import analizator.LexState;
import analizator.Pair;

public class GLA {
	
	public static void main(String[] args) {
		Map<String, List<Pair<String, List<String>>>> stateRules = null;
		List<ILexicalAnalizatorState> lexStates =
				new ArrayList<ILexicalAnalizatorState>();
		
		try {
			stateRules = RuleParser.parse();
		} catch (IOException e) {
			System.err.println("Error : could not read rules from stdin.");
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
	
	private static boolean serializeList(List<ILexicalAnalizatorState> states) {
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
