package net.floodlightcontroller.networkmerging;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.util.HexString;

import com.kenai.jaffl.struct.Struct.Unsigned64;

import net.floodlightcontroller.core.IOFSwitch;

public class NetworkUtility implements Comparable<NetworkUtility> {
	public IOFSwitch inputSwitch;
	public OFPhysicalPort inputPort;
	public byte packetData[];
	public OFPacketIn packetin;
	public STP legctinf;
	public long  rootset;
	public long sdnid;
	public int setnum;
	public int bandwith;
	
	
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
	
	NetworkUtility(IOFSwitch sw ,OFPhysicalPort po,byte data[],OFPacketIn pi,long sdnid,int bandwith){
		this.inputSwitch = sw;
		this.inputPort = po;
		this.packetData = data;
		this.packetin = pi;
		this.sdnid = sdnid ;
		this.bandwith;
		legctinf = new STP(packetData);

	}
	
	NetworkUtility(IOFSwitch sw ,OFPhysicalPort po){
		this.inputSwitch = sw;
		this.inputPort = po;

	}
	
	public boolean  isequal( NetworkUtility second){
		if(this.inputSwitch.equals(second.inputSwitch)){
			if(this.inputPort.equals(second.inputPort)) return true;	
		}
		return false;
	}
	
	public boolean setbandwith(int bandwith){
		this.bandwith=bandwith;
	}
	
	public void setdata(IOFSwitch inswitch , OFPhysicalPort inport,OFPacketIn pi){
		packetData = pi.getPacketData();
		inputSwitch = inswitch;
		inputPort = inport;
		
	}
	
	public void setdata(IOFSwitch inswitch , OFPhysicalPort inport){
		inputSwitch = inswitch;
		inputPort = inport;
		
	}
	
	public String toString(){
		String mystring ;
		mystring  = "my switch id : " + inputSwitch.getId() + "\n my port id : "+inputPort.getPortNumber()+" sdn num : "+sdnid +" \n"+
					"root id : "+rootset+" cost : " +HexString.toHexString(legctinf.cost)+"\n"+
					"packet data "+ HexString.toHexString(packetData);
		return mystring;
		
		
	}
	
	public long getsdin(){
		return this.sdnid;
	}
	
	
	public long getrootset(){
		return this.rootset;
	}
	
	public byte[] getpacketdat(){
		return this.packetData;
	}
	
	public void setdata(byte [] data){
		this.packetData = data;
	
	}
	
	public void sdnid(long id){
		sdnid = id;
	
	}
	
	public void countdSTPinf(){
		legctinf = new STP();
	}

	public int compareTo(NetworkUtility anotherInstance) {
        return this.bandwith - anotherInstance.bandwith;
    }
	
}
