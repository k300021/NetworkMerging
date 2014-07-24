package net.floodlightcontroller.networkmerging;

public class Land {
	long id;
	int size;
	boolean isSnd;
	
	Land(long id){
		this.id = id;
		size = 0;
		isSnd= false;
		
	}
	
	boolean isequal(Land other){
		return this.id==other.id ;
		
	}
	boolean isequal(long id){
		return this.id==id ;
		
	}
}
