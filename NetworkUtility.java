package net.floodlightcontroller.netmerging;

import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;

import net.floodlightcontroller.core.IOFSwitch;

public class NetworkUtility {
	IOFSwitch inputSwitch;
	OFPhysicalPort inputPort;
	byte packetData[];
	
	public boolean  isequal(NetworkUtility first , NetworkUtility second){
		if(first.inputSwitch.equals(second.inputSwitch)){
			if(first.inputPort.equals(second.inputPort)) return true;	
		}
		return false;
	}
	
	public void setdata(IOFSwitch inswitch , OFPhysicalPort inport,OFPacketIn pi){
		packetData = pi.getPacketData();
		inputSwitch = inswitch;
		inputPort = inport;
		
		
	}
	
}
