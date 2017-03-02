package net.omnidispatch.ionic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class OmniPrinterPlugin extends CordovaPlugin {
    static final String TAG = "OmniPrinter";

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
        } else if (action.equals("print")) {
            String address = args.getString(0);
            JSONArray content = args.getJSONArray(1);
            cordova.getThreadPool().submit(new PrintExec(address, content, callbackContext));
        }
        return true;
    }

    private void findDevices(CallbackContext callbackContext) throws JSONException {
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
