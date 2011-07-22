package org.oobium.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileToStringBuilder {

	public static void main(String[] args){
		for(String fileName : args){
			readFile(fileName);
		}
	}
	
	public static void readFile(String fileName){
		 try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(fileName);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Create StringBuffer
			  System.out.println("StringBuffer sb = new StringBuffer();");
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				  strLine = strLine.replace("\"", "\\\"");
				  System.out.println ("sb.append(\""+strLine+"\\n\");");
			  }
			  //Close the input stream
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	}
}
