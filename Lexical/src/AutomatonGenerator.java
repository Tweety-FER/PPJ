
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analizator.Automaton;
import analizator.Pair;

/**
 * Generates automatons from regular expressions (CS-style, not Perl) or combines a list of automatons
 * into a single automaton. The automatons are nondeterministic finite automatons with epsilon transitions.
 * @author Luka Skukan
 * @see Automaton
 */
public class AutomatonGenerator {
	
	/**
	 * Keeps a count of number of states to keep state names unique.
	 */
	private static int stateCnt;
	
	/**
	 * Generates an Automaton of a given name from a CS-style regular expression. All state names
	 * will be formed from the automaton name and a unique number, eg. for name AUT -> [AUT0, ... AUTn]
	 * @param name Automaton name
	 * @param regex Regular expression for which an automaton is formed.
	 * @return Generated automaton
	 * @see Automaton
	 */
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
	
	/**
	 * Combines a list of automatons into a new one which runs them all in parallel, using a
	 * combined set of initial and acceptable states.
	 * @param automatons List of Automatons
	 * @param name Name of the new automaton
	 * @return Combined automaton
	 * @see Automaton
	 */
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
	
	/**
	 * Transforms a regular expression into a set of rules for a provided automaton.
	 * Employs the algorithm given in "Prevođenje Programskih Jezika, Srbljić, 2012., Zagreb" 
	 * @param symbols Array of symbols forming a regular expression
	 * @param aut Automaton for which to create states
	 * @return Pair of first and last state to go through for given regular expression
	 */
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
	
	/**
	 * Extracts the number in a string formed as (not_number+)(digit+)
	 * @param s String containing numbers prefixed by at least one not-number
	 * @return Number sufix of the string
	 */
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
	
	/**
	 * Creates a new state for the automaton, naming it automaton.name + first_available_number.
	 * @param aut Automaton for which to create and add a state
	 * @return Number given to the new state
	 */
	private static int newState(Automaton aut) {
		int state = stateCnt++;
		aut.addState(aut.getName() + state);
		return state;
	}
	
	/**
	 * Creates a state name for the automaton using the automaton's name and a number
	 * @param a Automaton
	 * @param n Number
	 * @return Name formed as automaton.name + number
	 */
	private static String name(Automaton a, int n) {
		return a.getName() + n;
	}
	
	/**
	 * Adds a transition for an automaton, which for state from and token c transitions into
	 * states given by to.
	 * @param automaton Automaton to which a transition needs to be added
	 * @param c Token to transition for
	 * @param from State to transition from
	 * @param to States to transition to
	 */
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
	
	/**
	 * Checks whether a member of a given character array at certain position matches a character.
	 * Matching is defined as being equal to that character and not being an operator.
	 * @param c Character to match to
	 * @param symbols Symbol array from which a character is taken
	 * @param i Index at which a character is taken
	 * @return	True if matched, false otherwise.
	 * @see AutomatonGenerator::isOperator
	 */
	private static boolean matches(char c, char[] symbols, int i) {
		return symbols[i] == c && isOperator(i, symbols);
	}
	
	/**
	 * Checks whether a character in an array is an operator. A character prefixed by an odd number of
	 * backslashes is an operator.
	 * @param j Index at which the character is located
	 * @param symbols Array from which to take the character
	 * @return True if it is an operator, false otherwise
	 */
	private static boolean isOperator(int j, char[] symbols) {
		int cnt = 0;
		for(int i = j - 1;; cnt++, i--) {
			if(i < 0 || symbols[i] != '\\') break;
		}

		return cnt % 2 == 0;
	}
}
