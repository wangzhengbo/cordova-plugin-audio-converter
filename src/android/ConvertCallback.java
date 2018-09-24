package com.audio.converter;

import java.io.File;

public interface ConvertCallback {
    void onStart();

    void onProgress(String message);

    void onSuccess(File message);

    void onFailure(Exception e);

    void onFinish();
}
