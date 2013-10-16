package analizator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of a lexical state which is defined by a state name and sets of actions they
 * can perform for various regexes. The execution of those actions is not implemented as part of
 * this class. The lexical state can recognise when a match is found by parsing a string token
 * by token (tokens represented by characters) and can determine which actions should be performed
 * when such a match is found.
 * @author Luka Skukan
 *
 */
public class LexState implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3501420963191594359L;

	/**
	 * Current accepted parsing automaton state
	 */
	private String accepted;
	
	/**
	 * Map of actions for keys (usually first character of a parsing automaton state name)
	 */
	private Map<String, List<String>> actions;
	
	/**
	 * Private automaton for matching regular expressions.
	 */
	private Automaton automaton;
	
	/**
	 * Constructor, sets the regular expression parsing automaton and lists of actions
	 * for states of the parsing automaton.
	 * @param regexEngine
	 * @param actions
	 */
	public LexState(Automaton regexEngine, Map<String, List<String>> actions) {
		automaton = regexEngine;
		this.actions = actions;
		reset();
	}

	/**
	 * Lexical state name getter.
	 * @return Name
	 */
	public String getName() {
		return automaton.getName();
	}
	
	/**
	 * Parses a character token of a regular expression, determining whether it belongs to
	 * a regular expression belonging to the state and marking down the best of acceptable
	 * states reached after the transition, if any.
	 * @param token Character to parse
	 * @return Boolean indicating whether the parsed character belongs to a local regular expression,
	 * 			considering previously read characters.
	 */
	public boolean parseToken(char token) {
		if(automaton.parseToken(String.valueOf(token))) {
			if(automaton.isAcceptable()) {
				Set<String> potentialAcceptable = automaton.getCurrentStates();
				Set<String> acceptableStates = automaton.getAcceptables();
				accepted = null;
				for(String potential : potentialAcceptable) {
					if(acceptableStates.contains(potential)) {
						if(accepted == null || accepted.charAt(0) > potential.charAt(0)) {
							accepted = potential;
						}
					}
				}
			return true;
			}
		}
		
		return false;
	}

	/**
	 * Gets a list of actions for a key, if any. If the key is not recognised, an empty list is returned.
	 * An empty list can also be returned for a recognised key, if no actions are linked to it.
	 * @param key String key
	 * @return List of actions out of set [VRATI_SE n, PREDJI_U_STANJE stateName, NOVI_RED, LexicalTokenName]
	 */
	public List<String> getMatchedActions(String key) {
		if(!actions.containsKey(key)) {
			return new ArrayList<String>();
		} else {
			return actions.get(key);
		}
	}

	/**
	 * Resets the internal regex engine to an initial state.
	 */
	public void reset() {
		automaton.reset();
		accepted = null;
	}

	/**
	 * Checks whether the last read character matched any regular expressions stored within the state.
	 * @return Boolean
	 */
	public boolean isEmpty() {
		return this.automaton.getCurrentStates().isEmpty();
	}

	/**
	 * Checks whether any of the current states the parsing engine is within as acceptable.
	 * @return Boolean
	 */
	public boolean isAcceptable() {
		return accepted != null;
	}

	/**
	 * Gets the first of acceptable states the parsing engine is in, null if none.
	 * @return First acceptable state or null
	 */
	public String getAcceptedState() {
		return this.accepted;
	}

}
