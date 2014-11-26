package edu.utt.wallace.syntax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;


/**
 * @author Glen Wallace
 *
 *	Description:
 *		Performs lexical analysis on a given input file
 */
public class LexicalAnalyzer {
	
	/**
	 * Rudimentary debugging variable. Holds the line at which the lex
	 * analyzer is currently positioned.
	 */
	private int fileLineNumber = -1;
	
	/**
	 * Holds Finite State Machine transitions
	 */
	private int[][] stateTable;
	
	/**
	 * Holds the keyword -> token mapping.
	 * O(log(n)) access.
	 */
	private TreeMap<String, Token> keywordTable;
	
	/**
	 * Holds the source file to be analyzed.
	 */
	private BufferedReader file;
	
	/**
	 * String to determine whether a given character is alphabetical
	 */
	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/**
	 * String to determine whether a given character is a digit
	 */
	private String digits = "1234567890";
	
	/**
	 * String to determine whether a given character is an operator
	 */
	private String operators = "{}()=<>#~^+-*/;.";
	
	/**Constructor: LexicalAnalyzer(String)
	 * 
	 * @param fileName	Name of the source-file to be analyzed
	 * @throws FileNotFoundException 
	 */
	public LexicalAnalyzer(String fileName) throws FileNotFoundException{
		initStateTable();
		initKeywordTable();
		file = new BufferedReader(new FileReader(fileName));
	}
	
	/**Method: initStateTable(void)
	 * Description:
	 * 		Populates a 2d array of integers with a default state-transition
	 * 		table for the lexical analyzer's Finite State machine.
	 * @return	Finite State Machine transition table
	 */
	private void initStateTable(){
		//						L	D	{	}	(	)	=	<	>	op	p	sp
		int[][] table = {	{	1,	3,	5,	16,	7,	8,	9,	10,	13,	15,	17,	0	},	//0	Start
							{	1,	1,	2,	2,	2,	2,	2,	2,	2,	2,	2,	2	},	//1	In identifier
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//2 End identifier
							{	4,	3,	4,	4,	4,	4,	4,	4,	4,	4,	4,	4	},	//3 In constant
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//4 End constant
							{	5,	5,	5,	6,	5,	5,	5,	5,	5,	5,	5,	5	},	//5 In comment
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//6 End comment
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//7 Found (
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//8 Found )
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//9 Found =
							{	16,	16,	16,	16,	16,	16,	11, 16, 12, 16,	16,	16	},	//10 Found <
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//11 Found <=
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//12 Found <>
							{	16,	16,	16,	16,	16,	16,	14,	16,	16,	16,	16,	16	},	//13 Found >
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//14 Found >=
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//15 Found Operator
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//16 Found < or >
							{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0	},	//17 General punctuation
						};
		this.stateTable = table;
	}

	/**Method: initKeywordTable(void)
	 * 
	 * Description:
	 * 		Initializes the keyword table
	 */
	private void initKeywordTable(){
		this.keywordTable = new TreeMap<String, Token>();
		this.keywordTable.put("DIV", Token.INTDIV);
		this.keywordTable.put("MOD", Token.MOD);
		this.keywordTable.put("PROGRAM", Token.PROGRAM);
		this.keywordTable.put("END", Token.END);
		this.keywordTable.put("IF", Token.IF);
		this.keywordTable.put("WHILE", Token.WHILE);
		this.keywordTable.put("THEN", Token.THEN);
		this.keywordTable.put("DO", Token.DO);
	}

	/**Method: nextLine(void)
	 * 
	 * Description:
	 * 		Analyzes the next line of the source file, then outputs
	 * 		a Queue (implemented as a LinkedList) of the token->lexeme
	 * 		mappings.
	 * @return	Queue of the next line's token -> lexeme mappings, null
	 * 			if the end of file is reached
	 * @throws IOException 
	 */
	public LinkedList<KeyVal<Token, String>> nextLine() throws IOException{
		//Increment the line number
		++this.fileLineNumber;
		
		int state = 0;
		
		String line = file.readLine();
		
		if(line == null){//Check if EOF reached
			return null;
		}
		
		//Uppercase string for comparison. Add white noise to the end to
		//ensure single word lines get parsed
		String u_line = line.toUpperCase() + " ";
		
		//Output queue
		LinkedList<KeyVal<Token, String>> output = new LinkedList<KeyVal<Token, String>>();
		
		//Position of the beginning of the current lexeme
		int startChar = 0;
		//Position of the current character
		int curChar = 0;
		//Current column of the FSM
		int curColum = 0;
		
		//Current lexeme
		String lexeme = "";
		
		while(curChar < u_line.length()){
			
			curColum = getColumn(u_line.charAt(curChar));
			
			//Check to see if the inputted character is not recognized
			if(curColum == -1){
				System.out.printf("Unknown input at [Line: %d][Pos: %d]: %c", fileLineNumber, curChar, line.charAt(curChar));
			}else{//If the character is recognized then change the state
				state = this.stateTable[state][curColum];
			}
			
			switch(state){
			case 2:		//End identifier
				lexeme = line.substring(startChar, curChar);
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(kwSearch(lexeme.toUpperCase()), lexeme));
				
				//Backup the character pointer
				--curChar;
				
				break;
			case 4:		//End number
				lexeme = line.substring(startChar, curChar);
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.NUMBER, lexeme));
				
