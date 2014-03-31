package net.floodlightcontroller.netmerging;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPhysicalPort.OFPortState;
import org.openflow.protocol.OFPortMod;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.ImmutablePort;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.topology.ITopologyService;



public class NetworkMerging implements IOFMessageListener, IFloodlightModule {
	
	protected IFloodlightProviderService floodlightProvider;
	protected ITopologyService topology;
	
	protected Set macAddresses;
	protected static Logger logger;
	protected ArrayList<NetworkUtility> borderswitch ;
	protected ArrayList<Island> islandlist;
	private boolean ispass;
	private  MyTimer timejob;
	
	
	public static final String MODULE_NAME = "networkmerging";
	
	@Override
	public String getName() {
	    return MODULE_NAME;
	}
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return "topology".equals(name);
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
	    Collection<Class<? extends IFloodlightService>> l =
	        new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IFloodlightProviderService.class);
	    return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
	    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(NetworkMerging.class);
	    logger.info("welcome to networkMerging \n");
	    ispass = false;
	    borderswitch = new ArrayList<NetworkUtility>();
	    islandlist = new ArrayList<Island>();
	    timejob = new MyTimer();
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
	    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    	Thread t1 = new Thread(timejob, "T1");        
        //t1.start();
	}

	@Override
	   public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		
   		//logger.info("Here comes the packet");
		//logger.info(msg.toString()+"\n");
		//logger.info(cntx.toString()+"\n");
		
	    switch(msg.getType()) {
        	case PACKET_IN:
        		 return this.handlePacketIn(sw, (OFPacketIn) msg,cntx);
        		
        	default:
        		break;
        		
	    }
	    
	    	return Command.CONTINUE;
	        

	      
	    }
	
	 protected Command handlePacketIn(IOFSwitch sw, OFPacketIn pi,FloodlightContext cntx) {
		 Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
		 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		 
	     byte mydata[] = new byte [500];
	     byte packetindata[] = new byte [500];
		 OFMatch match = new OFMatch();
		 match.loadFromPacket(pi.getPacketData(), pi.getInPort());
	 	
		 mydata = eth.serialize();
		 OFPortState mystate;	
		 packetindata = pi.getPacketData();
	 	//IPacket myindata = eth.deserialize(mydata, 0, 93);
		 

		 ImmutablePort mypacketInportfeature = sw.getPort(pi.getInPort());
		 OFPhysicalPort myport = mypacketInportfeature.toOFPhysicalPort();   
		 
		 
		 if(eth.getDestinationMAC() .toString().endsWith("FF:FF:00:00:00:00") && HexString.toHexString(mydata).length()==203){
				logger.info("this is BUPD drop it!!!");
				//NetworkUtility tmpborder = new NetworkUtility(sw, myport, mydata, pi);
				//checknewinswitch(tmpborder);
				/*
				   OFPortMod mymod = (OFPortMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.PORT_MOD);
				   mymod.setPortNumber(myport.getPortNumber());          
				   mymod.setConfig(OFPhysicalPort.OFPortConfig.OFPPC_PORT_DOWN.getValue());
				   mymod.setHardwareAddress(myport.getHardwareAddress());
				   mymod.setMask(0xFFFFFFFF);
				   try {
					   sw.write(mymod,cntx);
				   } catch (IOException e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				   }
				   sw.flush();
				   logger.info("port down set!!!!!");*/
				NetworkUtility nu =new NetworkUtility(sw,myport,mydata,pi);
				checknewinswitch(nu);
				return Command.STOP;
				
		 }
			
			return Command.CONTINUE;
     	
		 
	 }
	 
	 private void checknewinswitch(NetworkUtility nu){
		 Iterator<NetworkUtility> ir = borderswitch.iterator() ;
		 while(ir.hasNext()){
			 NetworkUtility tmp = ir.next();
			 if(tmp.isequal(nu)) return;
		 }
		 borderswitch.add(nu);
		 
	 }
		 	
	 protected class MyTimer  implements Runnable {
			

	        public MyTimer() {

	        }
	        		
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				logger.info("start running");
				getboarderswitch();
				lsitall();		
			}
				
			private void getboarderswitch(){
				Iterator<Island> itr = islandlist.iterator();
				Iterator<NetworkUtility> swtr = borderswitch.iterator();
				
				while(swtr.hasNext()){
					NetworkUtility nu = swtr.next();
					while(itr.hasNext()){
						Island tmp = itr.next();
						NetworkUtility tmpnu = tmp.island.get(0);
						if(topology.inSameL2Domain(tmpnu.inputSwitch.getId(), nu.inputSwitch.getId())){
							tmp.island.add(nu);
						}
					}
					
					
					Island newisland = new Island();
					newisland.island.add(nu);
				}
			}
			
			private void lsitall(){
				Iterator<Island> itr = islandlist.iterator();
				int number = 0;
				while(itr.hasNext()){
					Island tmp = itr.next();
					Iterator<NetworkUtility> isitr =  tmp.island.iterator();
					logger.info("this is number : " + number + "\n");
					while(isitr.hasNext()){
						logger.info(isitr.next().toString());
						
					}
				}
			}


		}// Mytimer

}

