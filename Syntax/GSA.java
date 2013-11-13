import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class GSA {
	
	public final static String DELIM = "!";
	public final static String BOTTOM = "#";
	public final static String INITIAL = "<%>";
	public static String INIT_NAME;
	public static String FINAL_NAME;
	
	private static Map<String, HashSet<String>> begins;
	public static List<TerminalCharacter> tcs;
	public static List<NonTerminalCharacter> ntcs;
	private static List<String> empty = new ArrayList<String>();
	private static Map<String, Set<String>> dictionary = new HashMap<String, Set<String>>();
	
	public static void main(String[] args) {
		try {
			Pair<List<TerminalCharacter>, List<NonTerminalCharacter>> parsedData = 
					ProductionParser.parseMe();
			tcs = parsedData.x;
			ntcs = parsedData.y;
			
		} catch (IOException e) {
			System.err.println("Failed miserably :( Should not happen!");
			System.exit(-20);
		}
		
		System.out.println("Zapocinjem generiranje.\nGeneriram ZAPOCINJE.");
//		begins = calculatebegins();
		begins();
		
		System.out.println("Generiram eNKA");
		EpsNDAutomaton enka = generateAutomaton();
		System.out.println("\teNKA ima " + enka.getStates().size() + " stanja");
		
		System.out.println("Generiram DKA");
		FDAutomaton dka = AutomatonSimplifier.toDeterministic(enka);
		System.out.println("\tDKA ima " + dka.getStates().size() + " stanja");
		
		System.out.println("Generiram tablice");
		Table action = TableConstructor.constructAction(dka, tcs, ntcs.get(0).symbol);
		Table newState = TableConstructor.constructNewState(dka, ntcs);
		String initial = "LOL";

		for(String name : action.getRowSet()) {
			if(name.contains(INIT_NAME)) {
				initial = name;
				break;
			}
		}
		
		if(!serialize(initial, action, newState, ntcs)) {
			System.err.println("Could not serialize parsed data");
			System.exit(666);
		}
	}
	
	private static boolean serialize(String initialState, Table action, Table newState, List<NonTerminalCharacter> ntcs) {
		FileOutputStream outFile = null;
        ObjectOutputStream outObject = null;
       
        try {
                outFile = new FileOutputStream("initialState.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(initialState);
                outFile.close();
                outObject.close();
               
                outFile = new FileOutputStream("terminal.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(tcs);
                outFile.close();
                outObject.close();
               
                outFile = new FileOutputStream("tables.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(new Pair<Table, Table>(action, newState));
                outFile.close();
                outObject.close();
               
        } catch (IOException e) {
                return false;
        }
        return true;
	}
	
	
	private static void calculateEmpty() {
		boolean hasNew = true;
		
		while(hasNew) {
			hasNew = false;
			for(NonTerminalCharacter ntc : ntcs) {
				if(empty.contains(ntc.symbol)) continue;
				
				for(List<String> rights : ntc.transitions) {
					boolean f = true;
					
					for(String c : rights) {
						if(c.trim().equals("$")) continue;
						if(!c.matches("<.+?>") || !empty.contains(c)) {
							f = false;
							break;
						}
					}
					
					if(f) {
						hasNew = true;
						empty.add(ntc.symbol);
						break;
					}
				}
			}
		}
		
		empty.add("$");
	}
	
	private static void directlyBegins() {
		begins = new HashMap<String, HashSet<String>>();
		for(NonTerminalCharacter ntc : ntcs) {
			for(List<String> right : ntc.transitions) {
				for(String c : right) {
					if(c.trim().equals("$")) continue;
					HashSet<String> beginsOfNtc = begins.get(ntc.symbol);
					if(beginsOfNtc == null) beginsOfNtc = new HashSet<String>();
					beginsOfNtc.add(c);
					begins.put(ntc.symbol, beginsOfNtc);
					if(!empty.contains(c)) break;
				}
			}
		}
	}
	
	private static void begins() {
		calculateEmpty();
		directlyBegins();
		boolean hasNew = true;
		HashSet<String> set;
		
		for(TerminalCharacter tc : tcs) {
			set = new HashSet<String>();
			set.add(tc.symbol);
			begins.put(tc.symbol, set);
		}
		
		while(hasNew) {
			hasNew = false;
			for(NonTerminalCharacter ntc : ntcs) {
				HashSet<String> beginsNtc = begins.get(ntc.symbol);
				beginsNtc.add(ntc.symbol);
				Set<String> temp = new HashSet<String>();
				
				for(String c : beginsNtc) {
					set = begins.get(c);
					if(set == null) continue;
					
					for(String begC : set) {
						if(!beginsNtc.contains(begC)) {
							temp.add(begC);
							hasNew = true;
						}
					}
				}
				
				if(temp != null) {
					beginsNtc.addAll(temp);
				}
				
				begins.put(ntc.symbol, beginsNtc);
			}
		}
		
		for(NonTerminalCharacter ntc : ntcs) {
			Set<String> toDelete = new HashSet<String>();
			HashSet<String> beginsNtc = begins.get(ntc.symbol);
			
			for(String c : beginsNtc) {
				if(c.matches("<.+?>")) {
					toDelete.add(c);
				}
			}
			
			beginsNtc.removeAll(toDelete);
		}
	}

	
	private static EpsNDAutomaton generateAutomaton() {
		EpsNDAutomaton automaton = new EpsNDAutomaton("Roko");
		Set<String> set = new HashSet<String>();
		Set<String> alphabet = new HashSet<String>();
		
		for(NonTerminalCharacter ntc : ntcs) alphabet.add(ntc.symbol);
		for(TerminalCharacter tc : tcs) alphabet.add(tc.symbol);
		automaton.setAlphabet(alphabet);
		
		
		NonTerminalCharacter fst = ntcs.get(0);
		List<List<String>> tmpa = new ArrayList<List<String>>();
		List<String> tmpb = new ArrayList<String>();
		tmpb.add(fst.symbol);
		tmpa.add(tmpb);
		NonTerminalCharacter init = new NonTerminalCharacter(INITIAL, tmpa);
		ntcs.add(init);
 		
		set.add("#");
		
		recursiveGenerate(automaton, init, set, null);
		
		INIT_NAME = INITIAL + "->" + DELIM + fst.symbol + ",{" + BOTTOM + "}";
		FINAL_NAME = INITIAL + "->" + fst.symbol + DELIM + ",{" + BOTTOM + "}";

		automaton.setInitial(INIT_NAME);
		Set<String> newStates = new HashSet<String>();
		
		for(String state : automaton.getStates()) {
			if(state.matches("^" + fst.symbol + "->\\" + DELIM + ".+$"))
				newStates.add(state);
		}
		
		return automaton;
	}

	private static String flatten(String from, List<String> to, Set<String> set) {
		StringBuilder sb = new StringBuilder();
		sb.append(from + "->");
		for(String t : to) sb.append(t);
		sb.append(",{");
		for(String s : set) sb.append(s + ",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}
	
	private static List<List<String>> calculateSteps(List<String> original) {
		List<List<String>> transitions = new ArrayList<List<String>>();
		if(original.size() == 1 && original.get(0).equals("$")) {
			List<String> tmp = new ArrayList<String>();
			tmp.add(DELIM);
			transitions.add(tmp);
			return transitions;
		}
		for(int i = 0; i <= original.size(); i++) {
			List<String> tmp = new ArrayList<String>();
			tmp.addAll(original);
			tmp.add(i, DELIM);
			transitions.add(tmp);
		}
		return transitions;
	}
	
	private static Set<String> recursiveGenerate(
			EpsNDAutomaton a, NonTerminalCharacter origin, Set<String> set, List<String> nextC) { //String nextC
		
		Set<String> initials = new TreeSet<String>();
		set = calculateSet(set, nextC);
			
		for(List<String> transition : origin.transitions) {
			List<List<String>> steps = calculateSteps(transition);
			initials.add(flatten(origin.symbol,steps.get(0), set));
			if(steps.size() == 1) addToAutomaton(a, flatten(origin.symbol, steps.get(0), set));
			for(int i = 0; i < steps.size(); i++) {
				
				List<String> step = steps.get(i);
				String afterDelim = null;
				List<String> nextChars = null; 
				//Delimiter index je na i !!!
				
				//Dodaj prijelaz u iduci
				String p = flatten(origin.symbol, steps.get(i), set);
				addToAutomaton(a, p);
				if(i < steps.size() - 1) {
					afterDelim = step.get(i + 1);
					String n = flatten(origin.symbol, steps.get(i+1), set);
					addToAutomaton(a, n);
					Set<String> tmp = new HashSet<String>();
					tmp.add(n);
					a.addTransition(new Pair<String, String>(p, afterDelim), tmp);
					if(i < step.size() - 2) nextChars = step.subList(i+2, step.size()); 
						//nextChar = step.get(i + 2);
					if(afterDelim != null && afterDelim.matches("<.+?>")) {
						NonTerminalCharacter next = forName(afterDelim);
						String key = origin.symbol + set + next + calculateSet(set, nextChars).toString();
						Set<String> options;
						if(dictionary.containsKey(key)) {
							options = dictionary.get(key);
						} else {
							dictionary.put(key, new HashSet<String>());
							options = recursiveGenerate(a, next, set, nextChars);
							dictionary.put(key, options);
						}

						for(String option : options) {
							Set<String> temp = new HashSet<String>();
							temp.add(option);
							
							a.addTransition(
									new Pair<String, String>(p, null), 
									temp
									); 
						}
					}
				}
			}
		}
		
		return initials;
	}
	
	private static Set<String> calculateSet(Set<String> previous, List<String> cs) { //Just one c
		Set<String> current = new HashSet<String>();
		boolean generatesEmpty = true;
		
		if(cs != null) {
			for(String c : cs) {
				if(!empty.contains(c)) generatesEmpty = false;
			}
		}
		
		if(cs == null || generatesEmpty) {
			current.addAll(previous);
		} 
		
		if(cs != null) {
			for(String c : cs) {
				Set<String> toAdd = begins.get(c);
				if(toAdd != null) {
					current.addAll(toAdd);
				}
				if(!empty.contains(c)) break;
			}
		}
		
		return current;
	}
	
	private static void addToAutomaton(EpsNDAutomaton a, String name) {
		a.addState(name);
		a.addAcceptable(name);
	}

	private static NonTerminalCharacter forName(String name) {
		for(int i = 0; i < ntcs.size(); i++) {
			if(ntcs.get(i).symbol.equals(name)) {
				return ntcs.get(i);
			}
		}
		return null;
	}
}
