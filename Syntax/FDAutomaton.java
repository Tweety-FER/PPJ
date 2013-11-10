import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FDAutomaton {

	private String name;
	
	private String currentState;
	
	private Set<String> alphabet;
	
	private Set<String> states;
	
	private String initial;
	
	private Set<String> acceptables;
	
	private Map<Pair<String, String>, String> transitions;
	
	public FDAutomaton(String name) {
		this.name = name;
		this.states = new HashSet<String>();
		this.acceptables = new HashSet<String>();
		this.transitions = new HashMap<Pair<String,String>, String>();
	}
	
	public boolean parseToken(String token) {
		if(this.currentState == null || token == null) return false;
		
		this.currentState = this.transitions.get(new Pair<String, String>(this.currentState, token));
		return this.currentState == null;
	}
	
	public boolean isAcceptable() {
		return this.acceptables.contains(this.currentState);
	}
	
	public void reset() {
		this.currentState = initial;
	}

	public Set<String> getAlphabet() {
		return this.alphabet;
	}
	
	public void setAlphabet(Set<String> alphabet) {
		this.alphabet = alphabet;
	}
	
	public void addToAlphabet(String alpha) {
		this.alphabet.add(alpha);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the currentState
	 */
	public String getCurrentState() {
		return currentState;
	}

	/**
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	/**
	 * @return the states
	 */
	public Set<String> getStates() {
		return states;
	}

	/**
	 * @param states the states to set
	 */
	public void setStates(Set<String> states) {
		this.states = states;
	}
	
	public void addState(String state) {
		this.states.add(state);
	}

	/**
	 * @return the initial
	 */
	public String getInitial() {
		return initial;
	}

	/**
	 * @param initial the initial to set
	 */
	public void setInitial(String initial) {
		this.initial = initial;
	}

	/**
	 * @return the acceptables
	 */
	public Set<String> getAcceptables() {
		return acceptables;
	}

	/**
	 * @param acceptables the acceptables to set
	 */
	public void setAcceptables(Set<String> acceptables) {
		this.acceptables = acceptables;
	}
	
	public void addAcceptable(String acceptable) {
		this.acceptables.add(acceptable);
	}

	/**
	 * @return the transitions
	 */
	public Map<Pair<String, String>, String> getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions the transitions to set
	 */
	public void setTransitions(Map<Pair<String, String>, String> transitions) {
		this.transitions = transitions;
	}
	
	public void addTransition(Pair<String, String> key, String newState) {
		this.transitions.put(key, newState);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sigma: " + alphabet.toString() + "\n");
		sb.append("Q (" + states.size() +") : " + states.toString() + "\n");
		sb.append("F (" + acceptables.size() + "): " + acceptables.toString() + "\n");
		sb.append("q0 : " + initial + "\n");
		sb.append("d :\n");
		
		for(Pair<String, String> key : transitions.keySet()) {
			sb.append(key + " => " + transitions.get(key) + "\n");
		}
		
		return sb.toString();
	}
}
