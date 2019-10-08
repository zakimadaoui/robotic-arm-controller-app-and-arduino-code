package com.learning.roboticarmcontroler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;


public class DiscoverBtActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "DiscoverBtActivity" ;
    private BluetoothAdapter mBluetoothAdapter;

    public ArrayList<BluetoothDevice> mBTdevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter ;
    ListView devicesListView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_bt);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBTdevices = new ArrayList<>();
        devicesListView = findViewById(R.id.devices_list_view);

        Log.d(TAG, "onCreate: getting paierd devices");
        getPairedDevices();

        devicesListView.setOnItemClickListener(DiscoverBtActivity.this);


    }


    //----------------------- onItemClicked event for the devices list -----------------------------
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d(TAG, "onItemClick: Adevice has been clicked");

        // first thing to do ic cancel discovery because it is very memory intensive
        mBluetoothAdapter.cancelDiscovery();

        // get name and adress on bonded device
        String deviceName = mBTdevices.get(position).getName();
        String deviceAdress = mBTdevices.get(position).getAddress();
        Log.d(TAG, "Clicked Device: "+deviceName+": "+ deviceAdress);

        //NOTE: Requires API 17 or higher to use createBond() method
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBTdevices.get(position).createBond();
            Log.d(TAG, "onItemClick: Device Bonded");
        }


        Intent intent = new Intent(DiscoverBtActivity.this, MainActivity.class);
        intent.putExtra("EXTRA_BT_NAME",deviceName);
        intent.putExtra("EXTRA_BT_ADDRESS", deviceAdress);
        intent.putExtra("EXTRA_POSITION",Integer.toString(position));


        startActivity(intent);
        



    }

    //----------------------- Function for getting already paired devices --------------------------

    private void getPairedDevices(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();


        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                mBTdevices.add(bt);
                Log.d(TAG, "PairedDevices: "+ bt.getName());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.",
                    Toast.LENGTH_LONG).show();
        }

        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(),
                R.layout.devices_adapter_view, mBTdevices);

        devicesListView.setAdapter(mDeviceListAdapter);

    }

}
