package com.zebra.koncierge50;

import com.zebra.sdk.common.card.graphics.enumerations.TextAlignment;

import org.w3c.dom.Text;

public enum ETextAlignmentString {
    Left("Left"),
    Center("Center"),
    Right("Right"),
    Top("Top"),
    Bottom("Bottom");

    private String value;

    ETextAlignmentString(String stringValue)
    {
        value = stringValue;
    }

    public TextAlignment toTextAlignment()
    {
        switch(this)
        {
            case Left:
                return TextAlignment.Left;
            case Center:
                return TextAlignment.Center;
            case Right:
                return TextAlignment.Right;
            case Top:
                return TextAlignment.Top;
            case Bottom:
                return TextAlignment.Bottom;
        }
        return TextAlignment.Center;
    }

    public static ETextAlignmentString fromString(String stringValue)
    {
        switch(stringValue)
        {
            case "Left":
                return Left;
            case "Center":
                return Center;
            case "Right":
                return Right;
            case "Top":
                return Top;
            case "Bottom":
                return Bottom;
        }
        return Center;
    }

    public String toString()
    {
        return value;
    }
}
