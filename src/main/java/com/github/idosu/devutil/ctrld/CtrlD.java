package com.github.idosu.devutil.ctrld;

import com.github.idosu.devutil.ctrld.win32.Hotkey;
import com.github.idosu.devutil.ctrld.win32.Process;
import com.github.idosu.devutil.ctrld.win32.Window;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Robot;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.sun.jna.platform.win32.WinUser.MOD_CONTROL;
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

            Hotkey hotkey = Hotkey.of(1337, MOD_CONTROL, VK_D);

            hotkey.register();
            try {
                // noinspection InfiniteLoopStatement
                while (true) {
                    hotkey.waitForMessage();
                    System.out.println("User hit Ctrl+D");

                    if (isForegroundProcessCmd()) {
                        System.out.println("User is inside cmd, killing cmd...");
                        exitCmd();
                    } else {
                        System.out.println("User is not inside a cmd, resending Ctrl+D...");
                        hotkey.unregister();
                        // Ctrl is already pressed just need to add the d
                        pressKey(robot, VK_D);
                        hotkey.register();
                        hotkey.clearMesseges();
                    }
                }
            } finally {
                hotkey.unregister();
            }
        } catch (Throwable t) {
            showError(t);
        }
    }

    private static boolean isForegroundProcessCmd() {
        Window foregroundWindow = Window.getForegroundWindow();

        try (Process process = foregroundWindow.openProcess(PROCESS_QUERY_INFORMATION.or(PROCESS_VM_READ), false)) {
            String imageFileName = process.getImageFileName();

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
