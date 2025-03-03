package com.example.koncierge50;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SetupConfigurationClass {
    boolean VIP_MODE = false;
    boolean PRINT_WRISTBAND_FOR_VIP = false;
    int NUMBER_OF_CHARACTERS_BEFORE_SEARCHING_FOR_CANDIDATES = 3;
    String CARD_PRINTER_IP = "192.168.1.102";
    int CARD_PRINTER_PORT = 9100;
    String WRISTBAND_PRINTER_IP = "192.168.0.133";
    int WRISTBAND_PRINTER_PORT = 9100;
    boolean CAN_CREATE_CARD = true;
    boolean CAN_CREATE_VCARD = true;

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
