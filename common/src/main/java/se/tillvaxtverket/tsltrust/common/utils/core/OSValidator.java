/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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