package br.com.shoppinglist;

import java.util.ArrayList;

import br.com.shoppinglist.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity responsible for the operations realized in the lists
 * 
 * @author Crystiane Meira
 * @version 1.0
 */
public class EditList extends ActionBarActivity {
	/* Variables to handle the database */
	private SQLiteDatabase database = null;
	private ArrayList<Cursor> listCursors = new ArrayList<Cursor>();

	private int idList; // id representing the list this class refers to
	
	private int operation; // check if a list will be created or edited

	private EditText etEditList; // field to fill with the name of the list

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
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		openDB();

		Intent i = getIntent();
		operation = Integer.parseInt(i.getStringExtra("operation"));
		if (operation == 2) { // only when editing it is going to have an idList
			idList = Integer.parseInt(i.getStringExtra("idList"));
		}

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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				editRegister(etEditList.getText().toString());
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
		setContentView(R.layout.editlist);

		etEditList = (EditText) findViewById(R.id.etEditList);

		if (operation == 2) { // edition
			String currName = searchRegister();
			etEditList.setText(currName);
		}

		Button btDeleteList = (Button) findViewById(R.id.btDeleteList);
		btDeleteList.setBackgroundColor(0xFFED4343);

		/* if it is creating a list, do nothing */
		btDeleteList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (operation == 2) { // edition
					deleteRegister();
					finish();
				}
			}
		});
	}

	/***
	 * Open the database
	 * 
	 */
	private void openDB() {
		try {
			database = openOrCreateDatabase("ShoppingList", MODE_PRIVATE, null);
		} catch (Exception e) {
			Toast.makeText(EditList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Search the name of the list to fill in the edition window
	 * 
	 * @return String name of the list
	 */
	private String searchRegister() {
		try {
			String[] columns = { "name" };
			String where = "_id = " + idList;
			Cursor cursor = database.query("list", columns, where, null,
					null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				String answer = cursor.getString(cursor
						.getColumnIndex("name"));
				cursor.close();
				return answer;
			}
		} catch (Exception e) {
			Toast.makeText(EditList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}

	/***
	 * Edit/Insert the name of the list in the database
	 * 
	 * @param name
	 *            name of the list
	 */
	private void editRegister(String name) {
		try {
			ContentValues cv = new ContentValues();
			cv.put("name", name);
			if (operation == 1) {
				cv.put("qtyProducts", 0);
				cv.put("totalProducts", 0.0);
				database.insert("list", null, cv);
			} else if (operation == 2) {
				String[] whereArgs = new String[] {String.valueOf(idList)};
				database.update("list", cv, "_id=?", whereArgs);
			}
		} catch (Exception e) {
			Toast.makeText(EditList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Delete the list from the database
	 * 
	 * Cascade effect
	 * Delete all the products that are related to this list
	 * 
	 */
	private void deleteRegister() {
		try {
			String[] whereArgs = new String[] {String.valueOf(idList)};
			database.delete("list", "_id=?", whereArgs);
			whereArgs = new String[] {String.valueOf(idList)};
			database.delete("product", "id_list=?", whereArgs);
		} catch (Exception e) {
			Toast.makeText(EditList.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}
}
