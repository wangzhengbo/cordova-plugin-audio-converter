package com.audio.converter;

import android.content.Context;

import nl.bravobit.ffmpeg.FFmpeg;
import nl.bravobit.ffmpeg.FFmpegExecuteResponseHandler;
import nl.bravobit.ffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class AndroidAudioConverter {
    private static boolean loaded;
    private Context context;
    private File audioFile;
    private File targetAudioFile;
    private AudioFormat format;
    private ConvertCallback callback;

    private AndroidAudioConverter(Context context) {
        this.context = context;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void load(Context context, final LoadCallback callback) {
        try {
            FFmpeg.getInstance(context).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                public void onStart() {
                    callback.onStart();
                }

                public void onSuccess() {
                    AndroidAudioConverter.loaded = true;
                    callback.onSuccess();
                }

                public void onFailure() {
                    AndroidAudioConverter.loaded = false;
                    callback.onFailure(new Exception("Failed to loaded FFmpeg lib"));
                }

                public void onFinish() {
                    callback.onFinish();
                }
            });
        } catch (Exception e) {
            loaded = false;
            callback.onFailure(e);
        }
    }

    public static AndroidAudioConverter with(Context context) {
        return new AndroidAudioConverter(context);
    }

    private static File getConvertedFile(File originalFile, AudioFormat format) {
        String[] f = originalFile.getPath().split("\\.");
        String filePath = originalFile.getPath().replace(f[f.length - 1], format.getFormat());
        return new File(filePath);
    }

    public AndroidAudioConverter setFile(File originalFile) {
        this.audioFile = originalFile;
        return this;
    }

    public AndroidAudioConverter setTargetFile(File targetFile) {
        this.targetAudioFile = targetFile;
        return this;
    }

    public AndroidAudioConverter setFormat(AudioFormat format) {
        this.format = format;
        return this;
    }

    public AndroidAudioConverter setCallback(ConvertCallback callback) {
        this.callback = callback;
        return this;
    }

    public void execute() {

    }

    public void convert(String format, String codec, int channels, int bitRate, int samplingRate, int volume) {
        if (!isLoaded()) {
            this.callback.onFailure(new Exception("FFmpeg not loaded"));
        } else if (this.audioFile != null && this.audioFile.exists()) {
            if (!this.audioFile.canRead()) {
                this.callback.onFailure(new IOException("Can't read the file. Missing permission?"));
            } else {
                final File convertedFile = getConvertedFile(this.audioFile, this.format);
                List<String> cmd = new ArrayList<>();
                cmd.add("-y");
                cmd.add("-i");
                cmd.add(this.audioFile.getAbsolutePath());
                cmd.add("-f");
                cmd.add(format);
                if (!codec.isEmpty()) {
                    cmd.add("-acodec");
                    cmd.add(codec);
                }
                if (channels >= 0) {
                    cmd.add(String.format("-ac %d", channels));
                }
                if (bitRate >= 0) {
                    cmd.add(String.format("-ab %d", bitRate));
                }
                if (samplingRate >= 0) {
                    cmd.add(String.format("-ar %d", samplingRate));
                }
                if (volume >= 0) {
                    cmd.add(String.format("-vol %d", volume));
                }
                cmd.add(this.targetAudioFile.getAbsolutePath());

                try {
                    FFmpeg.getInstance(this.context).execute(cmd.toArray(new String[cmd.size()]), new FFmpegExecuteResponseHandler() {
                        public void onStart() {
                            AndroidAudioConverter.this.callback.onStart();
                        }

                        public void onProgress(String message) {
                            AndroidAudioConverter.this.callback.onProgress(message);
                        }

                        public void onSuccess(String message) {
                            AndroidAudioConverter.this.callback.onSuccess(convertedFile);
                        }

                        public void onFailure(String message) {
                            AndroidAudioConverter.this.callback.onFailure(new IOException(message));
                        }

                        public void onFinish() {
                            AndroidAudioConverter.this.callback.onFinish();
                        }
                    });
                } catch (Exception e) {
                    this.callback.onFailure(e);
                }
            }
        } else {
            this.callback.onFailure(new IOException("File not exists"));
        }
    }
}
