package pkg.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.trader.*;
import pkg.stock.*;
import pkg.market.api.*;

public class OrderBook{
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public HashMap<String, ArrayList<Order>> getBuyOrders() {
		return buyOrders;
	}

	public HashMap<String, ArrayList<Order>> getSellOrders() {
		return sellOrders;
	}

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		
		String traderName = order.getTrader().getName();
		
		if (order instanceof SellOrder) {
				if (sellOrders.containsKey(traderName))
				{//Update existing trader entry
					sellOrders.get(traderName).add(order);
				}else{//Create New
					ArrayList<Order> orderArray = new ArrayList<Order>();
					orderArray.add(order);
					this.sellOrders.put(traderName, orderArray);
				}
			}else{
				if (buyOrders.containsKey(traderName))
				{//Update existing trader entry
					buyOrders.get(traderName).add(order);
				}else{//Create New
					ArrayList<Order> orderArray = new ArrayList<Order>();
					orderArray.add(order);
					this.buyOrders.put(traderName, orderArray);
				}
			}
	}

	public void trade() {
		// Complete the trading.
		// 1. Follow and create the orderbook data representation (see spec)
		// 2. Find the matching price
		// 3. Update the stocks price in the market using the PriceSetter.
		// Note that PriceSetter follows the Observer pattern. Use the pattern.
		// 4. Remove the traded orders from the orderbook
		// 5. Delegate to trader that the trade has been made, so that the
		// trader's orders can be placed to his possession (a trader's position
		// is the stocks he owns)
		
		
		double matchPrice = createOrderBookRep(buyOrders, sellOrders);
		
		PriceSetter ps = new PriceSetter();
		ps.registerObserver(new IObserver(){

			@Override
			public void update() {
			
				
			}

			@Override
			public void setSubject(ISubject subject) {
				
				
			}
			
		});
		
		for (ArrayList<Order> ol : buyOrders.values()) {
			for(Order o: ol)
			{
				ps.setNewPrice(m, o.getStockSymbol(), matchPrice);
				Trader trader = o.getTrader();
					double finalMatchPrice = matchPrice * o.size;
					try {
						trader.tradePerformed(o, finalMatchPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
			}			
		}

		for (ArrayList<Order> ol : sellOrders.values()) {
			for(Order o: ol)
			{
				Trader trader = o.getTrader();
					double finalMatchPrice = matchPrice * o.size;
					try {
						trader.tradePerformed(o, finalMatchPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
			}			
		}		
		
	}
	
	public double createOrderBookRep(HashMap<String, ArrayList<Order>> bl, HashMap<String, ArrayList<Order>> sl){
		double matchingPrice = 0;
		
		ArrayList<Order> sortedBuyOrderBook = new ArrayList<Order>();
		ArrayList<Order> sortedSellOrderBook = new ArrayList<Order>();
		
/////////Sort Both Buy and Sell OrderBooks////
			for (Map.Entry<String, ArrayList<Order>> entry : bl.entrySet())
			{
				for (Order o:entry.getValue()){
					sortedBuyOrderBook.add(o);
				}
			}
			
			for (Map.Entry<String, ArrayList<Order>> entry : sl.entrySet())
			{
				for (Order o:entry.getValue()){
					sortedSellOrderBook.add(o);
				}			
			}		
			
		
			Collections.sort(sortedBuyOrderBook, new Comparator<Order>(){
				
				@Override
				public int compare(Order o1, Order o2){
					double price1 = o1.getPrice();
					double price2 = o2.getPrice();
					return (int) (price2 - price1);
				}
			});
			
			Collections.sort(sortedSellOrderBook, new Comparator<Order>(){
				
				@Override
				public int compare(Order o1, Order o2){
					double price1 = o1.getPrice();
				double price2 = o2.getPrice();
				return (int) (price1 - price2);
				}
		});


		return getMatchingPrice(sortedBuyOrderBook, sortedSellOrderBook);
	}

	private double getMatchingPrice(ArrayList<Order> sortedBuyOrderBook,
			ArrayList<Order> sortedSellOrderBook) {
		double matchingPrice;
		////////Calculating cumulative prices//////////
				int obSize;
				if (sortedBuyOrderBook.size() > sortedSellOrderBook.size())
				{
					obSize = sortedBuyOrderBook.size(); 
				}else{
					obSize = sortedSellOrderBook.size();
				}
				int[] cumulativeSell = new int[obSize];
				int[] cumulativeBuy = new int[obSize];
				int[] totalTrades = new int[obSize];
				double[] tradePrices = new double[obSize];
				
				int individualBuy = 0;
				int individualSell = 0;
				
				for (int i = 0; i < obSize; i++)
				{
					Order so;
					Order bo;
		
					if (i < sortedSellOrderBook.size()) {
						so = sortedSellOrderBook.get(i);
						individualSell = so.getSize();
						cumulativeSell[i] = (i > 0) ?  cumulativeSell[i-1] + individualSell : individualSell;
					}
					
					tradePrices[i] = sortedSellOrderBook.get(i).getPrice();
					
					if (i < sortedBuyOrderBook.size()) {
						bo = sortedBuyOrderBook.get(i);
						individualBuy = bo.getSize();
						cumulativeBuy[i] = (i > 0) ?  cumulativeBuy[i-1] + individualBuy : individualBuy;
					}
		
				}
		
			
		////////Calculating bigger demand//////////////
				for (int i = 0; i < obSize; i++){
					totalTrades[i] = cumulativeBuy[i] + cumulativeSell[i];
				}
				
				matchingPrice = tradePrices[0];
				for (int i = 0; i < totalTrades.length; i++) {
					if (totalTrades[i] > matchingPrice) {
						matchingPrice = tradePrices[i];
					}
				}		
				
		
				 return matchingPrice;
	}
}
