package com.punwire.kat.chart2;


import java.awt.*;
import java.util.Random;

public class ColorGenerator
{

    private static Random rand = new Random();

    private ColorGenerator()
    {}

    public static Color getRandomColor()
    {
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    public static Color getTransparentColor(Color color, int alpha)
    {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha);
    }

}