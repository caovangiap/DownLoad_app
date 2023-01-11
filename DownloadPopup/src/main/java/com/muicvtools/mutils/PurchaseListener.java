package com.muicvtools.mutils;

public interface PurchaseListener {
    void purchaseFailed(String item);
    void purchaseSuccess(String item);
    void purchaseCancel(String item);
}
