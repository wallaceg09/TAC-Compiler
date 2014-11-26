package edu.utt.wallace.syntax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;

public class SyntacticAnalyzer {
	
	private LexicalAnalyzer lex;
	
	/**
	 * Queue of Token-Lexeme pairs. 
	 */
	private LinkedList<KeyVal<Token, String>> tokenQueue;
	
	/**
	 * Output Stream object for character streams.
	 */
	private PrintStream out;
	
	/**Constructor
	 * @param fileName	Name of the input file to be analyzed.
	 * @param outStream	OutputStream to which the 3AC should be compiled to
	 * @throws FileNotFoundException	Could not open the file.
	 */
	public SyntacticAnalyzer(String fileName, OutputStream outStream) throws FileNotFoundException{
		lex = new LexicalAnalyzer(fileName);
		out = new PrintStream(outStream);
		tokenQueue = new LinkedList<KeyVal<Token, String>>();
		int a = 0;
	}
	
	/**Method: analyze(void)
	 * Description:
	 * 		Public interface to for the Analyzer's analysis functionality.
	 * 		Analyzes the program, then resets the Label numbering from the
	 * 		Memory class.
	 */
	public boolean analyze(){
		boolean result = Program();
		Memory.lClear();
		return result;
	}
	
	
	/**Method: Program(void)
	 * Description:
	 * 		Checks to see if the inputted program is a valid program.
	 * 		Follows the grammar rule:
	 * 			Program -> program Stmt_list end .
	 * @return	True if the program is syntactically correct, false otherwise.
	 */
	private boolean Program(){
		if(nextToken().key() == Token.PROGRAM){
			tokenQueue.pop();
			if(Stmt_list()){
				if(nextToken().key() == Token.END){
					tokenQueue.pop();
					if(nextToken().key() == Token.PERIOD){
						tokenQueue.pop();
						return true;
					}else { System.err.println("[ERROR] Expected Period"); }
				}else {System.err.println("[ERROR] Expected END");}
			}
		}
		return false;
	}
	
	/**Method: Stmt_list(void)
	 * Description:
	 * 		Determines whether the current queue of tokens
	 * 		contains a list of statements.
	 * 		Follows the Grammar rules:
	 * 			Stmt_list -> Stmt ; Stmt_list
	 * 			|	epsilon
	 * @return	true if Stmt_list false otherwise
	 */
	private boolean Stmt_list(){
		if(Stmt()){
			if(nextToken().key() == Token.SEMICOLON){
				tokenQueue.pop();
				return Stmt_list();
			}
		}
		return true;	//Epsilon transition
	}
	
