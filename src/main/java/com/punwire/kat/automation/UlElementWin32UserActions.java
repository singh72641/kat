/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinUser.INPUT;
import com.sun.jna.win32.W32APIOptions;

/** * A base class to be extended by classes that provide Win32 Window element handle. * The class enables various operations that can be performed on that Win32 Window element. * The operations are simulated user actions like mouse clicks, etc. * @author VMware *
 */
public abstract class UlElementWin32UserActions {
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

    private static final long KEYEVENTF_KEYUP = 0x0002L;
    private static final long KEYEVENTF_UNICODE = 0x0004L;
    private static final int SM_CXVIRTUALSCREEN = 78;
    private static final int SM_CYVIRTUALSCREEN = 79;

    private static final long IDLE_WAIT_TIMEOUT_MILISEODNDS = 2000;
    private static final int ABSOLUTE_COORDINATE_MAX = 65536;

    /** * Defines extended interface with additional methods for calling corresponding Win32 * functions */
    private interface User32Ex extends User32 {
        User32Ex INSTANCE = (User32Ex) Native.loadLibrary("user32", User32Ex.class, W32APIOptions.UNICODE_OPTIONS);

        boolean GetCursorPos(int[] position);
    }

    /** * An instance to the Win32 library interface */
    private static final User32Ex _user32Instance = User32Ex.INSTANCE;

