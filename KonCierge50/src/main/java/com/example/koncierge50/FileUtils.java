package com.example.koncierge50;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static void copyAssetToFolder(Context context, String assetName, String fileName, String targetFolder ) throws IOException {
        File fileTargetFolder = new File(targetFolder);
        if(fileTargetFolder.exists() == false)
        {
            fileTargetFolder.mkdirs();
        }

        AssetManager assetManager = context.getAssets();
        InputStream in = assetManager.open(assetName);
        File outFile = new File(fileTargetFolder, fileName);
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.flush();
        out.close();
    }

    public static void copyDrawableToFolder(Context context, int drawableId, String fileName, String targetFolder) {
        File fileTargetFolder = new File(targetFolder);
        if(fileTargetFolder.exists() == false)
        {
            fileTargetFolder.mkdirs();
        }

        InputStream inputStream = context.getResources().openRawResource(drawableId);
        File targetFile = new File(targetFolder, fileName);

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
