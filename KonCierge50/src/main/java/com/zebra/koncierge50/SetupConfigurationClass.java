package com.zebra.koncierge50;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SetupConfigurationClass {
    public class TextDataConfiguration
    {
        int x;
        int y;
        int width;
        int height;
        int rotation;
        String horizontalAlignment;
        String verticalAlignment;
        int fontSize;
        boolean shrinkToFit = true;

        public TextDataConfiguration(int x, int y, int width, int height, int rotation, String horizontalAlignment, String verticalAlignment, int fontSize, boolean shrinkToFit)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotation = rotation;
            this.horizontalAlignment = horizontalAlignment;
            this.verticalAlignment = verticalAlignment;
            this.fontSize = fontSize;
            this.shrinkToFit = shrinkToFit;
        }
    }

    public class QRCodeConfiguration
    {
        int x;
        int y;
        int width;
        int height;
        int rotation;

        public QRCodeConfiguration(int x, int y, int width, int height, int rotation)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotation = rotation;
        }
    }

    boolean ENABLE_SEARCH_MODE = true;
    boolean VIP_MODE = false;
    boolean PRINT_WRISTBAND_FOR_VIP = false;
    int NUMBER_OF_CHARACTERS_BEFORE_SEARCHING_FOR_CANDIDATES = 3;
    String CARD_PRINTER_IP = "192.168.0.170";
    int CARD_PRINTER_PORT = 9100;
    String WRISTBAND_PRINTER_IP = "192.168.0.133";
    int WRISTBAND_PRINTER_PORT = 9100;
    boolean CAN_CREATE_CARD = true;
    boolean CAN_CREATE_VCARD = false;
    String CARD_ORIENTATION = "Landscape";
    TextDataConfiguration  FIRST_NAME_CONFIG = new TextDataConfiguration(600,0, 620,100, 90, "Center", "Top", 16, true);
    TextDataConfiguration LAST_NAME_CONFIG = new TextDataConfiguration(520,0, 620,100, 90, "Center", "Top", 16, true);
    TextDataConfiguration COMPANY_CONFIG = new TextDataConfiguration(410,0, 620,80, 90, "Center", "Top", 12, true);
    TextDataConfiguration FUNCTION_CONFIG = new TextDataConfiguration(330,0, 620,80, 90, "Center", "Top", 12, true);
    boolean PRINT_QRCODE = true;
    QRCodeConfiguration QRCODE_CONFIG = new QRCodeConfiguration(50, 180, 300, 300, 90);
    boolean PRINT_COMPANY = true;
    boolean PRINT_FUNCTION = false;
    boolean USE_CUSTOM_FONT = false;

    // Serialize the object to a JSON file
    public void saveToFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw(e);
        }
    }

    // Deserialize the object from a JSON file
    public static SetupConfigurationClass loadFromFile(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, SetupConfigurationClass.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
