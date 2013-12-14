import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Does all kinds of checking and casting for the Type enum.
 * @author Sven Vidak
 *
 */
public class TypeCast {

	/**
	 * Map of possible casts from each type.
	 */
	private static Map<Type, List<Type>> casts;
	
	/**
	 * Checks whether type from can be cast to type to. Explicit is a flag denoting whether the
	 * cast is explicit or implicit.
	 * @param from Type we're casting from
	 * @param to Type we're casting to
	 * @param explicit Explicit if true, implicit otherwise
	 * @return Can it be done?!
	 */
	public static boolean canCastFromTo(Type from, Type to, boolean explicit) {
		return canCastFrom(from, explicit).contains(to);
	}
	
	/**
	 * Checks whether a type is an array. The canBeConstant flag allows or disallows it to be an array
	 * of constant values.
	 * @param t Type
	 * @param canBeConstant Can the array be constant?
	 * @return Is it an array
	 */
	public static boolean isArray(Type t, boolean canBeConstant) {
		if(!canBeConstant) {
			return t.equals(Type.ArrayChar) || t.equals(Type.ArrayInt);
		}
		
		return isArray(t, false) || t.equals(Type.ConstArrayChar) || t.equals(Type.ConstArrayInt); 
	}
	
	/**
	 * Converts an array(X) to X
	 * @param t Array(X)
	 * @return X or None if a non-array was given.
	 */
	public static Type fromArray(Type t) {
		if(t.equals(Type.ArrayChar)) return Type.Char;
		else if(t.equals(Type.ArrayInt)) return Type.Int;
		else if(t.equals(Type.ConstArrayChar)) return Type.ConstChar;
		else if(t.equals(Type.ConstArrayInt)) return Type.ConstInt;
		return Type.None;
 	}
	
	/**
	 * Converts from X to array(X)
	 * @param t X
	 * @return Array X or None if a non-X was given
	 */
	public static Type toArray(Type t) {
		if(t.equals(Type.Char)) return Type.ArrayChar;
		else if(t.equals(Type.Int)) return Type.ArrayInt;
		else if(t.equals(Type.ConstChar)) return Type.ConstArrayChar;
		else if(t.equals(Type.ConstInt)) return Type.ConstArrayInt;
		return Type.None;		
	}
	
	/**
	 * Converts a non-const value to a const-value
	 * @param t Non-const value X
	 * @return const X or None if it cannot be done
	 */
	public static Type toConst(Type t) {
		if(t.equals(Type.Char)) return Type.ConstChar;
		else if(t.equals(Type.Int)) return Type.ConstInt;
		else if(t.equals(Type.ArrayChar)) return Type.ConstArrayChar;
		else if(t.equals(Type.ArrayInt)) return Type.ConstArrayInt;
		return Type.None;
	}
	
	/**
	 * Checks whether a type is X. To be X it has to be Int, Char, Const Int or Const Char
	 * @param t Type
	 * @return Is it X? Can it join the X-Men?
	 */
	public static boolean isX(Type t) {
		return t.equals(Type.Int) || t.equals(Type.Char) || t.equals(Type.ConstChar) || t.equals(Type.ConstInt);
	}
	
	/**
	 * Checks whether a type is an array of X. See above for X.
	 * @param t Type
	 * @return Is it array X
	 */
	public static boolean isArrayX(Type t) {
		return t.equals(Type.ArrayInt) || t.equals(Type.ArrayChar) || 
				t.equals(Type.ConstArrayChar) || t.equals(Type.ConstArrayInt);
	}

	/**
	 * Checks whether a type is const.
	 * @param t Type
	 * @param isArray Is it an array or not
	 * @return It does the checking
	 */
	public static boolean isConst(Type t, boolean isArray) {
		if(isArray) {
			return t.equals(Type.ConstArrayChar) || t.equals(Type.ConstArrayInt);
		}
		
		return t.equals(Type.ConstChar) || t.equals(Type.ConstInt);
	}
	
	/**
	 * Lists all possible casts from a type, explicit or just implicit, depending on the flag.
	 * @param type Type
	 * @param explicit Does it include explicit casts?
	 * @return List of possible casts
	 */
	private static List<Type> canCastFrom(Type type, boolean explicit) {
		if (casts == null) {
			initCasts();
		}
		
		List<Type> cast = new ArrayList<Type>(casts.get(type));
		
		if ((type.equals(Type.Int) || type.equals(Type.ConstInt)) && explicit) {
			cast.add(Type.Char);
			cast.add(Type.ConstChar);
		}
		
		return cast;
	}
	
	private static void initCasts() {
		casts = new HashMap<Type, List<Type>>();
		
		// from int
		List<Type> cast = new ArrayList<Type>();
		cast.add(Type.ConstInt);
		cast.add(Type.Int);
		casts.put(Type.Int, cast);
		// from char
		cast = new ArrayList<Type>();
		cast.add(Type.Char);
		cast.add(Type.Int);
		cast.add(Type.ConstChar);
		cast.add(Type.ConstInt);
		casts.put(Type.Char, cast);
		// from int array;
		cast = new ArrayList<Type>();
		cast.add(Type.ArrayInt);
		cast.add(Type.ConstArrayInt);
		casts.put(Type.ArrayInt, cast);
		// from char array;
		cast = new ArrayList<Type>();
		cast.add(Type.ArrayChar);
		cast.add(Type.ConstArrayChar);
		casts.put(Type.ArrayChar, cast);

		// from const int
		cast = new ArrayList<Type>();
		cast.add(Type.Int);
		cast.add(Type.ConstInt);
		casts.put(Type.ConstInt, cast);
		// from const char
		cast = new ArrayList<Type>();
		cast.add(Type.Char);
		cast.add(Type.Int);
		cast.add(Type.ConstChar);
		cast.add(Type.ConstInt);
		casts.put(Type.ConstChar, cast);
		// from int array;
		cast = new ArrayList<Type>();
		cast.add(Type.ArrayInt);
		cast.add(Type.ConstArrayInt);
		casts.put(Type.ConstArrayInt, cast);
		// from char array;
		cast = new ArrayList<Type>();
		cast.add(Type.ArrayChar);
		cast.add(Type.ConstArrayChar);
		casts.put(Type.ConstArrayChar, cast);
		
		//void
		cast = new ArrayList<Type>();
		cast.add(Type.Void);
		casts.put(Type.Void, cast);
	}
}
