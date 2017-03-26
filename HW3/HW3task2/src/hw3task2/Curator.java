/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3task2;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.core.Location;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/**
 *
 * @author shereen
 */
public class Curator extends Agent 
{
    double estimatedRealPrice = 0;
    int biddingStrategy = 0;
    int initialPriceStrategy = 0;
    double  currentPrice = 0;
    double initialPrice = 0;
    boolean auctionOver = false;
    int id = 0;
    int step = -1;
    AID auctioneer = null;
    private Location destination;  
    private Map locations = new HashMap();
    protected void setup() 
    {
        
        this.doWait(10000);
        id =  0;
        biddingStrategy = ThreadLocalRandom.current().nextInt(4, 6 + 1);
        initialPriceStrategy = ThreadLocalRandom.current().nextInt(5, 7 + 1)/10;
        Object[] args = getArguments();
        Integer i = (Integer)args[0];
        destination = here();
        auctioneer = new AID("S2", AID.ISLOCALNAME);
       // System.out.println(biddingStrategy);
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
                      
                       }
                }
                catch (Exception e) { e.printStackTrace(); }

        }
        if (i==0)
            this.doClone((Location)locations.get("Container-3"), "AG1C");
        else
            this.doClone((Location)locations.get("Container-2"), "AG2C");
     //   this.doClone((Location)locations.get("Container-1"), "A1");
       // System.out.println("[Curator" + id + "-buyer] Looking for the auctioneer.");
        start();
/*
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("seller");
        template.addServices(sd);
        try {
            DFAgentDescription[] result= DFService.search(this,template);
            auctioneer = result[0].getName();
            System.out.println(getCuratorID() + " found auctioneer.");
            addBehaviour(new RegisterAuction());
        } catch(FIPAException fe) { fe.printStackTrace();} */
    }
    
    protected void start()
    {
        addBehaviour(new RegisterAuction());
        /*
        
        
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("seller");
        template.addServices(sd);
        try {
            DFAgentDescription[] result= DFService.search(this,template);
            auctioneer = result[0].getName();
            System.out.println(getCuratorID() + " found auctioneer.");
            addBehaviour(new RegisterAuction());
        } catch(FIPAException fe) { fe.printStackTrace();}*/
    }

    
          protected void beforeMove() {
// -----------------------------

   }

   protected void afterMove() {
// ----------------------------

   }

   protected void beforeClone() {
// -----------------------------

   }

   protected void afterClone() {
// ----------------------------
       // System.out.println(here().getName());
        id++;
          getContentManager().registerLanguage(new SLCodec());
	  getContentManager().registerOntology(MobilityOntology.getInstance());
        biddingStrategy = ThreadLocalRandom.current().nextInt(2, 6 + 1);
        initialPriceStrategy = ThreadLocalRandom.current().nextInt(3, 9 + 1)/10;
        System.out.println(this.getName());
        auctioneer = new AID("S1", AID.ISLOCALNAME);
        start();
   }
    
    protected class RegistrationNotification extends OneShotBehaviour
    {
        public void action()
        {
            ACLMessage msg =  receive(MessageTemplate.MatchSender(getDefaultDF()));
                
            if (msg != null)
            {
                try {
                      DFAgentDescription[] dfds = DFService.decodeNotification(msg.getContent());
                      System.out.println(getCuratorID()+"DF: " + dfds[0].getName());
                      auctioneer = dfds[0].getName();
                }catch (Exception e) {}
                addBehaviour(new RegisterAuction());
            }
            else
                block();
        }
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
    
    protected class RegisterAuction extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println(getCuratorID() + " registering at auctioneer.");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(auctioneer);
            send(msg);
            MessageTemplate mt = MessageTemplate.MatchPerformative(AdditionalPerformatives.START);
            ACLMessage reply = blockingReceive(mt);
            currentPrice = initialPrice = Double.parseDouble(reply.getContent());
            System.out.println(getCuratorID()+ " received intial price: " + initialPrice);
            estimateMarketPrice();
            addBehaviour(new StartAuction());
        }
    }
    
    protected class StartAuction extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println(getCuratorID() +  " starting auction.");
            while(auctionOver == false)
            {
                ACLMessage msg = blockingReceive();
                if (msg.getPerformative() != ACLMessage.CFP)
                {
                    System.out.println(getCuratorID() + " loses auction at price: " + currentPrice + " - Estimated real price: " + estimatedRealPrice);
                    auctionOver = true;
                    break;
                }
                currentPrice = Double.parseDouble(msg.getContent());
               // System.out.println(getCuratorID() + " new price: " + currentPrice);
                step++;
                estimateMarketPrice();
                if (currentPrice <=estimatedRealPrice)
                {
                    System.out.println(getCuratorID() + " sending bid.");
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    send(reply);
                    msg = blockingReceive();
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                    {
                        System.out.println(getCuratorID() + " wins auction at price: " + currentPrice + " -  Estimated real price: " +  estimatedRealPrice);
                        auctionOver = true;
                        break;
                    }
                }
            }
        }
    }
    
    public void estimateMarketPrice()
    {
        if (step < 1)
        {
            if (initialPriceStrategy == 0)
                estimatedRealPrice = initialPrice*0.5;
            else if (initialPriceStrategy == 1)
                estimatedRealPrice = initialPrice*0.6;
            else if (initialPriceStrategy == 2 )
                estimatedRealPrice = initialPrice*0.7;
            else if (initialPriceStrategy == 3)
                estimatedRealPrice = initialPrice*0.55;
        }
        else
        {
            double estReduction = (initialPrice-estimatedRealPrice)/(biddingStrategy*initialPrice);
            double reduction = (initialPrice-currentPrice)/(step*initialPrice);
            //System.out.println(reduction-estReduction);
            if (reduction > estReduction)
              estimatedRealPrice = estimatedRealPrice-initialPrice*0.1;
            else if (reduction < estReduction)
                estimatedRealPrice = estimatedRealPrice+initialPrice*0.1;
            
            
        }
        System.out.println(getCuratorID()+" Estimated price at step " + step + ": " +estimatedRealPrice );
    }

//  --- generating Conversation IDs -------------------

   protected static int cidCnt = 0;
   String cidBase ;
   
   String genCID() 
   { 
      if (cidBase==null) {
         cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
      }
      return  cidBase + (cidCnt++); 
   }


//  --- Methods to initialize ACLMessages -------------------

   ACLMessage newMsg( int perf, String content, AID dest)
   {
      ACLMessage msg = newMsg(perf);
      if (dest != null) msg.addReceiver( dest );
      msg.setContent( content );
      return msg;
   }

   ACLMessage newMsg( int perf)
   {
      ACLMessage msg = new ACLMessage(perf);
      msg.setConversationId( genCID() );
      return msg;
   }
   public String getCuratorID()
   {
       return "[Curator" + id + "-buyer]";
   }


}
