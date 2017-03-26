/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;
import jade.core.Agent;
import java.util.Set;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import java.util.Random;
import java.util.Map;
/**
 *
 * @author shereen
 */
public class Curator extends Agent{
    public Museum museum = new Museum();
    MessageTemplate template = MessageTemplate.MatchPerformative( ACLMessage.INFORM ); 
    ACLMessage reply;
    Random rnd = new Random();
    protected void setup()
    {
        Object[] args = getArguments();
        
        museum.loadFromFile((String) args[0]);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("artifacts-list");
        sd.setName("Show-artifacts");
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
        }
        catch(FIPAException fe) {
            fe.printStackTrace();
        }
        
        addBehaviour( new CyclicBehaviour(this) 
        {
            public void action() 
            {
                ACLMessage msg = receive(  );
                if (msg!=null) {
                    String content = msg.getContent();
                    if (content.contains("ARTIFACT INFO")) //information about artifact
                    {
                        System.out.println("[CURATOR]Artifact info sent");
                        reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        String[] splitted = content.split("\\s+");
                        Artifact temp = null;
                        if (splitted.length > 2)
                        {
                            String artifact = splitted[2];
                            temp = museum.artifacts.get(artifact);
                        }
                        if (temp == null)
                            reply.setContent("NULL");
                        else {
                            reply.setContent(temp.id +"-"+temp.name + "-" + temp.creator + "-"+temp.creationDate + "-" + temp.creationPlace
                             + "-" + temp.genre + "-" + temp.genreDetails+"-" +temp.description) ;
                        }
                        send(reply);
                    }
                    else if (content.contains("MUSEUM INFO"))
                    {
                        System.out.println("[CURATOR]Museum info sent");
                        reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        String replystr = myAgent.getName()+"-"+museum.name+"\n";
                        for (Map.Entry<String, Artifact> entry : museum.artifacts.entrySet())
                        {
                            Artifact temp = entry.getValue();
                            replystr += temp.id+"-"+temp.name + "-" + temp.creator + "-"+temp.creationDate + "-" + temp.creationPlace
                             + "-" + temp.genre + "-" + temp.genreDetails+"-" +temp.description+"\n";
                        }
                        reply.setContent(replystr);
                        send(reply);
                    }
                    else
                        block(1000);
                }
                else
                    block(1000);
            }
        });
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