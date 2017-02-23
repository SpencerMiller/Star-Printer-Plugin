package net.omnidispatch.ionic;

import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import android.content.Context;
import android.util.Log;

public class PrinterStatusExec implements Runnable {

    private final String portName;
    private final CallbackContext callbackContext;
    private final Context context;

    public PrinterStatusExec(String portName, CallbackContext callbackContext, Context context) {
        this.portName = portName;
        this.callbackContext = callbackContext;
        this.context = context;
    }

    @Override
    public void run() {
        StarIOPort printer = null;
        try {
            printer = StarIOPort.getPort(this.portName, "portable;l", OmniPrinterPlugin.TIMEOUT, this.context);
            StarPrinterStatus status = printer.retreiveStatus();
            JSONObject result = new JSONObject();
            result.put("online", !status.offline);
            result.put("coverOpen", status.coverOpen);
            result.put("paperEmpty", status.receiptPaperEmpty);

            callbackContext.success(result);
        } catch (Exception e) {
            callbackContext.error(OmniPrinterPlugin.formatResultError(e));
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
}
