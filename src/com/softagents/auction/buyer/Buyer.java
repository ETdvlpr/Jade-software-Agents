package com.softagents.auction.buyer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Buyer extends Agent {

    private UI GUI;
    private ArrayList<AID> auctioneer;
    private ArrayList<Coffee> coffeeList;

    protected void setup() {
        coffeeList = new ArrayList<>();
        auctioneer = new ArrayList<>();
        GUI = new UI(this);
        GUI.showGui();

        // Add a TickerBehaviour that tries to find auctioneer every second
        // stops when it finds one
        addBehaviour(new searchForAuctioneer(this, 1000));
        addBehaviour(new ReciveMessage());
    }

    @Override
    protected void takeDown() {
        if (GUI != null) {
            ACLMessage Leave = new ACLMessage(ACLMessage.REQUEST);
            for (AID aid : auctioneer) {
                Leave.addReceiver(aid);
            }
            Leave.setContent("Leave");
            send(Leave);
            if (GUI.isDisplayable()) {
                GUI.dispose();
            }
        }
    }

    void addCoffee(String name, int price) {
        Coffee c = new Coffee(name, price);
        coffeeList.add(c);
        GUI.updateTable(coffeeList);
    }

    private class ReciveMessage extends CyclicBehaviour {

        private String[] content;
        private String coffeeName;
        private int price;
        private MessageTemplate mt;

        public ReciveMessage() {
            mt = MessageTemplate.MatchConversationId("coffee-trade");
        }

        @Override
        public void action() {
            if (auctioneer != null) {
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    content = msg.getContent().split(",");
                    coffeeName = content[0];
                    String auctionID = content[4];
                    int index = find(coffeeName);
                    if (index != -1) {
                        switch (msg.getPerformative()) {
                            case ACLMessage.ACCEPT_PROPOSAL:
                                coffeeList.get(index).setBuyingPrice(Integer.parseInt(content[1].trim()));
                                coffeeList.get(index).setStatus("Bought");
                                price = Integer.parseInt(content[1].trim());
                                break;
                            case ACLMessage.CFP:
                                coffeeList.get(index).addActiveAuction(auctionID, msg);
                                if (!coffeeList.get(index).hasStandingBid(getName())) {
                                    ACLMessage cheapestAuction = coffeeList.get(index).getLowestPriceAuction();
                                    price = Integer.parseInt(cheapestAuction.getContent().split(",")[1].trim());
                                    if (price < coffeeList.get(index).getMaximumPrice()) {
                                        ACLMessage reply = cheapestAuction.createReply();
                                        reply.setPerformative(ACLMessage.PROPOSE);
                                        reply.setContent("" + price);
                                        myAgent.send(reply);
                                        coffeeList.get(index).setBuyingPrice(price);
                                        coffeeList.get(index).setStatus("In bid");
                                    } else {
                                        coffeeList.get(index).setBuyingPrice(price);
                                        coffeeList.get(index).setStatus("Out bid");
                                    }
                                } else {
                                    coffeeList.get(index).setStatus("Standing bid");
                                }
                                break;
                            case ACLMessage.INFORM:
                                content = msg.getContent().split(",");
                                coffeeName = content[0];
                                index = find(coffeeName);
                                if (index != -1) {
                                    coffeeList.get(index).removeAuction(content[4]);
                                }
                                break;
                            default:    
                                System.out.println("unknown message received by " + myAgent.getAID());
                                System.out.println(msg.getContent());
                                break;
                        }
                        GUI.updateTable(coffeeList);
                    }
                } else {
                    block();
                }
            }
        }
    }

    private class searchForAuctioneer extends TickerBehaviour {

        public searchForAuctioneer(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            // Look for auctioneer
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Auctioneer");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) { // if auctioneer found then set check time to every 90 seconds instead of every second
                    ArrayList<AID> old_auctioneers = (ArrayList<AID>) auctioneer.clone();
                    auctioneer.clear();
                    for (int i = 0; i < result.length; ++i) {
                        auctioneer.add(result[i].getName());
                    }
                    old_auctioneers.retainAll(auctioneer);
                    //once you filter out auctioneers that have been killed
                    // all entries found in auctioneer but not in old_auctioneer are new and should be registered
                    ACLMessage register = new ACLMessage(ACLMessage.REQUEST);
                    boolean newAuctioneer = false;
                    for (AID aid : auctioneer) {
                        if (!old_auctioneers.contains(aid)) {
                            register.addReceiver(aid);
                            newAuctioneer = true;
                        }
                    }
                    register.setContent("Join-auction");
                    if (newAuctioneer) {
                        myAgent.send(register);
                    }
                    addBehaviour(new searchForAuctioneer(myAgent, 90000));
                    stop();
                }
            } catch (FIPAException fe) {
                if (fe.getMessage() != null) {
                    JOptionPane.showMessageDialog(GUI, fe.getMessage());
                }
            }
        }
    }

    /**
     * Finds index of coffee in list given the coffee's name discards if coffee
     * has been bought b/c the bought coffee is only stored for record
     *
     * @param coffeeName
     * @return index of coffee if found or -1 if not found
     */
    protected int find(String coffeeName) {
        int index = -1;
        for (int i = 0; i < coffeeList.size(); i++) {
            if (coffeeList.get(i).getName().equals(coffeeName) && !coffeeList.get(i).getStatus().equals("Bought")) {
                index = i;
            }
        }
        return index;
    }
}
