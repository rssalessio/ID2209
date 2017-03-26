package hw3task2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dell
 */

import java.util.*;

import jade.core.*;
     
 public class Simple extends Agent 
 {       
     private jade.wrapper.AgentContainer auctioneerHome,museumone,museumtwo;
     protected void setup() 
     {
          jade.core.Runtime runtime = jade.core.Runtime.instance();
          double[] arg1 = {100,1.5,0.1,0};
         // Create the container objects
         auctioneerHome = runtime.createAgentContainer(new ProfileImpl());
         museumone = runtime.createAgentContainer(new ProfileImpl());
         museumtwo = runtime.createAgentContainer(new ProfileImpl());
         try{
             ArrayList<Integer> a = new ArrayList<Integer>();
             a.add(0);
         
         auctioneerHome.createNewAgent("S1", "hw3task2.ArtistManager",a.toArray()).start();
         a.clear();
         a.add(0);
         museumone.createNewAgent("AG1", "hw3task2.Curator", a.toArray()).start();
         a.clear();
         a.add(1);
         museumtwo.createNewAgent("AG2", "hw3task2.Curator", a.toArray()).start();
         
         } catch(Exception e){}
         
        
     }   //  --- setup ---


 }   //  --- class Simple1