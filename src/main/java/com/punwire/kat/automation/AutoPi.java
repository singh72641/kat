package com.punwire.kat.automation;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.input.KeyCode;

import java.util.List;

/**
 * Created by Kanwal on 23/01/16.
 */
public class AutoPi {

    User32 user32 = User32.INSTANCE;
    public KRobot robo;
    public KWinAuto auto;

    public void init() {
        getWindow();
    }
    public void getWindow() {
        //List<String> windows = WindowHelper.getAllWindowNames();
        WinDef.HWND piWindow = WindowHelper.findWindowByName("Pi");
        char[] className = new char[512];
        System.out.println(user32.GetClassName(piWindow,className,512));

        //UIElementWrapper wrapper = new UIElementWrapper(null,"IEFrame","Stock Charts - Internet Explorer",null);
        //UIElementWrapper wrapper = new UIElementWrapper(null, "Pi", "Pi", null);
        UIElementWrapper wrapper = new UIElementWrapper(piWindow);
        System.out.println(wrapper.getCaption());
        robo = new KRobot(wrapper.getWindow());
        auto = new KWinAuto();
        WindowHelper.setForegroundWindow(piWindow);
        robo.setFocus();
        robo.keyDown(KeyCode.A);
    }

    public static void main(String[] args){
        try {
            AutoPi t = new AutoPi();
            t.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
