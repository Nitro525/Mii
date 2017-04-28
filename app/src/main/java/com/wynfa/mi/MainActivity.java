package com.wynfa.mi;   // Package Name

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.wynfa.mi.db.ReminderContract;
import com.wynfa.mi.db.ReminderDbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ReminderDbHelper mHelper;
    private ListView mReminderListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* This sets the view of the activity as activity_amin.xml
        *  activity_main.xml controls the UI and Android Interface */
        setContentView(R.layout.activity_main);

        /* This creates our database manager */
        mHelper = new ReminderDbHelper(this);

        /* This creates our list of reminders */
        mReminderListView = (ListView) findViewById(R.id.list_reminder);

        updateUI();
    }

    /* This "inflates" or renders the menu in the main activity and makes use of
    *  onOptionsItemSelected() method for the user's input with the other items */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* This is what defines how the application reacts to user input by selection
    *  of any of the menu items found  within the main menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*  This is for the event that the + or the add button is clicked
            *   This opens up an alert dialog where user inputs reminder details
            *   to be saved into the database and added to the listview */
            case R.id.action_add_reminder:
                /* Create layout for the alert dialog */
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);

                /* EditText for the Reminder's Title */
                final EditText reminderEditText = new EditText(this);
                reminderEditText.setHint("Reminder Title");
                layout.addView(reminderEditText);

                /* EditText for the Reminder's Time */
                final EditText timeEditText = new EditText(this);
                timeEditText.setHint("Reminder Time");
                layout.addView(timeEditText);

                /* The details for the timepicker dialog */
                Calendar currTime = Calendar.getInstance();
                int hour = currTime.get(Calendar.HOUR_OF_DAY);
                int minute = currTime.get(Calendar.MINUTE);
                final TimePickerDialog tPicker;
                tPicker = new TimePickerDialog(timeEditText.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timeEditText.setText( hourOfDay + ":" + minute);
                    }
                }, hour, minute, true);
                tPicker.setTitle("Pick Time");


                timeEditText.setInputType(InputType.TYPE_NULL);
                timeEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tPicker.show();
                    }
                });
                timeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            tPicker.show();
                        }
                    }
                });

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Input Reminder Details")
                        .setView(layout)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String reminder = String.valueOf(reminderEditText.getText());
                                //String time = String.valueOf(timeEditText.getText());
                                /*String[] timeParts = time.split(":");
                                int cHour = Integer.valueOf(timeParts[0]);
                                int cMinute = Integer.valueOf(timeParts[1]);*/

                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(ReminderContract.ReminderEntry.COL_REMINDER_TITLE, reminder);
                                //values.put(ReminderContract.ReminderEntry.COL_REMINDER_TIME, time);
                                db.insertWithOnConflict(ReminderContract.ReminderEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }


                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* This updates the UI every time there is an action done */
    private void updateUI() {
        ArrayList<String> reminderList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(ReminderContract.ReminderEntry.TABLE,
                new String[]{ReminderContract.ReminderEntry._ID, ReminderContract.ReminderEntry.COL_REMINDER_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(ReminderContract.ReminderEntry.COL_REMINDER_TITLE);
            reminderList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_reminder,         /* which view to use for reminders */
                    R.id.reminder_title,            /* where to place the string of data */
                    reminderList);                      /* Where to get all the data */
            mReminderListView.setAdapter(mAdapter); /* Set is as the adapter of the ListView instance */
        } else {
            mAdapter.clear();
            mAdapter.addAll(reminderList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    /* This deletes the reminder */
    public void deleteReminder(View view) {
        View parent = (View) view.getParent();
        TextView reminderTextView = (TextView) parent.findViewById(R.id.reminder_title);
        String task = String.valueOf(reminderTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(ReminderContract.ReminderEntry.TABLE,
                ReminderContract.ReminderEntry.COL_REMINDER_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }

    /* This edits the reminder */
    public void editReminder(View view) {
        View parent = (View) view.getParent();
        TextView reminderTextView = (TextView) parent.findViewById(R.id.reminder_title);
        final String reminder = String.valueOf(reminderTextView.getText());
        final EditText reminderEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Reminder")
                .setView(reminderEditText)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String eReminder = String.valueOf(reminderEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        db.delete(ReminderContract.ReminderEntry.TABLE, ReminderContract.ReminderEntry.COL_REMINDER_TITLE + " = ?", new String[]{reminder});
                        ContentValues values = new ContentValues();
                        values.put(ReminderContract.ReminderEntry.COL_REMINDER_TITLE, eReminder);
                        db.insertWithOnConflict(ReminderContract.ReminderEntry.TABLE,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE);

                        db.close();
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
}
