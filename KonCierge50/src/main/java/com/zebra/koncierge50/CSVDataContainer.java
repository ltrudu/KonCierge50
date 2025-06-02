package com.zebra.koncierge50;

import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.zebra.koncierge50.StringHelpers.capitalizeFirstLetters;

public class CSVDataContainer {

    private final String TAG = "CSVData";

    private String[] titles = { "Prénom","Nom","Société","Email","Mobile","Fonction","VIP" };
    ArrayMap<String, Integer> titlesIndexes = null;

    private ArrayList<CSVDataModel> mAllItems = null;

    public CSVDataContainer()
    {
    }

    public void clean()
    {
        mAllItems.clear();
        mAllItems = null;
    }

    public void addModel(CSVDataModel csvDataModel)
    {
        mAllItems.add(csvDataModel);
    }

    private int findIndex(String searchedTitle, String[] titles)
    {
        int index = 0;
        for(String title : titles)
        {
            if(title.equalsIgnoreCase(searchedTitle))
            {
                return index;
            }
            index++;
        }
        return -1;
    }

    private void buildTitleIndexes(String titleString)
    {
        titlesIndexes = new ArrayMap<>();
        String[] titles = titleString.split(";");
        for(String title : titles)
        {
            titlesIndexes.put(title, findIndex(title, titles));
        }
    }

    public void readFile(File filePath)
    {
        try {
            Scanner scanner = new Scanner(filePath);
            String titleLine = "";
            // Jump directly to the second line to prevent reading
            // columns titles
            if(scanner.hasNextLine()) {
                titleLine = scanner.nextLine();
            }
            else
            {
                Log.e(TAG, "Error, file is empty");
                return;
            }

            buildTitleIndexes(titleLine);

            if(mAllItems == null)
                mAllItems = new ArrayList<>();
            else
                mAllItems.clear();

            // Now we can read the whole data
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.isEmpty() == false) {
                    String[] values = line.split(";");
                        CSVDataModel model = new CSVDataModel();
                        try {
                            if (titlesIndexes.containsKey("FirstName") && titlesIndexes.get("FirstName") != -1)
                                model.Prenom = values[titlesIndexes.get("FirstName")];
                            if (titlesIndexes.containsKey("LastName") && titlesIndexes.get("LastName") != -1)
                                model.Nom = values[titlesIndexes.get("LastName")];
                            if (titlesIndexes.containsKey("Company") && titlesIndexes.get("Company") != -1)
                                model.Societe = values[titlesIndexes.get("Company")];
                            if (titlesIndexes.containsKey("Email") && titlesIndexes.get("Email") != -1)
                                model.Email = values[titlesIndexes.get("Email")];
                            if (titlesIndexes.containsKey("Mobile") && titlesIndexes.get("Mobile") != -1)
                                model.Mobile = values[titlesIndexes.get("Mobile")];
                            if (titlesIndexes.containsKey("Role") && titlesIndexes.get("Role") != -1)
                                model.Fonction = values[titlesIndexes.get("Role")];
                            if (titlesIndexes.containsKey("VIP") && titlesIndexes.get("VIP") != -1)
                                model.VIP = (values[titlesIndexes.get("VIP")].equalsIgnoreCase("yes")) ? true : false;
                            model._AllText = capitalizeFirstLetters(model.Prenom + " " + model.Nom);
                            model.createVCard();
                            mAllItems.add(model);
                        }
                        catch(Exception e)
                        {
                            Log.e(TAG, "CSV Error line:" + line);
                        }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getCandidates(String searchText)
    {
        String searchTextLower = searchText.toLowerCase();
        ArrayList<String> returnList = new ArrayList<>();
        if(mAllItems != null) {
            for (CSVDataModel model : mAllItems) {
                if (model._AllText.toLowerCase().contains(searchTextLower)) {
                    returnList.add(model._AllText);
                }
            }
        }
        return returnList;
    }



    public CSVDataModel findItemWithAllText(String allTextToFind)
    {
        CSVDataModel returnItem = null;
        for(CSVDataModel model  : mAllItems)
        {
            if(model._AllText.equalsIgnoreCase(allTextToFind))
            {
                return model;
            }
        }
        return returnItem;
    }

}
