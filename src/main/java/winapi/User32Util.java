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
        return getMessage(null);
    }

    static MSG getMessage(HWND hWnd) throws Win32Exception {
        return getMessage(hWnd, 0);
    }

    static MSG getMessage(int windowMessage) throws Win32Exception {
        return getMessage(null, windowMessage);
    }

    static MSG getMessage(HWND hWnd, int windowMessage) throws Win32Exception {
        return getMessage(hWnd, windowMessage, windowMessage);
    }

    static MSG getMessage(int minWindowMessage, int maxWindowMessage) throws Win32Exception {
        return getMessage(null, minWindowMessage, maxWindowMessage);
    }

    static MSG getMessage(HWND hWnd, int minWindowMessage, int maxWindowMessage) throws Win32Exception {
        MSG lpMsg = new MSG();
        throwLastErrorIf(0 > User32.INSTANCE.GetMessage(lpMsg, hWnd, minWindowMessage, maxWindowMessage));
        return lpMsg;
    }

    interface RemoveMessage {
        /** Messages are not removed from the queue after processing by PeekMessage. */
        RemoveMessage PM_NOREMOVE = proc(0x0000);
        /** Messages are removed from the queue after processing by PeekMessage. */
        RemoveMessage PM_REMOVE = proc(0x0001);
        /**
         * Prevents the system from releasing any thread that is waiting for the caller to go idle (see WaitForInputIdle).
         * Combine this value with either PM_NOREMOVE or PM_REMOVE.
         */
        RemoveMessage PM_NOYIELD = proc(0x0002);

        int code();

        static RemoveMessage proc(int code) {
            return () -> code;
        }

        default RemoveMessage and(RemoveMessage other) {
            return () -> code() & other.code();
        }

        default RemoveMessage or(RemoveMessage other) {
            return () -> code() | other.code();
        }

        default RemoveMessage xor(RemoveMessage other) {
            return () -> code() ^ other.code();
        }

        default RemoveMessage not() {
            return () -> ~code();
        }
    }

    static MSG peekMessage(RemoveMessage removeMessage) {
        return peekMessage(null, removeMessage);
    }

    static MSG peekMessage(HWND hWnd, RemoveMessage removeMessage) {
        return peekMessage(hWnd, 0, removeMessage);
    }

    static MSG peekMessage(int windowMessage, RemoveMessage removeMessage) {
        return peekMessage(null, windowMessage, removeMessage);
    }

    static MSG peekMessage(HWND hWnd, int windowMessage, RemoveMessage removeMessage) {
        return peekMessage(hWnd, windowMessage, windowMessage, removeMessage);
    }

    static MSG peekMessage(int minWindowMessage, int maxWindowMessage, RemoveMessage removeMessage) {
        return peekMessage(null, minWindowMessage, maxWindowMessage, removeMessage);
    }

    static MSG peekMessage(HWND hWnd, int minWindowMessage, int maxWindowMessage, RemoveMessage removeMessage) {
        MSG lpMsg = new MSG();
        if (User32.INSTANCE.PeekMessage(lpMsg, hWnd, minWindowMessage, maxWindowMessage, removeMessage.code())) {
            return lpMsg;
        }
        return null;
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
    class ThreadProcessId {
        private int threadId;
        private int processId;
    }

    static ThreadProcessId getWindowThreadProcessId(HWND hWnd) {
        IntByReference lpdwProcessId;

        int threadId = User32.INSTANCE.GetWindowThreadProcessId(
            hWnd,
            lpdwProcessId = new IntByReference()
        );

        return ThreadProcessId.builder()
            .processId(lpdwProcessId.getValue())
            .threadId(threadId)
            .build();
    }
}
