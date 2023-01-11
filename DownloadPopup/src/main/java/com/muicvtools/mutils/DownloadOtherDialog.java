package com.muicvtools.mutils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.muicvtools.mutils.databinding.OtherDownloadDialogBinding;
import com.muicvtools.mutils.downloads.DownloadType;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import us.shandian.giga.get.DownloaderImpl;
import us.shandian.giga.get.MissionRecoveryInfo;
import us.shandian.giga.io.StoredDirectoryHelper;
import us.shandian.giga.io.StoredFileHelper;
import us.shandian.giga.service.DownloadManager;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.service.MissionState;
import us.shandian.giga.util.FilePickerActivityHelper;
import us.shandian.giga.util.FilenameUtils;
import us.shandian.giga.util.OtherSiteVideo;
import us.shandian.giga.util.Utility;

public class DownloadOtherDialog extends DialogFragment {
    String url_stream;
    String title_file;
    String url_source;
    DownloadType url_type;//tiktok,facebook,instagram

    private Context context;
    private StoredDirectoryHelper mainStorageVideo;
    private DownloadManager downloadManager;
    private SharedPreferences prefs;
    private ActionMenuItemView okButton = null;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private OtherDownloadDialogBinding dialogBinding;

    private String filenameTmp;
    private String mimeTmp;
    private final ActivityResultLauncher<Intent> requestDownloadPickVideoFolderLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::requestDownloadPickVideoFolderResult);

    public static DownloadOtherDialog newInstance(final Context context) {//, final String urlStream,final  String title_file, final String url_source,final String url_type
        final DownloadOtherDialog dialog = new DownloadOtherDialog();
//        dialog.setInfo(urlStream,title_file,url_source,url_type);
        return dialog;
    }

    public void setInfo(final String urlStream, final String title_file, final String url_source, final DownloadType url_type) {
        this.url_stream = urlStream;
        if (title_file != null)
            this.title_file = title_file;
        else
            this.title_file = "Video " + new Date().getTime();
        this.url_source = url_source;
        this.url_type = url_type;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.logDebug(this.getClass(), "onCreate() called with: "
                + "savedInstanceState = [" + savedInstanceState + "]");

        context = getContext();
        setStyle(STYLE_NO_TITLE, 0);

        final Intent intent = new Intent(context, DownloadManagerService.class);
        context.startService(intent);

        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName cname, final IBinder service) {
                final DownloadManagerService.DownloadManagerBinder mgr = (DownloadManagerService.DownloadManagerBinder) service;

                mainStorageVideo = mgr.getMainStorageVideo();
                downloadManager = mgr.getDownloadManager();

                okButton.setEnabled(true);
                context.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                // nothing to do
            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        Utils.logDebug(this.getClass(), "onCreateView() called with: "
                + "inflater = [" + inflater + "], container = [" + container + "], "
                + "savedInstanceState = [" + savedInstanceState + "]");
        return inflater.inflate(R.layout.other_download_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogBinding = OtherDownloadDialogBinding.bind(view);

        dialogBinding.fileName.setText(FilenameUtils.createFilename(getContext(),
                title_file));

        initToolbar(dialogBinding.toolbarLayout.toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        final int threads = prefs.getInt(getString(R.string.default_download_threads), 3);
        dialogBinding.threadsCount.setText(String.valueOf(threads));
        dialogBinding.threads.setProgress(threads - 1);
        dialogBinding.threads.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekbar, final int progress,
                                          final boolean fromUser) {
                final int newProgress = progress + 1;
                prefs.edit().putInt(getString(R.string.default_download_threads), newProgress)
                        .apply();
                dialogBinding.threadsCount.setText(String.valueOf(newProgress));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar p1) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar p1) {
            }
        });
        fetchStreamsSize();
    }

    private void initToolbar(final Toolbar toolbar) {
        Utils.logDebug(this.getClass(), "initToolbar() called with: toolbar = [" + toolbar + "]");

        toolbar.setTitle(R.string.download_dialog_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.inflateMenu(R.menu.dialog_url);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setNavigationContentDescription(R.string.cancel);

        okButton = toolbar.findViewById(R.id.okay);
        okButton.setEnabled(false); // disable until the download service connection is done

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.okay) {
                startDownloadFile();

                String typeDownload = "";
                if (url_type == DownloadType.INSTA)
                    typeDownload = "instagram";
                else if (url_type == DownloadType.FACEBOOK)
                    typeDownload = "facebook";
                else if (url_type == DownloadType.TIKTOK)
                    typeDownload = "tiktok";

                ApiManager.getSharedInstance().addToHistory(context, getNameEditText(), url_source, typeDownload);

                if (!prefs.contains("isRate")) {
                    final RatingDialog ratingDialog = new RatingDialog.Builder(context)
//                            .icon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                            .session(2)
                            .threshold(4)
                            .title("How was your experience with us?")
                            .titleTextColor(R.color.black)
                            .positiveButtonText("Not Now")
                            .negativeButtonText("Never")
                            .positiveButtonTextColor(R.color.color_gray_1)
                            .negativeButtonTextColor(R.color.red_500)
                            .ratingBarColor(R.color.yellow)
                            .onRatingChanged((rating, thresholdCleared) ->
                            {
                                prefs.edit().putBoolean("isRate", true).apply();
                            })
                            .onRatingBarFormSumbit(feedback ->
                            {
                                Toast.makeText(context, "Thanks for your review!", Toast.LENGTH_SHORT).show();
                            })
                            .build();

                    ratingDialog.show();
                }

                return true;
            }
            return false;
        });
    }

    private void fetchStreamsSize() {
        disposables.clear();

        Single<Long> fetchSizeObservable = Single.fromCallable(
                new Callable<Long>() {
                    @Override
                    public Long call() throws IOException {
                        long streamSize = DownloaderImpl.getInstance().getContentLength(
                                url_stream);
                        Utils.logDebug(this.getClass(), "value = " + streamSize);
                        return streamSize;
                    }
                }
        );

        disposables.add(fetchSizeObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((aLong, throwable) -> {
                    if (aLong != null)
                        dialogBinding.fileSize.setText("File size: " + Utility.formatBytes(aLong));
                    else
                        dialogBinding.fileSize.setText("File size: Unknown");
                }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public void onDestroyView() {
        dialogBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private String getNameEditText() {
        final String str = dialogBinding.fileName.getText().toString().trim();
        return FilenameUtils.createFilename(context, str.isEmpty() ? title_file : str);
    }

    public void startDownloadFile() {
        filenameTmp = getNameEditText().concat(".");
        MediaFormat format = MediaFormat.MPEG_4;
        filenameTmp += format.suffix;

        mimeTmp = "video/mp4";

        if (mainStorageVideo == null) {
            // Pick new download folder if one of:
            // - Download folder is not set
            // - Download folder uses SAF while SAF is disabled
            // - Download folder doesn't use SAF while SAF is enabled
            // - Download folder uses SAF but the user manually revoked access to it
            Toast.makeText(context, getString(R.string.no_dir_yet),
                    Toast.LENGTH_LONG).show();
            launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
            return;
        }

        checkSelectedDownload(mainStorageVideo, mainStorageVideo.findFile(filenameTmp), filenameTmp, mimeTmp);
    }

    private void launchDirectoryPicker(final ActivityResultLauncher<Intent> launcher) {
        launcher.launch(StoredDirectoryHelper.getPicker(context));
    }

    private void requestDownloadPickVideoFolderResult(final ActivityResult result) {
        requestDownloadPickFolderResult(
                result, getString(R.string.download_path_video_key), DownloadManager.TAG_VIDEO);
    }

    private void requestDownloadPickFolderResult(final ActivityResult result,
                                                 final String key,
                                                 final String tag) {
        if (result.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (result.getData() == null || result.getData().getData() == null) {
            showFailedDialog(R.string.general_error);
            return;
        }

        Uri uri = result.getData().getData();
        if (FilePickerActivityHelper.isOwnFileUri(context, uri)) {
            uri = Uri.fromFile(com.nononsenseapps.filepicker.Utils.getFileForUri(uri));
        } else {
            context.grantUriPermission(context.getPackageName(), uri,
                    StoredDirectoryHelper.PERMISSION_FLAGS);
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(getString(R.string.download_path_video_key), uri.toString()).apply();

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(getString(R.string.download_path_audio_key), uri.toString()).apply();

        try {
            final StoredDirectoryHelper mainStorage
                    = new StoredDirectoryHelper(context, uri, tag);
            checkSelectedDownload(mainStorage, mainStorage.findFile(filenameTmp),
                    filenameTmp, mimeTmp);
        } catch (final IOException e) {
            showFailedDialog(R.string.general_error);
        }
    }

    private void checkSelectedDownload(final StoredDirectoryHelper mainStorage,
                                       final Uri targetFile, final String filename,
                                       final String mime) {
        StoredFileHelper storage;
        try {
            if (mainStorage == null) {
                // using SAF on older android version
                storage = new StoredFileHelper(context, null, targetFile, "");
            } else if (targetFile == null) {
                // the file does not exist, but it is probably used in a pending download
                storage = new StoredFileHelper(mainStorage.getUri(), filename, mime,
                        mainStorage.getTag());
            } else {
                // the target filename is already use, attempt to use it
                storage = new StoredFileHelper(context, mainStorage.getUri(), targetFile,
                        mainStorage.getTag());
            }
        } catch (final Exception e) {
            showFailedDialog(R.string.general_error);
            return;
        }

        // get state of potential mission referring to the same file
        final MissionState state = downloadManager.checkForExistingMission(storage);
        @StringRes final int msgBtn;
        @StringRes final int msgBody;

        // this switch checks if there is already a mission referring to the same file
        switch (state) {
            case Finished: // there is already a finished mission
                msgBtn = R.string.overwrite;
                msgBody = R.string.overwrite_finished_warning;
                break;
            case Pending:
                msgBtn = R.string.overwrite;
                msgBody = R.string.download_already_pending;
                break;
            case PendingRunning:
                msgBtn = R.string.generate_unique_name;
                msgBody = R.string.download_already_running;
                break;
            case None: // there is no mission referring to the same file
                if (mainStorage == null) {
                    // This part is called if:
                    // * using SAF on older android version
                    // * save path not defined
                    // * if the file exists overwrite it, is not necessary ask
                    if (!storage.existsAsFile() && !storage.create()) {
                        showFailedDialog(R.string.error_file_creation);
                        return;
                    }
                    continueSelectedDownload(storage);
                    return;
                } else if (targetFile == null) {
                    // This part is called if:
                    // * the filename is not used in a pending/finished download
                    // * the file does not exists, create

                    if (!mainStorage.mkdirs()) {
                        showFailedDialog(R.string.error_path_creation);
                        return;
                    }

                    storage = mainStorage.createFile(filename, mime);
                    if (storage == null || !storage.canWrite()) {
                        showFailedDialog(R.string.error_file_creation);
                        return;
                    }

                    continueSelectedDownload(storage);
                    return;
                }
                msgBtn = R.string.overwrite;
                msgBody = R.string.overwrite_unrelated_warning;
                break;
            default:
                return; // unreachable
        }

        final AlertDialog.Builder askDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.download_dialog_title)
                .setMessage(msgBody)
                .setNegativeButton(android.R.string.cancel, null);
        final StoredFileHelper finalStorage = storage;


        if (mainStorage == null) {
            // This part is called if:
            // * using SAF on older android version
            // * save path not defined
            switch (state) {
                case Pending:
                case Finished:
                    askDialog.setPositiveButton(msgBtn, (dialog, which) -> {
                        dialog.dismiss();
                        downloadManager.forgetMission(finalStorage);
                        continueSelectedDownload(finalStorage);
                    });
                    break;
            }

            askDialog.create().show();
            return;
        }

        askDialog.setPositiveButton(msgBtn, (dialog, which) -> {
            dialog.dismiss();

            StoredFileHelper storageNew;
            switch (state) {
                case Finished:
                case Pending:
                    downloadManager.forgetMission(finalStorage);
                case None:
                    if (targetFile == null) {
                        storageNew = mainStorage.createFile(filename, mime);
                    } else {
                        try {
                            // try take (or steal) the file
                            storageNew = new StoredFileHelper(context, mainStorage.getUri(),
                                    targetFile, mainStorage.getTag());
                        } catch (final IOException e) {
                            Utils.logDebug(this.getClass(), "Failed to take (or steal) the file in "
                                    + targetFile.toString());
                            storageNew = null;
                        }
                    }

                    if (storageNew != null && storageNew.canWrite()) {
                        continueSelectedDownload(storageNew);
                    } else {
                        showFailedDialog(R.string.error_file_creation);
                    }
                    break;
                case PendingRunning:
                    storageNew = mainStorage.createUniqueFile(filename, mime);
                    if (storageNew == null) {
                        showFailedDialog(R.string.error_file_creation);
                    } else {
                        continueSelectedDownload(storageNew);
                    }
                    break;
            }
        });

        askDialog.create().show();
    }


    private void continueSelectedDownload(@NonNull final StoredFileHelper storage) {
        if (!storage.canWrite()) {
            showFailedDialog(R.string.permission_denied);
            return;
        }

        // check if the selected file has to be overwritten, by simply checking its length
        try {
            if (storage.length() > 0) {
                storage.truncate();
            }
        } catch (final IOException e) {
            Utils.logDebug(this.getClass(), "failed to truncate the file: " + storage.getUri().toString());
            showFailedDialog(R.string.overwrite_failed);
            return;
        }

        final Stream selectedStream;
        final char kind = 'v';
        int threads = 5;//dialogBinding.threads.getProgress() + 1;
        final String[] urls;
        final MissionRecoveryInfo[] recoveryInfo;
        String psName = null;
        String[] psArgs = null;
        long nearLength = 0;

        urls = new String[]{
                url_stream
        };

        recoveryInfo = new MissionRecoveryInfo[]{new MissionRecoveryInfo(new OtherSiteVideo("id", "content", true,MediaFormat.MPEG_4, DeliveryMethod.PROGRESSIVE_HTTP, url_stream ))};


        DownloadManagerService.startMission(context, urls, storage, kind, threads,
                url_source, psName, psArgs, nearLength, recoveryInfo);

        Toast.makeText(context, getString(R.string.download_has_started),
                Toast.LENGTH_SHORT).show();

        dismiss();
    }

    private void showFailedDialog(@StringRes final int msg) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.general_error)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.ok), null)
                .create()
                .show();
    }

}