	/**Method Stmt(void)
	 * Description:
	 * 		Determines whether the queue of tokens contains a statement.
	 * 		Follows the grammar rules:
	 * 			id[p] = Expr[q] {MOV}[q,p]		|
	 * 			if Expr[p] compare[c] Expr[q] {TST}[p, q, c, l] {JMP}[L] then {LBL}[L] Stmt {LBL}[L]	|
	 * 			while {LBL}[L] Expr[p] compare[c] Expr[q] {TST}[p, q, c, l] {JMP}[L] {LBL}[L]
	 * 			(L = lalloc)
	 * 
	 * @return	true if statement, false otherwise.
	 */
	private boolean Stmt(){
		Token nextTok = nextToken().key();
		
		//id[p] = Expr[q] {MOV}[q,p]
		if(nextTok == Token.IDENT){
			KeyVal<Token, String> stmtIdent = tokenQueue.pop();
			
			if(nextToken().key() == Token.EQUAL){
				tokenQueue.pop();
				String q = Expr();
				if(q != null){
					makeQuad("MOV", q, stmtIdent.value());
					Memory.clear();
					return true;
				}
			}
		}
		
		//if Expr[p] compare[c] Expr[q] {TST}[p, q, c, l] {JMP}[L] then {LBL}[L] Stmt {LBL}[L]
		else if(nextTok == Token.IF){
			tokenQueue.pop();
			String p = Expr();
			if(p != null){
				nextTok = nextToken().key();
				
				String c = compare();
				if(c != null){
					tokenQueue.pop();
					String q = Expr();
					if(q != null){
						String trueLbl = Memory.lalloc();
						String falseLbl = Memory.lalloc();
						TST(p, q, c, trueLbl);
						JMP(falseLbl);
						if(nextToken().key() == Token.THEN){
							tokenQueue.pop();
							LBL(trueLbl);
							if(Stmt()){
								LBL(falseLbl);
								Memory.clear();
								return true;
							}
						}else{ System.err.println("Error in If statement: \"Then\" expected...");}
					}
				}else{
					System.err.println("Error in If statement: comparison operator expected.");
				}
			}
			
		}
		
		//while {LBL}[L] Expr[p] compare[c] Expr[q] {TST}[p, q, c, l] {JMP}[L] {LBL}[L]
		else if(nextTok == Token.WHILE){
			tokenQueue.pop();
			String whileLBL = Memory.lalloc();
			LBL(whileLBL);
			
			String p = Expr();
			if(p != null){
				String c = compare();
				if(c != null){
					tokenQueue.pop();
					String q = Expr();
					if(q != null){
						String trueLBL = Memory.lalloc();
						String falseLbl = Memory.lalloc();
						
						TST(p, q, c, trueLBL);
						JMP(falseLbl);
						LBL(trueLBL);
						if(nextToken().key() == Token.DO){
							tokenQueue.pop();
							if(Stmt()){
								JMP(whileLBL);
								LBL(falseLbl);
								Memory.clear();
								return true;
							}
						}else{ System.err.println("Error in While loop: \"Do\" expected.");}
					}
				}else { System.err.println("Error in While loop: comparison operator expected."); }
			}
		}
		Memory.clear();
		return false;
	}
	
	/**Method: Expr(void)
	 * Description:
	 * 		Determines whether the queue of tokens contains
	 * 		an expression.
	 * 		Follows the grammar rules:
	 * 			Expr -> Term MoreTerms
	 * @return	String containing the next synthesized attribute if Expr() suceeds, null otherwise 
	 */
	private String Expr(){
		String q;
		if((q = Term()) != null){
			return moreTerms(q);
		}
		return null;
	}
	
	/**Method Term(void)
	 * Description:
	 * 		Determines whether the queue of tokens obeys the grammar rule for Term
	 * 		Grammar rule: 
	 * 			Term -> Factor MoreFactors
	 * @return	String containing the synthesized attribute if the grammar rule is satisfied, null otherwise
	 */
	private String Term(){
		String q = Factor();
		if (q != null){
			return MoreFactors(q);
		}
		else {return null;}
	}
	
	/**Method: moreTerms(String)
	 * Description:
	 * 		Determines whether the queue of tokens obeys the grammar rules for MoreTerms
	 * 		Grammar rules:
	 * 			MoreTerms ->	+ Term MoreTerms	|
	 * 							- Term MoreTerms	|
	 * 							epsilon
	 * @param p		Inherited value
	 * @return		String containing the next synthesized attribute if moreTerms succeeds, null otherwise
	 */
	private String moreTerms(String p){
		Token nextTok = nextToken().key();
		
		//+ Term MoreTerms	
		if(nextTok == Token.ADD){
			tokenQueue.pop();
			String t = Term();
			String r = Memory.talloc();
			makeQuad("ADD", p, t, r);
			return moreTerms(r);
		}
		
		//- Term MoreTerms	
		else if(nextTok == Token.SUB){
			tokenQueue.pop();
			String t = Term();
			String r = Memory.talloc();
			makeQuad("SUB", p, t, r);
			return moreTerms(r);
		}
		return p;
	}
	
