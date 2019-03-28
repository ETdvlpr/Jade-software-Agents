package com.softagents.auction.buyer;

import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * keeps track of the coffee item's data like maximum price, name, status...
 */
public class Coffee {

    /**
     * list of active auction for the coffee item with auctionID as key and the
     * details as value
     */
    private HashMap<String, ACLMessage> activeAuctions;
    private String Name;
    private int maximumPrice;
    private int buyingPrice;
    private String status;

    /**
     * Initializes coffee with the name and maximum price
     *
     * @param Name
     * @param maximumPrice
     */
    public Coffee(String Name, int maximumPrice) {
        this.Name = Name;
        this.maximumPrice = maximumPrice;
        status = "Waiting";
        this.activeAuctions = new HashMap<>();
    }

    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * @return the maximumPrice
     */
    public int getMaximumPrice() {
        return maximumPrice;
    }

    /**
     * @param maximumPrice the maximumPrice to set
     */
    public void setMaximumPrice(int maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    /**
     * @return the buyingPrice
     */
    public int getBuyingPrice() {
        return buyingPrice;
    }

    /**
     * @param buyingPrice the buyingPrice to set
     */
    public void setBuyingPrice(int buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the activeAuctions
     */
    public HashMap<String, ACLMessage> getActiveAuctions() {
        return activeAuctions;
    }

    /**
     * @param ID
     * @param msg
     */
    public void addActiveAuction(String ID, ACLMessage msg) {
        this.activeAuctions.put(ID, msg);
    }

    /**
     * checks if user is standing bid on any of the active auctions
     *
     * @param AgentName
     * @return
     */
    public boolean hasStandingBid(String AgentName) {
        for (Map.Entry<String, ACLMessage> entry : activeAuctions.entrySet()) {
            String[] content = entry.getValue().getContent().split(",");
            if (content[2].equals(AgentName)) {
                status = "Standing bid";
                return true;
            }
        }
        status = "In bid";
        return false;
    }

    ACLMessage getLowestPriceAuction() {
        int min = Integer.MAX_VALUE;
        String cheapest = null;
        for (Map.Entry<String, ACLMessage> entry : activeAuctions.entrySet()) {
            int price = Integer.parseInt(entry.getValue().getContent().split(",")[1].trim());
            if(min > price){
                min = price;
                cheapest = entry.getKey();
            }
        }
        return activeAuctions.get(cheapest);
    }

    void removeAuction(String ID) {
        activeAuctions.remove(ID);
    }
}
