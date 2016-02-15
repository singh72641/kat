/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.punwire.kat.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

/**
 * * Used when enumerating Window-s and filtering them by given criteria * @author %/Aware
 *
 */
public class WindowEnumeratorCallback implements WinUser.WNDENUMPROC {
    private final List<HWND> _windows = new ArrayList<HWND>();
    private final Set<Integer> _ids;
    private final boolean _onlyVisible;
    private final int _maximum;
    private final User32 _user32 = User32.INSTANCE;

    /**
     * Construct new instance
     */
    public WindowEnumeratorCallback() {
        this(null, false, 0);
    }

    /**
     * Construct new instance
     *
     * @param ids capture only Window-s with the specified process IDs
     */
    public WindowEnumeratorCallback(Set<Integer> ids) {
        this(ids, false, 0);
    }

    /**
     * Construct new instance * @param ids capture only Window-s with the specified process IDs * @param onlyVisible capture only visible Window-s
     */
    public WindowEnumeratorCallback(Set<Integer> ids, Boolean onlyVisible) {
        this(ids, onlyVisible, 0);
    }

    /**
     * Construct new instance * @param ids capture only Window-s with the specified process IDs * @param onlyVisible capture only visible Window-s
     *
     * @param maximum stop enumeration once the specified limit is reached
     */
    public WindowEnumeratorCallback(Set<Integer> ids, boolean onlyVisible, int maximum) {
        _ids = ids;
        _onlyVisible = onlyVisible;
        _maximum = maximum;
    }

    /**
     * Gets the enumerated Window-s * @return the enumerated Window-s
     */
    public List<HWND> getWindows() {
        return _windows;
    }

    /* * (non-Javadoc) */
    @Override
    public boolean callback(HWND window, Pointer parameter) {
        if (_onlyVisible && !_user32.IsWindowVisible(window)) {
            return true;
        }
        if (_ids != null) {
            IntByReference processId = new IntByReference();
            _user32.GetWindowThreadProcessId(window, processId);
            if (!_ids.contains(processId.getValue())) {
                return true;
            }
        }
        _windows.add(window);
        if (_windows.size() == _maximum) {
            return false;
        }
        return true;
    }
}