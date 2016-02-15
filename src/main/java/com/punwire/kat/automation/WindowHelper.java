/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class WindowHelper {
    private static final int BM_GETCHECK = 0x00F0;
    private static final long BST_CHECKED = 1;
    private static final long BST_UNCHECKED = 0;
    private static final int WS_DISABLED = 0x08000000;
    private static final int READ_BUFFER_MAX_LENGTH = 1024;
    /** * Defines extended interface with additional methods for calling corresponding Win32 * functions */
    private interface User32Ex extends User32 {
        User32Ex INSTANCE = (User32Ex) Native.loadLibrary(User32Ex.class, W32APIOptions.UNICODE_OPTIONS);

        long SendMessageA(Pointer hWnd, int msg, int num1, int num2);

        long SendMessageA(PointerType hWnd, int msg, int num1, int num2);

        HWND FindWindowEx(HWND hwndParent, HWND hwndChildAfter, String lpClassName, String lpWindowName);
    }

    private static final User32Ex _user32Instance = User32Ex.INSTANCE;
    private static final User32 user32 = User32.INSTANCE;

    /** * Find Window element that matches specified criteria
     * * @param window a parent Window element
     * * @param clsName name of the class of the Window element
     * * @param caption caption of the Window
     * * @param index an index / sequence number of the Window to return after finding
     * * @return the target Window element */
    public static WinDef.HWND findWindow(WinDef.HWND window, String clsName, String caption, Integer index) {
        if (index != null && index < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }
        if (clsName == null && caption == null && index == null) {
            throw new IllegalArgumentException("At least one criteria must be specified");
        }

        if (window == null) {
            return findParentWindow(clsName, caption, index);
        }

        for (WinDef.HWND child : getWindowChildren(window)) {
            boolean isClassNameMatch = false;
            boolean isCaptionMatch = false;
            boolean islndexMatch = false;
            if (clsName == null || clsName.equals(getWindowClassName(child))) {
                isClassNameMatch = true;
            }
            if (caption == null || caption.equals(getWindowCaption(child))) {
                isCaptionMatch = true;
            }
            if (index == null || index == 0) {
                islndexMatch = true;
            } else if (isClassNameMatch && isCaptionMatch) {
                index--;
            }
            if (isClassNameMatch && isCaptionMatch && islndexMatch) {
                return child;
            }
        }
        return null;
    }
    /** * Gets a Window caption * @param window the target Window * @return a Window caption */
    public static String getWindowCaption(WinDef.HWND window) {
        char[] readBuffer = new char[READ_BUFFER_MAX_LENGTH];
        int textSize = _user32Instance.GetWindowText(window, readBuffer, readBuffer.length);
        if (textSize == 0) {
            return "";
        }
        return new String(readBuffer, 0, textSize);
    }
    /** * Gets a Window class name * @param window the target Window * @return a Window class name */
    public static String getWindowClassName(WinDef.HWND window) {
        char[] readBuffer = new char[READ_BUFFER_MAX_LENGTH];
        int textSize = _user32Instance.GetClassName(window, readBuffer, readBuffer.length);
        if (textSize == 0) {
            return null;
        }
        return new String(readBuffer, 0, textSize);
    }

    /** * Gets Window bounds * @param window the target Window * @return Window bounds */
    public static Rectangle getWindowBounds(WinDef.HWND window) {
        WinUser.WINDOWINFO windowInfo = new WinUser.WINDOWINFO();
        _user32Instance.GetWindowInfo(window, windowInfo);
        return windowInfo.rcWindow.toRectangle();
    }

    /** * Gets a value indicating whether a checkable Window is checked * @param window the target Window * @return a value indicating whether a checkable Window is checked */
    public static Boolean isWindowChecked(WinDef.HWND window) {
        long result = _user32Instance.SendMessageA(window, BM_GETCHECK, 0, 0);
        if (result == BST_CHECKED) {
            return true;
        } else if (result == BST_UNCHECKED) {
            return false;
        }
        return null;
    }

    /** * Gets a value indicating whether a Window is enabled * @param window the target Window * @return a value indicating whether a Window is enabled */
    public static Boolean isWindowEnabled(WinDef.HWND window) {
        WinUser.WINDOWINFO windowInfo = new WinUser.WINDOWINFO();
        boolean result = _user32Instance.GetWindowInfo(window, windowInfo);
        if (result == false) {
            return null;
        }
        return (windowInfo.dwStyle & WS_DISABLED) == 0;
    }

    /** * Gets a value indicating whether a Window is valid * @param window the target Window
     * * @return a value indicating whether a Window is valid, false if the specified Window
     * * has been invalidated at some point */
    public static boolean isWindowValid(WinDef.HWND window) {
        WinUser.WINDOWINFO windowInfo = new WinUser.WINDOWINFO();
        return _user32Instance.GetWindowInfo(window, windowInfo);
    }
    /** * Find Window element that is parent (top-level) and that matches specified criteria
     * * @param clsName name of the class of the Window element
     * * @param caption caption of the Window
     * * @param index an index / sequence number of the Window to return after finding
     * * @return the target Window element */
    public static WinDef.HWND findParentWindow(String clsName, String caption, Integer index) {
        WinDef.HWND window = _user32Instance.FindWindow(clsName, caption);
        if (index != null) {
            for (int i = 0; window != null && i < index; i++) {
                window = _user32Instance.FindWindowEx(null, window, clsName, caption);
            }
        }
        return window;
    }

    /** * Find all Window elements that are children of the specified parent Window
     * * @param window the parent Window
     * * @return a {@link List) of all children of the specified Window element */

    public static List<WinDef.HWND> getWindowChildren(WinDef.HWND window) {
        WindowEnumeratorCallback callback = new WindowEnumeratorCallback();
        _user32Instance.EnumChildWindows(window, callback, null);
        return callback.getWindows();
    }

    public static List<String> getAllWindowNames() {
        final List<String> windowNames = new ArrayList<String>();
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            @Override
            public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
                char[] windowText1 = new char[512];
                user32.GetWindowText(hwnd, windowText1, 512);
                String wText = Native.toString(windowText1).trim();
                if (!wText.isEmpty()) {
                    windowNames.add(wText);
                }
                return true;
            }

        }, null);

        return windowNames;
    }

    public static boolean windowExists(final String startOfWindowName) {
        return !user32.EnumWindows(new User32.WNDENUMPROC() {

            @Override
            public boolean callback(WinDef.HWND hwnd, Pointer userData) {
                char[] windowText1 = new char[512];
                user32.GetWindowText(hwnd, windowText1, 512);
                String wText = Native.toString(windowText1).trim();
                if (!wText.isEmpty() && wText.startsWith(startOfWindowName)) {
                    return false;
                }
                return true;
            }
        }, null);
    }

    public static WinDef.HWND findWindowByName(String title) {
        final User32 user32 = User32.INSTANCE;
        final List<WinDef.HWND> windowNames = new ArrayList<WinDef.HWND>();
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            @Override
            public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
                char[] windowText1 = new char[512];
                user32.GetWindowText(hwnd, windowText1, 512);
                String wText = Native.toString(windowText1).trim();
                if (!wText.isEmpty() && wText.equals(title.trim())) {
                    windowNames.add(hwnd);
                }
                return true;
            }

        }, null);
        if( windowNames.size() > 0 ) return windowNames.get(0);
        return null;
    }


    public static boolean setForegroundWindow(WinDef.HWND hWnd) {
        return user32.SetForegroundWindow(hWnd);
    }

    public static WinDef.HWND getForegroundWindow() {
        return user32.GetForegroundWindow();
    }
}