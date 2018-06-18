package com.github.idosu.devutil.ctrld;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import winapi.Kernel32Util;
import winapi.Kernel32Util.Handle;
import winapi.User32Util;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Robot;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.sun.jna.platform.win32.WinUser.MOD_CONTROL;
import static com.sun.jna.platform.win32.WinUser.WM_HOTKEY;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_X;
import static winapi.Kernel32Util.ProcessAccess.PROCESS_QUERY_INFORMATION;
import static winapi.Kernel32Util.ProcessAccess.PROCESS_VM_READ;

public class CtrlD {
    private static Robot robot;

    public static void main(String[] args) {
        try {
            robot = new Robot();

            int id = 1337;
            User32Util.registerHotKey(id, MOD_CONTROL, VK_D);

            try {
                // noinspection InfiniteLoopStatement
                while (true) {
                    User32Util.waitMessage();
                    System.out.println("User hit Ctrl+D");
                    User32Util.getMessage(WM_HOTKEY);

                    if (isForegroundProcessCmd()) {
                        System.out.println("User is inside cmd, killing cmd...");
                        exitCmd();
                    }
                }
            } finally {
                User32Util.unregisterHotKey(id);
            }
        } catch (Throwable t) {
            showError(t);
        }
    }

    private static boolean isForegroundProcessCmd() {
        HWND foregroundWindow = User32Util.getForegroundWindow();
        int processId = User32Util.getWindowThreadProcessId(foregroundWindow).getProcessId();

        try (Handle<HANDLE> process = Kernel32Util.openProcess(PROCESS_QUERY_INFORMATION.or(PROCESS_VM_READ), false, processId)) {
            String imageFileName = Kernel32Util.getProcessImageFileName(process);

            // TODO: Do it in a configurable way
            return imageFileName.endsWith("\\Windows\\System32\\cmd.exe")
                || imageFileName.endsWith("powershell.exe");
        }
    }

    private static void exitCmd() {
        // Need to release the ctrl of the user(ctrl+d)
        robot.keyRelease(VK_CONTROL);
        pressKeys(robot, VK_E, VK_X, VK_I, VK_T, VK_ENTER);
        // Need to reset the ctrl of the user(ctrl+d)
        robot.keyPress(VK_CONTROL);
    }

    private static void pressKey(Robot robot, int keycode) {
        robot.keyPress(keycode);
        robot.keyRelease(keycode);
    }

    private static void pressKeys(Robot robot, int... keycodes) {
        for (int keycode : keycodes) {
            pressKey(robot, keycode);
        }
    }

    private static void showError(Throwable throwable) {
        try {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        } catch (Exception e) {
            // Ignoring error because it is just look and feel
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        throwable.printStackTrace(new PrintStream(bytes));
        JOptionPane.showMessageDialog(null, new String(bytes.toByteArray()), "cmd ctrl+d support", JOptionPane.ERROR_MESSAGE);
    }
}
