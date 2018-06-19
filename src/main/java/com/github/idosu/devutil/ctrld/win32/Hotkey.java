package com.github.idosu.devutil.ctrld.win32;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinUser.MSG;
import winapi.User32Util;

import static com.sun.jna.platform.win32.WinUser.WM_HOTKEY;

public interface Hotkey {
    int getModifiers();
    int getVirtualKey();

    void register() throws Win32Exception;
    void unregister() throws Win32Exception;
    MSG waitForMessage() throws Win32Exception;

    static Hotkey of(int id, int modifiers, int virtualKey) {
        return new  Hotkey() {
            boolean registered = false;

            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public int getVirtualKey() {
                return virtualKey;
            }

            @Override
            public void register() throws Win32Exception {
                if (!registered) {
                    User32Util.registerHotKey(id, modifiers, virtualKey);
                    registered = true;
                }
            }

            @Override
            public void unregister() throws Win32Exception {
                if (registered) {
                    User32Util.unregisterHotKey(id);
                    registered = false;
                }
            }

            @Override
            public MSG waitForMessage() throws Win32Exception {
                User32Util.waitMessage();
                return User32Util.getMessage(WM_HOTKEY);
            }
        };
    }
}
