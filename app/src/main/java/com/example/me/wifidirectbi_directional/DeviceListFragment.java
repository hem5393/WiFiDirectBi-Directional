package com.example.me.wifidirectbi_directional;

import java.util.ArrayList;
import java.util.List;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<>();  // check this for the error
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;
    public static final String TAG = "DeviceListFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;
        }
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    //Get device status
    private static String getDeviceStatus(int deviceStatus) {
        //Log.d(DeviceListFragment.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:{
                Log.d(DeviceListFragment.TAG, "Peer status :" + "Available");
                return "Available";
            }
            case WifiP2pDevice.INVITED: {
                Log.d(DeviceListFragment.TAG, "Peer status :" + "Invited");
                return "Invited";
            }
            case WifiP2pDevice.CONNECTED:{
                Log.d(DeviceListFragment.TAG, "Peer status :" + "Connected");
                return "Connected";
            }
            case WifiP2pDevice.FAILED: {
                Log.d(DeviceListFragment.TAG, "Peer status :" + "Failed");
                return "Failed";
            }
            case WifiP2pDevice.UNAVAILABLE:{
                Log.d(DeviceListFragment.TAG, "Peer status :" + "Unavailable");
                return "Unavailable";
            }
            default:
                return "Unknown";

        }
    }

    //Select a device from a list of adapters
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }

    //Update the text view in UI as per device status
    public void updateUI(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_device_name);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_device_status);
        view.setText(getDeviceStatus(device.status));
    }

    //After successful discovery call this method to add found devices into a list.
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(DeviceListFragment.TAG, "No devices found.");
            Toast.makeText(getActivity(),"No devices found. Make sure other devices are visible.",Toast.LENGTH_SHORT).show();
        }

    }

    //Clear the list when resetting data and UI
    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    // A method to start Discovery for other peers
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding new devices", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                });
    }

}
