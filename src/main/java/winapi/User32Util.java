package winapi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.IntByReference;
import lombok.Builder;
import lombok.Getter;

import static winapi.Kernel32Util.throwLastErrorIf;
import static winapi.Kernel32Util.throwLastErrorIfNot;

public interface User32Util extends Library {
    User32Util lib = Native.loadLibrary("user32", User32Util.class);

    static void registerHotKey(int id, int modifiers, int vk) throws Win32Exception {
        registerHotKey(null, id, modifiers, vk);
    }

    static void registerHotKey(HWND hWnd, int id, int modifiers, int vk) throws Win32Exception {
        throwLastErrorIfNot(User32.INSTANCE.RegisterHotKey(hWnd, id, modifiers, vk));
    }

    static void unregisterHotKey(int id) throws Win32Exception {
        unregisterHotKey(null, id);
    }

    // TODO: Tell JNA that it should be UnregisterHotKey(HWND, ...)
    static void unregisterHotKey(HWND hWnd, int id) throws Win32Exception {
        throwLastErrorIfNot(User32.INSTANCE.UnregisterHotKey(hWnd == null ? null : hWnd.getPointer(), id));
    }

    static MSG getMessage() throws Win32Exception {
        return getMessage(0);
    }

    static MSG getMessage(int windowMessage) throws Win32Exception {
        return getMessage(windowMessage, windowMessage);
    }

    static MSG getMessage(int minWindowMessage, int maxWindowMessage) throws Win32Exception {
        return getMessage(null, minWindowMessage, maxWindowMessage);
    }

    static MSG getMessage(HWND hWnd, int minWindowMessage, int maxWindowMessage) throws Win32Exception {
        MSG lpMsg = new MSG();
        throwLastErrorIf(0 > User32.INSTANCE.GetMessage(lpMsg, hWnd, minWindowMessage, maxWindowMessage));
        return lpMsg;
    }

    // TODO: Tell JNA that they've missed a function
    boolean WaitMessage(
    );

    static void waitMessage() throws Win32Exception {
        throwLastErrorIfNot(lib.WaitMessage());
    }

    static HWND getForegroundWindow() {
        return User32.INSTANCE.GetForegroundWindow();
    }

    @Getter @Builder
    class WindowThreadProcessId {
        private int threadId;
        private int processId;
    }

    static WindowThreadProcessId getWindowThreadProcessId(HWND hWnd) {
        IntByReference lpdwProcessId;

        int threadId = User32.INSTANCE.GetWindowThreadProcessId(
            hWnd,
            lpdwProcessId = new IntByReference()
        );

        return WindowThreadProcessId.builder()
            .processId(lpdwProcessId.getValue())
            .threadId(threadId)
            .build();
    }
}
