package com.zebra.koncierge50;

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
    private CardPrintingHelperCallback mCallback;
    private CSVDataModel mDataModel;
    private SetupConfigurationClass mSetupConfiguration;

    public interface CardPrintingHelperCallback
    {
        void onSuccess();
        void onMessage(String message);
        void onError(String message);
    }

    public CardPrintingHelper(Context context, SetupConfigurationClass setupConfigurationClass)
    {
        mContext = context;
        mSetupConfiguration = setupConfigurationClass;
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
                    onMessage("Connecting to printer: " + String.valueOf(mSetupConfiguration.CARD_PRINTER_IP) + " on port: " + mSetupConfiguration.CARD_PRINTER_PORT);
                    connection = new TcpConnection(mSetupConfiguration.CARD_PRINTER_IP, mSetupConfiguration.CARD_PRINTER_PORT);
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
            OrientationType cardOrientation = OrientationType.Landscape;
            switch(mSetupConfiguration.CARD_ORIENTATION)
            {
                case "Landscape":
                    cardOrientation = OrientationType.Landscape;
                    break;
                default:
                    cardOrientation = OrientationType.Portrait;
                    break;
            }
            graphics.initialize(mContext, 0, 0, cardOrientation, PrintType.MonoK, Color.WHITE);

            // TODO : Rajouter les autres éléments du data model sur la carte

            if(mDataModel.Prenom.isEmpty() == false)
                graphics.drawText(mDataModel.Prenom , mSetupConfiguration.PRENOM_CONFIG.x,mSetupConfiguration.PRENOM_CONFIG.y, mSetupConfiguration.PRENOM_CONFIG.width,mSetupConfiguration.PRENOM_CONFIG.height, mSetupConfiguration.PRENOM_CONFIG.rotation, ETextAlignmentString.fromString(mSetupConfiguration.PRENOM_CONFIG.horizontalAlignment).toTextAlignment(), ETextAlignmentString.fromString(mSetupConfiguration.PRENOM_CONFIG.verticalAlignment).toTextAlignment(), mSetupConfiguration.PRENOM_CONFIG.fontSize, Color.BLACK,mSetupConfiguration.PRENOM_CONFIG.shrinkToFit);
            if(mDataModel.Nom.isEmpty() == false)
                graphics.drawText(mDataModel.Nom , mSetupConfiguration.NOM_CONFIG.x,mSetupConfiguration.NOM_CONFIG.y, mSetupConfiguration.NOM_CONFIG.width,mSetupConfiguration.NOM_CONFIG.height, mSetupConfiguration.NOM_CONFIG.rotation, ETextAlignmentString.fromString(mSetupConfiguration.NOM_CONFIG.horizontalAlignment).toTextAlignment(), ETextAlignmentString.fromString(mSetupConfiguration.NOM_CONFIG.verticalAlignment).toTextAlignment(), mSetupConfiguration.NOM_CONFIG.fontSize, Color.BLACK,mSetupConfiguration.NOM_CONFIG.shrinkToFit);
            if(mDataModel.Societe.isEmpty() == false)
                graphics.drawText(mDataModel.Societe , mSetupConfiguration.SOCIETE_CONFIG.x,mSetupConfiguration.SOCIETE_CONFIG.y, mSetupConfiguration.SOCIETE_CONFIG.width,mSetupConfiguration.SOCIETE_CONFIG.height, mSetupConfiguration.SOCIETE_CONFIG.rotation, ETextAlignmentString.fromString(mSetupConfiguration.SOCIETE_CONFIG.horizontalAlignment).toTextAlignment(), ETextAlignmentString.fromString(mSetupConfiguration.SOCIETE_CONFIG.verticalAlignment).toTextAlignment(), mSetupConfiguration.SOCIETE_CONFIG.fontSize, Color.BLACK,mSetupConfiguration.SOCIETE_CONFIG.shrinkToFit);

            // Add QR Code
            if(mDataModel.VCARD != null && mSetupConfiguration.PRINT_QRCODE) {
                CodeQRUtil codeQRUtil = ZebraBarcodeFactory.getQRCode(graphics);
                codeQRUtil.drawBarcode(mDataModel.VCARD, mSetupConfiguration.QRCODE_CONFIG.x, mSetupConfiguration.QRCODE_CONFIG.y, mSetupConfiguration.QRCODE_CONFIG.width, mSetupConfiguration.QRCODE_CONFIG.height, Rotation.fromInteger(mSetupConfiguration.QRCODE_CONFIG.rotation));
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
