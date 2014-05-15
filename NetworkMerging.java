package net.floodlightcontroller.networkmerging;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Stack;
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
	protected ArrayList<NetworkUtility> blockSwitch ;
	protected ArrayList<NetworkUtility> remainSwitch ;
	protected ArrayList<Island> islandlist;
	private boolean ispass;
	private  NetworkmergingManager timejob;
	private Thread t1;
	private long receivenum = 0;
	
	
	
	private final int BODRERNUMBER = 31;
	
	public static final String MODULE_NAME = "networkmerging";
	private static final String Hello_message= "welcome to networkMerging \n";
	
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
		//return false;
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
	    l.add(ITopologyService.class);
	    return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
	    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(NetworkMerging.class);
	    topology = context.getServiceImpl(ITopologyService.class);
	    ispass = false;
	    borderswitch = new ArrayList<NetworkUtility>();
		blockSwitch = new ArrayList<NetworkUtility>();
		remainSwitch = new ArrayList<NetworkUtility>();
	    islandlist = new ArrayList<Island>();
	    timejob = new NetworkmergingManager();
	    t1 = new Thread(timejob, "T1");    
	    
	    logger.info(Hello_message);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
	    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    	    
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
		 NetworkUtility nu ;
	 	
		 mydata = eth.serialize();
		 OFPortState mystate;	
		 packetindata = pi.getPacketData();
	 	//IPacket myindata = eth.deserialize(mydata, 0, 93);
		 

		 ImmutablePort mypacketInportfeature = sw.getPort(pi.getInPort());
		 OFPhysicalPort myport = mypacketInportfeature.toOFPhysicalPort();   
		 
		 
		 
		 
		 
		 if(eth.getDestinationMAC() .toString().endsWith("FF:FF:00:00:00:00") && HexString.toHexString(mydata).length()==203){
				//logger.info("this is BUPD drop it!!!");
				//NetworkUtility tmpborder = new NetworkUtility(sw, myport, mydata, pi);
				//logger.info(tmpborder.toString());
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
				
			 
				
				if( receivenum >= 6*BODRERNUMBER && receivenum <= 8*BODRERNUMBER){
					long sdnid = topology.getL2DomainId(sw.getId());
					nu = new NetworkUtility(sw,myport,packetindata,pi,sdnid);
					checknewinswitch(nu);
				}
				
				receivenum ++;
				return Command.STOP;
				
		 }else{
			 
			 if( receivenum > 7*BODRERNUMBER  ){
				 
				 if(!ispass){
					 ispass = true;
					 t1.run();
				 
					 
				 }
				 
			 }
			
			 
			 
			 Iterator<NetworkUtility> itr = blockSwitch.iterator();
			 nu = new NetworkUtility(sw,myport);
			 
			 while(itr.hasNext()){
				 NetworkUtility tmp = itr.next();
				 if(tmp.isequal(nu)) return Command.STOP;
				 
			 }	
			 
			 
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
		 //logger.info(nu.toString());
		 remainSwitch.add(nu);
		 
	 }
		 	
	 protected class NetworkmergingManager  implements Runnable {
			

	        public NetworkmergingManager() {

	        }
	        		
	        
	        /*
	         * this is run
	         * 
	         * 
	         * 
	         *
	        */
			
			@Override
			public void run() {
								
				logger.info("start running");

				getboarderswitch();
				lsitall();
				multiPathElimate();

				countremain();
				logger.info(listList(remainSwitch));
				loopElimate();
				//loopElimate();
				//loopElimate();
			}
				
			
			
			private void getboarderswitch(){				
				Iterator<NetworkUtility> swtr = borderswitch.iterator();
								
				while(swtr.hasNext()){
					Iterator<Island> itr = islandlist.iterator();
					NetworkUtility nu = swtr.next();
					boolean islast = false;
					
					while(itr.hasNext()){
						
						Island tmp = itr.next();
						NetworkUtility tmpnu = tmp.island.get(0);
						if(topology.inSameL2Domain(tmpnu.inputSwitch.getId(), nu.inputSwitch.getId()) && nu.rootset == tmpnu.rootset ){
							tmp.island.add(nu);
							islast = true;
							
						}
					}
					
					if(!islast){
						Island newisland = new Island();
						newisland.island.add(nu);
						islandlist.add(newisland);
					}

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
					number++;
				}
			}
			
			private void multiPathElimate(){
				
				Iterator<Island> itr = islandlist.iterator();

				while(itr.hasNext()){
					Island tmp = itr.next();
					Iterator<NetworkUtility> isitr =  tmp.island.iterator();
					
					if(isitr.hasNext()){
						isitr.next();
					}else continue;

					while(isitr.hasNext()){
						NetworkUtility nu = isitr.next();
						portdownAt(nu);
						
					}
				}
				
			}
			/*
			private void loopElimate(){
				Iterator<NetworkUtility> reitr = remainSwitch.iterator();
				
				while(reitr.hasNext()){
					NetworkUtility head =reitr.next();
					
					
					logger.info("haha");
					ArrayList<NetworkUtility> tmplist = (ArrayList<NetworkUtility>) remainSwitch.clone();
					tmplist.remove(head);
					
					ArrayList<NetworkUtility> tmp = delooparound(head, head, tmplist, false);
					
					Iterator<NetworkUtility> tmpit = tmp.iterator();
					
					while(tmpit.hasNext()){
						NetworkUtility tmpnu = tmpit.next();
						
						portdownAt(tmpnu);
						remainSwitch.remove(tmpnu);
						reitr = remainSwitch.iterator();
						logger.info(tmp.toString());
						logger.info("haha");
						
						
					}


					
				}
			}*/
			
			private void loopElimate(){
				
				ArrayList<NetworkUtility> looplist = (ArrayList<NetworkUtility>) remainSwitch.clone();
				Iterator<NetworkUtility> reitr = looplist.iterator();
				
				while(reitr.hasNext()){
					NetworkUtility head =reitr.next();
					
					if(remainSwitch.indexOf(head) != -1){
						logger.info("haha in");
						ArrayList<NetworkUtility> tmplist = (ArrayList<NetworkUtility>) remainSwitch.clone();
						tmplist.remove(head);
						
						looparound(head, head, tmplist, false);
						
					}

					
					

					
				}
			}
			/*
			private ArrayList<NetworkUtility> delooparound(NetworkUtility head,NetworkUtility next,ArrayList<NetworkUtility> list,boolean islegacy){
				
				Iterator<NetworkUtility> itr = list.iterator(); 
				ArrayList<NetworkUtility> tmp = new ArrayList<NetworkUtility>();
				
				while(itr.hasNext()){
					NetworkUtility nu = itr.next();
					
					if(islegacy){
						if(nu.sdnid == next.sdnid){
							list.remove(nu);
							NetworkUtility tmpnu = looparound(head,nu,list,false);
							if( tmpnu != null ) tmp.add(tmpnu);
							
							itr = list.iterator(); 
					
						}
					}else{
						if(nu.rootset == next.rootset)
							
							if(nu.sdnid == head.sdnid){
								
								tmp.add(nu);
								return tmp;
							}else{
								list.remove(nu);
								looparound(head,nu,list,true);
								NetworkUtility tmpnu = looparound(head,nu,list,false);
								if( tmpnu != null ) tmp.add(tmpnu);
								
								
							}
							
					}
					
				}

				
				return tmp;
				
			}
			private NetworkUtility looparound(NetworkUtility head,NetworkUtility next,ArrayList<NetworkUtility> list,boolean islegacy){
				
				Iterator<NetworkUtility> itr = list.iterator(); 
				while(itr.hasNext()){
					NetworkUtility nu = itr.next();
					
					if(islegacy){
						if(nu.sdnid == next.sdnid){
							list.remove(nu);
							return looparound(head,nu,list,false);			
						}
					}else{
						if(nu.rootset == next.rootset)
							
							if(nu.sdnid == head.sdnid){
								return nu;
							}else{
								list.remove(nu);
								return looparound(head,nu,list,true);
							}
							
					}
					
				}

				
				return null;
				
			}
			
			 
			 */
			private void looparound(NetworkUtility head,NetworkUtility next,ArrayList<NetworkUtility> list,boolean islegacy){
				
				if(islegacy){
						Iterator<NetworkUtility> itr = list.iterator();
						ArrayList <NetworkUtility> tmplist = new ArrayList <NetworkUtility> () ;
						while(itr.hasNext()){
							NetworkUtility nu = itr.next();
							
							if(nu.sdnid == next.sdnid){
								tmplist.add(nu);
							}
						}
						
						Iterator <NetworkUtility> tmpitr = tmplist.iterator();
						while(tmpitr.hasNext()){
							
							NetworkUtility nu = tmpitr.next();
							ArrayList<NetworkUtility> newlist = (ArrayList<NetworkUtility>) list.clone();
							newlist.remove(nu);
							looparound(head,nu,newlist,false);
						}
						
						return;
						
						
				}else{
						Iterator<NetworkUtility> itr = list.iterator(); 
						ArrayList <NetworkUtility> tmplist = new ArrayList <NetworkUtility> () ;
						
						while(itr.hasNext()){

							NetworkUtility nu = itr.next();
							
							if(nu.rootset == next.rootset)
								
								if(nu.sdnid == head.sdnid){
									portdownAt(nu);
									remainSwitch.remove(nu);
									logger.info(nu.toString());
									logger.info("haha get");
									
									 return;
								}else{
									tmplist.add(nu);	
									
								}
							
						}
						Iterator <NetworkUtility> tmpitr = tmplist.iterator();
						while(tmpitr.hasNext()){
							NetworkUtility nu = tmpitr.next();
							ArrayList<NetworkUtility> newlist = (ArrayList<NetworkUtility>) list.clone();
							newlist.remove(nu);
							looparound(head,nu,newlist,true);
						}
						return;
						
				}
					
				

				
				
				
			}
			
			private void portdownAt(NetworkUtility nu){
				
				   FloodlightContext cntx = new FloodlightContext();
				   OFPortMod mymod = (OFPortMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.PORT_MOD);
				   mymod.setPortNumber(nu.inputPort.getPortNumber());          
				   mymod.setConfig(OFPhysicalPort.OFPortConfig.OFPPC_PORT_DOWN.getValue());
				   mymod.setHardwareAddress(nu.inputPort.getHardwareAddress());
				   mymod.setMask(0xFFFFFFFF);
				   try {
					   nu.inputSwitch.write(mymod,cntx);
				   } catch (IOException e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				   }
				   nu.inputSwitch.flush();
				   blockSwitch.add(nu);
			}
			
			private void listIsland(){
				
			}
			private String listList(ArrayList<NetworkUtility> list){
				Iterator <NetworkUtility> itr = list.iterator();
				String tmp = "";
				int number = 0;
				
				while(itr.hasNext()){
					number ++;
					NetworkUtility nu = itr.next();
					tmp +="number : " + number + "\n";
					tmp+= nu.toString();
					tmp+="\n";
				}
				
				return tmp;
				
			}

			
			private void countremain(){
				remainSwitch.removeAll(blockSwitch);
			}
			



		}// NetworkmergingManager

}

