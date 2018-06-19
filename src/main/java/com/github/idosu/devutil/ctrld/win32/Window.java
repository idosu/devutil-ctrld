package com.github.idosu.devutil.ctrld.win32;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import winapi.Kernel32Util.ProcessAccess;
import winapi.User32Util;
import winapi.User32Util.ThreadProcessId;

public interface Window {
    ThreadProcessId getThreadProcessId() throws Win32Exception;

    default int getProcessId() throws Win32Exception {
        return getThreadProcessId().getProcessId();
    }

    default Process openProcess(ProcessAccess desiredAccess, boolean inheritHandle) {
        return Process.open(desiredAccess, inheritHandle, getProcessId());
    }

    default int getThreadId() throws Win32Exception {
        return getThreadProcessId().getThreadId();
    }

    static Window getForegroundWindow() {
        return new Window() {
            HWND foregroundWindow = User32Util.getForegroundWindow();

            @Override
            public ThreadProcessId getThreadProcessId() throws Win32Exception {
                return User32Util.getWindowThreadProcessId(foregroundWindow);
            }
        };
    }
}
