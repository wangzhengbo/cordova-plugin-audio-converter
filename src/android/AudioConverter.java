package com.audio.converter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class AudioConverter extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("convert")) {
            Object options = data.get(0);
            callbackContext.success(options.getClass().getName());

            return true;
        }

        return false;
    }
}
