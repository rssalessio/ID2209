/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;
import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

/**
 *
 * @author shereen
 */

public class MyDFReg extends Agent 
{
    
    protected void setup() 
    {
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "TourGuide" ); //Representing service type
        sd.setName( getLocalName() ); 
        register( sd );
    }
    
    void register( ServiceDescription sd)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
    }
    
}