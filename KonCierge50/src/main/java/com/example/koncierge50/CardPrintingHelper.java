package com.example.koncierge50;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.GraphicType;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.ZebraGraphics;
import com.zebra.sdk.common.card.graphics.barcode.CodeQRUtil;
import com.zebra.sdk.common.card.graphics.barcode.ZebraBarcodeFactory;
import com.zebra.sdk.common.card.graphics.barcode.enumerations.Rotation;
import com.zebra.sdk.common.card.graphics.enumerations.TextAlignment;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.device.ZebraIllegalArgumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardPrintingHelper {

    private final static String TAG = "CardPrint";

    private Context mContext;
    private String mIP;
    private int mPort;
    private CardPrintingHelperCallback mCallback;
    private CSVDataModel mDataModel;

    public interface CardPrintingHelperCallback
    {
        void onSuccess();
        void onMessage(String message);
        void onError(String message);
    }

    public CardPrintingHelper(Context context, String ip, int port)
    {
        mContext = context;
        mIP = ip;
        mPort = port;
    }

    public void print(CSVDataModel dataModel, CardPrintingHelperCallback callback)
    {
        mCallback = callback;
        mDataModel = dataModel;
        new Thread(new Runnable() {
            public void run() {
                Connection connection = null;
                ZebraCardPrinter zebraCardPrinter = null;

                try {
                    onMessage("Connecting to printer: " + String.valueOf(mIP) + " on port: " + mPort);
                    connection = new TcpConnection(mIP, mPort);
                    connection.open();
                    onMessage("Printer connected");

                    zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);
                    onMessage("Zebra card printer created from factory");

                    List<GraphicsInfo> graphicsData = drawGraphics(zebraCardPrinter);

                    // Send job
                    onMessage("Sending job to printer.");
                    int jobId = zebraCardPrinter.print(1, graphicsData);

                    // Poll job status
                    JobStatusInfo jobStatusInfo = pollJobStatus(jobId, zebraCardPrinter);
                    onMessage(String.format(Locale.US, "Job %d completed with status '%s'", jobId, jobStatusInfo.printStatus));
                } catch (Exception e) {
                    onError("Error printing image: " + e.getLocalizedMessage());
                } finally {
                    onSuccess();
                    cleanUpQuietly(connection, zebraCardPrinter);
                }
            }
        }).start();
    }

    private List<GraphicsInfo> drawGraphics(ZebraCardPrinter zebraCardPrinter) throws ConnectionException, IOException, ZebraCardException {
        onMessage("Drawing graphics");
        List<GraphicsInfo> graphicsData = new ArrayList<GraphicsInfo>();
        ZebraGraphics graphics = null;

        try {
            graphics = new ZebraCardGraphics(zebraCardPrinter);
            graphics.initialize(mContext, 0, 0, OrientationType.Portrait, PrintType.MonoK, Color.WHITE);
            graphicsData.add(drawMonoImage(graphics, CardSide.Front));
        }
        catch(Exception e)
        {
            onError(e.getMessage());
        }
        finally {
            if (graphics != null) {
                graphics.close();
            }
        }
        onMessage("Graphics drawed with success");
        return graphicsData;
    }

    private GraphicsInfo drawMonoImage(ZebraGraphics graphics, CardSide side) throws IllegalArgumentException, IOException, ZebraCardException {
        try {
            graphics.initialize(mContext, 0, 0, OrientationType.Landscape, PrintType.MonoK, Color.WHITE);

            // TODO : Rajouter les autres éléments du data model sur la carte

            if(mDataModel.Prenom.isEmpty() == false)
                graphics.drawText(mDataModel.Prenom , 600,0, 620,100, 90, TextAlignment.Center, TextAlignment.Top, 16, Color.BLACK,true);
            if(mDataModel.Nom.isEmpty() == false)
                graphics.drawText(mDataModel.Nom , 520,0, 620,100, 90, TextAlignment.Center, TextAlignment.Top, 16, Color.BLACK,true);
            if(mDataModel.Societe.isEmpty() == false)
                graphics.drawText(mDataModel.Societe, 410,0, 620,80, 90, TextAlignment.Center, TextAlignment.Top, 12, Color.BLACK, true);

            // Add QR Code
            if(mDataModel.VCARD != null) {
                CodeQRUtil codeQRUtil = ZebraBarcodeFactory.getQRCode(graphics);
                codeQRUtil.drawBarcode(mDataModel.VCARD, 50, 180, 300, 300, Rotation.ROTATE_90);
            }

            ZebraCardImageI zebraCardImage = graphics.createImage();
            return addImage(side, PrintType.MonoK, 0, 0, -1, zebraCardImage);
        }
        finally {
            graphics.clear();
        }
    }

    private GraphicsInfo addImage(CardSide side, PrintType printType, int xOffset, int yOffset, int fillColor, ZebraCardImageI zebraCardImage) {
        GraphicsInfo graphicsInfo = new GraphicsInfo();
        graphicsInfo.fillColor = fillColor;
        graphicsInfo.graphicData = zebraCardImage != null ? zebraCardImage : null;
        graphicsInfo.graphicType = zebraCardImage != null ? GraphicType.BMP : GraphicType.NA;
        graphicsInfo.opacity = 0;
        graphicsInfo.overprint = false;
        graphicsInfo.printType = printType;
        graphicsInfo.side = side;
        graphicsInfo.xOffset = xOffset;
        graphicsInfo.yOffset = yOffset;
        return graphicsInfo;
    }

    private JobStatusInfo pollJobStatus(int jobId, ZebraCardPrinter zebraCardPrinter) throws ConnectionException, ZebraCardException, ZebraIllegalArgumentException {
        Log.d(TAG, "polling job status");

        long dropDeadTime = System.currentTimeMillis() + 40000;
        long pollInterval = 500;

        // Poll job status
        JobStatusInfo jobStatusInfo = new JobStatusInfo();

        do {
            jobStatusInfo = zebraCardPrinter.getJobStatus(jobId);

            String alarmDesc = jobStatusInfo.alarmInfo.value > 0 ? String.format(Locale.US, " (%s)", jobStatusInfo.alarmInfo.description) : "";
            String errorDesc = jobStatusInfo.errorInfo.value > 0 ? String.format(Locale.US, " (%s)", jobStatusInfo.errorInfo.description) : "";

            String statusString = String.format("Job %d, Status:%s, Card Position:%s, Alarm Code:%d%s, Error Code:%d%s", jobId, jobStatusInfo.printStatus, jobStatusInfo.cardPosition, jobStatusInfo.alarmInfo.value, alarmDesc, jobStatusInfo.errorInfo.value, errorDesc);
            onMessage(statusString);

            if (jobStatusInfo.printStatus.contains("done_ok")) {
                onMessage("Print job is done ok.");
                break;
            } else if (jobStatusInfo.printStatus.contains("alarm_handling")) {
                System.out.println("Alarm Detected: " + jobStatusInfo.alarmInfo.description);
                onMessage("Alarm Detected: " + jobStatusInfo.alarmInfo.description);
            } else if (jobStatusInfo.errorInfo.value > 0) {
                String errorMessage = String.format(Locale.US, "The job encountered an error [%s] and was cancelled.", jobStatusInfo.errorInfo.description);
                onError(errorMessage);
                System.out.println(errorMessage);
                zebraCardPrinter.cancel(jobId);
            }
            else if (jobStatusInfo.printStatus.contains("error")) {
                onError("Unkown error. Contact your administrator.");
                break;
            }
            else if (jobStatusInfo.printStatus.contains("cancelled"))
            {
                onMessage("Job has been cancelled.");
                break;
            }
            if (System.currentTimeMillis() > dropDeadTime) {
                onError("Timeout");
                break;
            }

            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                onError(e.getMessage());
                e.printStackTrace();
            }

        } while (true);

        return jobStatusInfo;
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

    private void cleanUpQuietly(Connection connection, ZebraCardPrinter genericPrinter) {
        Log.d(TAG, "Cleaning up quietly");
        try {

            if(mCallback != null)
            {
                mCallback = null;
            }

            if(mDataModel != null)
            {
                mDataModel = null;
            }

            if (genericPrinter != null) {
                genericPrinter.destroy();
                genericPrinter = null;
            }
        } catch (ZebraCardException e) {
            e.printStackTrace();
            onError(e.getMessage());
        }

        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (ConnectionException e) {
                e.printStackTrace();
                onError(e.getMessage());
            }
        }
    }
}
