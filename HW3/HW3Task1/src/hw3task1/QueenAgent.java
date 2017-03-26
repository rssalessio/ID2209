/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3task1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import static jade.tools.sniffer.Agent.i;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



public class QueenAgent extends Agent 
{
    Board board = null;
    int id = 0;
    List<Tuple> agents = null;
    int currentPlayer = -1;
                                                 
    protected void setup() 
    {
        Object[] args = getArguments();
        id = Integer.parseInt((String) args[0]);
        int size = Integer.parseInt((String)args[1]);
        agents = new ArrayList<Tuple>();
        board = new Board(size);
        printMsg("initialised.");
        registerService();
        printMsg("registered at DF.");
        addBehaviour(new registerOtherAgents());
        addBehaviour(new startGame());
    }
    
    protected class startGame extends OneShotBehaviour
    {
        public void action()
        {
            int i = 0; //represents the row, -> the agent
            int j = 0; //represents the 
            int msgid = 0;
            boolean solution=false;
            syncAll();
         //   printMsg("Starting game");
            while(!solution)
            {
                //printMsg("turn: "+String.valueOf(i));
                if (i == id || (id==board.size-1 && i >= board.size))
                {
                    if (i > board.size) i=board.size;
                  //  printMsg("Playing...");
                  if(i==board.size)
                  {
                      System.out.println(i+"-"+j);
                      System.out.println(board.rowPromising(i-1));
                  }
                    if (board.rowPromising(i-1) && j< board.size)
                    {
                        if (i == board.size)
                        {
                            System.out.println("SOLUTION!");
                            board.printBoard();
                            solution = true;
                            continue;
                        }
                        else
                        {
                            
                            //printMsg("Sending play..");
                            board.update( i, j);
                           // printMsg(new Move(i,j).toString());
                            sendUpdate(new Move(i,j),msgid++); //next row
                            board.printBoard();
                            i++;
                            j++;
                         //   ACLMessage reply = blockingReceive(); 
                        }
                    }
                    else    
                    {
                        
                        
                      //  printMsg("bad play");
                        board.undoLastMove();
                        sendUndoUpdate(msgid++,j);
                        if (i==board.size && j>=board.size)
                            i-=2;
                        else
                            i--;
                        if (i!=board.size-1)
                            j=0;
                    }   
                }
                else
                {
                 //   printMsg("Blocking...");
                    int t = i;
                    if (i >= board.size)
                        t--;
                    MessageTemplate mt = MessageTemplate.MatchSender(getAgentAID(t));   
                    ACLMessage reply = receive (MessageTemplate.and(MessageTemplate.MatchConversationId(String.valueOf(msgid)), mt));
                    if (reply != null)
                    {
                        msgid++;
                        if (reply.getPerformative() == ACLMessage.INFORM)
                        {
                           // printMsg("Received good play");
                           // printMsg(reply.getContent());
                            Move m = new Move(reply.getContent());
                            board.update(m);
                            i++;
                        }
                        else if (reply.getPerformative() == ACLMessage.REFUSE)
                        {
                           // printMsg("Received bad play");
                            int k = Integer.parseInt(reply.getContent());
                            board.undoLastMove();
                            if (i==board.size && k ==board.size)
                                i-=2;
                            else
                                i--;
                        }
                    }
                    else 
                        block(100);
                    try{
                    this.wait(100);} catch(Exception e ){}
                }
            }
        }
    }
    public AID getAgentAID(int i)
    {
        for (Tuple ag : agents)
        {
            if (ag.id == i)
                return ag.agent;
        }
        return null;
    }
    
    public void syncAll()
    {
        ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
        //printMsg("Start sync...");
        for (Tuple agent: agents)
        {
            msg.addReceiver(agent.agent);
          //  printMsg("sync to : " + agent.agent.getName());
            send(msg);
        }
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        
        for (Tuple agent: agents)
        {
            ACLMessage reply = blockingReceive(MessageTemplate.and(MessageTemplate.MatchSender(agent.agent), mt));
           // printMsg("sync reply from: " + reply.getSender().getName());
        }
       // printMsg("Finished sync...");
    }

    public void sendUndoUpdate(int msgid,int j)
    {
        ACLMessage msg = new ACLMessage(ACLMessage.REFUSE);
        msg.setConversationId(String.valueOf(msgid));
        msg.setContent(String.valueOf(j));
        for (Tuple agent: agents)
        {
            msg.addReceiver(agent.agent);
            send(msg);
        }
        
    }
    public void sendUpdate(Move m, int msgid)
    {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(m.toString());
        msg.setConversationId(String.valueOf(msgid));
        for (Tuple agent: agents)
        {
            //printMsg("id agent msg: "  + agent.id);
            msg.addReceiver(agent.agent);
            send(msg);
        }
    }
    
    protected class registerOtherAgents extends OneShotBehaviour
    {
        public void action()
        {
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType( "queen" );
            dfd.addServices(sd);
            while(true)//-1 because we should remove ourselves
            {
                block(500);
                try
                {
                    agents.clear();
                    DFAgentDescription[] result= DFService.search(myAgent,dfd);
                    for (i=0; i<result.length; i++) 
                    {
                        ServiceDescription s = (ServiceDescription) result[i].getAllServices().next();
                        Property p = (Property) s.getAllProperties().next();
                        if (Integer.parseInt((String)p.getValue())==id) continue;
                        boolean k = false;
                        for (Tuple ag : agents)
                        {
                            if (ag.id == Integer.parseInt((String)p.getValue()) )
                            {
                                k=true;
                                break;
                            }
                        }
                        if (!k)
                            agents.add(new Tuple(result[i].getName(), Integer.parseInt((String)p.getValue())));
                    }
                }
                catch (FIPAException fe) { fe.printStackTrace(); }
                if (agents.size()==board.size-1)break;
            }
            printMsg("Added all queens! (N: " + agents.size() +") and myself.");
            sortAgents();
            //select starting player
            currentPlayer = id;
            for (Tuple i: agents)
            {
                if (currentPlayer > i.id)
                    currentPlayer = i.id;
            }
        }
    }
    public void sortAgents()
    {
        if (agents == null) return;
        Collections.sort(agents, new Comparator<Tuple>() {
            public int compare(Tuple left, Tuple right)  {
                return left.id-right.id; // The order depends on the direction of sorting.
            }
        });
    }
    
    protected void registerService() //REGISTERS QUEEN AT DF WITH PROPERTY ("ID", id)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("queen");
        sd.setName("queen");
        Property p =new Property();
        p.setName("ID");
        p.setValue((int)id);
        sd.addProperties(p);
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
        }
        catch(FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    protected void printMsg(String msg)
    {
 
        System.out.println(Calendar.getInstance().get(Calendar.MILLISECOND)+" - [Queen " + id +"] " + msg);
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

