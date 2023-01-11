package com.muicvtools.mutils;

public interface ActiveCodeListener {
    void onActiveCodeSuccess();

    void onActiveCodeTimeFail();

    void onActiveCodeInvalidCode();
}
