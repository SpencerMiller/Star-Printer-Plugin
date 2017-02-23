package net.omnidispatch.ionic;

import org.apache.cordova.CallbackContext;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import android.content.Context;
import android.util.Log;

public abstract class PrinterExec implements Runnable {

    final String portName;
    final CallbackContext callbackContext;
    final Context context;

    public PrinterExec(String portName, CallbackContext callbackContext, Context context) {
        this.portName = portName;
        this.callbackContext = callbackContext;
        this.context = context;
    }

    @Override
    public void run() {
        StarIOPort printer = null;
        try {
            printer = StarIOPort.getPort(this.portName, "portable;l", OmniPrinterPlugin.TIMEOUT, this.context);
            run(printer);
        } catch (Exception e) {
            callbackContext.error(formatResultError(e));
        } finally {
            if (printer != null) {
                try {
                    StarIOPort.releasePort(printer);
                } catch (StarIOPortException e) {
                    Log.wtf(OmniPrinterPlugin.TAG, "Release printer", e);
                }
            }
        }
    }

    public abstract void run(StarIOPort printer) throws Exception;

    public static String formatResultError(Throwable t) {
        return t.getMessage() + "\n" + Log.getStackTraceString(t);
    }
}
