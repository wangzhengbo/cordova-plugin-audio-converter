package com.audio.converter;

import android.content.Context;

import nl.bravobit.ffmpeg.FFmpeg;
import nl.bravobit.ffmpeg.FFmpegExecuteResponseHandler;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Encoder {
//    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String LINE_SEPARATOR = "\\n";

    /**
     * This regexp is used to parse the ffmpeg output about the included
     * encoders/decoders.
     */
    private static final Pattern ENCODER_DECODER_PATTERN = Pattern.compile(
            "^\\s*([D ])([E ])([AVS]).{3}\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    /**
     * Returns a list with the names of all the audio decoders bundled with the
     * ffmpeg distribution in use. An audio stream can be decoded only if a
     * decoder for its format is available.
     *
     * @return A list with the names of all the included audio decoders.
     */
    public void getAudioDecoders(Context context, CallbackContext callbackContext) {
        getAudioEncodersOrDecoders(context, callbackContext, false);
    }

    /**
     * Returns a list with the names of all the audio encoders bundled with the
     * ffmpeg distribution in use. An audio stream can be encoded using one of
     * these encoders.
     *
     * @return A list with the names of all the included audio encoders.
     */
    public void getAudioEncoders(Context context, CallbackContext callbackContext) {
        getAudioEncodersOrDecoders(context, callbackContext, true);
    }

    private void getAudioEncodersOrDecoders(Context context, CallbackContext callbackContext, boolean encoders) {
        try {
            FFmpeg.getInstance(context).execute(new String[]{"-formats"}, new FFmpegExecuteResponseHandler() {
                public void onStart() {
                    // Do nothing
                }

                public void onProgress(String message) {
                    // Do nothing
                }

                public void onSuccess(String message) {
                    try {
                        JSONArray audioDecoders = new JSONArray();
                        String[] lines = message.split("\\n");
                        boolean evaluate = false;
                        for (String line : lines) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            if (line.startsWith("File formats:")) {
//                                callbackContext.success(line.split("\\n").length + ", " + line.split("\\r").length + ", "
//                                        + line.split("\\r\\n").length + ", "
//                                        + line.split("\\t").length);
                                callbackContext.success(line);
                                break;
                            }
                            if (evaluate) {
                                Matcher matcher = ENCODER_DECODER_PATTERN.matcher(line);
                                if (matcher.matches()) {
                                    if ("A".equals(matcher.group(3))) { // audio/video flag
                                        if (encoders) {
                                            if ("E".equals(matcher.group(2))) { // encoder flag
                                                audioDecoders.put(matcher.group(4));
                                            }
                                        } else {
                                            if ("D".equals(matcher.group(1))) { // decoder flag
                                                audioDecoders.put(matcher.group(4));
                                            }
                                        }
                                    }
                                } else {
                                    break;
                                }
                            } else if (line.trim().equals("Codecs:")) {
                                evaluate = true;
                            }
                        }

//                        callbackContext.success(audioDecoders);
                    } catch (Exception e) {
                        this.onFailure(e.getMessage());
                    }
                }

                public void onFailure(String message) {
                    callbackContext.error(message);
                }

                public void onFinish() {
                    // Do nothing
                }
            });
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }
}
