package com.learning.roboticarmcontroler;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = BluetoothConnectionService.class.getSimpleName();

    private static final String appName = "RoboticArmController";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static  BluetoothAdapter bluetoothAdapter ;
    Context mContext ;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread ;

    private ConnectedThread mConnectedTread ;

    private BluetoothDevice mmDevice ;

    private UUID deviceUUID;

    private  ProgressDialog mProgressDialog ;



    public BluetoothConnectionService(Context context) {
         mContext = context ;
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         start();


    }


class AcceptThread extends Thread{

    // the local server socket
    private final BluetoothServerSocket mmServerSocket ;
    public AcceptThread() {
        BluetoothServerSocket temp = null ;

        try {
            temp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

            Log.d(TAG, "AcceptThread: Setting up server using: "+ MY_UUID_INSECURE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmServerSocket = temp ;

    }

    
    public void run() {
        Log.d(TAG, "run: Accept thread running ");

        BluetoothSocket socket = null ;


        try {
        //this is a blocking call and it will only
        // return on a successful connection or an exception
        Log.d(TAG, "run: RFCOM server socket start..... ");

        socket = mmServerSocket.accept();

            Log.d(TAG, "run: RFCOM server socket accepted connection");
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        if (socket != null) {
            connected(socket,mmDevice); // this will be addressed int video 3
        }

        Log.i(TAG, "run: END AcceptThread");
    }
    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket"+ e.getMessage());
        }
    }
  }

    /***
     * this thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails
     */
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket ;

        public ConnectThread(BluetoothDevice device, UUID uuid) {

            mmDevice = device ;
            deviceUUID = uuid ;

        }

        public void run(){

            BluetoothSocket tmp =null;
            Log.d(TAG, "run: connectThread ");


            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "run: coudnt creat Insecure rfcom socket"+ e.getMessage());
            }
            mmSocket = tmp;


            bluetoothAdapter.cancelDiscovery();

            //Make a connection to the bluetooth socket

            try {
                //this is a blocking call and it will only
                // return on a successful connection or an exception



                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected ");

            } catch (IOException e) {

                //close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: closed socket");
                } catch (IOException e1) {
                    Log.e(TAG, "run: unable to close connection in socket" + e1.getMessage());
                }

                Log.d(TAG, "run: couldn't connect to UUID: "+MY_UUID_INSECURE);
            }

            connected(mmSocket,mmDevice); // this will be addressed int video 3

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of ConnectThread failed"+ e.getMessage());
            }
        }
    }

    

    /**
     * Start AcceptThread to begin a session in listening . called by the activity onResume()
     */

    public synchronized void start() {
        Log.d(TAG, "start");

        //cancel any thread trying to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread =null ;

        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
        mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection
     * then ConnectTread starts and attempts to make a connection with other devices acceptThread
     */

    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started");

        // init progress dialog

        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth",
                "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();

    }

    class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket ;
        private final InputStream mmInStream ;
        private final OutputStream mmOutStream ;


        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket ;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progress dialog when the connection is established
            mProgressDialog.dismiss();


            try {

                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn ;
            mmOutStream = tmpOut ;

        }

        public void run(){

            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()


            //keep listening to the inputStream until an exception occurs

            while (true) {
                // read from the Input stream

                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: "+ incomingMessage);

                } catch (IOException e) {
                    Log.e(TAG, "write: error reading to inputStream "+ e.getMessage());
                    break;
                }

            }
        }


        //call this from main activity to send data to remote device

        public void write(byte[] bytes) {

            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: "+text);

            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: error writing to outputStream "+ e.getMessage());
            }
        }


        // call this from main activity to shutdown the connection

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting");

        mConnectedTread = new ConnectedThread(mmSocket);
        mConnectedTread.start();


    }

    public void write(byte[] out){

        Log.d(TAG, "write: write called");

        // perform the write
        mConnectedTread.write(out);

    }
}
