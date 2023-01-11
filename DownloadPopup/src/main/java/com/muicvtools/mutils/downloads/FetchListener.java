package com.muicvtools.mutils.downloads;

public interface FetchListener {
    void requireLogin();
    void onFetchedSuccess(StreamOtherInfo detail);
    void onFetchedFail(String message);
}
