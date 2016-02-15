/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import net.sourceforge.tess4j.Tesseract;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KanwalJ on 9/22/2014.
 */
public class KWinAuto {
    public static Screen screen;
    static Tesseract instance;
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    HashMap<String, Point> refs = new HashMap<String, Point>();

    public String currRes = "RES19";

    public KWinAuto() {
        Settings.OcrTextRead = false;
        Settings.OcrTextSearch = false;


        try {
            screen = new Screen(0);
            instance = Tesseract.getInstance();
            instance.setTessVariable("load_system_dawg","0");
            instance.setTessVariable("load_freq_dawg","0");
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        refs.put("RES32", new Point(3200,1800));
        refs.put("RES19", new Point(1920,1080));

    }

    public void saveScreenShot() throws IOException
    {
        Point maxPoint = refs.get(currRes);
        ScreenImage img1 = screen.capture(0,0,maxPoint.x,maxPoint.y);
        ImageIO.write(img1.getImage(), "PNG", new File(img("screenshot.png")));
    }

    public void takeShot(int x, int y, int x2, int y2) throws IOException
    {
        Rectangle rect = new Rectangle(x,y,Math.abs(x2-x), Math.abs(y2-y));
        ScreenImage img1 = screen.capture(rect);
        ImageIO.write(img1.getImage(), "PNG", new File(img("takeShot.png")));
    }

    public Location findPosition(String image) throws Exception
    {
        try{
            Match f = screen.find(img(image+".png"));
            return f.getTopLeft();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public String getClip() throws Exception
    {
        String out = "";

        Transferable data = clipboard.getContents(null);
        if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            out = ((String)(data.getTransferData(DataFlavor.stringFlavor)));
        }
        return out;
    }

    public void setClip(String content){
        clipboard.setContents(new StringSelection(content), null);
    }

    public void pressEnter()
    {
        screen.keyDown(Key.ENTER);
        screen.keyUp(Key.ENTER);
    }

    public void copyClip()
    {
        screen.keyDown(Key.CTRL);
        screen.type("c");
        screen.keyUp(Key.CTRL);
    }
    public void type(String text, int mod){
        screen.type(text,mod);
    }

    public void type(String text){
        screen.type(text);
    }

    public void saveImage(ScreenImage img1, String name) throws IOException
    {
        ImageIO.write(img1.getImage(), "PNG", new File(img(name + ".png")));
    }

    public int getPid() throws Exception {
        Process p = Runtime.getRuntime().exec("tasklist");

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String line;
        int pid = 0;
        Region r = null;
        while ((line = reader.readLine()) != null) {
            line = line.replace("  ", " ");
            line = line.replace("  ", " ");
            line = line.replace("  ", " ");
            line = line.replace("  ", " ");
            if (line.contains("pi.exe")) {
                System.out.println(line);
                String[] parts = line.split(" ");
                System.out.println(parts[0]);
                System.out.println(parts[1]);
                pid = Integer.valueOf(parts[1]);
            }
        }
        return pid;
    }

    public void getText(Location loc, int width, int height) {
        try {
            Region r3 = screen.newRegion(loc, width, height);
            ScreenImage img1 = screen.capture(r3);

            // JNA Interface Mapping
            String result = instance.doOCR(img1.getImage());
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String img(String name) {
        return "c:\\projects\\ktrade\\img\\" + name;
    }

    public void printEnv() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n",
                    envName,
                    env.get(envName));
        }
    }

}
