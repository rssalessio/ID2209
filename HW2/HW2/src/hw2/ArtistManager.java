/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw2;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import java.util.LinkedList;
import java.util.List;



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
        
                                                 
    protected void setup() 
    {
        Object[] args = getArguments();
        marketPrice = Double.parseDouble( (String)args[0]);
        initialPrice = marketPrice*(1+Double.parseDouble( (String)args[1]));
        reservePrice = marketPrice*(1-Double.parseDouble( (String)args[2]));
        strategy = Integer.parseInt( (String)args[3]);
        currentPrice = initialPrice;
        
        System.out.println("[ArtistManager-Seller] Market Price: " +  marketPrice + " - Initial Price: " + initialPrice +
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
            });
    }
    protected class Auction extends OneShotBehaviour
    {
         public void action()
         {
            while(auctionOver == false)
            {
                
                System.out.println("[ArtistManager-Seller] Sending Price: " + currentPrice);
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
                    System.out.println("[ArtistManager-Seller] Winner is: " + msg.getSender() + " -  at price: " + currentPrice);
                    auctionOver=true;
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
        System.out.println("[ArtistManager-Seller] Reduced price to: " + currentPrice + " from : "+ oldPrice);
        if (currentPrice <= reservePrice)
        {
            System.out.println("[ArtistManager-Seller] Auction over, no winner, reached reserve price: " + reservePrice);
            auctionOver = true;
        }
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
