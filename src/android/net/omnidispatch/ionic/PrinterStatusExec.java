package net.omnidispatch.ionic;

import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarPrinterStatus;

import android.content.Context;

public class PrinterStatusExec extends PrinterExec {
    public PrinterStatusExec(String portName, CallbackContext callbackContext, Context context) {
        super(portName, callbackContext, context);
    }

    @Override
    public void run(StarIOPort printer) throws Exception {
        StarPrinterStatus status = printer.retreiveStatus();
        JSONObject result = new JSONObject();
        result.put("online", !status.offline);
        result.put("coverOpen", status.coverOpen);
        result.put("paperEmpty", status.receiptPaperEmpty);

        callbackContext.success(result);
    }
}
