package net.omnidispatch.ionic;

import org.apache.cordova.CallbackContext;

import com.starmicronics.stario.StarIOPort;

import android.content.Context;

public class PrintExec extends PrinterExec {
    private String content;

    public PrintExec(String portName, String content, CallbackContext callbackContext, Context context) {
        super(portName, callbackContext, context);
        this.content = content;
    }

    @Override
    public void run(StarIOPort printer) throws Exception {
        if (printer.retreiveStatus().offline) {
            callbackContext.error("Printer is offline");
        }
        printer.beginCheckedBlock();
        byte[] bytes = content.getBytes();
        printer.writePort(bytes, 0, bytes.length);
        printer.endCheckedBlock();
        callbackContext.success();
    }
}
