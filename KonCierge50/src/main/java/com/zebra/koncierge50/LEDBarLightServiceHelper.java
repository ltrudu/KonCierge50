package com.zebra.koncierge50;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zebra.ledbarlightservice.ILedBarLightService;

import static android.content.Context.BIND_AUTO_CREATE;

public class LEDBarLightServiceHelper {

    public interface LEDBarLightServiceInitializeCallback
    {
        void onInitialized();
        void onError(String message);
    }

    private static String TAG = "LEDBarService";

    private ILedBarLightService mLEDBarLightService;
    private ServiceConnection mConnection;
    private LEDBarLightServiceInitializeCallback mLEDBarLightServiceInitializeCallback;
    Context mContext;

    private static final int LED_ID = 101;

    public LEDBarLightServiceHelper(Context context)
    {
        mContext = context;
    }

    public void initialize(LEDBarLightServiceInitializeCallback ledBarLightServiceInitializeCallback) {
        Log.v(TAG, "Initialize LED Bar Service");
        mLEDBarLightServiceInitializeCallback = ledBarLightServiceInitializeCallback;
        if(mLEDBarLightService == null)
        {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mLEDBarLightService = ILedBarLightService.Stub.asInterface(iBinder);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mLEDBarLightService = null;
                }
            };

            Intent intent = new Intent("LedBarLightService");
            intent.setPackage("com.zebra.ledbarlightservice");
            mContext.bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
        else
        {
            Log.v(TAG, "Service already initialized, check your code, you may have forgotten to do a cleanup somewhere.");
        }
    }

    public void cleanup()
    {
        mLEDBarLightService = null;
        mConnection = null;
    }

    public void setColorARGB(int argb)
    {
        if(mLEDBarLightService != null)
        {
            try {
                mLEDBarLightService.setLight(LED_ID, argb);
                //Log.v(TAG, "LED Bar color changed to RGBA value: " + ARGBtoString(argb));
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TAG, "Error: Can not set color because the LEDBarService is not initialized.");
        }
    }

    // This methods work with Android Color static members
    // Like Color.RED, Color.WHITE, etc...
    // The color values are encoded using the ARGB hexadecimal format
    // We need to convert them to RGBA to be compatible with the LEDBar service.
    public void setAndroidColor(int color)
    {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        // Combine the components into RGBA format
        int ARGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
        setColorARGB(ARGB);
    }

    public static int ARGBtoRGBA(int color) {
        // Extract from AARRGGBB structure
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Combine the components into RRGGBBAA format
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    private String ARGBtoString(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return String.format("0x%02X%02X%02X%02X", alpha, red, green, blue);
    }
}
