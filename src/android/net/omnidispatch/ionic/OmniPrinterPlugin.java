package net.omnidispatch.ionic;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
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
//            cordova.getThreadPool().submit(new PrintExec(device, content, callbackContext, getContext()));
            print(content, callbackContext);
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
                    obj.put("deviceHardwareAddress", device.getAddress());
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

    private void print(String toPrint, CallbackContext callbackContext) throws JSONException {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice device = adapter.getRemoteDevice("00:15:0E:E5:71:C6");
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());

            out.write(new byte[]{0x1b, 0x40}); // ESC @ - Initialize printer

            out.write(toPrint.getBytes());

            // x lines for stamp + 5 to feed past cutter
            out.write(new byte[]{0x1b, 0x64, 5}); // ESC d n - Prints the data in the print buffer and feeds n lines
            out.flush();
            // none of out.flush(), out.close() nor socket.close() appear to wait for the buffer to flush so we wait here
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.e("LOG", Log.getStackTraceString(e));
            }
            out.close();

            callbackContext.success("Done");
//        } catch (JSONException e) {
//            throw e;
        } catch (Exception e) {
            callbackContext.error(PrinterExec.formatResultError(e));
        }
    }
}
