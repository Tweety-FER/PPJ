

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analizator.Automaton;
import analizator.Pair;

public class AutomatonGenerator {
	
	private static int stateCnt;
	
	public static Automaton generateAutomaton(String name, String regex) {
		Automaton automaton = new Automaton(name);
		Pair<String, String> states = transform(regex.toCharArray(), automaton);
		automaton.setInitial(states.x);
		Set<String> temp = new HashSet<String>();
		temp.add(states.y);
		automaton.setAcceptable(temp);
		stateCnt = 0;
		return automaton;
	}
	
	private static Pair<String, String> transform(char[] symbols, Automaton aut) {
		List<char[]> picks = new ArrayList<char[]>();
		int bracketCnt = 0, i;

		/*
		 * Create initial states
		 */
		int leftState = newState(aut);	
		int rightState = newState(aut);
		/*
		 * Handle all or operators within paired brackets, forming a list of potential
		 * variations of expressions.
		 */
		for(i = 0; i < symbols.length ; i++) {
			if(matches('(', symbols, i)) bracketCnt++;
			else if(matches(')', symbols, i)) bracketCnt--;
			else if(bracketCnt == 0 && matches('|', symbols, i)) {
				picks.add(Arrays.copyOfRange(symbols, 0, i));
				symbols = Arrays.copyOfRange(symbols, i + 1, symbols.length);
				i = -1; //Continues to read from the new beginning
			}
		}
		
		if(picks.size() > 0) { //Create options for all picks
			picks.add(symbols);
			for(i = 0; i < picks.size(); i++) {
				Pair<String, String> temp = transform(picks.get(i), aut);
				addTransition(aut, null, name(aut, leftState), temp.x);
				addTransition(aut, null, temp.y, name(aut, rightState));
			}
		} else {
			boolean prefix = false;
			int lastState = leftState;
			
			for(i = 0; i < symbols.length; i++) {
				int a, b;
				if(prefix) {
					prefix = false;
					char transChar;
					switch (symbols[i]) {
						case 't': transChar = '\t'; break;
						case 'n': transChar = '\n'; break;
						case '_' : transChar = ' '; break;
						default: transChar = symbols[i];
					}
					
					a = newState(aut);
					b = newState(aut);
					
					addTransition(
							aut, String.valueOf(transChar), 
							name(aut, a), name(aut, b)
							);
				} else {
					if(symbols[i] == '\\') {
						prefix = true;
						continue;
					}
					
					if(symbols[i] == '(') {
						int j = findPair(i, symbols);
						Pair<String, String> temp = transform(
								Arrays.copyOfRange(symbols, i+1, j), 
								aut
								);
						a = getNumber(temp.x);
						b = getNumber(temp.y);
						i = j;
					} else {
						a = newState(aut);
						b = newState(aut);
						addTransition(
								aut, 
								symbols[i] == '$' ? null : String.valueOf(symbols[i]), 
								name(aut, a), 
								name(aut, b)
								);
					}
				}
				
				if(i + 1 < symbols.length && symbols[i+1] == '*') {
					int x = a;
					int y = b;
					a = newState(aut);
					b = newState(aut);
					
					addTransition(aut, null, name(aut, a), name(aut, x));
					addTransition(aut, null, name(aut, y), name(aut, b));
					addTransition(aut, null, name(aut, a), name(aut, b));
					addTransition(aut, null, name(aut, y), name(aut, x));
					i++;
				}
				
				addTransition(aut, null, name(aut, lastState), name(aut, a));
				lastState = b;
			}
			
			addTransition(aut, null, name(aut, lastState), name(aut, rightState));
		}
		
		return new Pair<String, String>(name(aut, leftState), name(aut, rightState));
	}
	
	public static Automaton combine(List<Automaton> automatons, String name) {
		Automaton combined = new Automaton(name);
		Set<String> states = new HashSet<String>();
		Set<String> initials = new HashSet<String>();
		Set<String> finals = new HashSet<String>();
		Map<Pair<String, String>, Set<String>> transitions = 
				new HashMap<Pair<String, String>, Set<String>>();
		
		String initialState = "initial";
		
		for(Automaton automaton : automatons) {
			states.addAll(automaton.getStates());
			initials.add(automaton.getInitial());
			finals.addAll(automaton.getAcceptables());
			transitions.putAll(automaton.getTransitions());
		}
		
		combined.setAllStates(states);
		combined.setAllTransitions(transitions);
		
		combined.addState(initialState);
		
		combined.addTransition(new Pair<String, String>(initialState, null), initials);
		
		combined.setInitial(initialState);
		combined.setAcceptable(finals);
		
		return combined;
	}
	
	private static int getNumber(String s) {
		return Integer.parseInt(s.replaceAll("^\\D+(\\d+)$", "$1"));
	}
	
	private static int findPair(int start, char[] symbols) {		
		for(int j = start + 1, brCnt = 0; j < symbols.length; j++) {
			if(symbols[j] == '(' && isOperator(j, symbols)) brCnt++;
			else if(symbols[j] == ')' && isOperator(j, symbols)) {
				if(brCnt == 0) {
					return j;
				}
				brCnt--;
			}
		}
		return start;
	}
	
	private static int newState(Automaton aut) {
		int state = stateCnt++;
		aut.addState(aut.getName() + state);
		return state;
	}
	
	private static String name(Automaton a, int n) {
		return a.getName() + n;
	}
	
	private static void addTransition(
			Automaton automaton, String c, String from, String ... to
			) {
		Set<String> tempSet = new HashSet<String>();
		for(String t : to) {
			tempSet.add(t);
		}
		
		automaton.addTransition(
				new Pair<String, String>(from, c), 
				tempSet
			);
	}
	
	private static boolean matches(char c, char[] symbols, int i) {
		return symbols[i] == c && isOperator(i, symbols);
	}
	
	private static boolean isOperator(int j, char[] symbols) {
		int cnt = 0;
		for(int i = j - 1;; cnt++, i--) {
			if(i < 0 || symbols[i] != '\\') break;
		}

		return cnt % 2 == 0;
	}
}
