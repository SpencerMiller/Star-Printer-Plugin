package net.omnidispatch.ionic;

import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

public class OmniPrinterPlugin extends CordovaPlugin {
    static final String TAG = "OmniPrinter";
    static final int TIMEOUT = 10000;

    static String formatResultError(Throwable t) {
        return t.getMessage() + "\n" + Log.getStackTraceString(t);
    }

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
            String address = args.getString(0);
            cordova.getThreadPool().submit(new PrinterStatusExec(address, callbackContext, getContext()));
        } else if (action.equals("print")) {
            String address = args.getString(0);
            String content = args.getString(1);
            cordova.getThreadPool().submit(new PrintExec(address, content, callbackContext));
        }
        return true;
    }

    private Context getContext() {
        return cordova.getActivity().getApplicationContext();
    }

    private void findDevices(CallbackContext callbackContext) throws JSONException {
//        try {
//            List<PortInfo> ports = StarIOPort.searchPrinter("BT:", getContext());
//            JSONArray result = new JSONArray();
//
//            for (PortInfo port : ports) {
//                JSONObject obj = new JSONObject();
//                result.put(obj);
//
//                obj.put("mac", port.getMacAddress());
//                obj.put("usbSerial", port.getUSBSerialNumber());
//                obj.put("model", port.getModelName());
//                obj.put("port", port.getPortName());
//            }
//
//            callbackContext.success(result);
//        } catch (Exception e) {
//            callbackContext.error(PrinterExec.formatResultError(e));
//        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        JSONArray result = new JSONArray();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().startsWith("00:15:0E")) {
                    JSONObject obj = new JSONObject();
                    result.put(obj);

                    obj.put("name", device.getName());
                    obj.put("mac", device.getAddress());
                    obj.put("classDevice", device.getBluetoothClass().getDeviceClass());
                    obj.put("classMajorDevice", device.getBluetoothClass().getMajorDeviceClass());

                    JSONArray uuids = new JSONArray();
                    obj.put("uuids", uuids);
                    for (ParcelUuid uuid : device.getUuids()) {
                        uuids.put(uuid.toString());
                    }
                }
            }
        }

        callbackContext.success(result);
    }
}
