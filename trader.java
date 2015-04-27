package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.*;
import pkg.stock.Stock;

public class Trader {
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;
	
	public String getName()
	{
		return this.name;
	}
	
	public Trader(String name, double cashInHand) {
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market mMarket, String symbol, int volume)
			throws StockMarketExpection {
		
		Stock stock = mMarket.getStockForSymbol(symbol);
		
		if (stock.getPrice()*volume > this.cashInHand){
			throw new StockMarketExpection("The stock's price is larger than the cash possessed");
		}else{
			BuyOrder newBuyOrder = new BuyOrder(symbol, volume, stock.getPrice(), this);
			this.cashInHand -= stock.getPrice()*volume;
		}
	}

	public void placeNewOrder(Market market, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
			if (price > this.cashInHand){
				throw new StockMarketExpection("Cannot place order for stock: SBUX since there is not enough money. Trader: " + this.name);
			}
			
	        for (Order order : ordersPlaced) {
	            if (order.getStockSymbol() == symbol){
	            	throw new StockMarketExpection("A trader cannot place two orders for the same stock");
	            }
	            	
	        }
			
			switch (orderType)
			{
			case BUY:
				BuyOrder buyOrder = new BuyOrder(symbol, volume, price, this);
				ordersPlaced.add(buyOrder);
				m.addOrder(buyOrder);
				break;
			case SELL:
				SellOrder sellOrder = new SellOrder(symbol, volume, price, this);
				m.addOrder(sellOrder);
				ordersPlaced.add(sellOrder);
				break;
			}			
	}
//method manages placing a new market order
	public void placeNewMarketOrder(Market market, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
			if (price > this.cashInHand){
				throw new StockMarketExpection("Cannot place order for stock:" + symbol  + "since there is not enough money. Trader: " + this.name);
			}
			
	        for (Order order : ordersPlaced) {
	            if (order.getStockSymbol() == symbol){
	            	throw new StockMarketExpection("A trader cannot place two orders for the same stock");
	            }
	            	
	        }
			
			switch (orderType)
			{
			case BUY:
				BuyOrder buyOrder = new BuyOrder(symbol, volume, true, this);
				ordersPlaced.add(buyOrder);
				m.addOrder(buyOrder);
				break;
			case SELL:
				SellOrder sellOrder = new SellOrder(symbol, volume, true, this);
				m.addOrder(sellOrder);
				ordersPlaced.add(sellOrder);
				break;
			}			
	}

	public void tradePerformed(Order anOrder, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		
		if (anOrder instanceof BuyOrder)
		{
			if (matchPrice < this.cashInHand)
			{
				this.cashInHand -= matchPrice;
				this.position.add(o);
			}else{
				throw new StockMarketExpection("Cannot perform order for stock:" + anOrder.getStockSymbol() + ", since there is not enough money. Trader: " + this.name);
			}
			
		}
		if (anOrder instanceof SellOrder)
		{
			this.cashInHand += matchPrice;
			if (this.position.contains(o)){
				this.position.remove(this.position.indexOf(o));
			}
			
		}
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order anOrder : position) {
			anOrder.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order anOrder : ordersPlaced) {
			anOrder.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
