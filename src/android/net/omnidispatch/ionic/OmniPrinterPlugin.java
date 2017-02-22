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
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import android.content.Context;
import android.util.Log;

public class OmniPrinterPlugin extends CordovaPlugin {
    private static final String TAG = "OmniPrinter";
    private static final int TIMEOUT = 10000;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing Omni Printer");
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.w(TAG, "Exec " + action);
        try {
            if (action.equals("findDevices")) {
                findDevices(callbackContext);
            } else if (action.equals("status")) {
                String device = args.getString(0);
                status(device, callbackContext);
            } else if (action.equals("print")) {
                String device = args.getString(0);
                String content = args.getString(1);
                print(device, content, callbackContext);
            }
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, action, e);
            callbackContext.error(e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
        return true;
    }

    private Context getContext() {
        return cordova.getActivity().getApplicationContext();
    }

    private StarIOPort getPort(String port) throws StarIOPortException {
        return StarIOPort.getPort(port, "portable;l", TIMEOUT, getContext());
    }

    private void findDevices(CallbackContext callbackContext) throws Exception {
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
    }

    private void status(String device, CallbackContext callbackContext) throws Exception {
        StarIOPort port = null;
        try {
            port = getPort(device);
            StarPrinterStatus status = port.retreiveStatus();
            JSONObject result = new JSONObject();
            result.put("online", !status.offline);
            result.put("coverOpen", status.coverOpen);
            result.put("paperEmpty", status.receiptPaperEmpty);

            callbackContext.success(result);
        } finally {
            if (port != null) {
                StarIOPort.releasePort(port);
            }
        }
    }

    private void print(String device, String content, CallbackContext callbackContext) throws Exception {
        StarIOPort port = null;
        try {
            port = getPort(device);
            if (port.retreiveStatus().offline) {
                callbackContext.error("Printer is offline");
            }
            port.beginCheckedBlock();
            byte[] bytes = content.getBytes();
            port.writePort(bytes, 0, bytes.length);
            port.endCheckedBlock();
            callbackContext.success();
        } finally {
            if (port != null) {
                StarIOPort.releasePort(port);
            }
        }
    }
}
