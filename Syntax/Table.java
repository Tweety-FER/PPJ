import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Table implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, Integer> rows;
	
	private Map<String, Integer> cols;
	
	private int width;
	
	private int height;
	
	private String[][] table;
	
	public Table(List<String> states, List<String> specialChars) {
		rows = new HashMap<String, Integer>();
		cols = new HashMap<String, Integer>();
		
		width = specialChars.size();
		height = states.size();
		table = new String[height][width];
		
		for(int i = 0; i < height; i++) {
			rows.put(states.get(i), new Integer(i));
		}
		
		for(int i = 0; i < width; i++) {
			cols.put(specialChars.get(i), new Integer(i));
		}
	}
	
	public void set(String state, String specialChar, String value) {
		Integer i = rows.get(state);
		Integer j = cols.get(specialChar);
		if(i != null && j != null) {
			table[i][j] = value;
		}
	}
	
	public String get(String state, String specialChar) {
		Integer i = rows.get(state);
		Integer j = cols.get(specialChar);
		if(i == null || j == null) return null;
		return table[i][j];
	}
	
	public Set<String> getRowSet() {
		return rows.keySet();
	}
	
	public Set<String> getColSet() {
		return cols.keySet();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String c : cols.keySet()) sb.append(c + "\t\t");
		sb.append("\n");
		for(String state : rows.keySet()) {
			for(String c : cols.keySet()) {
				sb.append(this.get(state, c) + "\t\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
