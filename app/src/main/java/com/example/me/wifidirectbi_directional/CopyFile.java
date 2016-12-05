package com.example.me.wifidirectbi_directional;

import android.app.ProgressDialog;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

///import static com.example.me.wifidirectbi_directional.DeviceDetailFragment.Percentage;

/**
 * Created by ME on 05-Dec-16.
 */

class CopyFile {
    private static final String TAG = "CopyFile";
    private static ProgressDialog mProgressDialog;
    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        long total = 0;
        long test = 0;
        byte buf[] = new byte[1024];

        //byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
                    total += len;
                    if (DeviceDetailFragment.ActualFileLength > 0) {
                        DeviceDetailFragment.Percentage = (int) ((total * 100) / DeviceDetailFragment.ActualFileLength);
                    }
                    // Log.e("Percentage--->>> ", Percentage+"   FileLength" +
                    // EncryptedFilelength+"    len" + len+"");
                    //mProgressDialog.setProgress(DeviceDetailFragment.Percentage);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    DeviceDetailFragment.Percentage = 0;
                    DeviceDetailFragment.ActualFileLength = 0;
                }
            }
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(CopyFile.TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream,
                                           OutputStream out, Long length) {

        //byte buf[] = new byte[FileTransferService.ByteSize];
        byte buf[] = new byte[1024];
        byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
        String Decrypted;
        int len;
        long total = 0;
        int progresspercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    total += len;
                    if (length > 0) {
                        progresspercentage = (int) ((total * 100) / length);
                    }
                   // mProgressDialog.setProgress(progresspercentage);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(CopyFile.TAG, e.toString());
            return false;
        }
        return true;
    }
}
