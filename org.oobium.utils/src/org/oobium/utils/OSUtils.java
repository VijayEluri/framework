package org.oobium.utils;

public class OSUtils {
	 private static String OS = null;
	 
	   public static String getName() {
	      if(OS == null) { 
	    	  OS = System.getProperty("os.name");
	      }
	      return OS;
	   }
	   
	   public static boolean isWindows() {
	      return getName().startsWith("Windows");
	   }

	   public static boolean isMac(){
	      return getName().startsWith("Mac");
	   }
}
