/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;
import javafx.scene.input.KeyCode;

import java.awt.*;

/**
 * Created by KanwalJ on 9/22/2014.
 */
public class KRobot {
    private static final int SHORT_WAIT_TIME_MILISECODNDS = 50;
    private static final int MOUSE_MOVE_SMOOTH_INITIAL_FACTOR = 5;

    private static final long MOUSEEVENTF_MOVE = 0x0001L;
    private static final long MOUSEEVENTF_VIRTUALDESK = 0x4000L;
    private static final long MOUSEEVENTF_ABSOLUTE = 0x8000L;
    private static final long MOUSEEVENTF_LEFTDOWN = 0x0002L;
    private static final long MOUSEEVENTF_LEFTUP = 0x0004L;
    private static final long MOUSEEVENTF_RIGHTDOWN = 0x0008L;
    private static final long MOUSEEVENTF_RIGHTUP = 0x0010L;
    private static final long MOUSEEVENTF_MIDDLEDOWN = 0x0020L;
    private static final long MOUSEEVENTF_MIDDLEUP = 0x0040L;
    private static final int ABSOLUTE_COORDINATE_MAX = 65535;
    static User32 user32 = User32.INSTANCE;
    private static final User32Ex user32Ex = User32Ex.INSTANCE;
    public WinDef.HWND window;
    public Robot robot;

    public KRobot(WinDef.HWND window)
    {
        try{
            //WinDef.RECT rect = new WinDef.RECT();
            //user32.GetWindowRect(window, rect);
            robot = new Robot();

        } catch ( Exception ex)
        {

        }
        this.window = window;
    }

    /** * Defines extended interface with additional methods for calling corresponding Win32 * functions */
    private interface User32Ex extends User32 {
        User32Ex INSTANCE = (User32Ex) Native.loadLibrary("user32", User32Ex.class, W32APIOptions.UNICODE_OPTIONS);

        boolean GetCursorPos(int[] position);
    }

