/*
 * Copyright 2019 Zakaria Madaoui. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.learning.roboticarmcontroler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public static final int RefreshRate = 10 ;


    //declaring the views

    SeekBar seekBar1;

    TextView textView1, textView2, textView3, textView4;


    // Some needed variables
    public  boolean task2 =false ;

    public int connected = 1;


    // declaring Bluetooth objects
    private BluetoothAdapter bluetoothAdapter;

    Button connectBTN;

    BluetoothConnectionService bluetoothConnection ;

    public ArrayList<BluetoothDevice> btDevices = new ArrayList<>();

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice BtDevice ;




    // --------------------Create a BroadcastReceiver for ACTION_FOUND. ----------------------------

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                switch (state) {

                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE_OFF");
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE_TURNING_OFF");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE_ON");

                        ShowDevices();

                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: STATE_TURNING_ON");
                        break;
                }


            }
        }
    };


 //--------------------------------------- OnCreate Function ---------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Intent intent = getIntent(); // for getting the intent coming from  DiscoverBtActivity class
        String deviceName = intent.getStringExtra("EXTRA_BT_NAME");
        String deviceAddress = intent.getStringExtra("EXTRA_BT_ADDRESS");



        if (deviceName != null && connected == 1) { // this should happen after an intent is received at least ones
            int devicePosition =  Integer.valueOf(intent.getStringExtra("EXTRA_POSITION"));


            Log.d(TAG, "connected to : " + deviceName + ": " +
                    deviceAddress+"| position: "+ devicePosition);

            BtDevice = getDevice(devicePosition); // get device function down below

            //initializing and starting the bluetooth connection
            bluetoothConnection = new BluetoothConnectionService(MainActivity.this);

            startConnection();
            connected = 2 ;
        }


        // initializing all the views
        textView1 = findViewById(R.id.Grip_Strength);
        textView2 = findViewById(R.id.Forward_Backward_Strength);
        textView3 = findViewById(R.id.UpDown_Strength);
        textView4 = findViewById(R.id.Rotation_Angle);


        seekBar1 = findViewById(R.id.Grip_Slider);


        seekBar1.setMax(100);

        connectBTN = findViewById(R.id.Connect_Button);

        // initializing the BT adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        //=================================== JoySticks ============================================

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setAutoReCenterButton(false);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                byte buffer2[] = new byte[2] ;

                Log.d(TAG, "onMove: strength: "+strength);



                int str = ((int) Math.sin(Math.toRadians(angle)))*strength*180/100;
                int forward = (int)map(str,-180,180,0,180)/2;

                textView2.setText("Forward: "+ Integer.toString(forward));
                if (forward == 65 || forward == 68) { forward = 67; }

                buffer2[0] = (byte) 'c'; // which motor
                buffer2[1] = (byte) forward; // angle of servo c

                try{  bluetoothConnection.write(buffer2);}
                catch (NullPointerException e){

                    Log.d(TAG, "onStopTrackingTouch:  BT is not connected yet: "+e);
                }


            }
        },RefreshRate);


        JoystickView joystick1 = findViewById(R.id.Angle_joystick);
        joystick1.setAutoReCenterButton(false);

        joystick1.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                byte buffer1[] = new byte[2] ;


                textView4.setText("Angle: "+ Integer.toString(angle));

                if (angle == 65 || angle == 68) { angle = 67; }

                buffer1[0] = (byte) 'b'; // which motor
                buffer1[1] = (byte) angle; // angle of servo b


                Log.d(TAG, "onMove: angle: "+angle); // for debugging


                try{
                    if (angle<=180) {
                        bluetoothConnection.write(buffer1);

                    }}
                catch (NullPointerException e){

                    Log.d(TAG, "onStopTrackingTouch:  BT is not connected yet: "+e);

                }




            }
        },RefreshRate);



        JoystickView joystick2 = findViewById(R.id.UpDown_joystick);
        joystick2.setAutoReCenterButton(false);


        joystick2.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                byte buffer[] = new byte[2] ;
                Log.d(TAG, "onMove: angle: "+angle+" strength: "+strength);


                int Up = ((int) Math.sin(Math.toRadians(angle)))*strength*180/100;
                int UpDown = (int)map(Up,-180,180,0,180)/2;

                if (UpDown == 65 || UpDown == 68) { UpDown = 67; }

                textView3.setText("Up/Down: "+ Integer.toString(UpDown));

                buffer[0] = (byte) 'd'; // which motor
                buffer[1] = (byte) UpDown; // angle of servo d




                try{ bluetoothConnection.write(buffer);}
                catch (NullPointerException e){

                    Log.d(TAG, "onStopTrackingTouch:  BT is not connected yet: "+e);
                    Toast.makeText(MainActivity.this,
                            "Please connect to a Bluetooth device first !", Toast.LENGTH_SHORT).show();
                }


            }
        },RefreshRate);




        //==================================== seekBar1 ============================================

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            byte buffer[] = new byte[2] ;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress == 65 || progress == 68) { progress = 67; }

                buffer[0] = (byte) 'a';
                buffer[1] = (byte) progress;

                textView1.setText("Grip: "+Integer.toString(progress));

                try{bluetoothConnection.write(buffer);}
                catch (NullPointerException e){

                    Log.d(TAG, "onStopTrackingTouch:  BT is not connected yet: "+e);
                    Toast.makeText(MainActivity.this,
                            "Please connect to a Bluetooth device first !", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {



            }
        });


    }

    //-------------------------------------- OnDestroy function ------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver or you will get an exception.
      if (broadcastReceiver.isOrderedBroadcast()) unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onDestroy: Disabling BT");
    }

    //---------------------- onClick function for recording movement Button ------------------------

    public void Task2(View view) {
        task2 = true ;
        byte[] signal = new byte[1] ;
        signal[0] = (byte) 'R';


        try{bluetoothConnection.write(signal);}
        catch (NullPointerException e){
            task2 = false ;
            Log.d(TAG, "onStopTrackingTouch:  BT is not connected yet: "+e);
            Toast.makeText(MainActivity.this,
                    "Please connect to a Bluetooth device first !", Toast.LENGTH_SHORT).show();
        }


    }


    //------------------------ onClick function for save movement Button ---------------------------
    public void Task3(View view) {
        Intent helpMe = new Intent(MainActivity.this,HelpMeActivity.class);
        startActivity(helpMe);
    }


    //--------------------------- onClick function for connect Button ------------------------------
    public void Connect(View view) {

        Log.d(TAG, "Connect: onClick function for connect Button");
        enableBT(); //1- Enabling BT


    }


    //------------------------------- Enabling Bluetooth Function ----------------------------------

    void enableBT() {

        Log.d(TAG, "enableBT: Turning BT ON function");

        if (bluetoothAdapter == null) Log.d(TAG, "enableBT: Bluetooth is not supported");

        if (!bluetoothAdapter.isEnabled()) {

            Log.d(TAG, "enableBT: Enabling BT");

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTintent = new
                    IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

            registerReceiver(broadcastReceiver, BTintent);

        }

        if (bluetoothAdapter.isEnabled()) {

            Log.d(TAG, "enableBT: BT already enabled");

            ShowDevices();

        }


    }
    //--------------------------------- Show Bluetooth devices -------------------------------------

    private void ShowDevices() {

        Intent DiscoverabilityIntent = new Intent(MainActivity.this,
                DiscoverBtActivity.class);
        Log.d(TAG, "Connect: BT Discovery is starting ");
        startActivity(DiscoverabilityIntent);
    }

    //----------------------------------------------------------------------------------------------

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startConnection: initializing rfcom bluetooth connection");
        bluetoothConnection.startClient(device, uuid);
    }

    //----------------------------------------------------------------------------------------------

    public void startConnection() {
        startBTConnection(BtDevice, MY_UUID_INSECURE);
    }

    //----------------------------------------------------------------------------------------------

    public BluetoothDevice getDevice(int position){

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        Log.d(TAG, "getDevice: BT adapter ok");


        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                btDevices.add(bt);
                Log.d(TAG, "PairedDevices: "+ bt.getName());

                Log.d(TAG, "getDevice:"+ bt.getName());
            }
        }

        return btDevices.get(position);
    }

    long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
