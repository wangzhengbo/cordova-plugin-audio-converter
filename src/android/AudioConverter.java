package com.audio.converter;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AudioConverter extends CordovaPlugin {
    private static final String TAG = "Cordova.AudioConverter";

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        AndroidAudioConverter.load(this.cordova.getContext(), new LoadCallback() {
            @Override
            public void onStart() {
                Log.d(TAG, "FFmpeg load start.");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "FFmpeg load success.");
            }

            @Override
            public void onFailure(Exception e) {
                // FFmpeg is not supported by device
                Log.e(TAG, "FFmpeg load failed.", e);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "FFmpeg load finished.");
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getAudioDecoders")) {
            getAudioDecoders(callbackContext);
            return true;
        } else if (action.equals("getAudioEncoders")) {
            getAudioEncoders(callbackContext);
            return true;
        } else if (action.equals("convert")) {
            convert(args, callbackContext);
            return true;
        }

        return false;
    }

    private void getAudioDecoders(CallbackContext callbackContext) {
        Encoder encoder = new Encoder();
        encoder.getAudioDecoders(this.cordova.getContext(), callbackContext);
    }

    private void getAudioEncoders(CallbackContext callbackContext) {
        Encoder encoder = new Encoder();
        encoder.getAudioEncoders(this.cordova.getContext(), callbackContext);
    }

    private void convert(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = (JSONObject) args.get(0);

        // The format name for the encoded target multimedia file. Be sure this format is supported.
        String format = options.getString("format");

        // The source multimedia file. It cannot be null. Be sure this file can be decoded.
        String source = options.getString("source");
        File sourceFile = new File(source);

        // The target multimedia re-encoded file. It cannot be null. If this file already exists,
        // it will be overwrited.
        String target = options.optString("target");
        if (target.isEmpty()) {
            target = new File(sourceFile.getParentFile().getAbsolutePath(),
                    sourceFile.getName() + "." + format + ".tmp").getAbsolutePath();
        }
        File targetFile = new File(target);

        // The codec name for the encoding process. If not specified the encoder will perform a
        // direct stream copy.
        String codec = "";

        // The channels value (1=mono, 2=stereo) for the encoding process
        int channels = -1;

        // The bitrate value for the encoding process
        int bitRate = -1;

        // The samplingRate value for the encoding process
        int samplingRate = -1;

        // The volume value for the encoding process. If 256 no volume change will be performed.
        int volume = -1;

        JSONObject audioAttributes = options.optJSONObject("audioAttributes");
        if (audioAttributes != null) {
            codec = options.optString("codec");

            channels = options.optInt("channels", channels);

            bitRate = options.optInt("bitRate", bitRate);

            samplingRate = options.optInt("samplingRate", samplingRate);

            volume = options.optInt("volume", volume);
        }

        AndroidAudioConverter.with(this.cordova.getContext())
                // Your current audio file
                .setFile(sourceFile)
                // Target audio file
                .setTargetFile(targetFile)
                // An callback to know when conversion is finished
                .setCallback(new ConvertCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onProgress(String message) {
                    }

                    @Override
                    public void onSuccess(File convertedFile) {
                        callbackContext.success(convertedFile.getAbsolutePath());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callbackContext.error(e.getMessage());
                    }

                    @Override
                    public void onFinish() {
                    }
                })
                // Start conversion
                .convert(format, codec, channels, bitRate, samplingRate, volume);
    }
}
