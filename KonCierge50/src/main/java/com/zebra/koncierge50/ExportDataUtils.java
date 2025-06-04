package com.zebra.koncierge50;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportDataUtils {

    private static String exportFolder = Constants.DEMO_DATA_FOLDER + "Export/";

    public static File getTodayFile(String extension) throws Exception
    {
        File targetFolder = null;
        File dateFile = null;
        targetFolder = new File(exportFolder);
        if (targetFolder.exists() == false) {
            targetFolder.mkdirs();
        }
        dateFile = new File(targetFolder, getTodayDateString() + "." + extension);
        return dateFile;
    }

    public static String getTodayDateString()
    {
        Date nowDate = new Date();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        String currentDate = sdf2.format(nowDate);
        return currentDate;
    }

    public static void initializeCSVFile(File targetFile) throws Exception
    {
        if(targetFile.createNewFile()) {
            // Append data to the file
            FileWriter fileWriter = new FileWriter(targetFile, true); // Append mode
            fileWriter.append("FirstName;LastName;Company;Email;Mobile;Role;New;TimeStamp" + "\n");
            fileWriter.close();
        }
    }

    public static void appendDataToCSVFile(CSVDataModel model, Date nowDate, boolean newModel) throws Exception
    {
        File todayFile = getTodayFile("csv");
        if(!todayFile.exists())
        {
            initializeCSVFile(todayFile);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(nowDate);
        FileWriter fileWriter = new FileWriter(todayFile, true);
        fileWriter.append(model.Prenom + ";" + model.Nom + ";" + model.Societe + ";" + model.Email + ";" + model.Mobile + ";" + model.Fonction + ";" + (newModel ? "Yes" : "No") + ";" + currentTime + "\n");
        fileWriter.close();
    }


}
