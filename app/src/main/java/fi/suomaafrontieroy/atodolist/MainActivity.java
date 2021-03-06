package fi.suomaafrontieroy.atodolist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.ContentValues;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.util.ArrayList;

import fi.suomaafrontieroy.atodolist.db.TaskContract;
import fi.suomaafrontieroy.atodolist.db.TaskDbHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);

        updateUI();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             addTask();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_task:
                addTask();
                return true;
            case R.id.action_delete_all:
                delAllTasks();
                return true;
            case R.id.action_about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE, new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this, R.layout.item_todo, R.id.task_title, taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    private void addTask() {
        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_add_new_task)
                .setMessage(R.string.message_dialog_add)
                .setView(taskEditText)
                .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        if (!task.trim().isEmpty()) {
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                            db.close();
                            updateUI();
                        } else {
                            Context context = getApplicationContext();
                            CharSequence text = getString(R.string.empty_task);
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create();
        dialog.show();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        final String task = String.valueOf(taskTextView.getText());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_confirm_deletion)
                .setMessage(R.string.message_dialog_confirm_deletion)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        db.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry.COL_TASK_TITLE + " =?", new String[]{task});
                        db.close();
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create();
        dialog.show();
    }

    private void delAllTasks() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_confirm_deletion)
                .setMessage(R.string.message_dialog_confirm_deletion_all)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = mHelper.getWritableDatabase();
                        db.delete(TaskContract.TaskEntry.TABLE, null, null);
                        db.close();
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create();
        dialog.show();
    }
}
