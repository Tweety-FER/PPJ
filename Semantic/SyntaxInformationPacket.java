/**
 * Packet containing the contents of a node, hauled all the way from lexical analysis.
 * @author Luka Skukan
 *
 */
public class SyntaxInformationPacket {

	/**
	 * Either lexical type or syntactic state
	 */
	public final String type;
	
	/**
	 * Line in which it appeared in the source code, if lexical type, otherwise null
	 */
	public final String line;
	
	/**
	 * Contents grouped into lexical type, if it is one, otherwise null.
	 */
	public final String contents;
	
	/**
	 * Takes a line from the generative tree and makes a packet out of it.
	 * Is nice.
	 * @param contents Trimmed line for a generative tree
	 */
	public SyntaxInformationPacket(String contents) {
		String[] tmp = contents.split("\\s");
		this.type = tmp[0];
		if(tmp.length == 1) {
			this.line = null;
			this.contents = null;
			return;
		}
		this.line = tmp[1];
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 2; i < tmp.length; i++) {
			sb.append(tmp[i] + " ");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		
		this.contents = sb.toString();
	}
	
	@Override
	public String toString() {
		if(this.line != null && this.contents != null) {
			return this.type + "(" + this.line + "," + this.contents + ")";
		} else {
			return this.type;
		}
	}
	
}
