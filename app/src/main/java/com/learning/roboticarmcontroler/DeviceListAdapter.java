package com.learning.roboticarmcontroler;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * this class is for the custom list adapter of the discovered BT devices
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {


    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> mDevices ;
    private int ViewResourceId;


    public DeviceListAdapter(Context context, int resourceID, ArrayList<BluetoothDevice> devices) {
        super(context, resourceID, devices);
        this.mDevices = devices ;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewResourceId = resourceID ;

    }


    public View getView(int position, View convertView, ViewGroup parent){

        convertView = layoutInflater.inflate(ViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if (device != null){

            TextView deviceName = convertView.findViewById(R.id.device_name);
            TextView deviceAddress = convertView.findViewById(R.id.device_address);

            if (deviceName != null){
                deviceName.setText(device.getName());
            }

            if (deviceAddress != null){
                deviceAddress.setText(device.getAddress());
            }
       }

        return convertView;
    }
}
