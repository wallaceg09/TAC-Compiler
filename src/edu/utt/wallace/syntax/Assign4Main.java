package edu.utt.wallace.syntax;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class Assign4Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			System.out.println("Program1");
			SyntacticAnalyzer synt = new SyntacticAnalyzer("Program1.txt", new FileOutputStream("Program1.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			System.out.println("Program2");
			synt = new SyntacticAnalyzer("Program2.txt", new FileOutputStream("Program2.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			System.out.println("Program3");
			synt = new SyntacticAnalyzer("Program3.txt", new FileOutputStream("Program3.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			System.out.println("Program4");
			synt = new SyntacticAnalyzer("Program4.txt", new FileOutputStream("Program4.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			System.out.println("Program5");
			synt = new SyntacticAnalyzer("Program5.txt", new FileOutputStream("Program5.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			
			System.out.println("Program6");
			synt = new SyntacticAnalyzer("Program6.txt", new FileOutputStream("Program6.tac"));
			synt.analyze();
			System.out.println("------------------------------");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
