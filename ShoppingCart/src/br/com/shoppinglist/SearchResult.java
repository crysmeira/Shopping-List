package br.com.shoppinglist;

/**
 * Keep the information about a search result
 * 
 * @author Crystiane Meira
 * @version 1.0
 * 
 */
public class SearchResult {
	private int id;
	private String name;

	public SearchResult(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