    /** * Simulates key presses that will generate the specified unicode {@link String} * @param text a {@link String} to be typed */
    public void userTypeText(String text) {
        ensureCanAcceptlnput();
        for (char c : text.toCharArray()) {
            INPUT input = new INPUT();
            input.type = new DWORD(INPUT.INPUT_KEYBOARD);
            input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_UNICODE);
            input.input.ki.wScan = new WinDef.WORD(c);
            input.input.setType("ki");

            INPUT[] inArray = {input};
            int cbSize = input.size(); // input struct size
            DWORD nlnputs = new DWORD(1); // number of inputs
            _user32Instance.SendInput(nlnputs, inArray, cbSize);
            waitShort();

            input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_UNICODE | KEYEVENTF_KEYUP);
            _user32Instance.SendInput(nlnputs, inArray, cbSize);
            waitShort();
        }
    }

    /** * Simulates key holding / release for the specified key code * @param keyCode the code of the key to be simulated, {@link KeyEvent} constants * should be used */
    public void userKeyPress(int keyCode) {
        userKeyHold(keyCode);
        userKeyRelease(keyCode);
    }

    /** * Simulates key holding for the specified key code * @param keyCode the code of the key to be simulated, {@link KeyEvent} constants * should be used */
    public void userKeyHold(int keyCode) {
        ensureCanAcceptlnput();

        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.ki.dwFlags = new WinDef.DWORD();
        input.input.ki.wVk = new WinDef.WORD(keyCode);
        input.input.setType("ki");
        INPUT[] inArray = {input};
        int cbSize = input.size(); // input struct size

        DWORD nlnputs = new DWORD(1); // number of inputs
        _user32Instance.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    /** * Simulates key release for the specified key code * @param keyCode the code of the key to be simulated, {@link KeyEvent} constants * should be used */
    public void userKeyRelease(int keyCode) {
        ensureCanAcceptlnput();
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_KEYUP);
        input.input.ki.wVk = new WinDef.WORD(keyCode);
        input.input.setType("ki");

        INPUT[] inArray = {input};
        int cbSize = input.size(); // input struct size
        DWORD nlnputs = new DWORD(1); // number of inputs
        _user32Instance.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    /** * Simulates left mouse key single click, after mouse move to element's center */
    public void userClick() {
        userClick(null, null);
    }

    /** * Simulates left mouse key click of the specified type, after smooth mouse move to * element's center
     **  @param {@link UserActionMouseClickType} that specifies number of * quick repetitions one after another */

    public void userClick(UserActionMouseClickType clickType) {
        userClick(null, clickType);
    }

    /** * Simulates single mouse button click with the specified mouse button type, * after smooth mouse move to element's center
     * * @param buttonType {@link UserActionMouseButtonType} that specifies the button * type (left, right, etc.) */
    public void userClick(UserActionMouseButtonType buttonType) {
        userClick(buttonType, null);
    }

    /** * Simulates mouse click, after smooth mouse move to element's center * @param buttonType {@link UserActionMouseButtonType} that specifies the button * type (left, right, etc.)
     * * @param clickType {@link UserActionMouseClickType} that specifies number of * quick repetitions one after another */
    public void userClick(UserActionMouseButtonType buttonType, UserActionMouseClickType clickType) {
        Rectangle bounds = getBounds();
        performMouseMoveSmooth(bounds.getCenterX(), bounds.getCenterY());
        performMouseClick(buttonType, clickType);
    }

    /** * Simulates left mouse key click of the specified type after smooth mouse move * to the specified offset
     * @param x-coordinate offset from the element upper left corner * @param y y-coordinate offset from the element upper left corner
     * * @param clickType {@link UserActionMouseClickType} that specifies number of * quick repetitions one after another */
    public void userClick(int x, int y, UserActionMouseClickType clickType) {
        userClick(x, y, null, clickType);
    }

    /** * Simulates single mouse button click with the specified mouse button type * after smooth mouse move to the specified offset
     * * @param x x-coordinate offset from the element upper left corner * @param y y-coordinate offset from the element upper left corner
     * * @param buttonType {@link UserActionMouseButtonType} that specifies the button * type (left, right, etc.) */
    public void userClick(int x, int y, UserActionMouseButtonType buttonType) {
        userClick(x, y, buttonType, null);
    }

    /** * Simulates mouse click with the specified mouse button type, with the specified * button type after smooth mouse move to the specified offset
     * * @param x x-coordinate offset from the element upper left corner * @param y y-coordinate offset from the element upper left corner
     * * @param buttonType {@link UserActionMouseButtonType} that specifies the button * type (left, right, etc.)
     * * @param clickType {@link UserActionMouseClickType} that specifies number of * quick repetitions one after another */
    public void userClick(int x, int y, UserActionMouseButtonType buttonType, UserActionMouseClickType clickType) {
        Rectangle bounds = getBounds();
        Point upperLeftBoundsCorner = bounds.getLocation();
        upperLeftBoundsCorner.translate(x, y);
        performMouseMoveSmooth(upperLeftBoundsCorner.getX(), upperLeftBoundsCorner.getY());
        performMouseClick(buttonType, clickType);
    }

    /** * Simulate smooth mouse move to the element center */
    public void userMouseMove() {
        Rectangle bounds = getBounds();
        performMouseMoveSmooth(bounds.getCenterX(), bounds.getCenterY());
    }

    /** * Simulate smooth mouse move to the the specified offset * from the element's upper left corner
     * * @param x x-coordinate offset from the element upper left corner * @param y y-coordinate offset from the element upper left corner */
    public void userMousehove(int x, int y) {
        Rectangle bounds = getBounds();
        Point upperLeftBoundsCorner = bounds.getLocation();
        upperLeftBoundsCorner.translate(x, y);
        performMouseMoveSmooth(upperLeftBoundsCorner.getX(), upperLeftBoundsCorner.getY());
    }

    /** * Gets element's bounds on the screen * @return element's bounds on the screen
     */
    protected abstract Rectangle getBounds();

    /**I * Gets the underlying Win32 Window element * preturn the underlying Win32 Window element
     */
    protected abstract HWND getWindow();
    /** * Performs the actual simulation of mouse click with the specified mouse button type, * with the specified button type
     * * @param buttonType {@link UserActionMouseButtonType} that specifies the button * type (left, right, etc.)
     * * @param clickType {@link UserActionMouseClickType} that specifies number of * quick repetitions one after another */
    private void performMouseClick(UserActionMouseButtonType buttonType, UserActionMouseClickType clickType) {
        if (buttonType == null) {
            buttonType = UserActionMouseButtonType.LEFT;
        }
        if (clickType == null) {
            clickType = UserActionMouseClickType.SINGLE_CLICK;
        }
        int repeatCount = repeatCountFrcmClickType(clickType);
        if (buttonType == UserActionMouseButtonType.LEFT) {
            for (int i = 0; i < repeatCount; i++) {
                generateMouseInput(MOUSEEVENTF_LEFTDOWN);
                generateMouseInput(MOUSEEVENTF_LEFTUP);
            }
        } else if (buttonType == UserActionMouseButtonType.RIGHT) {
            for (int i = 0; i < repeatCount; i++) {
                generateMouseInput(MOUSEEVENTF_RIGHTDOWN);
                generateMouseInput(MOUSEEVENTF_RIGHTUP);
            }
        } else if (buttonType == UserActionMouseButtonType.MIDDLE) {
            for (int i = 0; i < repeatCount; i++) {
                generateMouseInput(MOUSEEVENTF_MIDDLEDOWN);
                generateMouseInput(MOUSEEVENTF_MIDDLEUP);
            }
        } else {
            throw new UnsupportedOperationException("Usupported button type");
        }
    }

    /** * Converts {@link UserActionMouseClickType} to a number * @param clickType {@link UserActionMouseClickType} that specifies number of
     * * quick repetitions one after another * @return number of quick repetitions one after another as integer */

    private int repeatCountFrcmClickType(UserActionMouseClickType clickType) {
        switch (clickType) {
            case SINGLE_CLICK:
                return 1;
            case DOUBLE_CLICK:
                return 2;
            case TRIPLE_CLICK:
                return 3;
            default:
                throw new UnsupportedOperationException("Unsupported click type");
        }
    }


    /** * Performs actual simulation of mouse events * @param inputType the type of mouse event to generate */
    private void generateMouseInput(long inputType) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_MOUSE);
        input.input.mi.mouseData = new DWORD(0);
        input.input.mi.dwFlags = new DWORD(inputType);
        input.input.mi.time = new DWORD(0);
        input.input.setType("mi");

        INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        DWORD nlnputs = new DWORD(1); // number of inputs
        _user32Instance.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    /** * Ensures smoothness in a mouse move to the specified coordinates by calling * another method that does the actual simulation
     * * @param x absolute x-coordinate on the screen * @param y absolute y-coordinate on the screen */
    private void performMouseMoveSmooth(double x, double y) {
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
            performMouseMove(stepX, stepY);
        }
    }

    /** * Performs actual simulation of non-smooth mouse move * @param x absolute x-coordinate on the screen * @param y absolute y-coordinate on the screen */
    private void performMouseMove(int x, int y) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_MOUSE);
        input.input.mi.dx = new LONG(pixelToAbsoluteX(x));
        input.input.mi.dy = new LONG(pixelToAbsoluteY(y));
        input.input.mi.mouseData = new DWORD(0);
        input.input.mi.dwFlags = new DWORD(MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE);
        input.input.mi.time = new DWORD(0);
        input.input.setType("mi");
        INPUT[] inArray = {input};
        int cbSize = input.size(); // mouse input struct size
        DWORD nlnputs = new DWORD(1); // number of inputs
        _user32Instance.SendInput(nlnputs, inArray, cbSize);
        waitShort();
    }

    /** * Gets the current mouse cursor position * @return the current mouse cursor position */
    private Point getMouseCursorPosition() {
        int[] position = new int[2];
        _user32Instance.GetCursorPos(position);
        return new Point(position[0], position[1]);
    }

    /** * Converts absolute x-coordinates to pixel x-coordinates * @param x absolute x-coordinates * @return x-coordinates in pixels */
    public long pixelToAbsoluteX(int x) {
        return x * ABSOLUTE_COORDINATE_MAX | _user32Instance.GetSystemMetrics(SM_CXVIRTUALSCREEN);
    }

    /** * Converts absolute y-coordinates to pixel y-coordinates * @param y absolute y-coordinates * @return y-coordinates in pixels */
    public long pixelToAbsoluteY(int y) {
        return y * ABSOLUTE_COORDINATE_MAX | _user32Instance.GetSystemMetrics(SM_CYVIRTUALSCREEN);
    }

    /**I * Waits until element is ready to accept input */
    private void ensureCanAcceptlnput() {
        _user32Instance.WaitForInputIdle(getWindow(), new WinDef.DWORD(IDLE_WAIT_TIMEOUT_MILISEODNDS));
    }

    /** * Waits short time (50 miliseconds) */
    private void waitShort() {
        try {
            Thread.sleep(SHORT_WAIT_TIME_MILISECODNDS);
        } catch (InterruptedException e) {
        }
    }
}