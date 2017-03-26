/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3task2;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Result;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 *
 * @author shereen
 */
public class ArtistManager extends Agent 
{
    double initialPrice = 0;
    double marketPrice = 0;
    double reservePrice = 0;
    double currentPrice = 0;
    int strategy = 0;
    boolean noMoreBuyers = false;
    boolean auctionOver = false;
    private List<AID> buyers = new LinkedList();
    private Location destination;
     private Location loc2;
      private Location loc3;
      double auctionprice=0;
    private Map locations = new HashMap();
    private AID otherauc = null;
    private String selfname;
    protected void setup() 
    {
        this.doWait(5000);
        selfname="S1";
        Object[] args = getArguments();
        marketPrice =100;
        initialPrice = marketPrice*(1+1.5);
        reservePrice = marketPrice*(1-0.1);
        strategy = 0;
        currentPrice = initialPrice;
        
        destination = here();
      //  if (x==0)
       //     this.doClone(here(), "S1");
        
        
        
        if(0==0)
        {
            getContentManager().registerLanguage(new SLCodec());
             getContentManager().registerOntology(MobilityOntology.getInstance());


            try {
                // Get available locations with AMS
                sendRequest(new Action(getAMS(), new QueryPlatformLocationsAction()));

                MessageTemplate mt = MessageTemplate.and(
                                             MessageTemplate.MatchSender(getAMS()),
                                             MessageTemplate.MatchPerformative(ACLMessage.INFORM));
               ACLMessage resp = blockingReceive(mt);
               ContentElement ce = getContentManager().extractContent(resp);
               Result result = (Result) ce;

               jade.util.leap.Iterator it = result.getItems().iterator();
               while (it.hasNext()) {
                  Location loc = (Location)it.next();
                  locations.put(loc.getName(), loc);
            //      System.out.println(loc.getName());
                      
                       }
                }
                catch (Exception e) { e.printStackTrace(); }

        }
        this.doMove((Location)locations.get("Container-3"));
        
        
       // locations.get("Container-2");
       //this.doMove((Location)locations.get("Container-2"));
      /*  System.out.println("[ArtistManager-Seller] Market Price: " +  marketPrice + " - Initial Price: " + initialPrice +
                " - Reserve Price: " + reservePrice + " - Strategy: "+ strategy);
        
        System.out.println("[ArtistManager-Seller] Registering at DF");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("seller");
        sd.setName("seller");
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
        }
        catch(FIPAException fe) {
            fe.printStackTrace();
        }
        
        
        
        addBehaviour(new OneShotBehaviour(this) 
        {
            public void action() 
            {
                MessageTemplate template = MessageTemplate.MatchPerformative( ACLMessage.INFORM );    
                while(noMoreBuyers == false)
                {
                    ACLMessage msg = receive( template );
                    if (msg!=null) {
                       buyers.add(msg.getSender());
                       System.out.println("[ArtistManager-Seller] Added buyer : " +  msg.getSender());
                       ACLMessage reply = msg.createReply();
                       reply.setPerformative(AdditionalPerformatives.START);
                       reply.setContent(String.valueOf(currentPrice));
                       send(reply);
                    }
                    block(1000);
                    if (buyers.size() == 4)
                        noMoreBuyers = true;
                }
            }
        });
        System.out.println("[ArtistManager-Seller] Waiting for all buyers to register...");
        addBehaviour( new OneShotBehaviour()
            {
                public void action() 
                {
                    System.out.println("[ArtistManager-Seller] No more buyers accepted!");
                    for (AID agent : buyers)
                    {
                        ACLMessage msg = new ACLMessage(AdditionalPerformatives.START);
                        msg.addReceiver(agent);
                    }
                    System.out.println("[ArtistManager-Seller] Starting the auction.");
                    addBehaviour(new Auction());
                }
            });*/
    }
    protected void start()
    {
        addBehaviour(new OneShotBehaviour(this) 
        {
            public void action() 
            {
                MessageTemplate template = MessageTemplate.MatchPerformative( ACLMessage.INFORM );    
                while(noMoreBuyers == false)
                {
                    ACLMessage msg = receive( template );
                    if (msg!=null) {
                       buyers.add(msg.getSender());
                       System.out.println("["+getNameAgent()+"]  Added buyer : " +  msg.getSender());
                       ACLMessage reply = msg.createReply();
                       reply.setPerformative(AdditionalPerformatives.START);
                       reply.setContent(String.valueOf(currentPrice));
                       send(reply);
                    }
                    block(1000);
                    if (buyers.size() == 2)
                        noMoreBuyers = true;
                }
            }
        });
        System.out.println("["+getNameAgent()+"]  Waiting for all buyers to register...");
        addBehaviour( new OneShotBehaviour()
            {
                public void action() 
                {
                    System.out.println("["+getNameAgent()+"] No more buyers accepted!");
                    for (AID agent : buyers)
                    {
                        ACLMessage msg = new ACLMessage(AdditionalPerformatives.START);
                        msg.addReceiver(agent);
                    }
                    System.out.println("["+getNameAgent()+"] Starting the auction.");
                    addBehaviour(new Auction());
                }
            });
    }
      protected void beforeMove() {
// -----------------------------

   }

