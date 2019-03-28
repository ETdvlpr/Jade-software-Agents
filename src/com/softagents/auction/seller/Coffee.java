package com.softagents.auction.seller;

import com.softagents.auction.buyer.*;
import jade.core.AID;
import java.util.ArrayList;

/**
 * keeps track of the coffee item's data
 * like starting price, name, status...
 */
public class Coffee 
{
    private String Name;
    private int startingPrice;
    private int sellingPrice;
    private String status;
    private ArrayList<AID> triedAuctioneers;

    /**
     * Initializes coffee with the name and maximum price
     * @param Name
     * @param startingPrice
     */
    public Coffee(String Name,int startingPrice) 
    {
        this.Name = Name;
        status = "NEW";
        this.startingPrice = startingPrice;
        triedAuctioneers = new ArrayList<>();
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
     * @return the triedAuctioneers
     */
    public ArrayList<AID> getTriedAuctioneers() {
        return triedAuctioneers;
    }

    /**
     * @param triedAuctioneer the triedAuctioneers to add
     */
    public void addTriedAuctioneer(AID triedAuctioneer) {
        this.triedAuctioneers.add(triedAuctioneer);
    }
}
