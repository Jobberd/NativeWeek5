package com.example.job94_000.lists_v2;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The MainActivity runs a program that can be used to maintain todolists.
 * It is possible to have multiple lists at the same time and you can switch between them using
 * a navigationdrawer.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Create global variables
     */
    Hashtable<String, ArrayList<String>> allLists = new Hashtable<>();
    Enumeration listNames;
    ArrayList<String> currentList = new ArrayList<>();
    String currentListName;
    ListView indexListView;
    EditText inputText;
    TextView titleText;
    Button addButton;
    ArrayAdapter<String> myIndexAdapter;
    NavigationView navigationView;
    Menu navigationMenu;
    SubMenu navSubMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Try to get data from "AllLists" and if this is not possible, create an example
        try {
            FileInputStream fis = openFileInput("AllLists");
            ObjectInputStream ois = new ObjectInputStream(fis);
            allLists = (Hashtable<String, ArrayList<String>>) ois.readObject();
            ois.close();
            listNames = allLists.keys();
            if (savedInstanceState != null) {
                currentListName = savedInstanceState.getString("listname");
            } else {
                currentListName = (String) listNames.nextElement();
            }
            currentList = allLists.get(currentListName);
        } catch (Exception e) {
            ArrayList<String> newList = new ArrayList<String>();
            newList.add("This is an example:"); newList.add("Get groceries");
            newList.add("Call my grandmother"); newList.add("Finish homework");
            currentListName = "Example list";
            allLists.put(currentListName, newList);
            currentList = newList;
        }

        // Make the drawer toggle by using the actionbar button
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Set title name
        titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText(currentListName);
        titleText.setLongClickable(true);

        // Finding the ListView and adding an ArrayAdapter for the items in the list
        indexListView = (ListView) findViewById(R.id.indexList);
        indexListView.setLongClickable(true);
        setListViewAdapter();

        // Find the EditText and Button to add items to the list with the String put in inputText
        inputText = (EditText) findViewById(R.id.inputText);
        addButton = (Button) findViewById(R.id.addButton);
        setAddButtonOnClick();

        // Create the navigationview and add the data to the menu
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationMenu = navigationView.getMenu();
        navSubMenu = navigationMenu.addSubMenu("Your To-Do Lists:");
        addNavigationItems();
    }

    /**
     * onSaveInstanceState() saves the state of the activity when it gets terminated.
     * It also writes a file with the items of the list stored, so it can be reaccessed later.
     * @param outState defines the state of the program
     */
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save currentlist name
        outState.putString("listname", currentListName);

        // write to a file
        try {
            FileOutputStream fos = openFileOutput("AllLists", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allLists);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the listviewadapter to the current list
     */
    private void setListViewAdapter() {
        myIndexAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currentList);
        indexListView.setAdapter(myIndexAdapter);
        setListItemLongClick();
        setTitleLongClick();
    }

    /**
     * On longclicking the title TextView, the user can remove the current list. It the searches for
     * the right menu item, removes it and removes the list from the hashtable. It creates an
     * empty list when the last list is removed to prevent crashing
     */
    private void setTitleLongClick() {
        titleText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                allLists.remove(currentListName);
                for (int i = 0; i < navSubMenu.size(); i++) {
                    String title = (String) navSubMenu.getItem(i).getTitle();
                    if (currentListName.equals(title)) {
                        navSubMenu.removeItem(i);
                    }
                }
                listNames = allLists.keys();
                if (listNames.hasMoreElements()) {
                    currentListName = (String) listNames.nextElement();
                    currentList = allLists.get(currentListName);
                } else {
                    currentListName = "Empty list";
                    currentList = new ArrayList<String>();
                    allLists.put(currentListName, currentList);
                }
                titleText.setText(currentListName);
                setListViewAdapter();
                addNavigationItems();
                return false;
            }
        });
    }

    /**
     * Set the add button to add the text from the inputtext to the current todolist
     */
    private void setAddButtonOnClick() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentList.add(inputText.getText().toString());
                inputText.setText("");
                myIndexAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Make the user be able to remove items from a list by long clicking them
     */
    private void setListItemLongClick() {
        indexListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currentList.remove(position);
                myIndexAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    /**
     * Make the drawer menu open on using the button in the actionbar
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Add the lists from the data to the navigation menu
     */
    private void addNavigationItems() {
        navSubMenu.clear();
        listNames = allLists.keys();
        while (listNames.hasMoreElements()) {
            String list = (String) listNames.nextElement();
            navSubMenu.add(list);
        }
        navigationView.setNavigationItemSelectedListener(this);
        editMenu();
        myIndexAdapter.notifyDataSetChanged();
    }

    /**
     * Set the onclick listeners for the menu items
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String list = (String) item.getTitle();

        if (list.equals("Add new list")) {
            promptUser();
        } else {
            currentListName = list;
            currentList = allLists.get(list);
            titleText.setText(list);
            setListViewAdapter();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Ask for user input when they try to add a new list by using an AlertDialog
     */
    private void promptUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name your list:");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the positivebutton, creating a new menu item and refreshing all adapters when selected
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentListName = input.getText().toString();
                currentList = new ArrayList<String>();
                allLists.put(currentListName, currentList);
                titleText.setText(currentListName);
                setListViewAdapter();
                navSubMenu.add(currentListName);
                editMenu();
            }
        });

        // Set up the negativebutton
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Stolen from the internet to update my navigationView during runtime
     */
    private void editMenu() {
        for (int i = 0, count = navigationView.getChildCount(); i < count; i++) {
            final View child = navigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }
}
