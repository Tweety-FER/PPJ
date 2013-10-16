package analizator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LexState implements ILexicalAnalizatorState, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3501420963191594359L;

	private String accepted;
	
	private Map<String, List<String>> actions;
	
	private Automaton automaton;
	
	public LexState(Automaton regexEngine, Map<String, List<String>> actions) {
		automaton = regexEngine;
		this.actions = actions;
		reset();
	}

	@Override
	public String getName() {
		return automaton.getName();
	}
	
	@Override
	public boolean parseToken(char token) {
//		System.out.println("***");
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
				
				
//				System.out.println(getName() + " char " + token + " accepts " + accepted + " -> " 
//						+ getMatchedActions(String.valueOf(accepted.charAt(0))));
//				System.out.println("Acceptable are " + acceptableStates);
//				System.out.println("Current are " + potentialAcceptable);
			} else {
//				System.out.println(getName() + " char " + token + " unacceptable;");
			}
//			System.out.println("***");
			return true;
		}
//		System.out.println(getName() + " char " + token + " refused");
//		System.out.println("***");
		
		return false;
	}

	@Override
	public boolean hasMatch() {
		return accepted != null;
	}

	@Override
	public List<String> getMatchedActions(String key) {
		if(!actions.containsKey(key)) {
			return new ArrayList<String>();
		} else {
			return actions.get(key);
		}
	}

	@Override
	public void reset() {
		automaton.reset();
		accepted = null;
	}

	@Override
	public boolean isEmpty() {
		return this.automaton.getCurrentStates().isEmpty();
	}

	@Override
	public boolean isAcceptable() {
		return accepted != null;
		//return automaton.isAcceptable();
	}

	@Override
	public String getAcceptedState() {
		return this.accepted;
	}

}
