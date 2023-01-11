package com.muicvtools.mutils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotifyViewModel : ViewModel() {
    var linkList = MutableLiveData<List<Notify>>()
    private var showAd: MutableLiveData<Int> = MutableLiveData()

    fun setShowAdData(value: Int) {
        showAd.postValue(value)
    }

    fun getShowAdData(): MutableLiveData<Int> {
        return showAd
    }

    fun setData(list: List<Notify>) {
        linkList.postValue(list)
    }
}