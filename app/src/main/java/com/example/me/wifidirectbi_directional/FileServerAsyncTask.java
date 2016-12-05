package com.example.me.wifidirectbi_directional;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ME on 05-Dec-16.
 */
public class FileServerAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG ="FileServerAsyncTask" ;
    //        private TextView statusText;
    private Context mFilecontext;
    public String Extension, Key;
    private File EncryptedFile;
    private long ReceivedFileLength;
    private static Handler handler;
    private static ProgressDialog mProgressDialog;
    private int PORT;
    /*
     * @param context
     * @param statusText
     */
    FileServerAsyncTask(Context context, int port) {
        this.mFilecontext = context;
//            this.statusText = (TextView) statusText;

        this.PORT = port;
//			myTask = new FileServerAsyncTask();
        if (mProgressDialog == null)
            mProgressDialog = new ProgressDialog(mFilecontext,ProgressDialog.STYLE_HORIZONTAL);

    }


    @Override
    protected String doInBackground(String... params) {
        try {
            Log.d(FileServerAsyncTask.TAG, "Port: "+ PORT);
            ServerSocket serverSocket = new ServerSocket(PORT);

            Log.d(FileServerAsyncTask.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d("Client's IP  ", "" + client.getInetAddress());

            DeviceDetailFragment.WiFiClientIP = client.getInetAddress().getHostAddress();
            SharedPreferences.setStringValues(mFilecontext,
                    "WiFiClientIp", DeviceDetailFragment.WiFiClientIP);

            ObjectInputStream ois = new ObjectInputStream(
                    client.getInputStream());
            WiFiTransferModal obj = null;
            // obj = (WiFiTransferModal) ois.readObject();
            String InetAddress;
            try {
                obj = (WiFiTransferModal) ois.readObject();
                InetAddress = obj.getInetAddress();
                if (InetAddress != null
                        && InetAddress
                        .equalsIgnoreCase(FileTransferService.inetAddress)) {
                    Log.d(FileServerAsyncTask.TAG,"Group Client Ip: " + DeviceDetailFragment.WiFiClientIP);
                    SharedPreferences.setStringValues(mFilecontext,
                            "WiFiClientIp", DeviceDetailFragment.WiFiClientIP);
                    Log.d(FileServerAsyncTask.TAG,"Client IP from SharedPreference: " +SharedPreferences.getStringValues(mFilecontext,"WiFiClientIp"));

                    //set boolean true which identifiy that this device will act as server.
                    SharedPreferences.setStringValues(mFilecontext,"ServerBoolean", "true");
                    ois.close();
                    serverSocket.close();
                    return "DONE";
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final Runnable r = new Runnable() {

                public void run() {
                    // TODO Auto-generated method stub
                    mProgressDialog.setMessage("Receiving...");
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setMax(100);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.setProgressNumberFormat(null);
//						mProgressDialog.setCancelable(false);
                    mProgressDialog
                            .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.show();
                }
            };
            //handler.post(r);
            Log.e("File from otherside ",
                    obj.getFileName());

            final File f = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + DeviceDetailFragment.FolderName + "/"
                            + obj.getFileName());

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

				/*
				 * Recieve file length and copy after it
				 */
            this.ReceivedFileLength = obj.getFileLength();

            InputStream inputstream = client.getInputStream();
            //Added New
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);


            CopyFile.copyRecievedFile(inputstream, bos,ReceivedFileLength);
            ois.close(); // close the ObjectOutputStream object after saving
            // file to storage.
            serverSocket.close();

				/*
				 * Set file related data and decrypt file in postExecute.
				 */
            this.Extension = obj.getFileName();
            this.EncryptedFile = f;

            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e(FileServerAsyncTask.TAG, e.getMessage());
            return null;
        }
    }


    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            if(!result.equalsIgnoreCase("DONE")){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                //mFilecontext.startActivity(intent);
            }
            else{
            		/*
					 * To initiate socket again we are intiating async task
					 * in this condition.
					 */
                FileServerAsyncTask FileServerObj = new
                        FileServerAsyncTask(mFilecontext,FileTransferService.PORT);
                if(FileServerObj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerObj.executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });

                    }
                    else FileServerObj.execute();

                }
            }
//                statusText.setText("File copied - " + result);

        }

    }

    @Override
    protected void onPreExecute() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mFilecontext);
        }
    }

}
