package com.example.me.wifidirectbi_directional;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ME on 05-Dec-16.
 */
public class FileTransferService extends IntentService {

    private static final String TAG = "FileTransferService" ;
    Handler mHandler;

    public static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public static  int PORT = 1234;
    public static final String inetAddress = "inetAddress";
    public static final int ByteSize = 512;
    public static final String Extension = "extension";
    public static final String FileLength = "fileLength";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mHandler = new Handler();
    }
    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            //Added New
            //File fileToSend = new File(fileUri);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            String extension = intent.getExtras().getString(Extension);
            String filelength = intent.getExtras().getString(FileLength);

            try {
                Log.d(FileTransferService.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(FileTransferService.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;

                /*
                 * Object that is used to send file name with extension and recieved on other side.
                 */
                Long FileLength = Long.parseLong(filelength);
                WiFiTransferModal transObj = null;
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                if(transObj == null) transObj = new WiFiTransferModal();

                transObj = new WiFiTransferModal(extension,FileLength);
                oos.writeObject(transObj);

                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(FileTransferService.TAG, e.toString());
                }
                //FileInputStream fis = new FileInputStream(fileToSend.toString());
                BufferedInputStream bis = new BufferedInputStream(is);
                CopyFile.copyFile(is, stream);
                Log.d(FileTransferService.TAG, "Client: Data written");
                oos.close();	//close the ObjectOutputStream after sending data.
            } catch (IOException e) {
                Log.e(FileTransferService.TAG, e.getMessage());
                e.printStackTrace();
                mHandler.post(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(FileTransferService.this, "Paired Device is not Ready to receive the file", Toast.LENGTH_LONG).show();
                    }
                });
                DeviceDetailFragment.DismissProgressDialog();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
