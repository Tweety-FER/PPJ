import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AutomatonSimplifier {
	
	public static FDAutomaton toDeterministic(EpsNDAutomaton eNKA) {
		Set<String> initial = epsSurround(eNKA.getTransitions(), eNKA.getInitial());
		FDAutomaton DKA = gotoDeterministic(epsToNondeterministic(eNKA), initial);
		DKA.reset();
		return DKA;
	}

	private static EpsNDAutomaton epsToNondeterministic(EpsNDAutomaton eNKA) {
		EpsNDAutomaton NKA = new EpsNDAutomaton("Franjo");
		NKA.setAlphabet(eNKA.getAlphabet());
		NKA.setAllStates(eNKA.getStates());
		NKA.setAcceptable(eNKA.getAcceptables());
		NKA.setInitial(eNKA.getInitial());
		
		eNKA.reset();
		if(eNKA.isAcceptable()) {
			NKA.addAcceptable(eNKA.getInitial());
		}
		
		Map<Pair<String, String>, Set<String>> eTransitions = eNKA.getTransitions();
		Map<Pair<String, String>, Set<String>> newTrans = new HashMap<Pair<String,String>, Set<String>>();
		
		for(String alpha : eNKA.getAlphabet()) {
			for(String state : NKA.getStates()) {
				calculateTransitions(eTransitions, newTrans, state, alpha);
			}
		}
		
		NKA.setAllTransitions(newTrans);
		return NKA;
	}
	
	private static FDAutomaton gotoDeterministic(EpsNDAutomaton NKA, Set<String> initial) {
		FDAutomaton DKA = new FDAutomaton("Josip");

		Set<String> alphabet = NKA.getAlphabet();
		Set<Set<String>> tempSet = new HashSet<Set<String>>();
		List<String> helper;
		DKA.setAlphabet(alphabet);
		Map<Pair<String,String>, Set<String>> transitions = NKA.getTransitions();
		Set<String> nkaAcceptable = NKA.getAcceptables();
		
		Set<String> initialSet = new HashSet<String>();
		initialSet.addAll(initial);
		DKA.addState(initialSet.toString());
		DKA.addAcceptable(initialSet.toString());
		DKA.setInitial(initialSet.toString());
		tempSet.add(initialSet);

		
		int oldlen = 0;
		int len = tempSet.size();
		boolean acceptable = false;
		while(len != oldlen) {
			Set<Set<String>> newSets = new HashSet<Set<String>>();
			for(Set<String> states : tempSet) {
				for(String a : alphabet) {
					Set<String> temp = new HashSet<String>();
					for(String s: states) {
						Pair<String, String> key = new Pair<String, String>(s, a);
						Set<String> got = transitions.get(key);
						if(got != null) {
							temp.addAll(got);
						}
						acceptable = false;
						for(String st : temp) {
							if(nkaAcceptable.contains(st)) {
								acceptable = true;
								break;
							}
						}
						
						helper = new ArrayList<String>(temp);
						Collections.sort(helper);
						String name = helper.toString();
						
						helper = new ArrayList<String>(states);
						Collections.sort(helper);
						String from = helper.toString();
						
						if(!temp.isEmpty()) {
							DKA.addState(name);
							if(acceptable) DKA.addAcceptable(name);
							DKA.addTransition(new Pair<String, String>(from, a), name);
						}
					}
					newSets.add(temp);
				}
			}
			
			tempSet.addAll(newSets);
			oldlen = len;
			len = tempSet.size();
		}
		
		return DKA;
	}
	
	private static void calculateTransitions(
			Map<Pair<String,String>, Set<String>> oldT,
			Map<Pair<String, String>, Set<String>> newT, 
			String state, String token) {
		Set<String> expanded = epsSurround(oldT, state);
		Set<String> to = new HashSet<String>();
		
		for(String e : expanded) {
			Set<String> tmp = oldT.get(new Pair<String, String>(e,token));
			if(tmp != null) to.addAll(tmp);
		}
		
		to = epsSurround(oldT, to);
		if(!to.isEmpty()) {
			newT.put(new Pair<String, String>(state, token), to);
		}
	}
	
	private static Set<String> epsSurround(
			Map<Pair<String,String>, Set<String>> transitions, String s){
		Set<String> set = new HashSet<String>();
		set.add(s);
		return epsSurround(transitions, set);
	}
	
	private static Set<String> epsSurround(
			Map<Pair<String,String>, Set<String>> transitions, Set<String> s) {
		Set<String> expanded = new HashSet<String>(s);
		int oldlen = 0, len = expanded.size();
		while(oldlen != len) {
			Set<String> temp = new HashSet<String>();
			for(String str : expanded) {
				Pair<String, String> key = new Pair<String, String>(str, null);
				Set<String> to = transitions.get(key);
				if(to != null && !to.isEmpty()) {
					temp.addAll(to);
				}
			}
			expanded.addAll(temp);
			
			oldlen = len;
			len = expanded.size();
		}
		return expanded;
	}
}
