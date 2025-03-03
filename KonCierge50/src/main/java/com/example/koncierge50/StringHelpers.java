package com.example.koncierge50;

public class StringHelpers {
    public static String capitalizeFirstLetters(String name)
    {
        if(name.isEmpty() == false) {
            String[] parts = name.split(" ");
            StringBuilder capitalized = new StringBuilder();
            for (String part : parts) {
                if (part.length() > 0) {
                    capitalized.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase()).append(" ");
                }
            }
            return capitalized.toString().trim();
        }
        else
        {
            return "";
        }
    }
}
