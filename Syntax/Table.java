import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A model of a 2D table which holds strings.
 * The rows and columns are named (as strings) and can be accessed via their symbolic names.
 * @author Luka Skukan
 *
 */
public class Table implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Internal map of row name to row index
	 */
	private Map<String, Integer> rows;
	
	/**
	 * Internal map of column name to column index
	 */
	private Map<String, Integer> cols;
	
	/**
	 * Table width
	 */
	private final int width;
	
	/**
	 * Table height
	 */
	private final int height;
	
	/**
	 * The table's data matrix
	 */
	private String[][] table;
	
	/**
	 * Constructs a new table in which rows are indexed by names presented in list rows and
	 * columns are indexed by strings in the list cols.
	 * @param rows List of strings by which rows are indexed
	 * @param cols List of strings by which columns are indexed
	 */
	public Table(List<String> rows, List<String> cols) {
		this.rows = new HashMap<String, Integer>();
		this.cols = new HashMap<String, Integer>();
		
		width = cols.size();
		height = rows.size();
		table = new String[height][width];
		
		for(int i = 0; i < height; i++) {
			this.rows.put(rows.get(i), new Integer(i));
		}
		
		for(int i = 0; i < width; i++) {
			this.cols.put(cols.get(i), new Integer(i));
		}
	}
	
	/**
	 * Sets the value of the table at position indexed by (row, column) to given value.
	 * @param row Name of the row
	 * @param col Name of the column
	 * @param value Value to set
	 */
	public void set(String row, String col, String value) {
		Integer i = rows.get(row);
		Integer j = cols.get(col);
		if(i != null && j != null) {
			table[i][j] = value;
		}
	}
	
	/**
	 * Gets the value of the table at position indexed by (row, column).
	 * @param row Name of the row
	 * @param col Name of the column
	 * @return Value at position (row, col).
	 */
	public String get(String row, String col) {
		Integer i = rows.get(row);
		Integer j = cols.get(col);
		if(i == null || j == null) return null;
		return table[i][j];
	}
	
	/**
	 * Gets the list of row names.
	 * @return List of row names.
	 */
	public Set<String> getRowSet() {
		return rows.keySet();
	}
	
	/**
	 * Gets the list of column names.
	 * @return List of column names.
	 */
	public Set<String> getColSet() {
		return cols.keySet();
	}
	
	/**
	 * Gets the table's internal map of row_name => row_index pairs.
	 * @return Map of name -> index for rows
	 */
	public Map<String, Integer> getRows() {
		return rows;
	}
	
	/**
	 * Gets the table's internal map of col_name => col_index pairs.
	 * @return Map of name -> index for columns.
	 */
	public Map<String, Integer> getCols() {
		return cols;
	}
	
	/**
	 * Sets the table's internal map of col_name => col_index pairs.
	 * The new map must of of equal length as the old one to preserve mapping.
	 * @param cols Map of name -> index for columns
	 * @throws UnequalRowLengthException When current map length is unequal to the new one.
	 */
	public void setCols(Map<String, Integer> cols) throws UnequalRowLengthException {
		if(cols.size() != this.cols.size()) throw new UnequalRowLengthException();
		this.cols = cols;
	}
	
	/**
	 * Sets the table's internal map of row_name => row_index pairs.
	 * The new map must of of equal length as the old one to preserve mapping.
	 * @param cols Map of name -> index for rows
	 * @throws UnequalColumnLengthException When current map length is unequal to the new one.
	 */
	public void setRows(Map<String, Integer> rows) throws UnequalColumnLengthException {
		if(rows.size() != this.rows.size()) throw new UnequalColumnLengthException();
		this.rows = rows;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String c : cols.keySet()) sb.append(c + "\t\t\t");
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
