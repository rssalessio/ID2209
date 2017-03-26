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
public class Seller1 extends Agent
{
     protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
                    public void action()
                    {
                        int val = 5;
                        for (int price=105; price>=0; price=price-val)
                       {
                       ACLMessage msg = receive();
                        if(msg!=null)
                        {
                            int ans = Integer.parseInt(msg.getContent());
                            System.out.println("From now this picture is yours "+msg.getSender().getName());
                            System.out.println(""+msg.getContent());
                            val = price;  
                            doDelete();
                        }                          
                            else 
                          
                            {  
                                //System.out.print("Price is "+price);
                                ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                                msg2.setContent(""+price);
                                for(int i=1; i<3; i++)
                                msg2.addReceiver(new AID("B"+i, AID.ISLOCALNAME));
                                send(msg2); 
                            }  }
                        block();
                    }
        });
        /*
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setContent(""+price);
    for(int i=1; i<=2; i++)
    msg.addReceiver(new AID("B"+i, AID.ISLOCALNAME));
    send(msg);
*/
     }
}