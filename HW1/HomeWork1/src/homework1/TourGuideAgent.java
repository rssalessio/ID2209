/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import java.util.*;
import jade.core.AID;
import jade.lang.acl.*;
import jade.proto.states.MsgReceiver;
import java.io.StringReader;

/**
 *
 * @author dell
 */
public class TourGuideAgent extends Agent {
    private AID[] curatorAgents ;
    ACLMessage msg;
    LinkedList<CuratorInfo> museumDetails = new LinkedList();
	
    protected void setup() 
    {
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Virtual-Tour" ); //Representing service type
        sd.setName("Virtual-Tour"); 
        register( sd );
        
        addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick()
                {
                    //update list of curators
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("artifacts-list");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result= DFService.search(myAgent,template);
                        curatorAgents = new AID[result.length];
                        for (int x = 0; x < result.length; x++) {
                            curatorAgents[x] = result[x].getName();
                            System.out.println("[TOUR GUIDE AGENT]Added curator: " +  result[x].getName());
                        }
                    } catch(FIPAException fe) { fe.printStackTrace();}
                }
            }
        );
        
        
     
        addBehaviour(new MsgRec());
    }
    
    protected class MsgRec extends MsgReceiver 
    {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                System.out.println("[TOUR GUIDE AGENT] Received request from profiler");
                ACLMessage reply = msg.createReply();
                String[] interests = msg.getContent().split("-");
                
                ACLMessage msgr = newMsg( ACLMessage.QUERY_REF ); 
                SequentialBehaviour seq = new SequentialBehaviour();
                addBehaviour( seq );
                System.out.println("[TOUR GUIDE AGENT] Building tour - phase 1");
                ParallelBehaviour par = new ParallelBehaviour( ParallelBehaviour.WHEN_ALL );
                seq.addSubBehaviour( par );
                for (int i=0; i < curatorAgents.length; i++)
                {
                    System.out.println("[TOUR GUIDE AGENT] Sending query to curator - phase 2");
                    msgr.addReceiver( curatorAgents[i]);
                    msgr.setContent("MUSEUM INFO");
                    send(msgr);
                    par.addSubBehaviour( new OneShotBehaviour() 
                       {
                          public void action() 
                          {   
                              //  MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                                System.out.println("[TOUR GUIDE AGENT]Received response from curator - phase 3");
                                ACLMessage msgt = blockingReceive();
                                CuratorInfo ci = new CuratorInfo();
                                
                                String[] lines = msgt.getContent().split("\\r?\\n");
                                String[] str = lines[0].split("-");
                                ci.museumname = str[1];
                                ci.curator = str[0];
                                ci.artifacts= Artifact.toArtifacts(Arrays.copyOfRange(lines, 1, lines.length));
                                museumDetails.add(ci);
                                System.out.println("[TOUR GUIDE AGENT] Built museum details - phase 4");
                          }
                       });
                }
                
               
                seq.addSubBehaviour( new OneShotBehaviour()
                {
                    public void action() 
                    {
                        System.out.println("[TOUR GUIDE AGENT] Preparing tour - phase 5");
                        CuratorInfo cinfo =  museumDetails.getFirst();
                        Tour tour = new Tour();
                        tour.setCurator(cinfo.curator);
                        tour.museum = cinfo.museumname;
                        System.out.println("[TOUR GUIDE AGENT] Checking user interests - phase 6");
                        for (Artifact i: cinfo.artifacts)
                        {
                            boolean k = false;
                            for (String s: interests)
                            {
                                if (s.equals(i.genreDetails))
                                {
                                    k = true;
                                    break;
                                }
                            }
                            if (k==true){
                                 System.out.println("[TOUR GUIDE AGENT] - Added element to tour - phase 7");
                                 tour.artifacts.add(i.name);
                            }
                        }

                         reply.setContent(tour.ConverttoString());
                         send(reply);
                       System.out.println("[TOUR GUIDE AGENT]Tour sent! - phase 8");
                    }
                });
        
               //send ( msg );
                
            }
            else
                block();
        }
    }
    
    protected static int cidCnt = 0;
    String cidBase ;
   
    String genID() 
    { 
       if (cidBase==null) {
          cidBase = getLocalName() + hashCode() +
                       System.currentTimeMillis()%10000 + "_";
       }
       return  cidBase + (cidCnt++); 
    }
    ACLMessage newMsg( int perf, String content, AID dest)
    {
       ACLMessage msg = newMsg(perf);
       if (dest != null) 
           msg.addReceiver( dest );
       msg.setContent( content );
       return msg;
    }

    ACLMessage newMsg( int perf)
    {
       ACLMessage msg = new ACLMessage(perf);
       msg.setConversationId( genID() );
       return msg;
    }
    
    void register( ServiceDescription sd)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {  
            DFService.register(this, dfd );  
        } catch (FIPAException fe) { fe.printStackTrace(); }
    }
    
    protected void takeDown()
    {
        try {
            DFService.deregister(this);
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent: " +getAID().getName()+" terminating.");
    }
}
