package br.com.shoppinglist;

import java.util.ArrayList;

import br.com.shoppinglist.R;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity responsible for filling the window with the products of a specific
 * list
 * 
 * @author Crystiane Meira
 * @version 1.0
 * @see EditProduct
 */
public class ProductList extends ActionBarActivity {
	/* Variables to handle the database */
	private SQLiteDatabase database = null;
	private Cursor products;
	private SimpleCursorAdapter cursorAdapter;
	private ArrayList<Cursor> listCursors = new ArrayList<Cursor>();

	private EditText etSearchProduct; // field used to search for a specific product
	private ListView lvList; // ListView that is going to have the information from the existing lists

	/* The information representing the list this class refers to */
	private int idList;
	private String listName;

	/**
	 * When creating the activity, open the database and load
	 * the main window
	 * 
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Set the color of the ActionBar */
		android.support.v7.app.ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ED4343")));
		
		openDB();

		Intent i = getIntent();
		idList = Integer.parseInt(i.getStringExtra("idList"));

		/* set title of the ActionBar */
		listName = i.getStringExtra("listName");
		setTitle(listName);

		loadMainWindow();
	}

	/**
	 * When returning to this activity, the list must be updated
	 * 
	 */
	public void onRestart() {
		super.onRestart();

		updateListData();

		loadMainWindow();
	}

	/**
	 * Close the cursors and the database
	 * 
	 */
	public void onDestroy() {
		super.onDestroy();

		for (int i = 0; i < listCursors.size(); i++) {
			listCursors.get(i).close();
		}

		database.close();
	}

	/***
	 * Used for the items in the ActionBar
	 * 
	 * Instructions from http://developer.android.com
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/***
	 * Used for the items in the ActionBar
	 * 
	 * Instructions from http://developer.android.com
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new1:
				Intent myIntent = new Intent(ProductList.this, EditProduct.class);
				myIntent.putExtra("idList", Integer.toString(idList));
				myIntent.putExtra("listName", listName);
				myIntent.putExtra("operation", "1");
				startActivity(myIntent);
				return true;
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
	}

	/***
	 * Load the main window and prepare for the actions that may occur,
	 * OnItemClickListener and TextChangedListener, the last one associated 
	 * with the search
	 * 
	 */
	private void loadMainWindow() {
		setContentView(R.layout.listlists);

		/* EditText related to the search */
		etSearchProduct = (EditText) findViewById(R.id.etSearchProduct);

		etSearchProduct.addTextChangedListener(new TextWatcher() {
			/* Every time the text in the search bar changes, update
			 * the list according to what is being searched
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				loadProducts();
			}
		});

		int qtyProducts = loadProducts();

		/* verify if there are products in the current list */
		if (qtyProducts > 0) {
			/* if an item was clicked, go to the edition window */
			lvList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					String idProduct = String.valueOf(id);
					Intent myIntent = new Intent(ProductList.this,
							EditProduct.class);
					myIntent.putExtra("idProduct", idProduct);
					myIntent.putExtra("listName", listName);
					myIntent.putExtra("operation", "2"); // edition
					startActivity(myIntent);
				}
			});
		}
	}

	/***
	 * Fill the ListView with the name and information of the existing
	 * products
	 * 
	 * @return int quantity of existing products in the database
	 */
	private int loadProducts() {
		final String[] from = { "name", "quantity", "price", "id_list",
				"_id" };
		final int[] to = { R.id.etListProduct, R.id.etListQuantity,
				R.id.etListPrice };

		products = updateCursorFromSearchProduct();

		/* verify if it has a register */
		if (products.getCount() > 0) {
			cursorAdapter = new SimpleCursorAdapter(ProductList.this,
					R.layout.listproducts, products, from, to, 0);

			lvList = (ListView) findViewById(R.id.lvList2);

			/* fill the list with the registers */
			lvList.setAdapter(cursorAdapter);

			/* data for the footer */
			Total est = calculateData();

			TextView etListSum = (TextView) findViewById(R.id.etListSum);
			etListSum.setText(String.format("%.2f", est.getTotalPrice()));

			TextView etListTotalQuantity = (TextView) findViewById(R.id.etListTotalQuantity);
			etListTotalQuantity.setText(Integer.toString(est
					.getTotalQuantity()));

			/* add a footer to the ListView */
			LinearLayout footerList = (LinearLayout) findViewById(R.id.footerList);

			footerList.setVisibility(0);
			
			/* if it is searching for a specific product, do not show the footer
			 * 
			 */
			if (!etSearchProduct.getText().toString().equals("")) {
				footerList.setVisibility(4);
			}
			return products.getCount();
		}
		return 0;
	}

	/***
	 * Select the products that must be presented in the window based on the
	 * search product condition
	 * 
	 * @return Cursor products that must be presented in the window
	 */
	private Cursor updateCursorFromSearchProduct() {
		int textlength = etSearchProduct.getText().length();

		ArrayList<SearchResult> productsList = new ArrayList<SearchResult>();
		productsList = searchInfoProducts();

		StringBuilder ids = new StringBuilder(); // ids of the products

		ids.append("(");
		int countOccurrences = 0;
		for (int i = 0; i < productsList.size(); i++) {
			if (textlength <= productsList.get(i).getName().length()) {
				if (etSearchProduct
						.getText()
						.toString()
						.equalsIgnoreCase(
								(String) productsList.get(i).getName().substring(0, textlength))) {
					countOccurrences++;
					if (countOccurrences > 1) {
						ids.append(", ");
					}
					ids.append(productsList.get(i).getId());
				}
			}
		}
		ids.append(")");
		String selection_where = "_id in " + ids.toString();

		String[] from = { "name", "quantity", "price", "id_list", "_id" };

		/* the information will be displayed in the order that they were added in from */
		products = database.query("product", from, selection_where, null,
				null, null, null);

		listCursors.add(products);

		return products;
	}

	/**
	 * Update footer, that shows the information about the total quantity of items and
	 * the sum of all the items in the list
	 * 
	 */
	private void updateListData() {
		Total total = calculateData();

		String sql = "UPDATE list SET qtyProducts = '"
				+ total.getTotalQuantity() + "', totalProducts = '"
				+ String.format("%.2f", total.getTotalPrice())
				+ "' where _id = '" + idList + "' ;";

		database.execSQL(sql);
	}

	/***
	 * Calculate the total value and the quantity of item in the list
	 * 
	 * @return Total total value and quantity of item in the list
	 */
	private Total calculateData() {
		products = updateCursorFromSearchProduct();
		products.moveToFirst();

		int totalQuantity = 0;
		double total = 0;

		for (int i = 0; i < products.getCount(); i++) {
			int quantity = products.getInt(products
					.getColumnIndex("quantity"));
			double price = products.getDouble(products.getColumnIndex("price"));
			total += (quantity * price);
			totalQuantity += quantity;

			products.moveToNext();
		}

		Total t = new Total(totalQuantity, total);
		return t;
	}

	/***
	 * Open database
	 * 
	 */
	private void openDB() {
		try {
			database = openOrCreateDatabase("ShoppingList", MODE_PRIVATE, null);
			String sql = "CREATE TABLE IF NOT EXISTS product(_id INTEGER PRIMARY KEY, name TEXT NOT NULL, quantity TEXT, price TEXT, id_list INTEGER, FOREIGN KEY(id_list) REFERENCES list(_id));";
			database.execSQL(sql);
		} catch (Exception e) {
			Toast.makeText(ProductList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Search for the ids and names of the products to fill the list
	 * 
	 * @return ArrayList ids and names of the products related to the current list
	 */
	private ArrayList<SearchResult> searchInfoProducts() {
		ArrayList<SearchResult> products = new ArrayList<SearchResult>();

		try {
			String[] columns = { "name", "_id" };
			String where = "id_list = " + idList;
			Cursor cursor = database.query("product", columns, where,
					null, null, null, null);

			listCursors.add(cursor);

			if (cursor != null) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					products.add(new SearchResult(cursor.getInt(cursor
							.getColumnIndex("_id")), cursor.getString(cursor
							.getColumnIndex("name"))));
					cursor.moveToNext();
				}
			}
			return products;
		} catch (Exception e) {
			Toast.makeText(ProductList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}
}