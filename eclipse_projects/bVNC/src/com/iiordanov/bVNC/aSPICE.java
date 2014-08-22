/**
 * Copyright (C) 2012 Iordan Iordanov
 * 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

package com.iiordanov.bVNC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * aSPICE is the Activity for setting up SPICE connections.
 */
public class aSPICE extends Activity implements MainConfiguration {
    private final static String TAG = "aSPICE";
    private LinearLayout layoutAdvancedSettings;
    private EditText ipText;
    private EditText portText;
    private EditText passwordText;
    private Button goButton;
    private ToggleButton toggleAdvancedSettings;
    private Spinner spinnerConnection;
    private Database database;
    private ConnectionBean selected;
    private EditText textNickname;
    private CheckBox checkboxKeepPassword;
    private CheckBox checkboxUseDpadAsArrows;
    private CheckBox checkboxRotateDpad;
    private CheckBox checkboxLocalCursor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        System.gc();
        setContentView(R.layout.main_spice);

        ipText = (EditText) findViewById(R.id.textIP);
        portText = (EditText) findViewById(R.id.textPORT);
        passwordText = (EditText) findViewById(R.id.textPASSWORD);
        textNickname = (EditText) findViewById(R.id.textNickname);

        checkboxKeepPassword = (CheckBox) findViewById(R.id.checkboxKeepPassword);
        checkboxUseDpadAsArrows = (CheckBox) findViewById(R.id.checkboxUseDpadAsArrows);
        checkboxRotateDpad = (CheckBox) findViewById(R.id.checkboxRotateDpad);
        checkboxLocalCursor = (CheckBox) findViewById(R.id.checkboxUseLocalCursor);
        
