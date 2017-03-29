/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */

package se.tillvaxtverket.tsltrust.common.utils.core;

/**
 * Utility functions for determining the OS of the local host
 */
public class OSValidator{

	/*public static void main(String[] args)
	{
		if(isWindows()){
			System.out.println("This is Windows");
		}else if(isMac()){
			System.out.println("This is Mac");
		}else if(isUnix()){
			System.out.println("This is Unix or Linux");
		}else{
			System.out.println("Your OS is not support!!");
		}
	}//*/

	public static boolean isWindows(){

		String os = System.getProperty("os.name").toLowerCase();
		//windows
	    return (os.indexOf( "win" ) >= 0);

	}

	public static boolean isMac(){

		String os = System.getProperty("os.name").toLowerCase();
		//Mac
	    return (os.indexOf( "mac" ) >= 0);

	}

	public static boolean isUnix(){

		String os = System.getProperty("os.name").toLowerCase();
		//linux or unix
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);

	}
}