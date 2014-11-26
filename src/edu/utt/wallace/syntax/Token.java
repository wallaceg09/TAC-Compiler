package edu.utt.wallace.syntax;

public enum Token {
	IDENT, 			//Identifier
	NUMBER, 		//Integer number
	OPARENTH, 		//Open parenthesis
	CPARENTH, 		//Close parenthesis
	UPLUS, 			//Unary plus
	UMINUS, 		//Unary minus
	ADD, 			//Add
	SUB, 			//Subtract
	MULT, 			//Multiply
	DIV, 			//Divide
	EQUAL,			//Equal
	INTDIV, 		//Integer division
	MOD, 			//Modulus
	EXPON, 			//Exponential
	SEMICOLON,		//Semi-colon
	PERIOD,			//Period token
	PROGRAM,		//Program token
	END,			//End token
	LT,				//Less than
	LE,				//Less than/equal to
	GT,				//Greater than
	GE,				//Greather than/equal to
	NE,				//Not equal to
	IF,
	THEN,
	WHILE,
	DO,
	ERROR			//Broad-spectrum lexical error detector
}