        spinnerConnection = (Spinner) findViewById(R.id.spinnerConnection);
        spinnerConnection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> ad, View view,
                            int itemIndex, long id) {
                        selected = (ConnectionBean) ad.getSelectedItem();
                        updateViewFromSelected();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> ad) {
                        selected = null;
                    }
                });
        
        goButton = (Button) findViewById(R.id.buttonGO);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ipText.getText().length() != 0
                        && portText.getText().length() != 0)
                    canvasStart();
                else
                    Toast.makeText(view.getContext(),
                            R.string.spice_server_empty, Toast.LENGTH_LONG)
                            .show();
            }
        });

        // The advanced settings button.
        toggleAdvancedSettings = (ToggleButton) findViewById(R.id.toggleAdvancedSettings);
        layoutAdvancedSettings = (LinearLayout) findViewById(R.id.layoutAdvancedSettings);
        toggleAdvancedSettings.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0,
                            boolean checked) {
                        if (checked)
                            layoutAdvancedSettings.setVisibility(View.VISIBLE);
                        else
                            layoutAdvancedSettings.setVisibility(View.GONE);
                    }
                });

        database = new Database(this);
    }

    protected void onDestroy() {
        database.close();
        System.gc();
        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case R.id.itemMainScreenHelp:
            return createHelpDialog();
        }
        return null;
    }

    /**
     * Creates the help dialog for this activity.
     */
    private Dialog createHelpDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this).setMessage(
                R.string.spice_main_screen_help_text).setPositiveButton(
                R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // We don't have to do anything.
                    }
                });
        Dialog d = adb.setView(new ListView(this)).create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        d.show();
        d.getWindow().setAttributes(lp);
        return d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.androidvncmenu, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            menu.findItem(R.id.itemDeleteConnection).setEnabled(selected!=null && ! selected.isNew());
            menu.findItem(R.id.itemSaveAsCopy).setEnabled(selected!=null && ! selected.isNew());
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.itemSaveAsCopy:
            if (selected.getNickname()
                    .equals(textNickname.getText().toString()))
                textNickname.setText("Copy of " + selected.getNickname());
            updateSelectedFromView();
            selected.set_Id(0);
            saveAndWriteRecent();
            arriveOnPage();
            break;
        case R.id.itemDeleteConnection:
            Utils.showYesNoPrompt(this, "Delete?",
                    "Delete " + selected.getNickname() + "?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            selected.Gen_delete(database.getWritableDatabase());
                            database.close();
                            arriveOnPage();
                        }
                    }, null);
            break;
        case R.id.itemMainScreenHelp:
            showDialog(R.id.itemMainScreenHelp);
            break;
        // Disabling Manual/Wiki Menu item as the original does not correspond
        // to this project anymore.
        // case R.id.itemOpenDoc :
        // Utils.showDocumentation(this);
        // break;
        }
        return true;
    }

    public void updateViewFromSelected() {
        if (selected == null)
            return;

        ipText.setText(selected.getAddress());

        if (selected.getPort() < 0) {
            portText.setText("");
        } else {
            portText.setText(Integer.toString(selected.getPort()));
        }

        if (selected.getKeepPassword() || selected.getPassword().length() > 0) {
            passwordText.setText(selected.getPassword());
        }

        checkboxKeepPassword.setChecked(selected.getKeepPassword());
        checkboxUseDpadAsArrows.setChecked(selected.getUseDpadAsArrows());
        checkboxRotateDpad.setChecked(selected.getRotateDpad());
        checkboxLocalCursor.setChecked(selected.getUseLocalCursor());
        textNickname.setText(selected.getNickname());
    }

    /**
     * Returns the current ConnectionBean.
     */
    public ConnectionBean getCurrentConnection() {
        return selected;
    }

    /**
     * Returns the display height, or if the device has software buttons, the
     * 'bottom' of the view (in order to take into account the software buttons.
     * 
     * @return the height in pixels.
     */
    public int getHeight() {
        View v = getWindow().getDecorView().findViewById(android.R.id.content);
        Display d = getWindowManager().getDefaultDisplay();
        int bottom = v.getBottom();
        int height = d.getHeight();

        if (android.os.Build.VERSION.SDK_INT >= 14) {
            android.view.ViewConfiguration vc = ViewConfiguration.get(this);
            if (vc.hasPermanentMenuKey())
                return bottom;
        }
        return height;
    }

    /**
     * Returns the display width, or if the device has software buttons, the
     * 'right' of the view (in order to take into account the software buttons.
     * 
     * @return the width in pixels.
     */
    public int getWidth() {
        View v = getWindow().getDecorView().findViewById(android.R.id.content);
        Display d = getWindowManager().getDefaultDisplay();
        int right = v.getRight();
        int width = d.getWidth();
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            android.view.ViewConfiguration vc = ViewConfiguration.get(this);
            if (vc.hasPermanentMenuKey())
                return right;
        }
        return width;
    }

    private void updateSelectedFromView() {
        if (selected == null) {
            return;
        }
        selected.setAddress(ipText.getText().toString());
        
        String port = portText.getText().toString();
        if (!port.equals("")) {
            try {
                selected.setPort(Integer.parseInt(portText.getText().toString()));
            } catch (NumberFormatException nfe) { }
        } else {
            selected.setPort(-1);
        }
        
        selected.setNickname(textNickname.getText().toString());

        selected.setPassword(passwordText.getText().toString());
        selected.setKeepPassword(checkboxKeepPassword.isChecked());
        selected.setUseDpadAsArrows(checkboxUseDpadAsArrows.isChecked());
        selected.setRotateDpad(checkboxRotateDpad.isChecked());
        selected.setUseLocalCursor(checkboxLocalCursor.isChecked());
    }

    protected void onStart() {
        super.onStart();
        System.gc();
        arriveOnPage();
    }

    protected void onResume() {
        super.onStart();
        System.gc();
        arriveOnPage();
    }

    @Override
    public void onWindowFocusChanged(boolean visible) {}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged called");
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Return the object representing the app global state in the database, or
     * null if the object hasn't been set up yet
     * 
     * @param db
     *            App's database -- only needs to be readable
     * @return Object representing the single persistent instance of
     *         MostRecentBean, which is the app's global state
     */
    public static MostRecentBean getMostRecent(SQLiteDatabase db) {
        ArrayList<MostRecentBean> recents = new ArrayList<MostRecentBean>(1);
        MostRecentBean.getAll(db, MostRecentBean.GEN_TABLE_NAME, recents,
                MostRecentBean.GEN_NEW);
        if (recents.size() == 0)
            return null;
        return recents.get(0);
    }

    public void arriveOnPage() {
        ArrayList<ConnectionBean> connections = new ArrayList<ConnectionBean>();
        ConnectionBean.getAll(database.getReadableDatabase(),
                ConnectionBean.GEN_TABLE_NAME, connections,
                ConnectionBean.newInstance);
        Collections.sort(connections);
        connections.add(0, new ConnectionBean(this));
        int connectionIndex = 0;
        if (connections.size() > 1) {
            MostRecentBean mostRecent = getMostRecent(database
                    .getReadableDatabase());
            if (mostRecent != null) {
                for (int i = 1; i < connections.size(); ++i) {
                    if (connections.get(i).get_Id() == mostRecent
                            .getConnectionId()) {
                        connectionIndex = i;
                        break;
                    }
                }
            }
        }
        spinnerConnection.setAdapter(new ArrayAdapter<ConnectionBean>(this,
                R.layout.connection_list_entry, connections
                        .toArray(new ConnectionBean[connections.size()])));
        spinnerConnection.setSelection(connectionIndex, false);
        selected = connections.get(connectionIndex);
        updateViewFromSelected();
    }
    
    protected void onStop() {
        super.onStop();
        if (selected == null) {
            return;
        }
        updateSelectedFromView();

        saveAndWriteRecent();
    }
    
    protected void onPause() {
        Log.e(TAG, "onPause called");
        super.onPause();
    }
    
    public Database getDatabaseHelper() {
        return database;
    }
    
    private void canvasStart() {
        if (selected == null)
            return;
        MemoryInfo info = Utils.getMemoryInfo(this);
        if (info.lowMemory)
            System.gc();
        start();
    }

    public void saveAndWriteRecent() {
        // We need server address to be filled out to save. Otherwise,
        // we keep adding empty connections.
        if (selected.getAddress().equals(""))
            return;
        
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            selected.save(db);
            MostRecentBean mostRecent = getMostRecent(db);
            if (mostRecent == null) {
                mostRecent = new MostRecentBean();
                mostRecent.setConnectionId(selected.get_Id());
                mostRecent.Gen_insert(db);
            } else {
                mostRecent.setConnectionId(selected.get_Id());
                mostRecent.Gen_update(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Starts the activity which makes a VNC connection and displays the remote
     * desktop.
     */
    private void start () {
        updateSelectedFromView();
        saveAndWriteRecent();
        Intent intent = new Intent(this, RemoteCanvasActivity.class);
        intent.putExtra(Constants.CONNECTION, selected.Gen_getValues());
        startActivity(intent);
    }
}