				//Backup the character pointer
				--curChar;
				
				break;
		
			case 6:		//End comment
				state = 0;
				break;
			case 7:		//Found (
				lexeme = String.valueOf(line.charAt(curChar));
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.OPARENTH, lexeme));
				
				break;
			case 8:		//Found )
				lexeme = String.valueOf(line.charAt(curChar));
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.CPARENTH, lexeme));
				
				break;
			case 9:		//Found =
				lexeme = String.valueOf(line.charAt(curChar));
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.EQUAL, lexeme));
				break;
			case 11:	//Found <=
				lexeme = line.substring(startChar, curChar);
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.LE, lexeme));
				break;
			case 12:	//Found <>
				lexeme = line.substring(startChar, curChar);
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.NE, lexeme));
				break;
			case 14:	//Found >=
				lexeme = line.substring(startChar, curChar);
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(Token.GE, lexeme));
				
				break;
		
			case 15:		//Found Operator
				lexeme = String.valueOf(line.charAt(curChar));
				
				//Determine which operator the character is
				int opCode = operators.indexOf(lexeme);
				
				Token token;
				
				switch (opCode){
				
				case 4://'=' : Assignment
					token = Token.EQUAL;
					break;
					
				case 7://'#' : Unary Plus
					token = Token.UPLUS;
					break;
					
				case 8://'~' : Unary Minus
					token = Token.UMINUS;
					break;
					
				case 9://'^' : Exponential
					token = Token.EXPON;
					break;
					
				case 10://'+' : Addition
					token = Token.ADD;
					break;
					
				case 11://'-' : Subtraction
					token = Token.SUB;
					break;
					
				case 12://'*' : Multiplication
					token = Token.MULT;
					break;
					
				case 13://'/' : Division
					token = Token.DIV;
					break;
					
					
				case 14://';' : Semicolon
					token = Token.SEMICOLON;
					break;
				case 15://'.' : period
					token = Token.PERIOD;
					break;
				
				default:
					token = Token.ERROR;
				}
				
				//Go back to the starting state
				state = 0;
				
				output.add(new KeyVal<Token, String>(token, lexeme));
				
				break;
			case 16:	//Found either < or >
				lexeme = String.valueOf(line.charAt(curChar-1));
				
				//Go back to the starting state
				state = 0;
				if(lexeme.equals("<")){
					output.add(new KeyVal<Token, String>(Token.LT, lexeme));					
				}else if(lexeme.equals(">")){
					output.add(new KeyVal<Token, String>(Token.GT, lexeme));
				}else {output.add(new KeyVal<Token, String>(Token.ERROR, lexeme));}
				
				--curChar;
				break;
			case 17:	//General punctuation.
				
				break;
			}//End switch
			
			//Increment the character pointer
			++curChar;
			
			if(state == 0){
				startChar = curChar;
			}

		}
		
		//Clear the lexeme
		lexeme = "";
		
		return output;
	}
	
	/**Method: getColumn(char)
	 * 
	 * Description
	 * 		Finds the column of the state table associated with the inputted character
	 * @param input		-	Inputted character
	 * @return	The column of the character. -1 if the character isn't found
	 */
	private int getColumn(char input){
		
		if(alphabet.indexOf(input) >= 0){			//Letter
			return 0;
		}else if(digits.indexOf(input) >= 0){		//Digit
			return 1;
		}else{
			int opCode = operators.indexOf(input);
			if(opCode >=0){							//Either { } ( ) = < > or an operator
				if(opCode < 8){
					return opCode + 2;					
				}else { return 9 ;}
			}else if(input == ' '){					//Space
				return 11;
			}else { return 10; } 					//General punctuation
		}
		
	}
	
	/**Method: kwSearch(String)
	 * 
	 * Description:
	 * 		Determines whether the inputted lexeme is a keyword or identifier.
	 * @param lexeme
	 * @return	Keyword's Token if found, Token.IDENT otherwise.
	 */
	private Token kwSearch(String lexeme){
		Token token = this.keywordTable.get(lexeme);
		if(token == null){
			token = Token.IDENT;
		}
		return token;
	}
}
