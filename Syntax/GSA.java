
import java.io.File;
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
	private static List<TerminalCharacter> tcs;
	private static List<NonTerminalCharacter> ntcs;
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
		begins = calculatebegins();
		
		System.out.println("Generiram eNKA");
		EpsNDAutomaton enka = generateAutomaton();
		System.out.println("\teNKA ima " + enka.getStates().size() + " stanja");
		
		System.out.println("Generiram DKA");
		FDAutomaton dka = AutomatonSimplifier.toDeterministic(enka);
		System.out.println("\tDKA ima " + dka.getStates().size() + " stanja");
		
		System.out.println("Generiram tablice");
		Table action = TableConstructor.constructAction(dka, tcs, ntcs.get(0).symbol);
		Table newState = TableConstructor.constructNewState(dka, ntcs);
		String initial = "0";

		for(String name : action.getRowSet()) {
			if(name.matches("^.*?" + INITIAL +".*$")) {
				initial = name;
				break;
			}
		}
		
		if(!serialize(initial, action, newState, ntcs)) {
			System.err.println("Could not serialize parsed data");
			System.exit(666);
		}
		
		System.out.println(dka);
		System.out.println(action);
		System.out.println(newState);
	}
	
	private static boolean serialize(String initialState, Table action, Table newState, List<NonTerminalCharacter> ntcs) {
		FileOutputStream outFile = null;
        ObjectOutputStream outObject = null;
        
        File f = new File("analizator");
        if(!f.exists()) {
        	if(!f.mkdir()) {
        		return false;
        	}
        }
       
        try {
                outFile = new FileOutputStream("analizator/initialState.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(initialState);
                outFile.close();
                outObject.close();
               
                outFile = new FileOutputStream("analizator/terminal.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(tcs);
                outFile.close();
                outObject.close();
               
                outFile = new FileOutputStream("analizator/tables.ser");
                outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(new Pair<Table, Table>(action, newState));
                outFile.close();
                outObject.close();
               
        } catch (IOException e) {
                return false;
        }
        return true;
	}
	
	private static Map<String, HashSet<String>> calculatebegins() {
		Map<String, HashSet<String>> begins = new HashMap<String, HashSet<String>>();
		
		//Calculates directlyBegins
		for(NonTerminalCharacter ntc : ntcs) {
			for(List<String> right : ntc.transitions) {
				if(right.isEmpty()) continue;
				if(right.get(0).equals("$")) continue; //Screw eps productions
				if(! begins.containsKey(ntc.symbol)) {
					HashSet<String> temp = new HashSet<String>();
					temp.add(right.get(0));
					begins.put(ntc.symbol, temp);
				} else {
					begins.get(ntc.symbol).add(right.get(0));
				}
			}
		}
		
		//Calculates begins
		for(NonTerminalCharacter ntc : ntcs) {
			Set<String> beginnings = begins.get(ntc.symbol);
			beginnings.add(ntc.symbol);
			int len = beginnings == null ? 0 : beginnings.size();
			int oldLen = 0;
			
			while(len != oldLen) {
				Set<String> temp = new HashSet<String>();
				for(String c : beginnings) {
					if(!c.matches("^<.+>$")) continue;
					Set<String> cBegins = begins.get(c);
					temp.addAll(cBegins);
				}
				
				beginnings.addAll(temp);
				begins.put(ntc.symbol, (HashSet<String>) beginnings);
				oldLen = len;
				len = beginnings.size();
			}
		}
		
		//Reflexive for terminal characters
		for(TerminalCharacter tc : tcs) {
			HashSet<String> temp = new HashSet<String>();
			temp.add(tc.symbol);
			begins.put(tc.symbol, temp);
		}
		
		return begins;
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
//		
//		automaton.addState(INITIAL);
//		automaton.addAcceptable(INITIAL);
		automaton.setInitial(INIT_NAME);
		Set<String> newStates = new HashSet<String>();
		
		for(String state : automaton.getStates()) {
			if(state.matches("^" + fst.symbol + "->\\" + DELIM + ".+$"))
				newStates.add(state);
		}
//		
//		automaton.addTransition(
//				new Pair<String, String>(INITIAL, null), 
//				newStates
//				);
		
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
			EpsNDAutomaton a, NonTerminalCharacter origin, Set<String> set, String nextC) {
		
		Set<String> initials = new TreeSet<String>();
		set = calculateSet(set, nextC);
			
		for(List<String> transition : origin.transitions) {
			List<List<String>> steps = calculateSteps(transition);
			initials.add(flatten(origin.symbol,steps.get(0), set));
			if(steps.size() == 1) addToAutomaton(a, flatten(origin.symbol, steps.get(0), set));
			for(int i = 0; i < steps.size(); i++) {
				
				List<String> step = steps.get(i);
				String afterDelim = null;
				String nextChar = null; 
				
				try{
					//Ako postoji iduci :
					int delim = steps.indexOf(DELIM);
					if(delim + 1 < steps.size())
						afterDelim = step.get(step.indexOf(DELIM) + 1);
					
					//Dodaj prijelaz iz ovog u iduci (radi)
					String p = flatten(origin.symbol, steps.get(i), set);
					addToAutomaton(a, p);
					
					try {
						String n = flatten(origin.symbol, steps.get(i + 1), set);
						addToAutomaton(a, n);
						Set<String> newStates = new HashSet<String>();
						newStates.add(n);
						a.addTransition(new Pair<String, String>(p, afterDelim), newStates);
						nextChar = step.get(step.indexOf(DELIM) + 2);
					} catch(IndexOutOfBoundsException ignorable2) {}
					
					//Za nonTerminal dodaj epsilon prijelaz u sve produkcije tog
					if(afterDelim != null && afterDelim.matches("^<\\w+>$")) {
						NonTerminalCharacter next = forName(afterDelim);
						String key = origin.symbol + next + calculateSet(set, nextChar).toString();
						Set<String> options;
						if(dictionary.containsKey(key)) {
							options = dictionary.get(key);
						} else {
							dictionary.put(key, new HashSet<String>());
							options = recursiveGenerate(a, next, set, nextChar);
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

				} catch(IndexOutOfBoundsException ignorable) {}
			}
		}
		
		return initials;
	}
	
	private static Set<String> calculateSet(Set<String> previous, String c) {
		Set<String> current = new HashSet<String>();
		if(c == null) {
			current.addAll(previous);
		} else {
			if(c.matches("^<\\w+>$")) {
				NonTerminalCharacter ntc = forName(c);
				boolean empty = false;
				for(List<String> transition : ntc.transitions) {
					if(transition.size() == 1 && transition.get(0).equals("$")) {
						empty = true;
						break;
					}
				}
				
				if(empty) {
					current.add(BOTTOM);
				}
			}
			
			Set<String> toAdd = begins.get(c);
			for(String add : toAdd) {
				if(forName(add) == null) current.add(add);
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