   protected void afterMove() {
// ----------------------------
   //System.out.println("lol+"+here().getName());
   
   if (auctionOver==false)
   {
       this.doClone((Location)locations.get("Container-2"), "S2");
       otherauc = new AID("S2", AID.ISLOCALNAME);
       start();
   }
   else
    {
        getWinner();
          
    }
   }
   protected void getWinner()
   {
        addBehaviour(new OneShotBehaviour(this) 
        {
            public void action() 
            {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
          msg.addReceiver(otherauc);
          msg.setContent(String.valueOf(auctionprice));
          send(msg);
          MessageTemplate mt = MessageTemplate.MatchSender(otherauc);
          ACLMessage reply = blockingReceive(mt);
          double val = Double.valueOf(reply.getContent());
          if (val >= auctionprice)
              System.out.println("["+getNameAgent()+"] Auction won at: " + val);
            }
        });
   }

   protected void beforeClone() {
// -----------------------------

   }

   protected void afterClone() {
// ----------------------------
     //   System.out.println(here().getName());
       getContentManager().registerLanguage(new SLCodec());
	  getContentManager().registerOntology(MobilityOntology.getInstance());
          selfname="S2";
           otherauc = new AID("S1", AID.ISLOCALNAME);
          start();
   }
   
    void sendRequest(Action action) {
// ---------------------------------

      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
      request.setLanguage(new SLCodec().getName());
      request.setOntology(MobilityOntology.getInstance().getName());
      try {
	     getContentManager().fillContent(request, action);
	     request.addReceiver(action.getActor());
	     send(request);
	  }
	  catch (Exception ex) { ex.printStackTrace(); }
   }
    protected class Auction extends OneShotBehaviour
    {
         public void action()
         {
            while(auctionOver == false)
            {
                
                System.out.println("["+getNameAgent()+"]  Sending Price: " + currentPrice);
                for (AID agent : buyers)
                {
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.addReceiver(agent);
                    msg.setContent(String.valueOf(currentPrice));
                    send(msg);
                }
                
      //          System.out.println("[ArtistManager-Seller] Check if there is any buyer..");
                MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = blockingReceive(template,1500);
                if(msg != null)
                {
                    System.out.println("["+getNameAgent()+"]  Winner is: " + msg.getSender() + " -  at price: " + currentPrice);
                    auctionOver=true;
                    auctionprice = currentPrice;
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    send(reply);
                    for (AID agent: buyers)
                    {
                        if (agent.equals(msg.getSender())) continue;
                        ACLMessage replym = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        replym.addReceiver(agent);
                        send(replym);
                    }
                    this.getAgent().doMove((Location)locations.get("Container-1"));
                }
                else
                    reducePrice();
            }

             
         }
    }
    
    public void reducePrice()
    {
        double oldPrice = currentPrice;
        if (strategy == 0)
            currentPrice = currentPrice-initialPrice*0.1;
        else if (strategy == 1)
            currentPrice = currentPrice-initialPrice*0.2;
        else if (strategy == 2)
            currentPrice = currentPrice-initialPrice*0.3;
        System.out.println("["+getNameAgent()+"]  Reduced price to: " + currentPrice + " from : "+ oldPrice);
        if (currentPrice <= reservePrice)
        {
            System.out.println("["+getNameAgent()+"] Auction over, no winner, reached reserve price: " + reservePrice);
            auctionOver = true;
            auctionprice = 0;
            this.doMove((Location)locations.get("Container-1"));
        }
    }
    
    protected String getNameAgent()
    {
        return selfname;
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
