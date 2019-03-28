package com.softagents.auction.auctioneer;

/**
 *
 * @author Dawit Samuel
 *
 */
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

/**
 * Auctioneer Agent in charge of administering the auction
 */
public class Auctioneer extends Agent {

    private ArrayList<AID> buyers;
    private ArrayList<Coffee> ledger;
    private UI GUI;

    @Override
    protected void setup() {
        buyers = new ArrayList<>();
        ledger = new ArrayList<>();
        GUI = new UI(this);
        GUI.showGui();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Auctioneer");
        sd.setName("The auction house");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            if (fe.getMessage() != null) {
                JOptionPane.showMessageDialog(GUI, fe.getMessage());
            }
        }

        addBehaviour(new Registration(this));
    }

    /**
     * handles agent clean up like disposing of UI and De-registering agent from
     * DF
     */
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            if (fe.getMessage() != null) {
                JOptionPane.showMessageDialog(GUI, fe.getMessage());
            }
        }
        GUI.dispose();
    }

    /**
     * Inner class Registration. This is the behaviour used by auctioneer to
     * register and De-register incoming buyers (i.e. greetings/leave) the
     * behaviour also registers new items provided by sellers. if buyer is
     * trying to join or leave auction The auctioneer adds/removes the buyer
     * from list of buyers if seller is trying to register coffee this method
     * registers it to to_be_sold
     */
    private class Registration extends CyclicBehaviour {

        public Registration(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // INFORM Message received. Process it
                String message = msg.getContent();
                if (message.equals("Join-auction")) {
                    if (!buyers.contains(msg.getSender())) {
                        buyers.add(msg.getSender());
                    }
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent("welcome you have successfully registered");
                    myAgent.send(reply);
                    GUI.updateConsole("-->  sent to : " + reply.getSender().getName() + " : " + reply.getContent());
                    GUI.updateBuyerTable(buyers);
                } else if (message.equals("Leave")) {
                    buyers.remove(msg.getSender());
                    GUI.updateBuyerTable(buyers);
                } else if (msg.getConversationId().equals("Register coffee")) {
                    if (buyers.size() > 0) {
                        String[] content = message.split(",");
                        int price = Integer.parseInt(content[1].trim());
                        Coffee c = new Coffee(content[0], price, msg);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent(content[0] + ", successfully registered");
                        myAgent.send(reply);
                        GUI.updateConsole("-->  Sent to : " + reply.getSender().getName() + " : " + reply.getContent());

                        ledger.add(c);
                        myAgent.addBehaviour(new commenceAuction(myAgent, ledger.size() - 1));
                        GUI.updateLedgerTable(ledger);
                    } else {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.DISCONFIRM);
                        reply.setContent(message.split(",")[0] + ",No buyers");
                        myAgent.send(reply);
                        GUI.updateConsole("-->  Sent to : " + msg.getSender().getLocalName() + " : " + reply.getContent());
                    }
                }
            } else {
                block();
            }
        }
    }

    /**
     * Inner class commenceAuction.
     *
     * This behaviour gets called every 3 seconds to check for bids if its
     * counter reaches three calls with out a new bid it will go ahead and sell
     * to the highest bidder or let the buyer know if there are no bidders It
     * sends out messages in the form (coffee name, bid price, standing bid,
     * standing bid price, auction ID)
     */
    private class commenceAuction extends TickerBehaviour {

        /**
         * Initializes auction settings like the agent with the behaviour 3 sec.
         * time out for the behavior and coffee index to work with from the
         * ledger, When finished initializing this method puts out a call for
         * bids to all prospective buyers
         *
         * @param a
         * @param coffeeIndex
         */
        public commenceAuction(Agent a, int coffeeIndex) {
            super(a, 3000);
            this.coffeeIndex = coffeeIndex;
            bestPrice = ledger.get(coffeeIndex).getStartingPrice();
            ledger.get(coffeeIndex).setStatus("IN BID");
            // unique identifier for this specific auction
            auctionID = ledger.get(coffeeIndex).getRequest().getSender().getLocalName() + "-" + myAgent.getLocalName() + System.currentTimeMillis();

            BroadcastBid();
        }
        private String auctionID;
        private AID bestBuyer;
        private int bestPrice;
        private int coffeeIndex;
        private int count = 0;
        private int increment = 0;
        private MessageTemplate mt;

        /**
         * puts out a CFP to all buyers with coffee name and current acceptable
         * bidding price and it prepares the message template to receive
         * incoming messages for this broadcast
         */
        private void BroadcastBid() {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < buyers.size(); ++i) {
                cfp.addReceiver(buyers.get(i));
            }
            String standingBid = (bestBuyer != null) ? bestBuyer.getName() : "";
            // name, new price to bid for, standing bid, standing price, auction ID
            cfp.setContent(ledger.get(coffeeIndex).getName() + "," + (bestPrice + increment) + "," + standingBid + "," + bestPrice + "," + auctionID);
            cfp.setConversationId("coffee-trade");
            cfp.setReplyWith(getLocalName() + "cfp" + System.currentTimeMillis()); // Unique value for proposal
            myAgent.send(cfp);
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("coffee-trade"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
            GUI.updateConsole("-->  Sent Broadcast : " + cfp.getContent());
        }

        protected void onTick() {
            if (count < 3) {
                // capture first proposal as best proposal
                count++;
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    GUI.updateConsole("<--  Received from : " + reply.getSender().getName() + " : " + reply.getContent());
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        int price = Integer.parseInt(reply.getContent());
                        if (bestBuyer == null || bestPrice + increment <= price) {
                            // This offer matches bid
                            bestPrice = price;
                            bestBuyer = reply.getSender();
                            increment += 10;
                            count = 0;

                            // checks for people with higher bid
                            reply = myAgent.receive(mt);
                            while (reply != null) {
                                if (bestPrice < Integer.parseInt(reply.getContent())) {
                                    bestPrice = Integer.parseInt(reply.getContent());
                                    bestBuyer = reply.getSender();
                                }
                                GUI.updateConsole("<--  Received from : " + reply.getSender().getName() + " : " + reply.getContent());
                                reply = myAgent.receive(mt);
                            }
                            ledger.get(coffeeIndex).setSellingPrice(bestPrice);
                            ledger.get(coffeeIndex).setBuyer(bestBuyer);
                            GUI.updateLedgerTable(ledger);
                            BroadcastBid();
                        }
                    }
                } else {
                    BroadcastBid();
                }
            } else {
                ACLMessage msgToSeller = ledger.get(coffeeIndex).getRequest().createReply();
                if (bestBuyer == null) {
                    // sell Attempt failed
                    msgToSeller.setPerformative(ACLMessage.FAILURE);
                    ledger.get(coffeeIndex).setStatus("NOT SOLD");
                    msgToSeller.setContent(ledger.get(coffeeIndex).getName() + ",Couldn't sell the coffee");
                } else {
                    // Send the confirmation to the buyer that provided the best offer
                    ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    accept.addReceiver(bestBuyer);
                    accept.setConversationId("coffee-trade");
                    accept.setContent(ledger.get(coffeeIndex).getName() + "," + (bestPrice) + "," + bestBuyer.getName() + "," + bestPrice + "," + auctionID);
                    myAgent.send(accept);
                    GUI.updateConsole("<--  Sent to : " + bestBuyer.getName() + " : ACCEPT");

                    // tell all others auction is closed
                    ACLMessage auctionClosed = new ACLMessage(ACLMessage.INFORM);
                    for (int i = 0; i < buyers.size(); ++i) {
                        if (!buyers.get(i).equals(bestBuyer)) {
                            auctionClosed.addReceiver(buyers.get(i));
                        }
                    }
                    auctionClosed.setContent(ledger.get(coffeeIndex).getName() + "," + (bestPrice) + "," + bestBuyer.getName() + "," + bestPrice + "," + auctionID);
                    auctionClosed.setConversationId("coffee-trade");
                    myAgent.send(auctionClosed);

                    ledger.get(coffeeIndex).setSellingPrice(bestPrice);
                    ledger.get(coffeeIndex).setBuyer(bestBuyer);
                    ledger.get(coffeeIndex).setStatus("SOLD");
                    msgToSeller.setPerformative(ACLMessage.INFORM);
                    msgToSeller.setContent(ledger.get(coffeeIndex).getName() + "," + bestPrice + ", successfully sold");
                }
                myAgent.send(msgToSeller);
                GUI.updateConsole("--> Sent to : " + ledger.get(coffeeIndex).getRequest().getSender().getName() + " : " + msgToSeller.getContent());
                GUI.updateLedgerTable(ledger);
                stop();
            }
        }
    }
}
