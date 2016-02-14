package com.example.haibol.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    NsdHelper mNsdHelper;

    private TextView mStatusView;
    private Handler mUpdateHandler;
    private Button register;
    private Button discover;
    private Button connect;
    private Button send;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        register = (Button) findViewById(R.id.advertise_btn);
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Register service
                if(mConnection.getLocalPort() > -1) {
                    mNsdHelper.registerService(mConnection.getLocalPort());
                } else {
                    Log.d(TAG, "ServerSocket isn't bound.");
                }
            }
        });

        discover = (Button) findViewById(R.id.discover_btn);
        discover.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mNsdHelper.discoverServices();
            }
        });

        connect = (Button) findViewById(R.id.connect_btn);
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                if (service != null) {
                    Log.d(TAG, "Connecting.");
                    mConnection.connectToServer(service.getHost(),
                            service.getPort());
                } else {
                    Log.d(TAG, "No service to connect to!");
                }
            }
        });

        send = (Button) findViewById(R.id.send_btn);
        send.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText messageView = (EditText) findViewById(R.id.chatInput);
                if (messageView != null) {
                    String messageString = messageView.getText().toString();
                    if (!messageString.isEmpty()) {
                        mConnection.sendMessage(messageString);
                    }
                    messageView.setText("");
                }
            }
        });

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }
}
