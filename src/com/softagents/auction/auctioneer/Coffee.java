package com.softagents.auction.auctioneer;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * keeps track of the coffee item's data
 * like starting price, name, status...
 */
public class Coffee 
{
    private ACLMessage request; // to keep track order and seller
    private String Name;
    private int startingPrice;
    private int sellingPrice;
    private String status;
    private AID buyer;

    /**
     * Initializes coffee with the name and starting price
     * @param Name
     * @param startingPrice
     * @param request
     */
    public Coffee(String Name,int startingPrice, ACLMessage request) 
    {
        this.Name = Name;
        this.startingPrice = startingPrice;
        this.request = request;
        status = "NEW";
    }

    /**
     * @return the coffeName
     */
    public String getName() {
        return Name;
    }

    /**
     * @param coffeName the coffeName to set
     */
    public void setName(String coffeName) {
        this.Name = coffeName;
    }

    /**
     * @return the startingPrice
     */
    public int getStartingPrice() {
        return startingPrice;
    }

    /**
     * @param startingPrice the startingPrice to set
     */
    public void setStartingPrice(int startingPrice) {
        this.startingPrice = startingPrice;
    }

    /**
     * @return the sellingPrice
     */
    public int getSellingPrice() {
        return sellingPrice;
    }

    /**
     * @param sellingPrice the sellingPrice to set
     */
    public void setSellingPrice(int sellingPrice) {
        this.sellingPrice = sellingPrice;
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
     * @return the buyer
     */
    public AID getBuyer() {
        return buyer;
    }

    /**
     * @param buyer the buyer to set
     */
    public void setBuyer(AID buyer) {
        this.buyer = buyer;
    }

    /**
     * @return the request
     */
    public ACLMessage getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(ACLMessage request) {
        this.request = request;
    }
}
