package com.example.me.wifidirectbi_directional;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int RESULT_CODE = 1234;
    private static final String TAG = "DeviceDetailFragment" ;
    public static String FolderName = "WiFi Direct";
    private View contentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    static ProgressDialog progressDialog = null;


    public static String WiFiServerIP = "";
    public static String WiFiClientIP = "";
    static Boolean ClientCheck = false;
    public static String GroupOwnerIP= "";
    static long ActualFileLength = 0;
    static int Percentage = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.device_detail, null);


        contentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        contentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        contentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("Image/*");
                        //Intent i = Intent.createChooser()
                        startActivityForResult(intent,RESULT_CODE);
                    }
                });

        return contentView;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.group_owner);

      if (info.isGroupOwner == true) {
          view.setText(getResources().getString(R.string.group_owner_text) + getResources().getString(R.string.yes));
      }
        else {
          view.setText(getResources().getString(R.string.group_owner_text) + getResources().getString(R.string.no));
      }

        view = (TextView) contentView.findViewById(R.id.device_info);
        if(info.groupOwnerAddress.getHostAddress()!=null)
            view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        else{
            Toast.makeText(getActivity(), "Host Address not found", Toast.LENGTH_SHORT).show();
        }

        // GroupOwner will be the server.

        try {
            String GroupOwner = info.groupOwnerAddress.getHostAddress();
            if(GroupOwner!=null && !GroupOwner.equals("")){
                SharedPreferences.setStringValues(getActivity(), "GroupOwnerIP", GroupOwner);
            }
            contentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            if (info.groupFormed && info.isGroupOwner) {
        	     SharedPreferences.setStringValues(getActivity(), "ServerBoolean", "true");

            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
                FileServerAsyncTask FileServerObj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (FileServerObj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerObj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                        // FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Void);
                    }
                    else
                        FileServerObj.execute();
                }
            }
            else  {
                // The other device acts as the client. In this case, we enable the
                // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
                if (!ClientCheck) {
                    FirstConnectionMessage firstObj = new FirstConnectionMessage(
                            GroupOwnerIP);
                    if (firstObj != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            firstObj.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR,
                                    new String[] { null });
                        } else
                            firstObj.execute();
                    }
                }

                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (FileServerobj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                    }
                    else
                        FileServerobj.execute();
                }
            }
        }
        catch(Exception e){

        }
    }

    class FirstConnectionMessage extends AsyncTask<String, Void, String> {

        String GroupOwnerAddress = "";

        public FirstConnectionMessage(String owner) {
            // TODO Auto-generated constructor stub
            this.GroupOwnerAddress = owner;

        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(DeviceDetailFragment.TAG,"Begin to connect for the first time");

            // TODO Auto-generated method stub
            Intent serviceIntent = new Intent(getActivity(),
                    WiFiClientIPTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

            if (info.groupOwnerAddress.getHostAddress() != null) {
                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        info.groupOwnerAddress.getHostAddress());

                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                        FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.inetAddress,
                        FileTransferService.inetAddress);

            }

            getActivity().startService(serviceIntent);

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result!=null){
                if(result.equalsIgnoreCase("success")){
                    Log.d(DeviceDetailFragment.TAG,"First time connection Successful");
                    ClientCheck = true;
                }
            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        if(resultCode == getActivity().RESULT_OK){
            Uri uri = data.getData();
    	        /*
    	         * get actual file name and size of file, it will be send to socket and recieved at other device.
    	         * File size help in displaying progress dialog actual progress.
    	         */
            String selectedFilePath =null;
            try {
                selectedFilePath = uri.toString();

                Log.e("Selected File Path-> ", selectedFilePath);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            String Extension = "";
            if(selectedFilePath!=null){
                File f = new File(selectedFilePath);
                System.out.println("file name is   ::" + f.getName());
                Long FileLength = f.length();
                ActualFileLength = FileLength;
                try {
                    Extension = f.getName();
                    Log.e("Name of File-> ", "" + Extension);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
            else{
                Log.d(DeviceDetailFragment.TAG,"Path is null.");
                return;
            }


            TextView statusText = (TextView) contentView.findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(DeviceDetailFragment.TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
    	        /*
    	         * Choose on which device file has to send weather its server or client
    	         */
            String Ip = SharedPreferences.getStringValues(
                    getActivity(), "WiFiClientIp");
            String OwnerIp = SharedPreferences.getStringValues(
                    getActivity(), "GroupOwnerIP");
            if (OwnerIp != null && OwnerIp.length() > 0) {
                //CommonMethods.e("", "inside the check -- >");
                // if(!info.groupOwnerAddress.getHostAddress().equals(LocalIp)){
                String host=null;
                int  sub_port =-1;

                String ServerBool = SharedPreferences.getStringValues(getActivity(), "ServerBoolean");
                if (ServerBool!=null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {

                    //-----------------------------
                    if (Ip != null && !Ip.equals("")) {
                        Log.d(DeviceDetailFragment.TAG,"Sending data to IP: "+ Ip);
                        // Get Client Ip Address and send data
                        host=Ip;
                        sub_port=FileTransferService.PORT;
                        serviceIntent
                                .putExtra(
                                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                        Ip);
//    							serviceIntent
//    									.putExtra(
//    											WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    											WiFiFileTransferService.CLIENTPORT);

                    }


                } else {
                    Log.d(DeviceDetailFragment.TAG,"Sending data to IP: "+ OwnerIp);


                    FileTransferService.PORT = 1234;

                    host=OwnerIp;
                    sub_port=FileTransferService.PORT;
                    serviceIntent
                            .putExtra(
                                    FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                    OwnerIp);

//    					serviceIntent
//    							.putExtra(
//    									WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    									WiFiFileTransferService.PORT);
                    // anuj


                }


                serviceIntent.putExtra(FileTransferService.Extension, Extension);

                serviceIntent.putExtra(FileTransferService.FileLength,
                        ActualFileLength + "");
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
                if(host !=null && sub_port!=-1)
                {
                    //CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
                    showprogress("Sending...");
                    getActivity().startService(serviceIntent);
                }
                else {
                    //Toast.makeText((MainActivity) getActivity(),"Host not found. Re-connect!");
                    DisplayToast(getActivity(),"Host Address not found, Please Re-Connect");
                    DismissProgressDialog();
                }

            } else {
                //Toast.makeText(this,"Host not found. Re-connect!");
                DismissProgressDialog();
                DisplayToast(getActivity(),"Host Address not found, Please Re-Connect");
            }
        }
        else{
            //oast.makeText(getActivity(),"Cancelled Request");

            DisplayToast(getActivity(), "Cancelled Request");
        }
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());

//        getActivity().startService(serviceIntent);
    }


    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) contentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    public static void DisplayToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void resetViews() {
        contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        contentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
        /*
         * Remove All the prefrences here
         */
        SharedPreferences.setStringValues(getActivity(),
                "GroupOwnerIP", "");
        SharedPreferences.setStringValues(getActivity(),
                "ServerBoolean", "");
        SharedPreferences.setStringValues(getActivity(),
                "WiFiClientIp", "");
    }

    public void showprogress(final String task) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity(),
                    ProgressDialog.THEME_HOLO_LIGHT);
        }
        Handler handle = new Handler();
        final Runnable send = new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                progressDialog.setMessage(task);
                // mProgressDialog.setProgressNumberFormat(null);
                // mProgressDialog.setProgressPercentFormat(null);
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
//				mProgressDialog.setCancelable(false);
                progressDialog.setProgressNumberFormat(null);
                progressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }
        };
        handle.post(send);
    }

    public static void DismissProgressDialog() {
        try {
            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
