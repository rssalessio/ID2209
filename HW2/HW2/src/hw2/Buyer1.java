/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw2;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
/**
 *
 * @author shereen
 */
public class Buyer1 extends Agent
{
    int BestPrice = 60;
       protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
           public void action()
           {
               ACLMessage msg= receive();
               if (msg != null)
               {
                   //System.out.println("Got message "+ msg.getContent() +" from "+ msg.getSender().getName());
                int offer = Integer.parseInt(msg.getContent());
                if(offer<= BestPrice)
                {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Price is good ");
                    send(reply);
                }
               } block();
           } 
        });
    }
    
    
}
