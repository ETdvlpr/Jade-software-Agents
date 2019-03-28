package com.softagents.auction.seller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Seller agent handles selling, by contacting the auctioneer on behalf of the
 * user, has methods that allow it to receive requests from the user and
 * messages from the auctioneer It also possesses fields for storing the coffee
 * information and updating the UI
 */
public class Seller extends Agent {

    private ArrayList<AID> auctioneer;
    private UI GUI;
    private ArrayList<Coffee> coffeeList;

    protected void setup() {
        GUI = new UI(this);
        GUI.showGui();
        coffeeList = new ArrayList<>();
        auctioneer = new ArrayList<>();
        // Add a TickerBehaviour that tries to find auctioneer every second till it does
        addBehaviour(new FindAuctioneer(this, 1000));
    }

    @Override
    protected void takeDown() {
        if (GUI.isDisplayable()) {
            GUI.dispose();
        }
    }

    public void registerCoffee(String Name, int startingPrice) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                Coffee c = new Coffee(Name, startingPrice);
                coffeeList.add(c);
                GUI.updateTable(coffeeList);
                myAgent.addBehaviour(new sell(myAgent, coffeeList.size() - 1));
            }
        });
    }

    private class FindAuctioneer extends TickerBehaviour {

        public FindAuctioneer(Agent a, long period) {
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
                    ArrayList<AID> newAuctioneers = new ArrayList<>();
                    for (int i = 0; i < result.length; ++i) {
                        newAuctioneers.add(result[i].getName());
                    }
                    auctioneer = (ArrayList<AID>) newAuctioneers.clone();
                    addBehaviour(new FindAuctioneer(myAgent, 90000));
                    stop();
                }
            } catch (FIPAException fe) {
                if (fe.getMessage() != null) {
                    JOptionPane.showMessageDialog(GUI, fe.getMessage());
                }
            }
        }
    }

    class sell extends SimpleBehaviour {

        private final int coffeeIndex;
        private MessageTemplate mt;
        private boolean finished = false;

        public sell(Agent a, int coffeeIndex) {
            super(a);
            this.coffeeIndex = coffeeIndex;
        }

        private void sendSellRequest(AID auctioneer) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(auctioneer);
            message.setConversationId("Register coffee");
            message.setContent(coffeeList.get(coffeeIndex).getName() + "," + coffeeList.get(coffeeIndex).getStartingPrice());
            message.setReplyWith("SELL" + System.currentTimeMillis());
            myAgent.send(message);
            coffeeList.get(coffeeIndex).addTriedAuctioneer(auctioneer);
            coffeeList.get(coffeeIndex).setStatus("Waiting");
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Register coffee"), MessageTemplate.MatchInReplyTo(message.getReplyWith()));
        }

        @Override
        public void action() {
            if (auctioneer.size() > 0) {
                if (coffeeList.get(coffeeIndex).getStatus().equals("NEW")) {
                    sendSellRequest(auctioneer.get(0));
                    GUI.updateTable(coffeeList);
                } else {
                    //@TODO implement timed behaviour or other mechanism to avoid auctioneer not responding
                    ACLMessage reply = myAgent.receive(mt);
                    // wait for responce from auctioneer
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            coffeeList.get(coffeeIndex).setStatus("Selling");
                        } else if (reply.getPerformative() == ACLMessage.DISCONFIRM || reply.getPerformative() == ACLMessage.FAILURE) {
                            ArrayList<AID> auctioneersToTry = (ArrayList<AID>) auctioneer.clone();
                            auctioneersToTry.removeAll(coffeeList.get(coffeeIndex).getTriedAuctioneers());
                            if (auctioneersToTry.size() > 0) {
                                sendSellRequest(auctioneersToTry.get(0));
                            } else {
                                coffeeList.get(coffeeIndex).setStatus("Failure to Sell");
                                finished = true;
                            }
                        } else if (reply.getPerformative() == ACLMessage.INFORM) {
                            coffeeList.get(coffeeIndex).setStatus("Sold");
                            coffeeList.get(coffeeIndex).setSellingPrice(Integer.parseInt(reply.getContent().split(",")[1].trim()));
                            finished = true;
                        }
                        GUI.updateTable(coffeeList);
                    }
                }
            } else {
                coffeeList.get(coffeeIndex).setStatus("Failure to Sell");
                finished = true;
            }
        }

        @Override
        public final boolean done() {
            return finished;
        }
    }

    /**
     * Finds index of coffee in list given the coffee's name discards if coffee
     * has already been sold or failed to sell
     *
     * @param coffeeName
     * @return index of coffee if found or -1 if not found
     */
    protected int find(String coffeeName) {
        int index = -1;
        for (int i = 0; i < coffeeList.size(); i++) {
            if (coffeeList.get(i).getName().equals(coffeeName) && !(coffeeList.get(i).getStatus().equals("Sold") || coffeeList.get(i).getStatus().equals("Failure to Sell"))) {
                index = i;
            }
        }
        return index;
    }
}
