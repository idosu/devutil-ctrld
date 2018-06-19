package com.github.idosu.devutil.ctrld.win32;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import winapi.Kernel32Util;
import winapi.Kernel32Util.Handle;
import winapi.Kernel32Util.ProcessAccess;

public interface Process extends AutoCloseable {
    String getImageFileName() throws Win32Exception;

    @Override
    void close() throws Win32Exception;

    static Process open(ProcessAccess desiredAccess, boolean inheritHandle, int processId) {
        return new Process() {
            Handle<HANDLE> process = Kernel32Util.openProcess(desiredAccess, inheritHandle, processId);

            @Override
            public String getImageFileName() throws Win32Exception {
                return Kernel32Util.getProcessImageFileName(process);
            }

            @Override
            public void close() throws Win32Exception {
                process.close();
            }
        };
    }
}
