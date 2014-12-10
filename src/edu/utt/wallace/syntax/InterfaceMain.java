package edu.utt.wallace.syntax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class InterfaceMain {

	public static void main(String[] args) {
		String userInput = "";
		StringBuilder userCode = new StringBuilder();
		Scanner in = new Scanner(System.in);
		System.out.println("Write your program:");
		while(!((userInput = in.nextLine()).equals("exit")))
		{
			//TODO: Impliment "open" command to open a file and compile it
			userCode.append(userInput + "\n");
			if(userInput.charAt(userInput.length()-1) == '.')
			{
				System.out.print("Filename to save to: ");
				userInput = in.nextLine();
				System.out.println();
				String[] splitString = userInput.split("\\.");
				if(splitString.length < 2)
				{
					userInput += ".txt";
				}
				try {
					File file = new File(userInput);
					PrintWriter w = new PrintWriter(file);
					w.print(userCode.toString());//FIXME: PrintWriter.print does not accept "\n" as a valid whitespace character. Instead iterate through userCode and println() each element
					w.close();
					new SyntacticAnalyzer(userInput, System.out).analyze();//TODO: Allow the user to also save the code to file...
				} catch (FileNotFoundException e) {
					System.err.printf("Could not create file %s\n", userInput);
					e.printStackTrace();
				}
			}
		}
	}
}
