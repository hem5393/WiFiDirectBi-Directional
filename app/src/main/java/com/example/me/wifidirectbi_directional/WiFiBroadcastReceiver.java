package com.example.me.wifidirectbi_directional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
/**
 * Created by ME on 04-Dec-16.
 */
public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiBroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity mainActivity;

    public WiFiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity mainActivity) {

        super();
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mainActivity.setIsWifiP2pEnabled(true);
            } else {
                mainActivity.setIsWifiP2pEnabled(false);
                mainActivity.resetData();
            }
            Log.d(WiFiBroadcastReceiver.TAG, "P2P state changed - " + state);
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) mainActivity.getFragmentManager().findFragmentById(R.id.frag_list));
            }
            Log.d(WiFiBroadcastReceiver.TAG, "P2P peers changed");
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                DeviceDetailFragment fragment = (DeviceDetailFragment) mainActivity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);
            }
            else {
                mainActivity.resetData();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            DeviceListFragment fragment = (DeviceListFragment) mainActivity.getFragmentManager().findFragmentById(R.id.frag_list);
            fragment.updateUI((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }
}
