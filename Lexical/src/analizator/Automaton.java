package analizator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A model of a nondeterministic finite automaton with epsilon transitions which
 * reaches its next step according to a string input. The model has only one initial
 * and acceptable state. It needs to be filled with states and transitions, as well as
 * having its initial and acceptable states initialised to function
 * <p><p>
 * The automaton uses textual representations of states.
 * @author Luka Skukan
 *
 */
public class Automaton implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5209065088979439865L;

	/**
	 * The name of the automaton
	 */
	private String name;
	
	/**
	 * A set of currently held states in the automaton.
	 */
	private Set<String> currentStates;
	
	/**
	 * A set of all states in the automaton.
	 */
	private Set<String> states;
	
	/**
	 * The initial automaton state
	 */
	private String initial;
	
	/**
	 * The actual automaton state.
	 */
	private Set<String> acceptables;
	
	/**
	 * A map of states transitioned to for a given pair of state and input token.
	 * @see Pair
	 */
	private Map<Pair<String, String>, Set<String>> transitions;
	
	/**
	 * Constructs a blank automaton with no states or transitions. <b>Must</b> be
	 * initialised to function.
	 */
	public Automaton(String name) {
		this.name = name;
		this.currentStates = new HashSet<String>();
		this.states = new HashSet<String>();
		this.transitions = new HashMap<Pair<String, String>, Set<String>>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<String> getStates() {
		return this.states;
	}
	
	public Set<String> getCurrentStates() {
		return this.currentStates;
	}
	
	public Map<Pair<String, String>, Set<String>> getTransitions() {
		return this.transitions;
	}
	
	/**
	 * Adds a state into to the automaton model.
	 * @param state Unique textual representation of a state.
	 */
	public void addState(String state) {
		this.states.add(state);
	}
	
	/**
	 * Adds a transition to the automaton model.
	 * @param condition Pair of state and input token to set transitions for.
	 * @param newStates Set of new states to transition to.
	 */
	public void addTransition(Pair<String, String> key, Set<String> newStates) {
		if(this.transitions.containsKey(key)) {
			Set<String> allStates = this.transitions.get(key);
			allStates.addAll(newStates);
			this.transitions.put(key, allStates);
			return;
		}
		
		this.transitions.put(key, newStates);
	}
	
	/**
	 * Sets the initial state, provided it is a previously defined state, and performs
	 * epsilon transitions to expand the starting state set.
	 * @param state Textual representation of a state.
	 */
	public void setInitial(String state) {
		if(this.states.contains(state)) {
			this.currentStates.add(state);
			this.initial = state;
			this.epsilonTransition();
			return;
		}
		
		System.err.println("Automaton configuration error - initial state not valid");
		System.exit(-5);
	}
	
	public String getInitial() {
		return this.initial;
	}
	
	public Set<String> getAcceptables() {
		return this.acceptables;
	}
	
	public void setAllStates(Set<String> states) {
		this.states = states;
	}
	
	public void setAllTransitions(Map<Pair<String,String>, Set<String>> transitions) {
		this.transitions = transitions;
	}
	
	/**
	 * Sets the acceptable state, provided it is a previously defined state.
	 * @param state Textual representation of a state
	 */
	public void setAcceptable(Set<String> states) {
		this.acceptables = states;
	}
	
	/**
	 * Checks whether the current state is an acceptable one.
	 * @return Whether the current state is acceptable.
	 */
	public boolean isAcceptable() {
		for(String state : this.currentStates) {
			if(this.acceptables.contains(state)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Parses a string token, transitioning into the next set of states.
	 * @param a String token to process
	 * @return Whether a non-empty set was reached.
	 */
	public boolean parseToken(String a) {
		Set<String> nextStates = new HashSet<String>();
		for(String state : this.currentStates) {
			Set<String> next = transition(state, a);
			for(String nextState : next) {
				nextStates.add(nextState);
			}
		}
		
		this.currentStates = nextStates;
		this.epsilonTransition();
		return ! this.currentStates.isEmpty();
	}
	
	/**
	 * Resets the automaton to the initial state it had after being filled and
	 * assigned initial and acceptable states.
	 */
	public void reset() {
		this.currentStates = new HashSet<String>();
		this.setInitial(this.initial);
	}
	
	/**
	 * Transitions for the given state and token.
	 * @param state Textual representation of a state.
	 * @param a String token
	 * @return Set of states transitioned into
	 */
	private Set<String> transition(String state, String a) {
		Pair<String, String> key = new Pair<String, String>(state, a);
		
		if(this.transitions.containsKey(key)) {
			return this.transitions.get(key);
		}
		
		return new HashSet<String>();
	}
	
	/**
	 * Performs epsilon transitions, expanding the set of current states, as long as
	 * it is viable.
	 */
	private void epsilonTransition() {
		int oldlen = 0;
		int len = this.currentStates.size();
		Set<String> additionalStates = new HashSet<String>();
		
		while(oldlen != len) {
			for(String state : this.currentStates) {
				additionalStates.addAll(this.transition(state, null));
			}
			
			this.currentStates.addAll(additionalStates);
			additionalStates.clear();
			
			oldlen = len;
			len = this.currentStates.size();
		}
	}
	
}
