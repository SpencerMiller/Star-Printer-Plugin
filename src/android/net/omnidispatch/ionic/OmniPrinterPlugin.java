package net.omnidispatch.ionic;

import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;

import android.content.Context;
import android.util.Log;

public class OmniPrinterPlugin extends CordovaPlugin {
    static final String TAG = "OmniPrinter";
    static final int TIMEOUT = 10000;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing Omni Printer");
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.w(TAG, "Exec " + action);
        if (action.equals("findDevices")) {
            findDevices(callbackContext);
        } else if (action.equals("status")) {
            String device = args.getString(0);
            cordova.getThreadPool().submit(new PrinterStatusExec(device, callbackContext, getContext()));
        } else if (action.equals("print")) {
            String device = args.getString(0);
            String content = args.getString(1);
            cordova.getThreadPool().submit(new PrintExec(device, content, callbackContext, getContext()));
        }
        return true;
    }

    private Context getContext() {
        return cordova.getActivity().getApplicationContext();
    }

    private void findDevices(CallbackContext callbackContext) {
        try {
            List<PortInfo> ports = StarIOPort.searchPrinter("BT:", getContext());
            JSONArray result = new JSONArray();

            for (PortInfo port : ports) {
                JSONObject obj = new JSONObject();
                result.put(obj);

                obj.put("mac", port.getMacAddress());
                obj.put("usbSerial", port.getUSBSerialNumber());
                obj.put("model", port.getModelName());
                obj.put("port", port.getPortName());
            }

            callbackContext.success(result);
        } catch (Exception e) {
            callbackContext.error(PrinterExec.formatResultError(e));
        }
    }
}
