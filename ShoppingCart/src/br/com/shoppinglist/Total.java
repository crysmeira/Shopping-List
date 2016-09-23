package br.com.shoppinglist;

/**
 * Keep the total quantity of products and the total price of the purchase
 * 
 * @author Crystiane Meira
 * @version 1.0
 * 
 */
public class Total {
	private int totalQuantity;
	private double totalPrice;

	public Total(int quantity, double price) {
		this.totalQuantity = quantity;
		this.totalPrice = price;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

}
