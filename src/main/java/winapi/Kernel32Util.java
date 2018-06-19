package winapi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.Closeable;

import static lombok.AccessLevel.PACKAGE;

public interface Kernel32Util extends Library {
    Kernel32Util lib = Native.loadLibrary("kernel32", Kernel32Util.class);

    static int getLastError() {
        return Kernel32.INSTANCE.GetLastError();
    }

    static void throwLastErrorIf(boolean check) throws Win32Exception {
        if (check) {
            int errorCode = getLastError();
            throw new Win32Exception(errorCode);
        }
    }

    static void throwLastErrorIfNot(boolean check) throws Win32Exception {
        throwLastErrorIf(!check);
    }

    static <T> T throwLastErrorIfNull(T pointer) throws Win32Exception {
        throwLastErrorIf(pointer == null);
        return pointer;
    }

    @Getter @AllArgsConstructor(access=PACKAGE)
    class Handle<T extends HANDLE> implements Closeable {
        @NonNull
        private T handle;

        @Override
        public String toString() {
            return handle.toString();
        }

        @Override
        public void close() throws Win32Exception {
            closeHandle(this);
        }
    }

    interface ProcessAccess {
        /** Required to delete the object. */
        ProcessAccess DELETE = proc(0x0001_0000);
        /** Required to read information in the security descriptor for the object, not including the information in the SACL. To read or write the SACL, you must request the ACCESS_SYSTEM_SECURITY access right. For more information, see SACL Access Right. */
        ProcessAccess READ_CONTROL = proc(0x0002_0000);
        /**
         * The right to use the object for synchronization. This enables a thread to wait until the object is in the signaled state.
         * Required to wait f;
         */
        ProcessAccess SYNCHRONIZE = proc(0x0010_0000);
        /** Required to modify the DACL in the security descriptor for the object. */
        ProcessAccess WRITE_DAC = proc(0x0004_0000);
        /** Required to change the owner in the security descriptor for the object. */
        ProcessAccess WRITE_OWNER = proc(0x0008_0000);

        //PROCESS_ALL_ACCESS	All possible access rights for a process object.
        //Windows Server 2003 and Windows XP:  The size of the PROCESS_ALL_ACCESS flag increased on Windows Server 2008 and Windows Vista. If an application compiled for Windows Server 2008 and Windows Vista is run on Windows Server 2003 or Windows XP, the PROCESS_ALL_ACCESS flag is too large and the function specifying this flag fails with ERROR_ACCESS_DENIED. To avoid this problem, specify the minimum set of access rights required for the operation. If PROCESS_ALL_ACCESS must be used, set _WIN32_WINNT to the minimum operating system targeted by your application (for example, #define _WIN32_WINNT _WIN32_WINNT_WINXP). For more information, see Using the Windows Headers.

        /** Required to create a process. */
        ProcessAccess PROCESS_CREATE_PROCESS = proc(0x0080);
        /** Required to create a thread. */
        ProcessAccess PROCESS_CREATE_THREAD = proc(0x0002);
        /** Required to duplicate a handle using DuplicateHandle. */
        ProcessAccess PROCESS_DUP_HANDLE = proc(0x0040);
        /** Required to retrieve certain information about a process, such as its token, exit code, and priority class (see OpenProcessToken). */
        ProcessAccess PROCESS_QUERY_INFORMATION = proc(0x0400);
        /**
         * Required to retrieve certain information about a process (see GetExitCodeProcess, GetPriorityClass, IsProcessInJob, QueryFullProcessImageName). A handle that has the PROCESS_QUERY_INFORMATION access right is automatically granted PROCESS_QUERY_LIMITED_INFORMATION.
         * Windows Server 2003 and Windows XP:  This access right is not supported.
         */
        ProcessAccess PROCESS_QUERY_LIMITED_INFORMATION = proc(0x1000);
        /** Required to set certain information about a process, such as its priority class (see SetPriorityClass). */
        ProcessAccess PROCESS_SET_INFORMATION = proc(0x0200);
        /** Required to set memory limits using SetProcessWorkingSetSize. */
        ProcessAccess PROCESS_SET_QUOTA = proc(0x0100);
        /** Required to suspend or resume a process. */
        ProcessAccess PROCESS_SUSPEND_RESUME = proc(0x0800);
        /** Required to terminate a process using TerminateProcess. */
        ProcessAccess PROCESS_TERMINATE = proc(0x0001);
        /** Required to perform an operation on the address space of a process (see VirtualProtectEx and WriteProcessMemory). */
        ProcessAccess PROCESS_VM_OPERATION = proc(0x0008);
        /** Required to read memory in a process using ReadProcessMemory. */
        ProcessAccess PROCESS_VM_READ = proc(0x0010);
        /** Required to write to memory in a process using WriteProcessMemory. */
        ProcessAccess PROCESS_VM_WRITE = proc(0x0020);

        int code();

        static ProcessAccess proc(int code) {
            return () -> code;
        }

        default ProcessAccess and(ProcessAccess other) {
            return () -> code() & other.code();
        }

        default ProcessAccess or(ProcessAccess other) {
            return () -> code() | other.code();
        }

        default ProcessAccess xor(ProcessAccess other) {
            return () -> code() ^ other.code();
        }

        default ProcessAccess not() {
            return () -> ~code();
        }
    }

    static Handle<HANDLE> openProcess(ProcessAccess desiredAccess, boolean inheritHandle, int processId) throws Win32Exception {
        HANDLE process = throwLastErrorIfNull(Kernel32.INSTANCE.OpenProcess(desiredAccess.code(), inheritHandle, processId));
        return new Handle<>(process);
    }

    int K32GetProcessImageFileNameW(
        HANDLE hProcess,
        char[] lpImageFileName,
        int nSize
    );

    static String getProcessImageFileName(Handle<HANDLE> process) throws Win32Exception {
        char[] lpImageFileName = new char[127];
        int writtenSize = lpImageFileName.length;

        while (writtenSize == lpImageFileName.length) {
            writtenSize = lib.K32GetProcessImageFileNameW(process.getHandle(), lpImageFileName, lpImageFileName.length);
            throwLastErrorIf(writtenSize == 0);
            // TODO: Remove double if(while and if)
            if (writtenSize == lpImageFileName.length) {
                lpImageFileName = new char[(int)Math.ceil(lpImageFileName.length * 1.5)];
            }
        }

        return Native.toString(lpImageFileName);
    }

    static void closeHandle(Handle<? extends HANDLE> handle) throws Win32Exception {
        throwLastErrorIfNot(Kernel32.INSTANCE.CloseHandle(handle.getHandle()));
    }
}
