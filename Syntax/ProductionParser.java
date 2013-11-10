import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Sven Vidak
 *
 */
public class ProductionParser {

        private static List<String> nonterminalChars = new ArrayList<String>();
        private static List<String> terminalChars = new ArrayList<String>();
        private static List<String> syncChars = new ArrayList<String>();
        private static Map<String, List<List<String>>> productions = new HashMap<String, List<List<String>>>();

        private static String currentNonterminal;
        
        public static Pair<List<TerminalCharacter>, List<NonTerminalCharacter>> parseMe() throws IOException {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                
                String line;
                Tag tag = Tag.NONTERMINAL;
                while((line = reader.readLine()) != null) {
                        if(line.isEmpty()) {
                                break;
                        }
                        switch(tag) {
                                case NONTERMINAL:
                                        if(line.startsWith("%V")) {
                                                parseChars(line, nonterminalChars);
                                        } else {
                                                System.err.println("Fatal error. Should never happen.");
                                                System.exit(-1);
                                        }
                                        tag = Tag.TERMINAL;
                                        break;
                                case TERMINAL:
                                        if(line.startsWith("%T")) {
                                                parseChars(line, terminalChars);
                                        } else {
                                                System.err.println("Fatal error. Should never happen.");
                                                System.exit(-1);
                                        }
                                        tag = Tag.SYNC;
                                        break;
                                case SYNC:
                                        if(line.startsWith("%Syn")) {
                                                parseChars(line, syncChars);
                                        } else {
                                                System.err.println("Fatal error. Should never happen.");
                                                System.exit(-1);
                                        }
                                        tag = Tag.PRODUCTIONS;
                                        break;
                                case PRODUCTIONS:
                                        parseProductions(line, productions);
                                        break;
                                default:
                                        System.err.println("Fatal error. Should never happen.");
                                        System.exit(-1);
                        }
                }
                
                reader.close();
                
                List<NonTerminalCharacter> realNonTerminal = getNonTerminal();
                List<TerminalCharacter> realTerminal = getTerminal();
                
                return new Pair<List<TerminalCharacter>, List<NonTerminalCharacter>>(realTerminal, realNonTerminal);
        }
        
        private static List<TerminalCharacter> getTerminal() {
        	List<TerminalCharacter> nt = new ArrayList<TerminalCharacter>();
        	for(String ch : terminalChars) {
        		nt.add(new TerminalCharacter(ch, syncChars.contains(ch)));
        	}
        	
        	return nt;
        }
        
        private static List<NonTerminalCharacter> getNonTerminal() {
        	List<NonTerminalCharacter> t = new ArrayList<NonTerminalCharacter>();
        	for(String ch : nonterminalChars) {
        		t.add(new NonTerminalCharacter(ch, productions.get(ch)));
        	}
        	
        	return t;
        }

        private static void parseChars(String line, List<String> chars) {
                String trimmedLine = line.trim();
                String rightLine = trimmedLine.substring
                                (trimmedLine.indexOf(" ") + 1);
                String[] splittedChars = rightLine.trim().split(" ");
                for(String s : splittedChars) {
                        chars.add(s);
                }
        }

        
        private static void parseProductions(String line,
                        Map<String, List<List<String>>> productions2) {
                if(!line.startsWith(" ")) {
                        currentNonterminal = line;
                        if(productions2.get(currentNonterminal) == null) {
                                productions2.put(currentNonterminal, new ArrayList<List<String>>());
                        }
                } else {
                        String trimmedLine = line.trim();
                        String[] splittedLine = trimmedLine.split(" ");
                        List<String> temp = new ArrayList<String>();
                        for(String s : splittedLine) {
                                temp.add(s);
                        }
                        productions2.get(currentNonterminal).add(temp);
                }
        }

        private enum Tag {
                NONTERMINAL, TERMINAL, SYNC, PRODUCTIONS
        }
}