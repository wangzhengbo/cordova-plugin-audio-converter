package com.audio.converter;

public interface LoadCallback {
    void onStart();

    void onSuccess();

    void onFailure(Exception e);

    void onFinish();
}