    public void mouseMove(int x, int y)
    {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
        input.input.mi.dx = new WinDef.LONG( pixelToAbsoluteX(x));
        input.input.mi.dy = new WinDef.LONG(pixelToAbsoluteY(y));
        input.input.mi.mouseData = new WinDef.DWORD(0);
        input.input.mi.dwFlags = new WinDef.DWORD(MOUSEEVENTF_ABSOLUTE  | MOUSEEVENTF_MOVE);
        input.input.mi.time = new WinDef.DWORD(0);
        input.input.setType("mi");
        WinUser.INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        WinDef.DWORD nlnputs = new WinDef.DWORD(1); // number of inputs
        user32.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    public void setFocus()
    {
        user32.SetFocus(window);
        user32.SetForegroundWindow(window);
    }


    public void leftClick()
    {
       generateMouseInput(MOUSEEVENTF_LEFTDOWN);
       generateMouseInput(MOUSEEVENTF_LEFTUP);

    }

    public void leftClick(int num) {
        for(int i=0;i<num;i++)
        {
            leftClick();
        }
    }

    public void doubleClick()
    {
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
        generateMouseInput(MOUSEEVENTF_LEFTUP);
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
        generateMouseInput(MOUSEEVENTF_LEFTUP);
    }

    public void trippleClick()
    {
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
        generateMouseInput(MOUSEEVENTF_LEFTUP);
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
        generateMouseInput(MOUSEEVENTF_LEFTUP);
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
        generateMouseInput(MOUSEEVENTF_LEFTUP);
    }

    public void mouseMoveSmooth(double x, double y) {
        Point point = getMouseCursorPosition();
        double targetX = Math.round(x);
        double targetY = Math.round(y);
        double factorX = MOUSE_MOVE_SMOOTH_INITIAL_FACTOR;
        double factorY = MOUSE_MOVE_SMOOTH_INITIAL_FACTOR;
        while (targetX != point.getX() || targetY != point.getY()) {
            double diffX = targetX - point.getX();
            double diffY = targetY - point.getY();
            int stepX = (int) targetX;
            int stepY = (int) targetY;
            if (Math.abs(diffX) < factorX) {
                factorX = Math.abs(diffX);
            }
            if (factorX >= 2) {
                stepX = (int) (point.getX() + diffX / factorX);
            }
            if (Math.abs(diffY) < factorY) {
                factorY = Math.abs(diffY);
            }
            if (factorY >= 2) {
                stepY = (int) (point.getY() + diffY / factorY);
            }
            point.move(stepX, stepY);
            mouseMove(stepX, stepY);
        }
    }

    public Point getMouseCursorPosition() {
        int[] position = new int[2];
        user32Ex.GetCursorPos(position);
        return new Point(position[0], position[1]);
    }


    public void press()
    {
        generateMouseInput(MOUSEEVENTF_LEFTDOWN);
    }

    public void release()
    {
        generateMouseInput(MOUSEEVENTF_LEFTUP);
    }

    private void generateMouseInput(long inputType) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
        input.input.mi.mouseData = new WinDef.DWORD(0);
        input.input.mi.dwFlags = new WinDef.DWORD(inputType);
        input.input.mi.time = new WinDef.DWORD(0);
        input.input.setType("mi");

        WinUser.INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        WinDef.DWORD nlnputs = new WinDef.DWORD(1); // number of inputs
        user32.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    public static long pixelToAbsoluteX(int x) {
        return x * ABSOLUTE_COORDINATE_MAX / user32.GetSystemMetrics(0);
    }

    /** * Converts absolute y-coordinates to pixel y-coordinates * @param y absolute y-coordinates * @return y-coordinates in pixels */
    public static long pixelToAbsoluteY(int y) {
        return y * ABSOLUTE_COORDINATE_MAX / user32.GetSystemMetrics(1);
    }

    public void drag(int x, int y)
    {
        press();
        mouseMoveSmooth(x,y);
        release();
    }

    public static void waitShort() {
        try {
            Thread.sleep(SHORT_WAIT_TIME_MILISECODNDS);
        } catch (InterruptedException e) {
        }
    }

    public void keyUp(KeyCode keyCode)
    {

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.ki.wVk = new WinDef.WORD(Long.valueOf(""+keyCode.impl_getCode()));
        input.input.ki.wScan = new WinDef.WORD(0);
        input.input.ki.dwFlags = new WinDef.DWORD(2);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
        input.input.setType("ki");
        WinUser.INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        WinDef.DWORD nlnputs = new WinDef.DWORD(1); // number of inputs
        user32.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    public void keyDown(KeyCode keyCode)
    {

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.ki.wVk = new WinDef.WORD(Long.valueOf(""+keyCode.impl_getCode()));
        input.input.ki.wScan = new WinDef.WORD(0);
        input.input.ki.dwFlags = new WinDef.DWORD(0);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
        input.input.setType("ki");
        WinUser.INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        WinDef.DWORD nlnputs = new WinDef.DWORD(1); // number of inputs
        user32.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    public void keyPress(KeyCode keyCode)
    {
        keyDown(keyCode);
        keyUp(keyCode);
//
//        WinUser.INPUT downInput = new WinUser.INPUT();
//        downInput.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
//        downInput.input.ki.wVk = new WinDef.WORD(Long.valueOf(""+keyCode.impl_getCode()));
//        downInput.input.ki.wScan = new WinDef.WORD(0);
//        downInput.input.ki.dwFlags = new WinDef.DWORD(0);
//        downInput.input.ki.time = new WinDef.DWORD(0);
//        downInput.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
//        downInput.input.setType("ki");
//        WinUser.INPUT[] inArray = {downInput, upInput};
//        int cbSize = upInput.size() + downInput.size(); // mouse input struct size
//        WinDef.DWORD nlnputs = new WinDef.DWORD(2); // number of inputs
//        user32.SendInput(nlnputs, inArray, cbSize);
//        waitShort();
    }

}
