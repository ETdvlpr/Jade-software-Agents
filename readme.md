# English auction agents in jade
This project is a PoC on building cooperative auction agents with jade. The rules to the auction are as follows.
It proceeds as follows.

1. The auctioneer opens the auction by announcing a Suggested Opening Bid or starting price for the item on sale.
2. The auctioneer accepts increasingly higher bids from the floor, consisting of buyers with an interest in the item. In this implementation the auctioneer determines the minimum increment of bids, raising it as bidding goes on longer.
3. The highest bidder at any given moment is considered to have the standing bid, which can only be displaced by a higher bid from a competing buyer.
4. If no competing bidder challenges the standing bid within a given time frame, the standing bid becomes the winning bid, and the item is sold.
5. If no bidder makes a bid on an item the auctioneer lets the buyer know that there is no interest at which point seller can choose to change auctioneers or lower price.

The system also allows bidding in multiple auctions simultaneously.

# System design
This system is created using java and the JADE framework. It consists of three kinds of agents that communicate; The Auctioneer, buyers and sellers. Below is a description of these actors and the functionalities and features they possess.

### Auctioneer
The auctioneer connects buyers and sellers, and keeps track of the auction details. Its process is as
follows:
i. Registers a list of buyers
ii. Gets sell request from seller with coffee name and starting price
iii. Broadcasts Item and price to prospective buyers
iv. Records the first bid or the highest bid as the best bid
v. Increments bid and checks for any buyers at new price
vi. If no buyers can outbid the best offer then auctioneer accepts the buyers’ price and lets the seller
know
vii. If no buyer wants the coffee auctioneer sends FAIL message to seller

Features included by auctioneer
a) Capable of running multiple bids at the same time
b) The increment grows larger as bidding goes on longer

![Flow chart for auctioneer logic](flow%20chart%20for%20auctioneer.png)

### Buyer
By definition multiple instances of buyers are supported. They have maximum bid capacities for bidding on the individual items. The functionality of buyers commences as follows:
i. Get a list of auctioneers from DF
ii. Register with auctioneer as a buyer
iii. Regularly check for new auctioneers
iv. Maintain a list of coffee items from user with associated bid limit
v. Bid on items(up to limit) as if user was doing it
Features included by buyer
a) Ability to bid on multiple items from multiple auctions simultaneously
b) Maintain only one standing bid for an item
c) Update user interface to keep user informed

![Flow chart for buyer](flow%20chart%20for%20buyer.png)

### Seller
The seller is an agent providing a conduit for the user to put up coffee items for sale. It communicates with auctioneers on the users behalf and updates its UI to inform the user of the current state of affairs.It sequentially sends sell information to all auctioneers till coffee is sold, or it runs out of auctioneers.

![Flow chart of Seller](flow%20chart%20for%20seller.png)

## Screen shot
![Screen shot of a bid in action](screen%20shot%20of%20system%20in%20action.jpg)

##### TODO
* Security
* intelligence / reasoning
