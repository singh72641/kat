/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import java.awt.Rectangle;

import com.punwire.kat.automation.UlElementWin32UserActions;
import com.punwire.kat.automation.WindowHelper;
import com.sun.jna.platform.win32.WinDef.HWND;

/** * A base class that wraps UI element * @author Vhware
 *
 */

public class UIElementWrapper extends UlElementWin32UserActions {
    private HWND _window;

    public UIElementWrapper(HWND parent, String nameOfClass, String caption, Integer index) {
        HWND window = WindowHelper.findWindow(parent, nameOfClass, caption, index);
        setWindow(window);
    }

    public UIElementWrapper(HWND handle) {
        setWindow(handle);
    }

    /* * (non-Javadoc) */
    @Override
    public Rectangle getBounds() {
        return WindowHelper.getWindowBounds(getValidWindow());
    }

    /**
     * Gets the value indicating a checkable Win32 Window element is checked * @return the value indicating a checkable Win32 Window element is checked
     */
    public boolean isChecked() {
        return WindowHelper.isWindowChecked(getValidWindow());
    }

    /**
     * Gets the value indicating a Win32 Window element is enabled * @return the value indicating a Win32 Window element is enabled
     */
    public boolean isEnabled() {
        return WindowHelper.isWindowEnabled(getValidWindow());
    }

    /**
     * Gets the Win32 Window element caption * @return the Win32 Window element caption
     */
    public String getCaption() {
        return WindowHelper.getWindowCaption(getValidWindow());
    }

    /* * (non-Javadoc) */
    @Override
    protected HWND getWindow() {
        return getValidWindow();
    }

    /**
     * Gets the underlying Win32 Window element * @param validate whether to validate if the current value is non-null * @return the underlying Win32 Window element
     */
    protected HWND getWindow(boolean validate) {
        if (validate == true) {
            return getValidWindow();
        }
        return _window;
    }

    /**
     * Sets the underlying Win32 Window element * @param window the underlying Win32 Window element
     */
    protected void setWindow(HWND window) {
        _window = window;
    }

    /**
     * Gets the underlying Win32 Window element with performing validation * currently it is non-null * @return the underlying Win32 Window element
     */
    protected HWND getValidWindow() {
        if (_window == null) {
            throw new IllegalStateException("Not initialized (null Window instance)");
        }
        if (WindowHelper.isWindowValid(_window) == false) {
            throw new IllegalStateException("Window is no longer valid");
        }
        return _window;
    }
}