	/**Method: MoreFactors(String)
	 * Description:
	 * 		Determines whether the queue of tokens follows the
	 * 		grammar rules for MoreFactors
	 * 		The Grammar Rules:
	 * 			MoreFactors -> 	* Factor MoreFactors	|
	 * 							/ Factor MoreFactors	|
	 * 							div Factor MoreFactors	|
	 * 							mod Factor MoreFactors	|
	 * 							epsilon
	 * @param p	Inherited attribute
	 * @return	String containing synthesized attribute, 
	 * 			null if the queue fails the grammar check
	 */
	private String MoreFactors(String p){
		Token nextTok = nextToken().key();
		
		// * Factor MoreFactors
		if(nextTok == Token.MULT){
			tokenQueue.pop();
			String t = Factor();
			if(t != null){
				String r = Memory.talloc();
				makeQuad("MUL", p, t, r);
				return MoreFactors(r);
			}else{
				System.err.println("Error in MoreFactors: " + tokenQueue.toString());
			}
		}
		// / Factor MoreFactors
		else if(nextTok == Token.DIV){
			tokenQueue.pop();
			String t = Factor();
			if(t != null){
				String r = Memory.talloc();
				makeQuad("DVD", p, t, r);
				return MoreFactors(r);
			}
		}
		// div Factor MoreFactors
		else if(nextTok == Token.INTDIV){
			tokenQueue.pop();
			String t = Factor();
			if(t != null){
				String r = Memory.talloc();
				makeQuad("DIV", p, t, r);
			}
		}
		// mod Factor MoreFactors
		else if(nextTok == Token.MOD){
			tokenQueue.pop();
			String t = Factor();
			if(t != null){
				String r = Memory.talloc();
				makeQuad("MOD", p, t, r);
			}
		}
		//epsilon
		return p;
	}
	
	/**Method: Factor(void)
	 * Description:
	 * 		
	 * @return
	 */
	private String Factor(){
		String p = Base();
		Token nextTok = nextToken().key();
		if(p != null){
			if(nextTok == Token.EXPON){
				tokenQueue.pop();
				String t = Factor();
				String r = Memory.talloc();
				makeQuad("EXP", p, t, r);
				
				return r;
			}else{
				return p;
			}
		}
		return null;
	}
	
	/**Method: Base(void)
	 * Description:
	 * 		Determines if the token queue contains a positive or negative number
	 * @return	String containing the synthetic attribute
	 */
	private String Base(){
		Token nextTok = nextToken().key();
		String p = null;
		//Check for unary plus
		if(nextTok == Token.UPLUS){
			tokenQueue.pop();
			p = Value();
			if(p != null){
				String r = Memory.talloc();
				makeQuad("PLS", p, r);
				return r;
			}
		}
		//Check for unary minus
		else if(nextTok == Token.UMINUS){
			tokenQueue.pop();
			p = Value();
			if(p != null){
				String r = Memory.talloc();
				makeQuad("NEG", p, r);
				return r;
			}
		}
		return Value();
		
	}
	
	/**Method: Value(void)
	 * Description:
	 * 		Determines if the token queue contains either
	 * 		an expression enclosed in parentheses, an identifier,
	 * 		or a constant number.
	 * @return	String containing the synthesized attribute. This attribute is the current lexeme.
	 */
	private String Value(){
		KeyVal<Token, String> nextTok = nextToken();
		String p = null;
		if(nextTok.key() == Token.OPARENTH){
			tokenQueue.pop();
			p = Expr();
			if(nextToken().key() == Token.CPARENTH){
				tokenQueue.pop();
				return p;
			}
		}else if(nextTok.key() == Token.IDENT || nextTok.key() == Token.NUMBER){
			tokenQueue.pop();
			return nextTok.value();
		}else { System.err.println("Error in Value(): Expected Open Parenthesis, Identifier, or Constant"); }
		return null;
	}
	
