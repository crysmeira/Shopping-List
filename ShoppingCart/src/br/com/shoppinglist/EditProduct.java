package br.com.shoppinglist;

import java.util.ArrayList;

import br.com.shoppinglist.R;

import android.annotation.SuppressLint;
import android.content.ContentValues;
//import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity responsible for the operations with the products
 * 
 * @author Crystiane Meira
 * @version 1.0
 */
//@SuppressLint("DefaultLocale")
public class EditProduct extends ActionBarActivity {
	/* Variables to handle the database */
	private SQLiteDatabase database = null;
	private ArrayList<Cursor> listCursors = new ArrayList<Cursor>();

	/* The ids representing the product and list this class refers to */
	private int idProduct;
	private int idList;
	
	private int operation; // check if a product will be created or edited

	/* Fields to fill with the product information */
	private EditText etEditProduct;
	private EditText etEditQuantity;
	private EditText etEditPrice;
	private TextView etEditTotal;

	/**
	 * When creating the activity, open the database and
	 * load the main window
	 * 
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Set the color of the ActionBar */
		android.support.v7.app.ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ED4343")));
		
		openDB();

		Intent i = getIntent();
		operation = Integer.parseInt(i.getStringExtra("operation"));
		if (operation == 1) {
			/* if a product is being created, get the idList */
			idList = Integer.parseInt(i.getStringExtra("idList"));
		} else if (operation == 2) {
			/* only if it is an edition, the idProduct will be filled */
			idProduct = Integer.parseInt(i.getStringExtra("idProduct"));
		}

		/* set the title in the ActionBar */
		String nameList = i.getStringExtra("listName");
		setTitle(nameList);

		loadEditionWindow();
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
		inflater.inflate(R.menu.save, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/***
	 * Used for the items in the ActionBar
	 * 
	 * Instructions from http://developer.android.com
	 * 
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				if (etEditPrice.getText().toString().equals("")) {
					etEditPrice.setText("0.00");
				}
				String price = String.format("%.2f",
						Double.parseDouble(etEditPrice.getText().toString()));
				if (etEditQuantity.getText().toString().equals("")) {
					etEditQuantity.setText("0");
				}
				editRegister(etEditProduct.getText().toString(),
						etEditQuantity.getText().toString(), price);
				finish();
				return true;
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/***
	 * Load the window to edit the list
	 * 
	 */
	private void loadEditionWindow() {
		setContentView(R.layout.editproduct);

		etEditProduct = (EditText) findViewById(R.id.etEditProduct);
		etEditQuantity = (EditText) findViewById(R.id.etEditQuantity);
		etEditPrice = (EditText) findViewById(R.id.etEditPrice);
		etEditTotal = (TextView) findViewById(R.id.etEditTotal);

		if (operation == 1) { // create
			etEditQuantity.setText(Integer.toString(1));
		} else if (operation == 2) { // edit
			Product current = searchRegister();

			/* fill EditTexts with the information about the product */
			etEditProduct.setText(current.getName());
			etEditQuantity.setText(Integer.toString(current.getQuantity()));
			etEditPrice.setText(String.format("%.2f", current.getPrice()));

			if (!etEditPrice.getText().toString().equals("")
					&& !etEditQuantity.getText().toString().equals("")) {
				/* calculate, if it has price and quantity filled */
				etEditTotal.setText(calculateTotal());
			}
		}

		/* if the quantity of the product is changed, execute this */
		etEditQuantity.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!etEditPrice.getText().toString().equals("")
						&& !etEditQuantity.getText().toString().equals("")) {
					etEditTotal.setText(calculateTotal());
				} else {
					/* if quantity or price are not filled */
					etEditTotal.setText("0.00");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		/* if the price of the product is changed, execute this */
		etEditPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!etEditQuantity.getText().toString().equals("")
						&& !etEditPrice.getText().toString().equals("")
						&& !etEditPrice.getText().toString().equals(".")) {
					etEditTotal.setText(calculateTotal());
				} else {
					/* if quantity or price are not filled */
					etEditTotal.setText("0.00");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		Button btDeleteProduct = (Button) findViewById(R.id.btDeleteProduct);

		btDeleteProduct.setBackgroundColor(0xFFED4343);
		btDeleteProduct.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (operation == 2) { // if it is an edition, there is something to delete
					deleteRegister();
					finish();
				}
			}
		});
	}

	/***
	 * Open database
	 * 
	 */
	private void openDB() {
		try {
			database = openOrCreateDatabase("ShoppingList", MODE_PRIVATE, null);
		} catch (Exception e) {
			Toast.makeText(EditProduct.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Search for the content of the product in order to edit
	 * 
	 * @return Product columns of the product to edit
	 */
	private Product searchRegister() {
		try {
			String[] columns = { "name, quantity, price" };
			String where = "_id = " + idProduct;
			Cursor cursor = database.query("product", columns, where,
					null, null, null, null);

			listCursors.add(cursor);

			if (cursor != null) {
				cursor.moveToFirst();

				return new Product(cursor.getString(cursor
						.getColumnIndex("name")), cursor.getInt(cursor
						.getColumnIndex("quantity")), cursor.getDouble(cursor
						.getColumnIndex("price")));
			}
		} catch (Exception e) {
			Toast.makeText(EditProduct.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}

	/***
	 * Edit/Insert product in the database
	 * 
	 * @param name
	 *            name of the product
	 * @param quantity
	 *            quantity of the product
	 * @param price
	 *            price for unit of the product
	 * 
	 */
	private void editRegister(String name, String quantity, String price) {
		try {
			ContentValues cv = new ContentValues();
			cv.put("name", name);
			cv.put("quantity", quantity);
			cv.put("price", price);
			if (operation == 1) { // create
				cv.put("id_list", idList);
				database.insert("product", null, cv);
			} else if (operation == 2) { // edit
				String[] whereArgs = new String[] {String.valueOf(idProduct)};
				database.update("product", cv, "_id=?", whereArgs);
			}
		} catch (Exception e) {
			Toast.makeText(EditProduct.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Delete list from the database
	 * 
	 */
	private void deleteRegister() {
		try {
			String[] whereArgs = new String[] {String.valueOf(idProduct)};
			database.delete("product", "_id=?", whereArgs);
		} catch (Exception e) {
			Toast.makeText(EditProduct.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Calculate the total value of the purchase based on the quantity of products
	 * and their prices
	 * 
	 * @return String total value of the purchase (formatted to two decimal precision)
	 */
	@SuppressLint("DefaultLocale")
	private String calculateTotal() {
		int quantity = Integer.parseInt(etEditQuantity.getText()
				.toString());
		double price = Double.parseDouble(etEditPrice.getText().toString());
		double total = (quantity * price);

		return String.format("%.2f", total);
	}
}
