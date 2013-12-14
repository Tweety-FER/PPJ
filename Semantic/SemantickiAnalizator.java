import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SemantickiAnalizator {

	private static SyntacticTreeNode root;
	private static Type functionReturnType;
	private static int insideLoop;
	private static SymbolTable table;
	
	public static void main(String[] args) {
		try {
			table = GlobalSymbolTableProvider.instance();
			root = TreeParser.read(System.in);
			insideLoop = 0;
			functionReturnType = Type.None;
			prijevodna_jedinica(root);
			
			Symbol main = table.getRoot().getSymbol("main");
			if(main == null || !main.isFunction()) {
				System.err.println("main");
				System.exit(101);
			}
			
			if(!table.allFunctionsDefined()) {
				System.err.println("funkcija");
				System.exit(102);
			}
			
		} catch (IOException e) {
			System.err.println("Cannot read System.in. Much confuse.");
			System.exit(20);
		}
		

	}
	
	private static void primarni_izraz(SyntacticTreeNode node) {
		SyntacticTreeNode child0 = node.getChildren().get(0);
		SyntaxInformationPacket data = child0.getInfoPacket();
		
		if(data.type.equals("IDN")) {
			Symbol idn = table.getSymbol(data.contents);
			if(idn == null) {
				perror(node);
			} 
			
			if(idn.isFunction()) {
				node.setType(Type.Function);
				node.setFunctionSignature(idn.signature.x, idn.signature.y);
			} else {
				node.setType(idn.type);
				node.setLExpression(true);
			}
		} else if(data.type.equals("BROJ")) {
			if(!isInt(data.contents)) {
				perror(node);
			} 
			node.setType(Type.Int);
			node.setLExpression(false);
			
		} else if(data.type.equals("ZNAK")) {
			if(!isChar(data.contents)) {
				perror(node);
			} 
			
			node.setType(Type.Char);
			node.setLExpression(false);
		} else if(data.type.equals("NIZ_ZNAKOVA")) {
			if(!isString(data.contents)) {
				perror(node);
			} 
			
			node.setType(Type.ConstArrayChar);
			node.setLExpression(false);
		} else if(data.type.equals("L_ZAGRADA")) {
			SyntacticTreeNode child1 = node.getChild(1);
			izraz(child1);
			node.inheritType(child1);
			node.setLExpression(child1.isLExpression());
		} 

	}
	
	private static void postfiks_izraz(SyntacticTreeNode node) {
		SyntacticTreeNode child0 = node.getChild(0);
		SyntaxInformationPacket data = child0.getInfoPacket();
		if(data.type.equals("<primarni_izraz>")) {
			primarni_izraz(child0);
			node.inheritType(child0);
			node.setLExpression(child0.isLExpression());
			
		} else if(data.type.equals("<postfiks_izraz>")) {
			postfiks_izraz(child0);
			SyntaxInformationPacket lBracket = node.getChild(1).getInfoPacket();
			
			if(lBracket.type.equals("L_UGL_ZAGRADA")) {
				if(!TypeCast.isArray(child0.getType(), true)) {
					perror(node);
				}
				
				izraz(node.getChild(2));
				if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
					perror(node);
				}
				
				node.setType(TypeCast.fromArray(node.getChild(2).getType()));
				node.setLExpression(!TypeCast.isConst(node.getType(), false));
			} else if(lBracket.type.equals("L_ZAGRADA") && node.getChildren().size() == 3) {
				if(!(child0.isFunction() && child0.getArgumentTypes().isEmpty())) {
					perror(node);
				}
				node.setType(child0.getReturnType());
			} else if(lBracket.type.equals("L_ZAGRADA") && node.getChildren().size() == 4) {
				lista_argumenata(node.getChild(2));
				List<Type> arguments = node.getChild(2).getTypes();
				
				if(!(child0.isFunction() && arguments.size() == child0.getArgumentTypes().size())) {
					perror(node);
				}
				
				boolean argumentsMatch = true;
				for(int i = 0; i < arguments.size(); i++) {
					argumentsMatch = argumentsMatch &&
							TypeCast.canCastFromTo(
									arguments.get(i), 
									child0.getArgumentTypes().get(i), 
									false
									);
				}
				
				if(!argumentsMatch) {
					perror(node);
				}
				
				node.setType(child0.getReturnType());
			
			} else if(lBracket.type.equals("OP_INC") || lBracket.type.equals("OP_DEC")) {
				SyntacticTreeNode child1 = node.getChild(1);
				postfiks_izraz(child1);
				if(!(child1.isLExpression() && TypeCast.canCastFromTo(child1.getType(), Type.Int, false))) {
					perror(node);
				}
			}
		}
	}
	
	private static void izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			izraz_pridruzivanja(node.getChild(0));
			node.setType(node.getChild(0).getType());
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			izraz(node.getChild(0));
			izraz_pridruzivanja(node.getChild(1));
			node.setType(node.getChild(1).getType());
		}
	}
	
	private static void izraz_pridruzivanja(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			log_ili_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			postfiks_izraz(node.getChild(0));
			if(!(node.getChild(0).isLExpression())) {
				perror(node);
			}
			
			izraz_pridruzivanja(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), node.getChild(0).getType(), false)) {
				perror(node);
			}
			
			node.setType(node.getChild(2).getType());
 		}
	}
	
	private static void lista_argumenata(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			izraz_pridruzivanja(node.getChild(0));
			node.addType(node.getChild(0).getType());
		} else {
			lista_argumenata(node.getChild(0));
			izraz_pridruzivanja(node.getChild(2));
			node.setTypes(node.getChild(0).getTypes());
			node.addType(node.getChild(2).getType());
		}
	}
	
	private static void unarni_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			postfiks_izraz(node.getChild(0));
			node.setType(node.getChild(0).getType());
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			if(node.getChild(0).getInfoPacket().type.equals("<unarni_operator>")) {
				cast_izraz(node.getChild(1));
				if(!(TypeCast.canCastFromTo(node.getChild(1).getType(), Type.Int, false))) {
					perror(node);
				}
			} else {
				unarni_izraz(node.getChild(1));
				if(!(node.getChild(1).isLExpression() && 
						TypeCast.canCastFromTo(node.getChild(1).getType(), Type.Int, false))) {
					perror(node);
				}
			}
			node.setType(Type.Int);
		}
	}
	
	private static void cast_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			unarni_izraz(node.getChild(0));
			node.setType(node.getChild(0).getType());
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			ime_tipa(node.getChild(1));
			cast_izraz(node.getChild(3));
			if(!(TypeCast.canCastFromTo(
					node.getChild(3).getType(), 
					node.getChild(1).getType(), 
					true
			))) {
				perror(node);
			}
			
			node.setType(node.getChild(1).getType());
		}
	}
	
	private static void ime_tipa(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			specifikator_tipa(node.getChild(0));
			node.setType(node.getChild(0).getType());
		} else {
			specifikator_tipa(node.getChild(1));
			if(node.getChild(1).getType().equals(Type.Void)) {
				perror(node);
			}
			
			node.setType(TypeCast.toConst(node.getChild(1).getType()));
		}
	}
	
	private static void specifikator_tipa(SyntacticTreeNode node) {
		SyntaxInformationPacket info = node.getChild(0).getInfoPacket();
		
		if(info.type.equals("KR_VOID")) {
			node.setType(Type.Void);
		} else if(info.type.equals("KR_CHAR")) {
			node.setType(Type.Char);
		} else {
			node.setType(Type.Int);
		}
	}
	
	private static void multiplikativni_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			cast_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			multiplikativni_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			cast_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void aditivni_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			multiplikativni_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			aditivni_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			multiplikativni_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void odnosni_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			aditivni_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			odnosni_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			aditivni_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void jednakosni_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			odnosni_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			jednakosni_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			odnosni_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void bin_i_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			jednakosni_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			bin_i_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			jednakosni_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void bin_xili_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			bin_i_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			bin_xili_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			bin_i_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void bin_ili_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			bin_xili_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			bin_ili_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			bin_xili_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void log_i_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			bin_ili_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			log_i_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			bin_ili_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void log_ili_izraz(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			log_i_izraz(node.getChild(0));
			node.inheritType(node.getChild(0));
			node.setLExpression(node.getChild(0).isLExpression());
		} else {
			log_ili_izraz(node.getChild(0));
			if(!TypeCast.canCastFromTo(node.getChild(0).getType(), Type.Int, false)) {
				perror(node);
			}
			
			log_i_izraz(node.getChild(2));
			if(!TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false)) {
				perror(node);
			}
			
			node.setType(Type.Int);
		}
	}
	
	private static void slozena_naredba(SyntacticTreeNode node) {
		if(node.getChildren().size() == 3) {
			table = table.makeChild();
			lista_naredbi(node.getChild(1));
			table = table.getParent();
		} else {
			table = table.makeChild();
			lista_deklaracija(node.getChild(1));
			lista_naredbi(node.getChild(2));
			table = table.getParent();
		}
	}
	
	private static void lista_naredbi(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			naredba(node.getChild(0));
		} else {
			lista_naredbi(node.getChild(0));
			naredba(node.getChild(1));
		}
	}

	
	private static void naredba(SyntacticTreeNode node) {
		SyntaxInformationPacket info = node.getChild(0).getInfoPacket();
		if(info.type.equals("<slozena_naredba>")) {
			slozena_naredba(node.getChild(0));
		} else if(info.type.equals("<izraz_naredba>")) {
			izraz_naredba(node.getChild(0));
		} else if(info.type.equals("<naredba_grananja>")) {
			naredba_grananja(node.getChild(0));
		} else if(info.type.equals("<naredba_petlje>")) {
			naredba_petlje(node.getChild(0));
		} else {
			naredba_skoka(node.getChild(0));
		}
	}
	
	private static void izraz_naredba(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			node.setType(Type.Int);
		} else {
			izraz(node.getChild(0));
			node.setType(node.getChild(0).getType());
		}
	}
	
	private static void naredba_grananja(SyntacticTreeNode node) {
		//TODO Implement
	}
	
	private static void naredba_petlje(SyntacticTreeNode node) {
		//TODO Implement
	}
	
	private static void naredba_skoka(SyntacticTreeNode node) {
		SyntaxInformationPacket info = node.getChild(0).getInfoPacket();
		if(node.getChildren().size() == 2) {
			if(info.type.equals("KR_RETURN")) {
				if(!functionReturnType.equals(Type.Void)) {
					perror(node);
				}
			} else {
				if(insideLoop == 0) {
					perror(node);
				}
			}
		} else {
			izraz(node.getChild(1));
			if(!TypeCast.canCastFromTo(node.getChild(1).getType(), functionReturnType, false)) {
				perror(node);
			}
		}
	}
	
	private static void prijevodna_jedinica(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			vanjska_deklaracija(node.getChild(0));
		} else {
			prijevodna_jedinica(node.getChild(0));
			vanjska_deklaracija(node.getChild(1));
		}
	}
	
	private static void vanjska_deklaracija(SyntacticTreeNode node) {
		SyntaxInformationPacket info = node.getChild(0).getInfoPacket();
		if(info.type.equals("<definicija_funkcije>")) {
			definicija_funkcije(node.getChild(0)); 
		} else {
			deklaracija(node.getChild(0));
		}
	}
	
	private static void lista_deklaracija(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			deklaracija(node.getChild(0));
		} else {
			lista_deklaracija(node.getChild(0));
			deklaracija(node.getChild(1));
		}
	}
	
	private static void deklaracija(SyntacticTreeNode node) {
		ime_tipa(node.getChild(0));
		lista_init_deklaratora(node.getChild(1), node.getChild(0).getType());
	}
	
	private static void lista_init_deklaratora(SyntacticTreeNode node, Type inheritedType) {
		if(node.getChildren().size() == 1) {
			init_deklarator(node.getChild(0), inheritedType);
		} else {
			lista_init_deklaratora(node.getChild(0), inheritedType);
			init_deklarator(node.getChild(2), inheritedType);
		}
	}
	
	private static void init_deklarator(SyntacticTreeNode node, Type inheritedType) {
		if(node.getChildren().size() == 1) {
			izravni_deklarator(node.getChild(0), inheritedType);
			if(TypeCast.isConst(node.getChild(0).getType(), true) || 
					TypeCast.isConst(node.getChild(0).getType(), false)) {
				perror(node);
			}
		} else {
			izravni_deklarator(node.getChild(0), inheritedType);
			inicijalizator(node.getChild(2));
			
			if(TypeCast.isX(node.getChild(0).getType())) {
				if(!(TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Char, false)
					|| TypeCast.canCastFromTo(node.getChild(2).getType(), Type.Int, false))) {
			
						perror(node);
					}
				
			} else if(TypeCast.isArrayX(node.getChild(0).getType())) {
				List<Type> inicijalizator = node.getChild(2).getTypes();
				List<Type> deklarator = node.getChild(0).getTypes();
	
				if(inicijalizator.size() > deklarator.size()) perror(node);

				boolean isFine = true;
				for(int i = 0; i < inicijalizator.size(); i++) {
					isFine = isFine && TypeCast.canCastFromTo(inicijalizator.get(i), deklarator.get(i), false);
				}
				
				if(!isFine) perror(node);
			} else {
				perror(node);
			}
		}
	}
	
	private static void izravni_deklarator(SyntacticTreeNode node, Type inheritedType) {
		if(node.getChildren().size() == 1) {
			if(inheritedType.equals(Type.Void)) {
				perror(node);
			}
			Symbol symbol = new Symbol(node.getChild(0).getInfoPacket().contents, inheritedType, null);
			if(!table.putSymbol(symbol)) {
				perror(node);
			}
			node.setType(inheritedType);
		} else if(node.getChild(1).getInfoPacket().type.equals("L_UGL_ZAGRADA")) {
			if(inheritedType.equals(Type.Void)) {
				perror(node);
			}
			
			int number = Integer.valueOf(node.getChild(2).getInfoPacket().contents);
			Symbol symbol = new Symbol(node.getChild(0).getInfoPacket().contents, inheritedType, null);
			
			if(number <= 0 || number > 1024 || !table.putSymbol(symbol)) {
				perror(node);
			}
			
			node.setType(TypeCast.toArray(inheritedType));
			for(int i = 0; i < number; i++) node.addType(inheritedType);
		} else if(node.getChild(2).getInfoPacket().type.equals("KR_VOID")) {
			String name = node.getChild(0).getInfoPacket().contents;
			
			if(!table.declareFunction(name, inheritedType, new ArrayList<Type>())) {
				perror(node);
			}
			node.setType(Type.Function);
			node.setFunctionSignature(inheritedType, new ArrayList<Type>());
		} else {
			lista_parametara(node.getChild(2));
			String name = node.getChild(0).getInfoPacket().contents;
			List<Type> types = node.getChild(2).getTypes();
			if(!table.defineFunction(name, inheritedType, types)) {
				perror(node);
			}
			
			node.setType(Type.Function);
			node.setFunctionSignature(inheritedType, types);
		}
	}
	
	private static void inicijalizator(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			izraz_pridruzivanja(node.getChild(0));
			int strlen = generatesString(node.getChild(0));
			if(strlen > 0) {
				for(int i = 0; i < strlen; i++) {
					node.addType(Type.Char);
				}
			} else {
				node.setType(node.getChild(0).getType());
			}
		} else {
			lista_izraza_pridruzivanja(node.getChild(1));
			node.setTypes(node.getChild(1).getTypes());
		}
	}
	
	private static void lista_izraza_pridruzivanja(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			izraz_pridruzivanja(node.getChild(0));
			node.addType(node.getChild(0).getType());
		} else {
			lista_izraza_pridruzivanja(node.getChild(0));
			izraz_pridruzivanja(node.getChild(2));
			node.setTypes(node.getChild(0).getTypes());
			node.addType(node.getChild(2).getType());
		}
	}
	
	private static void definicija_funkcije(SyntacticTreeNode node) {
		SyntaxInformationPacket params = node.getChild(3).getInfoPacket();
		ime_tipa(node.getChild(0));
		if(params.type.equals("KR_VOID")) {
			if(TypeCast.isConst(node.getChild(0).getType(), false) || 
				TypeCast.isConst(node.getChild(0).getType(), true) ||
				!table.defineFunction(node.getChild(1).getInfoPacket().contents, 
						node.getChild(0).getType(), new ArrayList<Type>())) 
			{
				perror(node);			
			}
			
			Type temp = functionReturnType;
			functionReturnType = node.getChild(0).getType();
			table = table.makeChild();
			slozena_naredba(node.getChild(5));
			table = table.getParent();
			functionReturnType = temp;
			
		} else {
			if(TypeCast.isConst(node.getChild(0).getType(), false) || 
					TypeCast.isConst(node.getChild(0).getType(), true) ||
					table.isDefinedFunction(node.getChild(1).getContents())) {
				perror(node);					
			}
			
			lista_parametara(node.getChild(3));
			
			if(!table.defineFunction(node.getChild(1).getContents(), node.getChild(0).getType(), 
					node.getChild(3).getArgumentTypes())) {
				perror(node);	
			}
			
			List<String> paramNames = node.getChild(3).getNames();
			List<Type> paramTypes = node.getChild(3).getTypes();
			
			Type temp = functionReturnType;
			functionReturnType = node.getChild(0).getType();
			table = table.makeChild();
			
			//Adding function parameters as variables within scope
			for(int i = 0; i < paramNames.size(); i++) {
				table.putSymbol(new Symbol(paramNames.get(i), paramTypes.get(i), null));
			}
			
			slozena_naredba(node.getChild(5));
			
			table = table.getParent();
			functionReturnType = temp;
		}
	}
	
	private static void lista_parametara(SyntacticTreeNode node) {
		if(node.getChildren().size() == 1) {
			deklaracija_parametra(node.getChild(0));
			node.addType(node.getChild(0).getType());
			node.addName(node.getChild(0).getName());
		} else {
			lista_parametara(node.getChild(0));
			deklaracija_parametra(node.getChild(1));
			if(node.getChild(0).getNames().contains(node.getChild(1).getName())) {
				perror(node);
			}
			
			node.setTypes(node.getChild(0).getTypes());
			node.addType(node.getChild(1).getType());
			node.setNames(node.getChild(0).getNames());
			node.addName(node.getChild(1).getName());
		}
	}
	
	private static void deklaracija_parametra(SyntacticTreeNode node) {
		ime_tipa(node.getChild(0));
		if(node.getChild(0).getType().equals(Type.Void)) {
			perror(node);
		}
		
		node.setName(node.getChild(1).getInfoPacket().contents);
		if(node.getChildren().size() == 2) {
			node.setType(node.getChild(0).getType());
		} else {
			node.setType(TypeCast.toArray(node.getChild(0).getType()));
		}
	}
	
	
	
	/* #################################################################
	 *  Helper functions start here!
	 *  Also, some dragons are probably here.
	 *  They were an hour ago, although they could have left.
	 *  The helper functions make a horrible lair.
	 * #################################################################
	 */
	private static void perror(SyntacticTreeNode node) {
		StringBuilder expression = new StringBuilder();
		
		for(SyntacticTreeNode child : node.getChildren()) {
			expression.append(child.getInfoPacket() + " ");
		}
		System.err.println(node.getInfoPacket() + " ::= " + expression.toString().trim());
		System.exit(100);
	}
	
	private static boolean isChar(String strRepr) {
		if(!strRepr.matches("^\'.{1,2}\'$")) return false;
		strRepr = strRepr.replaceAll("^\'(.{1,2})\'$", "$1");
		
		char min = 0;
		char max = 255;
		char val;
		
		if(strRepr.charAt(0) == '\\') {
			if(strRepr.length() == 2) {
				char c = strRepr.charAt(1);
			
				switch(c) {
					case 't': val = '\t';
					case 'n': val = '\n';
					case '0': val = '\0';
					case '\'': val = '\'';
					case '"': val = '"';
					case '\\': val = '\\';
					break;
					default: return false;
				}
			} else {
				return false;
			}
		} else {
			if(strRepr.equals("\"") || strRepr.equals("'")) return false;
			val = strRepr.charAt(0);
		}
		
		return (val >= min && val <= max);
	}
	
	private static boolean isInt(String strRepr) {
		BigInteger min = new BigInteger("-2147483648");
		BigInteger max = new BigInteger("2147483647");
		BigInteger integer = new BigInteger(strRepr);
		return (integer.compareTo(min) != -1 && integer.compareTo(max) != 1);
	}

	private static boolean isString(String strRepr) {
		if(!strRepr.matches("^\".+?\"$")) return false;
		strRepr = strRepr.replaceAll("^\"(.+?)\"$", "$1");
		char charRepr[] = strRepr.toCharArray();
		
		boolean isString = true;
		
		for(int i = 0; i < strRepr.length();) {
			if(charRepr[i] == '\\') {
				if(i + 1 == strRepr.length()) {
					return false;
				} else {
					isString = isString && isChar("'" + charRepr[i] + charRepr[i+1] + "'");
					i += 2;
				}
			} else {
				isString = isString && isChar("'" + charRepr[i++] + "'");
			}
		}
		return isString;
	}
	
	private static int generatesString(SyntacticTreeNode node) {
		SyntaxInformationPacket info = node.getInfoPacket();
		if(!info.type.matches("^<.*?>$")) {
			if(info.type.equals("NIZ_ZNAKOVA")) {
				return info.contents.trim().length() - 1;
			}
			
			return 0;
		} 
		
		return generatesString(node.getChild(0));
	}
}
