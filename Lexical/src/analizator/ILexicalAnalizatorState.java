package analizator;

import java.util.List;

public interface ILexicalAnalizatorState {
	
	public String getName();
	
	public String getAcceptedState();
	
	public boolean isEmpty();
	
	public boolean isAcceptable();

	public boolean parseToken(char token);
	
	public boolean hasMatch();
	
	public List<String> getMatchedActions(String key);
	
	public void reset();
}