	/**Method: nextToken(void)
	 * Description:
	 * 		Returns the next line of tokens in the queue,
	 * 		if the queue is empty, it will request a new queue
	 * 		from the Lexical Analyzer. This is in order to possibly
	 * 		facilitate running the Lexical analyzer on a separate thread
	 * 		in future endeavors.
	 * @return	KeyVal pair of the Token + lexeme of the next token set in the queue.
	 */
	private KeyVal<Token, String> nextToken(){
		while(tokenQueue == null || tokenQueue.isEmpty()){
			try {
				tokenQueue = lex.nextLine();
			} catch (IOException e) {
				System.err.println("An IO Error occurred. The lexical analyzer failed to tokenize properly.");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return tokenQueue.peek();
	}
	
	/**Method: compare(void)
	 * Description:
	 * 		Determines if the next token is a comparison operator,
	 * 		if it is it returns the associated integer associated
	 * 		with the operator.
	 * @return	String of the operator code if the token is a comparison operator,
	 * 			null otherwise
	 */
	private String compare(){
		Token nextTok = nextToken().key();
		
		switch (nextTok){
		case EQUAL:
			return "0";
		case LT:
			return "1";
		case LE:
			return "2";
		case GT:
			return "3";
		case GE:
			return "4";
		case NE:
			return "5";
		}
		return null;
	}
	
	/**Method: TST(String, String, String, String)
	 * Description:
	 * 		Writes the 3AC Test instruction to the output stream.
	 * 
	 * @param lOperand
	 * @param rOperand
	 * @param comparator
	 * @param jmp
	 */
	private void TST(String lOperand, String rOperand, String comparator, String jmp){
		out.printf("%s, %s, %s, %s, %s\n", "TST", lOperand, rOperand, comparator, jmp);
	}
	
	/**Method: JMP(String)
	 * Description:
	 * 		Writes a 3AC jump instruction to the output stream.
	 * @param lbl
	 */
	private void JMP(String lbl){
		out.printf("JMP, %s\n", lbl);
	}
	
	/**Method: LBL(String)
	 * Description:
	 * 		Writes a 3AC label to the output stream.
	 * 
	 * @param lbl
	 */
	private void LBL(String lbl){
		out.println(lbl);
	}
	
	/**Method: makeQuad(String, String, String, String)
	 * Description:
	 * 		Creates a 3AC Quadruple for four inputs.
	 * 
	 * @param operation
	 * @param lOperand
	 * @param rOperand
	 * @param result
	 */
	private void makeQuad(String operation, String lOperand, String rOperand, String result){
		out.printf("%s, %s, %s, %s\n", operation, lOperand, rOperand, result);
	}
	
	/**Method: makeQuad(String, String, String)
	 * Description:
	 * 		Creates a 3AC Quadruple for three inputs.
	 * @param operation
	 * @param operand
	 * @param result
	 */
	private void makeQuad(String operation, String operand, String result){
		out.printf("%s, %s, %s\n", operation, operand, result);
	}
		
	/**
	 * @author Glen
	 *	Description:
	 *		Contains some static helper functions for 3AC memory management.
	 */
	private static class Memory{
		
		/**
		 * Integer of the next temp variable
		 */
		private static int nextTemp = -1;
		
		/**
		 *	Integer of the next lable variable 
		 */
		private static int nextLabel = -1;
		
		/**Method: talloc(void)
		 * Description:
		 * 		Performs memory allocation for a temporary variable.
		 * @return
		 */
		private static String talloc(){
			return String.format("T%d", ++nextTemp);
		}
		
		/**Method: clear(void)
		 * Description:
		 * 		Resets the nextTemp variable.
		 */
		private static void clear(){
			nextTemp = -1;
		}
		
		/**Method: lClear(void)
		 * Description:
		 * 		Resets the nextLabel variable.
		 * 
		 */
		private static void lClear(){
			nextLabel = -1;
		}
		
		/**Method: lalloc(void)
		 * Description:
		 * 		Performs memory allocation for a new label.
		 * @return
		 */
		private static String lalloc(){
			return String.format("L%d", ++nextLabel);
		}
	}
}
