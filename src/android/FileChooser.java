package com.megster.cordova;

import android.app.Activity;
import android.os.Build;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;

    public static final String MIME = "mime";
    public static final String PICKER_TITLE = "picker_title";

    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            JSONObject filters = inputs.optJSONObject(0);
            chooseFile(filters, callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(JSONObject filter, CallbackContext callbackContext) {
        ArrayList<String> mimeList = new ArrayList<>();
        JSONArray mimeTypesArray = filter.has(MIME) ? filter.optJSONArray(MIME) : new JSONArray();
        String select_title = filter.has(PICKER_TITLE) ? filter.optString(PICKER_TITLE) : "Select File";

        for (int j = 0; j < mimeTypesArray.length(); j++) {
            try {
                mimeList.add(mimeTypesArray.getString(j));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String baseMime = mimeList.get(0);
        mimeList.remove(0);// unshift kinda
        String[] extraMime = mimeList.toArray(new String[mimeList.size()]);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(baseMime.length() > 0 ? baseMime : "*/*");

            if (extraMime.length > 1) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMime);
            }
        } else {
            String mimeTypesStr = "";
            mimeTypesStr += baseMime + "|";
            for (String mimeType : extraMime) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, select_title);
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

                    Log.w(TAG, uri.toString());
                    callback.success(uri.toString());

                } else {

                    callback.error("File uri was null");

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // keep this string the same as in iOS document picker plugin
                // https://github.com/iampossible/Cordova-DocPicker
                callback.error("User canceled.");
            } else {

                callback.error(resultCode);
            }
        }
    }
}
