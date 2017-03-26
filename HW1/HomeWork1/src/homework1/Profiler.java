/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import java.util.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import jade.proto.states.MsgReceiver;
/**
 *
 * @author shereen
 */


public class Profiler extends Agent{
    //private ProfilerGui myGui;
    public User userInfo = new User();
    Tour tour= new Tour();
    private AID[] curatorAgents;
    private AID[] tourguideAgents;
    protected void setup() {
        Object[] args = getArguments();
        
        userInfo.loadFromFile((String) args[0]);
        showUserInfo();
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
                            System.out.println("[PROFILER] Added curator: " +  result[x].getName());
                        }
                    } catch(FIPAException fe) { fe.printStackTrace();}
                }
            }
        );  
        
        addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick()
                {
                    //update list of tour guide agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("Virtual-Tour");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result= DFService.search(myAgent,template);
                        tourguideAgents = new AID[result.length];
                        for (int x = 0; x < result.length; x++) {
                            tourguideAgents[x] = result[x].getName();
                            System.out.println("[PROFILER] Added tourguide: " +  result[x].getName());
                        }
                    } catch(FIPAException fe) { fe.printStackTrace();}
                }
            }
        );    
          
         
         
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Virtual-Tour");
        dfd.addServices(sd);
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(new Long(1));
        //get a notificaiton when a tourguide registers
        send(DFService.createSubscriptionMessage(this, getDefaultDF(), 
                                                       dfd, sc));
        addBehaviour(new RegistrationNotification()); //behaviour that starts communication with the tour guide
    } 
    

    
    class RegistrationNotification extends CyclicBehaviour 
    {
         public void action() 
         {
             ACLMessage msg =  receive(MessageTemplate.MatchSender(getDefaultDF()));
                
            if (msg != null)
            {
              try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(msg.getContent());
                    System.out.println("[PROFILER]DF: " + dfds[0].getName());
                    
                    addBehaviour( new WakerBehaviour( myAgent, 15000) 
                       {
                          public void handleElapsedTimeout() 
                          {   
                              //send info aobut interests to the tour guide
                                System.out.println("[PROFILER]Sending request to TOUR GUIDE");
                                ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
                                reply.setConversationId(genID());
                                reply.addReceiver(dfds[0].getName());
                                reply.setContent(userInfo.getInterests());
                                send(reply);
                                //wait for reply
                                addBehaviour( new WakerBehaviour( myAgent, 3000) 
                                   {
                                      public void handleElapsedTimeout() 
                                      {   
                                            ACLMessage msg =  myAgent.blockingReceive(MessageTemplate.MatchSender(dfds[0].getName()));
                                            String ret = msg.getContent();
                                            System.out.println("[PROFILER] Received tour!");
                                            tour.fromString(ret);
                                            System.out.println("[PROFILER] Tour museum: " +  tour.museum);
                                            System.out.println("[PROFILER] Tour curator: " +  tour.curator);
                                            for (String s: tour.artifacts)
                                                System.out.println("[PROFILER]Tour arifact name: " + s);
                                            addBehaviour(new TourDetails()); //get details from curator
                                      }
                                   });
                          }
                       });
                    
               }
               catch (Exception exc) {}
            }
           
            block();
   
         }  
    }
    
    class TourDetails extends OneShotBehaviour
    {
        public void action()
        {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(tour.curator));
            
            for (String s: tour.artifacts)
            {
                msg.setContent("ARTIFACT INFO " + s);
                send(msg); //for each item in the tour send a query message
            }
            for (int i=0; i < tour.artifacts.size(); i++)
            { //for each item in the tour waits for a response with details about the item
                ACLMessage reply = blockingReceive();
                System.out.println("[PROFILER] Tour Artifact details: " + reply.getContent());
            }
        }
    }
    public void showUserInfo()
    {
        addBehaviour(new OneShotBehaviour() {
            public void action()
            {
                System.out.println("Age: " + userInfo.age);
                System.out.println("Occupation: " + userInfo.occupation);
                System.out.println("Gender: " + userInfo.gender);
                if (userInfo.interests.isEmpty() == false)
                {
                    Iterator<String> iterator = userInfo.interests.iterator();
                    while(iterator.hasNext()) {
                        System.out.println("Interest: " + iterator.next());
                    }
                }
                if (userInfo.artifacts.isEmpty() == false)
                {
                    Iterator<String> iterator = userInfo.artifacts.iterator();
                    while(iterator.hasNext()) {
                        System.out.println("Artifact: " + iterator.next());
                    }
                }
            }
        });
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
    
}
