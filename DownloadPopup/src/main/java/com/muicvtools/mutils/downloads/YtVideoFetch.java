package com.muicvtools.mutils.downloads;

import android.content.Context;

import androidx.annotation.NonNull;

import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import us.shandian.giga.util.ExtractorHelper;

public class YtVideoFetch {

    public static void getVideo(Context context,String url_source, FetchListener listener) {
        ExtractorHelper.getStreamInfo(0, url_source, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull StreamInfo result) -> {
                   if(listener != null)
                   {
                       ArrayList<VideoDetail> listVideos = new ArrayList<VideoDetail>();

                       listVideos.add(new VideoDetail(null,StreamQuality.HD, result.getThumbnailUrl()));
                       listVideos.add(new VideoDetail(null,StreamQuality.SD, result.getThumbnailUrl()));

                       StreamOtherInfo streamOtherInfo = new StreamOtherInfo(url_source,DownloadType.YOUTUBE,result,listVideos,result.getName(),result.getThumbnailUrl());
                       listener.onFetchedSuccess(streamOtherInfo);
                   }
                }, (@NonNull Throwable throwable) -> {
                    System.out.println(throwable
                    );
                });
    }
}
