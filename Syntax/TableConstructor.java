import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class TableConstructor {
	
	private static List<String> productions;

	public static Table constructAction(FDAutomaton dka, List<TerminalCharacter> tcs, String initial) {
		System.out.println("\tStvaram tablicu Akcija");
		
		constructProductionsPriority();
		
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
					String c = s.replaceAll("^.*?!(.+?)\\,.*$", "$1");
					
					c = expand(c, terminals).split("\\s+")[0];
					Pair<String, String> key = new Pair<String,String>(set.toString(), c);
					String tSym = trans.get(key);
					if(tSym != null) {
						action.set(t, c, "Pomakni(" + tSym + ")");
					}
				} else if(s.matches("^.*?!\\,\\{(.+?)\\}.*$")) {
					String state = s.replaceAll("^(<.+?>)->.*$", "$1");
					String line = s.replaceAll("^<.+?>->(.*?)!.*$", "$1");
					if(line.isEmpty()) {
						line = "$";
					} else {
						line = expand(line, terminals);
					}
					
					
					Set<String> tSet = new HashSet<String>();
					
					String[] elems = s.replaceAll("^.*?!\\,\\{(.+?)\\}.*$", "$1").split(",");
					for(String e : elems) tSet.add(e);
					
					for(String ch : tSet) {
						String reduceStr = "Reduciraj(" + state + "->" + line + ")";
						if(!state.equals(GSA.INITIAL) && (action.get(t, ch) == null
								|| hasPriority(reduceStr, action.get(t, ch)))) {
							action.set(t, ch, reduceStr);
						} 

						if(ch.equals(GSA.BOTTOM) && s.equals(GSA.FINAL_NAME)) {
							action.set(t, GSA.BOTTOM, "Prihvati()");
						}
					}
				}
			}
		}
		
		System.out.println("\tUredujem tablicu Akcija");
		finalize(action);
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
		
		System.out.println("\tUredujem tablicu NovoStanje");
		finalize(newState);
		return newState;
	}
	
	private static void constructProductionsPriority() {
		List<String> productions = new ArrayList<String>();
		for(NonTerminalCharacter ntc : GSA.ntcs) {
			for(List<String> tr : ntc.transitions) {
				StringBuilder sb = new StringBuilder();
				for(String t : tr) sb.append(t);
				String production = "Reduciraj(" + ntc.symbol + "->" + sb.toString() + ")";
				productions.add(production);
			}
		}
		TableConstructor.productions = productions;
	}
	
	private static boolean hasPriority(String a, String b) {
		if(!b.startsWith("Reduciraj")) return false;
		String s1 = a.replaceAll("\\s+", "");
		String s2 = b.replaceAll("\\s+", "");
		return(productions.indexOf(s1) > productions.indexOf(s2));
	}
	
	private static void finalize(Table t) {	
		for(String r : t.getRowSet()) {
			for(String c: t.getColSet()) {
				if(t.get(r, c) == null) t.set(r, c, "Odbaci()");
			}
		}
	}
	
	private static String expand(String s, List<String> tcs) {
		Set<String> replacements = new HashSet<String>();
		for(String tc : tcs) {
			if(s.contains(tc)) replacements.add(tc);
		}
		
		Set<String> delete = new HashSet<String>();
		
		for(String a : replacements) {
			for(String b : replacements) {
				if(a.contains(b) && a.length() > b.length()) {
					delete.add(b);
				}
			}
		}
		
		replacements.removeAll(delete);
		
		for(String r : replacements) {
			s = s.replace(r, r + " ");
		}
		
		s = s.replace(">", "> ");
		return s.trim();
	}
	
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
