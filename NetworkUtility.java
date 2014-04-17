package net.floodlightcontroller.netmerging;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.util.HexString;

import com.kenai.jaffl.struct.Struct.Unsigned64;

import net.floodlightcontroller.core.IOFSwitch;

public class NetworkUtility {
	public IOFSwitch inputSwitch;
	public OFPhysicalPort inputPort;
	public byte packetData[];
	public OFPacketIn packetin;
	public STP legctinf;
	public long  rootset;
	public long sdnid;
	public int setnum;
	
	
	public class STP{
		byte [] rootid;
		byte [] cost;
		long tmp;
		STP(byte data[]){
			
			ByteBuffer bb ;
			
			cost = new byte[4];
			rootid = new byte[8];
		 	for(int i=0; i < 8 ; i++){
		 		rootid[i] = data[20+i];
		 		if(i < 4) cost[i] = data[28+i];
		 	}
		 	bb = ByteBuffer.wrap(rootid);
		 	rootset = bb.getLong();
		}
	}
	
	
	NetworkUtility(IOFSwitch sw ,OFPhysicalPort po,byte data[],OFPacketIn pi){
		this.inputSwitch = sw;
		this.inputPort = po;
		this.packetData = data;
		this.packetin = pi;
		legctinf = new STP(packetData);

	}
	
	
	NetworkUtility(IOFSwitch sw ,OFPhysicalPort po,byte data[],OFPacketIn pi,long sdnid){
		this.inputSwitch = sw;
		this.inputPort = po;
		this.packetData = data;
		this.packetin = pi;
		this.sdnid = sdnid ;
		legctinf = new STP(packetData);

	}
	
	public boolean  isequal( NetworkUtility second){
		if(this.inputSwitch.equals(second.inputSwitch)){
			if(this.inputPort.equals(second.inputPort)) return true;	
		}
		return false;
	}
	
	public void setdata(IOFSwitch inswitch , OFPhysicalPort inport,OFPacketIn pi){
		packetData = pi.getPacketData();
		inputSwitch = inswitch;
		inputPort = inport;
		
		
	}
	public String toString(){
		String mystring ;
		mystring  = "my switch id : " + inputSwitch.getId() + "\n my port id : "+inputPort.getPortNumber()+" set num : "+setnum +" \n"+
					"root id : "+rootset+" cost : " +HexString.toHexString(legctinf.cost)+"\n"+
					"packet data "+ HexString.toHexString(packetData);
		return mystring;
		
		
	}
	

	
}
