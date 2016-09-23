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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Activity responsible for creating an initial window filled
 * with the existing lists
 * 
 * @author Crystiane Meira
 * @version 1.0
 * @see EditList
 * @see ProductList
 */
public class ShoppingListMenu extends ActionBarActivity {
	/* Variables to handle the database */
	private SQLiteDatabase database = null;
	private SimpleCursorAdapter cursorAdapter;
	private ArrayList<Cursor> listCursors = new ArrayList<Cursor>();

	private ListView lvList; // ListView that is going to have the information from the existing lists

	/**
	 * After creating the activity, create or open the database
	 * and load the main window
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Set the color of the ActionBar */
		android.support.v7.app.ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ED4343")));
				
		createOrOpenDB();

		loadMainWindow();
	}

	/**
	 * When returning to this activity, the list must be updated
	 * 
	 */
	public void onRestart() {
		super.onRestart();

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
	 * Used for the items in ActionBar
	 * 
	 * Instructions from http://developer.android.com
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Inflate the menu items for use in the action bar */
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/***
	 * Used for the items in ActionBar
	 * 
	 * Instructions from http://developer.android.com
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* Handle presses on the action bar items */
		switch (item.getItemId()) {
			case R.id.new1:
				Intent myIntent = new Intent(ShoppingListMenu.this, EditList.class);
				myIntent.putExtra("operation", "1"); // create new list
				startActivity(myIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/***
	 * Load the main window and prepare for the actions that may occur
	 * (OnItemLongClickListener e OnItemClickListener)
	 * 
	 * OnItemLongClickListener: list edition
	 * 
	 * OnItemClickListener: access to the items in list
	 * 
	 */
	private void loadMainWindow() {
		setContentView(R.layout.shoppinglist);

		int qtyLists = loadList();

		if (qtyLists > 0) {
			lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					/* position - 1, due to the insertion of the header */
					Object o = cursorAdapter.getItemId(position - 1);
					String idList = o.toString();
					Intent myIntent = new Intent(ShoppingListMenu.this, EditList.class);
					myIntent.putExtra("idList", idList);
					myIntent.putExtra("operation", "2"); // list edition
					startActivity(myIntent);

					return true;
				}
			});

			lvList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					/* position - 1, due to the insertion of the header */
					Object o = cursorAdapter.getItemId(position - 1);
					String idList = o.toString();
					String name = fetchName(Integer.parseInt(idList));
					Intent myIntent = new Intent(ShoppingListMenu.this,
							ProductList.class);
					myIntent.putExtra("idList", idList);
					/* give the name of the list to place in the action bar */
					myIntent.putExtra("listName", name);
					startActivity(myIntent);
				}
			});
		}
	}

	/***
	 * Fill the ListView lvList with the name and information from
	 * the existing lists
	 * 
	 * @return int quantity of existing lists in the database
	 */
	private int loadList() {
		final String[] from = { "name", "qtyProducts", "totalProducts", "_id" };

		final int[] to = { R.id.etListList, R.id.etQtyProducts,
				R.id.etTotalProducts };

		/* the information will be in the order that it was placed in from */
		Cursor lists = database.query("list", from, null, null, null,
				null, null);

		listCursors.add(lists);

		/* verify if it has any register */
		if (lists.getCount() > 0) {
			cursorAdapter = new SimpleCursorAdapter(ShoppingListMenu.this,
					R.layout.listitems, lists, from, to, 0);

			lvList = (ListView) findViewById(R.id.lvList);

			/* insert the header in the list */
			LayoutInflater inflater = getLayoutInflater();
			ViewGroup header = (ViewGroup) inflater.inflate(
					R.layout.shoppinglist_header, lvList, false);
			lvList.addHeaderView(header, null, false);

			/* fill the list with the registers */
			lvList.setAdapter(cursorAdapter);

			return lists.getCount();
		}
		return 0;
	}

	/***
	 * Create or open the database
	 * 
	 */
	private void createOrOpenDB() {
		try {
			database = openOrCreateDatabase("ShoppingList", MODE_PRIVATE, null);
			String sql = "CREATE TABLE IF NOT EXISTS list(_id INTEGER PRIMARY KEY, name TEXT NOT NULL, qtyProducts TEXT, totalProducts TEXT);";
			database.execSQL(sql);
		} catch (Exception e) {
			Toast.makeText(ShoppingListMenu.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * Search for the name of a list according to its id
	 * 
	 * @return String name of a list
	 */
	private String fetchName(int id) {
		try {
			String[] column = { "name" };
			String where = "_id = " + id;
			Cursor cursor = database.query("list", column, where, null,
					null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();

				String s = cursor.getString(cursor.getColumnIndex("name"));
				cursor.close();
				return s;
			}
		} catch (Exception e) {
			Toast.makeText(ShoppingListMenu.this, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return null;
	}
}
