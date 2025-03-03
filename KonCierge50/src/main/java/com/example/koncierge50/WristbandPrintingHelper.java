package com.example.koncierge50;

import android.content.Context;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;

public class WristbandPrintingHelper {
    private final static String TAG = "WristbandPrint";

    private Context mContext;
    private String mIP;
    private Integer mPort;

    private WristbandPrintingHelper.WristbandPrintingHelperCallback mCallback;


    public interface WristbandPrintingHelperCallback {

        void onSuccess();
        void onMessage(String message);
        void onError(String message);
    }

    public WristbandPrintingHelper(Context context, String ip, int port)
    {
        mContext = context;
        mIP = ip;
        mPort = port;
    }

    public void print(WristbandPrintingHelperCallback callback)
    {
        mCallback = callback;

        new Thread(new Runnable() {

            @Override
            public void run() {
                Connection wristbandPrinterConnection = new TcpConnection(mIP,mPort);
                try{
                    // Ouverture connection vers imprimante bracelet
                    wristbandPrinterConnection.open();
                    onMessage("Printer connected");
                    String zplData = "^XA^XFE:VIP.ZPL^FS^XZ";
                    // Envoi de la chaine ZPL
                    wristbandPrinterConnection.write(zplData.getBytes());
                    onMessage("Sending job to printer.");
                } catch (ConnectionException e){
                    onError(e.getMessage());
                } finally {
                    try {
                        wristbandPrinterConnection.close();
                        onSuccess();
                    } catch (ConnectionException e) {
                        onError(e.getMessage());
                    }
                }
            }
        }).start();

    }

    private void onMessage(final String message) {
        if(mCallback != null)
        {
            mCallback.onMessage(message);
        }
    }

    private void onError(final String message) {
        if(mCallback != null)
        {
            mCallback.onError(message);
        }
    }

    private void onSuccess() {
        if(mCallback != null)
        {
            mCallback.onSuccess();
        }
    }
}
