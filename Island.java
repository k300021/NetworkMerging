package net.floodlightcontroller.networkmerging;

import java.util.ArrayList;
import java.util.Iterator;



import net.floodlightcontroller.core.IOFSwitch;

public class Island {
	public ArrayList<NetworkUtility> island;
	
	Island(){
		island = new ArrayList<NetworkUtility> ();
		
	}
	
	public String listIsland(){
		Iterator<NetworkUtility> itr = this.island.iterator();
		
		String tmp = "";
		int number = 0;
		while(itr.hasNext()){
			number ++;
			NetworkUtility nu = itr.next();
			tmp +="number : " + number + "\n";
			tmp+= nu.toString();
		}
		
		return tmp;
		
	}
}
