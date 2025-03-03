package com.zebra.koncierge50;

import java.util.EnumMap;

public class CSVDataModel
{
    public String Nom;
    public String Prenom;
    public String Societe;
    public String Email;
    public String Mobile;
    public String Fonction;
    public Boolean VIP;
    public String _AllText;
    public String VCARD;

    public String createVCard()
    {
        if(Nom.isEmpty() == false && Prenom.isEmpty() == false && Societe.isEmpty() == false)
        {
            VCARD = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "N:"+ Nom + ";"+ Prenom + "\n" +
                    "FN:" + Prenom + " " + Nom + "\n";
                    if(Mobile != null && Mobile.isEmpty() == false)
                        VCARD += "TEL;CELL:"+Mobile+"\n";
                    if(Email != null && Email.isEmpty() == false)
                        VCARD += "EMAIL;WORK;INTERNET:"+ Email + "\n";
                    if(Fonction != null && Fonction.isEmpty() == false)
                        VCARD += "TITLE:"+ Fonction + "\n" +
                    "ORG:"+ Societe + "\n" +
                    "END:VCARD\n";
        }
        else
            VCARD = null;
        return VCARD;
    }
}