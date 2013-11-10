import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class TableConstructor {

	public static Table constructAction(FDAutomaton dka, List<TerminalCharacter> tcs, String initial) {
		System.out.println("\tStvaram tablicu Akcija");
		List<String> stateList = new ArrayList<String>(dka.getStates());
		List<String> terminals = new ArrayList<String>();
		Map<Pair<String,String>, String> trans = dka.getTransitions();
		for(TerminalCharacter tc : tcs) terminals.add(tc.symbol);
		terminals.add(GSA.BOTTOM);
		
		Table action = new Table(stateList, terminals);
		
		for(String t : stateList) {
			Set<String> set = setFromString(t);
			for(String s : set) {
				if(s.matches("^.*?!([^<,]).*$")) {
					String c = s.replaceAll("^.*?!(.).*$", "$1");
					if(action.get(t, c) == null) {
						Pair<String, String> key = new Pair<String,String>(set.toString(), c);
						String tSym = trans.get(key);
						if(tSym != null) {
							action.set(t, c, "Pomakni(" + tSym + ")");
						}
					} else if(s.matches("^.*?!\\,\\{(.+?)\\}.*$")) {
						String state = s.replaceAll("^(<.+?>)->.*$", "$1");
						String line = s.replaceAll("^<.+?>->(.*?)!.*$", "$1");
						if(line.isEmpty()) line = "$";
						Set<String> tSet = new HashSet<String>();
						
						String[] elems = s.replaceAll("^.*?!\\,\\{(.+?)\\}.*$", "$1").split(",");
						for(String e : elems) tSet.add(e);
						
						for(String ch : tSet) {
							if(action.get(t, ch) == null) {
								action.set(t, ch, "Reduciraj(" + state + "->" + line + ")");
							} 
							
							if(ch.equals(GSA.BOTTOM) && state.equals(initial)) {
								action.set(t, GSA.BOTTOM, "Prihvati()");
							}
						}
					}
				}
			}
		}
//		
//		for(String t : stateList) {
//			Set<String> set = setFromString(t);
//			for(String s : set) {
//				if(s.matches("^.*?!\\,\\{(.+?)\\}.*$")) {
//					String state = s.replaceAll("^(<.+?>)->.*$", "$1");
//					String line = s.replaceAll("^<.+?>->(.*?)!.*$", "$1");
//					if(line.isEmpty()) line = "$";
//					Set<String> tSet = new HashSet<String>();
//					
//					String[] elems = s.replaceAll("^.*?!\\,\\{(.+?)\\}.*$", "$1").split(",");
//					for(String e : elems) tSet.add(e);
//					
//					for(String c : tSet) {
//						if(action.get(t, c) == null) {
//							action.set(t, c, "Reduciraj(" + state + "->" + line + ")");
//						} 
//						
//						if(c.equals(GSA.BOTTOM) && state.equals(initial)) {
//							action.set(t, GSA.BOTTOM, "Prihvati()");
//						}
//					}
//				}
//			}
//		}
//	
//		System.out.println("\tUredujem tablicu Akcija");
//		finalize(action);
		return action;
	}
	
	public static Table constructNewState(FDAutomaton dka, List<NonTerminalCharacter> ntcs) {
		System.out.println("\tStvaram tablicu NovoStanje");
		
		List<String> stateList = new ArrayList<String>(dka.getStates());
		List<String> nonTerminals = new ArrayList<String>();
		Map<Pair<String,String>, String> trans = dka.getTransitions();
		for(NonTerminalCharacter ntc : ntcs) nonTerminals.add(ntc.symbol);
		
		Table newState = new Table(stateList, nonTerminals);
		
		for(String state : newState.getRowSet()) {
			String stateStr = setFromString(state).toString();
			for(String ntc : newState.getColSet()) {
				String t = trans.get(new Pair<String,String>(stateStr, ntc));
				if(t != null) {
					newState.set(state, ntc, "Stavi(" + t + ")");
				}
			}
		}
		
//		System.out.println("\tUredujem tablicu NovoStanje");
//		finalize(newState);
		return newState;
	}
	
//	private static void finalize(Table t) {
//		
//		Map<String, String> pairs = new HashMap<String, String>();
//		Map<String, Integer> newRows = new HashMap<String, Integer>(); 
//		
//		for(String key : t.getRowSet()) {
//			Integer v = t.getRows().get(key);
//			pairs.put(key, v.toString());
//			newRows.put(v.toString(), v);
//		}
//		
//		for(String r : t.getRowSet()) {
//			for(String c: t.getColSet()) {
//				String s = t.get(r, c);
//				if(s == null) continue;
//				for(String left : pairs.keySet()) {
//					String right = pairs.get(left);
//					s = s.replace(left, right);
//				}
//
//				t.set(r, c, s);
//			}
//		}
//		
//		try {
//			t.setRows(newRows);
//		} catch (UnequalColumnLengthException e) {
//			System.err.println("Internal error. Sorry :(");
//			System.exit(-11);
//		}
//		
//		for(String r : t.getRowSet()) {
//			for(String c: t.getColSet()) {
//				if(t.get(r, c) == null) t.set(r, c, "Odbaci()");
//			}
//		}
//	}
	
	private static Set<String> setFromString(String str) {
		if(str.length() < 3) return new TreeSet<String>();
		String[] elems = str.substring(1, str.length() - 1).split(",\\s+");
		Set<String> set = new HashSet<String>();
		
		for(String elem : elems) {
			set.add(elem);
		}

		return new TreeSet<String>(set);
	}
}
