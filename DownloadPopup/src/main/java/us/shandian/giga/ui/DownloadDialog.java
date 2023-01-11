package us.shandian.giga.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.muicvtools.mutils.AdsManager;
import com.muicvtools.mutils.ApiManager;
import com.muicvtools.mutils.R;
import com.muicvtools.mutils.databinding.DownloadDialogBinding;
import com.nononsenseapps.filepicker.Utils;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import us.shandian.giga.get.MissionRecoveryInfo;
import us.shandian.giga.io.StoredDirectoryHelper;
import us.shandian.giga.io.StoredFileHelper;
import us.shandian.giga.postprocessing.Postprocessing;
import us.shandian.giga.service.DownloadManager;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.service.DownloadManagerService.DownloadManagerBinder;
import us.shandian.giga.service.MissionState;
import us.shandian.giga.settings.NewPipeSettings;
import us.shandian.giga.util.ACCEPT_DOWNLOAD;
import us.shandian.giga.util.FilePickerActivityHelper;
import us.shandian.giga.util.FilenameUtils;
import us.shandian.giga.util.ListHelper;
import us.shandian.giga.util.SecondaryStreamHelper;
import us.shandian.giga.util.StreamItemAdapter;
import us.shandian.giga.util.StreamItemAdapter.StreamSizeWrapper;

public class DownloadDialog extends DialogFragment
        implements RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "DialogFragment";

    StreamInfo currentInfo;
    StreamSizeWrapper<AudioStream> wrappedAudioStreams = StreamSizeWrapper.empty();
    StreamSizeWrapper<VideoStream> wrappedVideoStreams = StreamSizeWrapper.empty();
    StreamSizeWrapper<SubtitlesStream> wrappedSubtitleStreams = StreamSizeWrapper.empty();
    int selectedVideoIndex = 0;
    int selectedAudioIndex = 0;
    int selectedSubtitleIndex = 0;

    @Nullable
    private OnDismissListener onDismissListener = null;

    private StoredDirectoryHelper mainStorageAudio = null;
    private StoredDirectoryHelper mainStorageVideo = null;
    private DownloadManager downloadManager = null;
    private ActionMenuItemView okButton = null;
    private Context context;
    private boolean askForSavePath;

    private StreamItemAdapter<AudioStream, Stream> audioStreamsAdapter;
    private StreamItemAdapter<VideoStream, AudioStream> videoStreamsAdapter;
    private StreamItemAdapter<SubtitlesStream, Stream> subtitleStreamsAdapter;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private DownloadDialogBinding dialogBinding;

    private SharedPreferences prefs;

    // Variables for file name and MIME type when picking new folder because it's not set yet
    private String filenameTmp;
    private String mimeTmp;

    private final ActivityResultLauncher<Intent> requestDownloadSaveAsLauncher =
            registerForActivityResult(
                    new StartActivityForResult(), this::requestDownloadSaveAsResult);
    private final ActivityResultLauncher<Intent> requestDownloadPickAudioFolderLauncher =
            registerForActivityResult(
                    new StartActivityForResult(), this::requestDownloadPickAudioFolderResult);
    private final ActivityResultLauncher<Intent> requestDownloadPickVideoFolderLauncher =
            registerForActivityResult(
                    new StartActivityForResult(), this::requestDownloadPickVideoFolderResult);

    private ACCEPT_DOWNLOAD type_download = ACCEPT_DOWNLOAD.LOW_QUALITY;

    public void setAcceptDownload(ACCEPT_DOWNLOAD type) {
        this.type_download = type;
    }



    /*//////////////////////////////////////////////////////////////////////////
    // Instance creation
    //////////////////////////////////////////////////////////////////////////*/

    public static DownloadDialog newInstance(final StreamInfo info) {
        final DownloadDialog dialog = new DownloadDialog();
        dialog.setInfo(info);
        return dialog;
    }

    public static DownloadDialog newInstance(final Context context, final StreamInfo info) {
        final ArrayList<VideoStream> streamsList = new ArrayList<>(ListHelper
                .getSortedStreamVideosList(context, info.getVideoStreams(),
                        info.getVideoOnlyStreams(), false));
        final int selectedStreamIndex = ListHelper.getDefaultResolutionIndex(context, streamsList);

        final DownloadDialog instance = newInstance(info);
        instance.setVideoStreams(streamsList);
        instance.setSelectedVideoStream(selectedStreamIndex);
        instance.setAudioStreams(info.getAudioStreams());
        instance.setSubtitleStreams(info.getSubtitles());

        return instance;
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Setters
    //////////////////////////////////////////////////////////////////////////*/

    private void setInfo(final StreamInfo info) {
        this.currentInfo = info;
    }

    public void setAudioStreams(final List<AudioStream> audioStreams) {
        setAudioStreams(new StreamSizeWrapper<>(audioStreams, getContext()));
    }

    public void setAudioStreams(final StreamSizeWrapper<AudioStream> was) {
        this.wrappedAudioStreams = was;
    }

    public void setVideoStreams(final List<VideoStream> videoStreams) {
        setVideoStreams(new StreamSizeWrapper<>(videoStreams, getContext()));
    }

    public void setVideoStreams(final StreamSizeWrapper<VideoStream> wvs) {
        this.wrappedVideoStreams = wvs;
    }

    public void setSubtitleStreams(final List<SubtitlesStream> subtitleStreams) {
        setSubtitleStreams(new StreamSizeWrapper<>(subtitleStreams, getContext()));
    }

    public void setSubtitleStreams(
            final StreamSizeWrapper<SubtitlesStream> wss) {
        this.wrappedSubtitleStreams = wss;
    }

    public void setSelectedVideoStream(final int svi) {
        this.selectedVideoIndex = svi;
    }

    public void setSelectedAudioStream(final int sai) {
        this.selectedAudioIndex = sai;
    }

    public void setSelectedSubtitleStream(final int ssi) {
        this.selectedSubtitleIndex = ssi;
    }

    public void setOnDismissListener(@Nullable final OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Android lifecycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AdsManager.DEBUG) {
            Log.d(TAG, "onCreate() called with: "
                    + "savedInstanceState = [" + savedInstanceState + "]");
        }

//        if (!PermissionHelper.checkStoragePermissions(getActivity(),
//                PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE)) {
//            dismiss();
//            return;
//        }

        context = getContext();

        setStyle(STYLE_NO_TITLE, 0);

        final SparseArray<SecondaryStreamHelper<AudioStream>> secondaryStreams
                = new SparseArray<>(4);
        final List<VideoStream> videoStreams = wrappedVideoStreams.getStreamsList();

        for (int i = 0; i < videoStreams.size(); i++) {
            if (!videoStreams.get(i).isVideoOnly()) {
                continue;
            }
            final AudioStream audioStream = SecondaryStreamHelper
                    .getAudioStreamFor(wrappedAudioStreams.getStreamsList(), videoStreams.get(i));

            if (audioStream != null) {
                secondaryStreams
                        .append(i, new SecondaryStreamHelper<>(wrappedAudioStreams, audioStream));
            } else if (AdsManager.DEBUG) {
                Log.w(TAG, "No audio stream candidates for video format "
                        + videoStreams.get(i).getFormat().name());
            }
        }

        this.videoStreamsAdapter = new StreamItemAdapter<>(context, wrappedVideoStreams,
                secondaryStreams);
        this.audioStreamsAdapter = new StreamItemAdapter<>(context, wrappedAudioStreams);
        this.subtitleStreamsAdapter = new StreamItemAdapter<>(context, wrappedSubtitleStreams);

        final Intent intent = new Intent(context, DownloadManagerService.class);
        context.startService(intent);

        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName cname, final IBinder service) {
                final DownloadManagerBinder mgr = (DownloadManagerBinder) service;

                mainStorageAudio = mgr.getMainStorageAudio();
                mainStorageVideo = mgr.getMainStorageVideo();
                downloadManager = mgr.getDownloadManager();
                askForSavePath = mgr.askForSavePath();

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
        if (AdsManager.DEBUG) {
            Log.d(TAG, "onCreateView() called with: "
                    + "inflater = [" + inflater + "], container = [" + container + "], "
                    + "savedInstanceState = [" + savedInstanceState + "]");
        }
        return inflater.inflate(R.layout.download_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogBinding = DownloadDialogBinding.bind(view);

        dialogBinding.fileName.setText(FilenameUtils.createFilename(getContext(),
                currentInfo.getName()));
        selectedAudioIndex = ListHelper
                .getDefaultAudioFormat(getContext(), currentInfo.getAudioStreams());

        selectedSubtitleIndex = getSubtitleIndexBy(subtitleStreamsAdapter.getAll());

        dialogBinding.qualitySpinner.setOnItemSelectedListener(this);

        dialogBinding.videoAudioGroup.setOnCheckedChangeListener(this);

        initToolbar(dialogBinding.toolbarLayout.toolbar);
        setupDownloadOptions();

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
        if (AdsManager.DEBUG) {
            Log.d(TAG, "initToolbar() called with: toolbar = [" + toolbar + "]");
        }

        toolbar.setTitle(R.string.download_dialog_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.inflateMenu(R.menu.dialog_url);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setNavigationContentDescription(R.string.cancel);

        okButton = toolbar.findViewById(R.id.okay);
        okButton.setEnabled(false); // disable until the download service connection is done

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.okay) {
                prepareSelectedDownload();

                String title = currentInfo.getName();
                String link = currentInfo.getUrl();
                ApiManager.getSharedInstance().addToHistory(context, title, link, "youtube");

                final RatingDialog ratingDialog = new RatingDialog.Builder(context)
//                            .icon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                        .session(2)
                        .threshold(4)
                        .title("How was your experience with us?")
                        .titleTextColor(R.color.black)
                        .positiveButtonText("Not Now")
                        .negativeButtonText("Never")
//                        .positiveButtonTextColor(R.color.accent)
//                        .negativeButtonTextColor(R.color.grey_500)
                        .ratingBarColor(R.color.yellow)
                        .onRatingChanged((rating, thresholdCleared) ->
                        {
                            if (thresholdCleared) {
                                AdsManager.getInstance().setDoNotShowOpen();
                            }
                        })
                        .onRatingBarFormSumbit(feedback ->
                        {
                            Toast.makeText(context, "Thanks for your review!", Toast.LENGTH_SHORT).show();
                        })
                        .build();

                ratingDialog.show();

                return true;
            }
            return false;
        });
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
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


    /*//////////////////////////////////////////////////////////////////////////
    // Video, audio and subtitle spinners
    //////////////////////////////////////////////////////////////////////////*/

    private void fetchStreamsSize() {
        disposables.clear();
        disposables.add(StreamSizeWrapper.fetchSizeForWrapper(wrappedVideoStreams)
                .subscribe(result -> {
                    if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId()
                            == R.id.video_button) {
                        setupVideoSpinner();
                    }
                }, throwable ->
//                        ErrorActivity.reportErrorInSnackbar(context,
//                        new ErrorInfo(throwable, UserAction.DOWNLOAD_OPEN_DIALOG,
//                                "Downloading video stream size",
//                                currentInfo.getServiceId()))));
                        showErrorActivity("Cannot get videos size")));
        disposables.add(StreamSizeWrapper.fetchSizeForWrapper(wrappedAudioStreams)
                .subscribe(result -> {
                    if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId()
                            == R.id.audio_button) {
                        setupAudioSpinner();
                    }
                }, throwable ->
//                        ErrorActivity.reportErrorInSnackbar(context,
//                        new ErrorInfo(throwable, UserAction.DOWNLOAD_OPEN_DIALOG,
//                                "Downloading audio stream size",
//                                currentInfo.getServiceId()))));
                        showErrorActivity("Cannot get audio size")));
        disposables.add(StreamSizeWrapper.fetchSizeForWrapper(wrappedSubtitleStreams)
                .subscribe(result -> {
                    if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId()
                            == R.id.subtitle_button) {
                        setupSubtitleSpinner();
                    }
                }, throwable ->
//                        ErrorActivity.reportErrorInSnackbar(context,
//                        new ErrorInfo(throwable, UserAction.DOWNLOAD_OPEN_DIALOG,
//                                "Downloading subtitle stream size",
//                                currentInfo.getServiceId()))));
                        showErrorActivity("Cannot get subtitle size")));
    }

    private void setupAudioSpinner() {
        if (getContext() == null) {
            return;
        }

        dialogBinding.qualitySpinner.setAdapter(audioStreamsAdapter);
        dialogBinding.qualitySpinner.setSelection(selectedAudioIndex);
        setRadioButtonsState(true);
    }

    private void setupVideoSpinner() {
        if (getContext() == null) {
            return;
        }

        dialogBinding.qualitySpinner.setAdapter(videoStreamsAdapter);
        dialogBinding.qualitySpinner.setSelection(selectedVideoIndex);
        setRadioButtonsState(true);

        if (type_download == ACCEPT_DOWNLOAD.LOW_QUALITY)
            dialogBinding.qualitySpinner.setEnabled(false);
    }

    private void setupSubtitleSpinner() {
        if (getContext() == null) {
            return;
        }

        dialogBinding.qualitySpinner.setAdapter(subtitleStreamsAdapter);
        dialogBinding.qualitySpinner.setSelection(selectedSubtitleIndex);
        setRadioButtonsState(true);
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Activity results
    //////////////////////////////////////////////////////////////////////////*/

    private void requestDownloadPickAudioFolderResult(final ActivityResult result) {
        requestDownloadPickFolderResult(
                result, getString(R.string.download_path_audio_key), DownloadManager.TAG_AUDIO);
    }

    private void requestDownloadPickVideoFolderResult(final ActivityResult result) {
        requestDownloadPickFolderResult(
                result, getString(R.string.download_path_video_key), DownloadManager.TAG_VIDEO);
    }

    private void requestDownloadSaveAsResult(final ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (result.getData() == null || result.getData().getData() == null) {
            showFailedDialog(R.string.general_error);
            return;
        }

        if (FilePickerActivityHelper.isOwnFileUri(context, result.getData().getData())) {
            final File file = Utils.getFileForUri(result.getData().getData());
            checkSelectedDownload(null, Uri.fromFile(file), file.getName(),
                    StoredFileHelper.DEFAULT_MIME);
            return;
        }

        final DocumentFile docFile
                = DocumentFile.fromSingleUri(context, result.getData().getData());
        if (docFile == null) {
            showFailedDialog(R.string.general_error);
            return;
        }

        // check if the selected file was previously used
        checkSelectedDownload(null, result.getData().getData(), docFile.getName(),
                docFile.getType());
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
            uri = Uri.fromFile(Utils.getFileForUri(uri));
        } else {
            context.grantUriPermission(context.getPackageName(), uri,
                    StoredDirectoryHelper.PERMISSION_FLAGS);
        }

//        PreferenceManager.getDefaultSharedPreferences(context).edit()
//                .putString(key, uri.toString()).apply();

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


    /*//////////////////////////////////////////////////////////////////////////
    // Listeners
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCheckedChanged(final RadioGroup group, @IdRes final int checkedId) {
        if (AdsManager.DEBUG) {
            Log.d(TAG, "onCheckedChanged() called with: "
                    + "group = [" + group + "], checkedId = [" + checkedId + "]");
        }
        boolean flag = true;

        if (checkedId == R.id.audio_button) {
            setupAudioSpinner();
        } else if (checkedId == R.id.video_button) {
            setupVideoSpinner();
        } else if (checkedId == R.id.subtitle_button) {
            setupSubtitleSpinner();
            flag = false;
        }

        dialogBinding.threads.setEnabled(flag);
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view,
                               final int position, final long id) {
        if (AdsManager.DEBUG) {
            Log.d(TAG, "onItemSelected() called with: "
                    + "parent = [" + parent + "], view = [" + view + "], "
                    + "position = [" + position + "], id = [" + id + "]");
        }
        int checkedRadioButtonId = dialogBinding.videoAudioGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.audio_button) {
            selectedAudioIndex = position;
        } else if (checkedRadioButtonId == R.id.video_button) {
            selectedVideoIndex = position;
        } else if (checkedRadioButtonId == R.id.subtitle_button) {
            selectedSubtitleIndex = position;
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Download
    //////////////////////////////////////////////////////////////////////////*/

    protected void setupDownloadOptions() {
        setRadioButtonsState(false);

        final boolean isVideoStreamsAvailable = videoStreamsAdapter.getCount() > 0;
        final boolean isAudioStreamsAvailable = audioStreamsAdapter.getCount() > 0;
        final boolean isSubtitleStreamsAvailable = subtitleStreamsAdapter.getCount() > 0;

        dialogBinding.audioButton.setVisibility(isAudioStreamsAvailable ? View.VISIBLE : View.GONE);
        dialogBinding.videoButton.setVisibility(isVideoStreamsAvailable ? View.VISIBLE : View.GONE);
        dialogBinding.subtitleButton.setVisibility(isSubtitleStreamsAvailable
                ? View.VISIBLE : View.GONE);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        String defaultMedia;
        if (type_download == ACCEPT_DOWNLOAD.HIGH_QUALITY)
            defaultMedia = prefs.getString(getString(R.string.last_used_download_type),
                    getString(R.string.last_download_type_video_key));
        else
            defaultMedia = getString(R.string.last_download_type_video_key);

        if (isVideoStreamsAvailable
                && (defaultMedia.equals(getString(R.string.last_download_type_video_key)))) {
            dialogBinding.videoButton.setChecked(true);
            setupVideoSpinner();
        } else if (isAudioStreamsAvailable
                && (defaultMedia.equals(getString(R.string.last_download_type_audio_key)))) {
            dialogBinding.audioButton.setChecked(true);
            setupAudioSpinner();
        } else if (isSubtitleStreamsAvailable
                && (defaultMedia.equals(getString(R.string.last_download_type_subtitle_key)))) {
            dialogBinding.subtitleButton.setChecked(true);
            setupSubtitleSpinner();
        } else if (isVideoStreamsAvailable) {
            dialogBinding.videoButton.setChecked(true);
            setupVideoSpinner();
        } else if (isAudioStreamsAvailable) {
            dialogBinding.audioButton.setChecked(true);
            setupAudioSpinner();
        } else if (isSubtitleStreamsAvailable) {
            dialogBinding.subtitleButton.setChecked(true);
            setupSubtitleSpinner();
        } else {
            Toast.makeText(getContext(), R.string.no_streams_available_download,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void setRadioButtonsState(final boolean enabled) {
        dialogBinding.audioButton.setEnabled(enabled);
        dialogBinding.videoButton.setEnabled(enabled);
        dialogBinding.subtitleButton.setEnabled(enabled);

//        if (type_download == ACCEPT_DOWNLOAD.LOW_QUALITY) {
//            dialogBinding.videoButton.setEnabled(true);
//            dialogBinding.videoButton.setChecked(true);
//
//            dialogBinding.audioButton.setEnabled(false);
//            dialogBinding.subtitleButton.setEnabled(false);
//        }
    }

    private int getSubtitleIndexBy(final List<SubtitlesStream> streams) {
        final Localization preferredLocalization = NewPipe.getPreferredLocalization();

        int candidate = 0;
        for (int i = 0; i < streams.size(); i++) {
            final Locale streamLocale = streams.get(i).getLocale();

            final boolean languageEquals = streamLocale.getLanguage() != null
                    && preferredLocalization.getLanguageCode() != null
                    && streamLocale.getLanguage()
                    .equals(new Locale(preferredLocalization.getLanguageCode()).getLanguage());
            final boolean countryEquals = streamLocale.getCountry() != null
                    && streamLocale.getCountry().equals(preferredLocalization.getCountryCode());

            if (languageEquals) {
                if (countryEquals) {
                    return i;
                }

                candidate = i;
            }
        }

        return candidate;
    }

    private String getNameEditText() {
        final String str = dialogBinding.fileName.getText().toString().trim();

        return FilenameUtils.createFilename(context, str.isEmpty() ? currentInfo.getName() : str);
    }

    private void showFailedDialog(@StringRes final int msg) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.general_error)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    private void launchDirectoryPicker(final ActivityResultLauncher<Intent> launcher) {
        AdsManager.getInstance().setDoNotShowOpen();
        launcher.launch(StoredDirectoryHelper.getPicker(context));
    }

    private void prepareSelectedDownload() {
        final StoredDirectoryHelper mainStorage;
        final MediaFormat format;
        final String selectedMediaType;

        // first, build the filename and get the output folder (if possible)
        // later, run a very very very large file checking logic

        filenameTmp = getNameEditText().concat(".");

        int checkedRadioButtonId = dialogBinding.videoAudioGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.audio_button) {
            selectedMediaType = getString(R.string.last_download_type_audio_key);
            mainStorage = mainStorageAudio;
            format = audioStreamsAdapter.getItem(selectedAudioIndex).getFormat();
            if (format == MediaFormat.WEBMA_OPUS) {
                mimeTmp = "audio/ogg";
                filenameTmp += "opus";
            } else {
                mimeTmp = format.mimeType;
                filenameTmp += format.suffix;
            }
        } else if (checkedRadioButtonId == R.id.video_button) {
            selectedMediaType = getString(R.string.last_download_type_video_key);
            mainStorage = mainStorageVideo;
            format = videoStreamsAdapter.getItem(selectedVideoIndex).getFormat();
            mimeTmp = format.mimeType;
            filenameTmp += format.suffix;
        } else if (checkedRadioButtonId == R.id.subtitle_button) {
            selectedMediaType = getString(R.string.last_download_type_subtitle_key);
            mainStorage = mainStorageVideo; // subtitle & video files go together
            format = subtitleStreamsAdapter.getItem(selectedSubtitleIndex).getFormat();
            mimeTmp = format.mimeType;
            filenameTmp += (format == MediaFormat.TTML ? MediaFormat.SRT : format).suffix;
        } else {
            throw new RuntimeException("No stream selected");
        }

        if (!askForSavePath
                && (mainStorage == null
        )) {
            // Pick new download folder if one of:
            // - Download folder is not set
            // - Download folder uses SAF while SAF is disabled
            // - Download folder doesn't use SAF while SAF is enabled
            // - Download folder uses SAF but the user manually revoked access to it
            Toast.makeText(context, getString(R.string.no_dir_yet),
                    Toast.LENGTH_LONG).show();

            if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId() == R.id.audio_button) {
                launchDirectoryPicker(requestDownloadPickAudioFolderLauncher);
            } else {
                launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
            }

            return;
        }

        if (askForSavePath) {
            final Uri initialPath;
            if (NewPipeSettings.useStorageAccessFramework(context)) {
                initialPath = null;
            } else {
                final File initialSavePath;
                if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId() == R.id.audio_button) {
                    initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MUSIC);
                } else {
                    initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MOVIES);
                }
                initialPath = Uri.parse(initialSavePath.getAbsolutePath());
            }

            requestDownloadSaveAsLauncher.launch(StoredFileHelper.getNewPicker(context,
                    filenameTmp, mimeTmp, initialPath));

            return;
        }

        // check for existing file with the same name
        checkSelectedDownload(mainStorage, mainStorage.findFile(filenameTmp), filenameTmp, mimeTmp);

        // remember the last media type downloaded by the user
        if (type_download == ACCEPT_DOWNLOAD.HIGH_QUALITY)
            prefs.edit().putString(getString(R.string.last_used_download_type), selectedMediaType)
                    .apply();
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
//            ErrorActivity.reportErrorInSnackbar(this,
//                    new ErrorInfo(e, UserAction.DOWNLOAD_FAILED, "Getting storage"));
            showErrorActivity("Getting storage failed");
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
                            Log.e(TAG, "Failed to take (or steal) the file in "
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
            Log.e(TAG, "failed to truncate the file: " + storage.getUri().toString(), e);
            showFailedDialog(R.string.overwrite_failed);
            return;
        }

        final Stream selectedStream;
        Stream secondaryStream = null;
        final char kind;
        int threads = dialogBinding.threads.getProgress() + 1;
        final String[] urls;
        final MissionRecoveryInfo[] recoveryInfo;
        String psName = null;
        String[] psArgs = null;
        long nearLength = 0;

        // more download logic: select muxer, subtitle converter, etc.
        int checkedRadioButtonId = dialogBinding.videoAudioGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.audio_button) {
            kind = 'a';
            selectedStream = audioStreamsAdapter.getItem(selectedAudioIndex);

            if (selectedStream.getFormat() == MediaFormat.M4A) {
                psName = Postprocessing.ALGORITHM_M4A_NO_DASH;
            } else if (selectedStream.getFormat() == MediaFormat.WEBMA_OPUS) {
                psName = Postprocessing.ALGORITHM_OGG_FROM_WEBM_DEMUXER;
            }
        } else if (checkedRadioButtonId == R.id.video_button) {
            kind = 'v';
            selectedStream = videoStreamsAdapter.getItem(selectedVideoIndex);

            final SecondaryStreamHelper<AudioStream> secondary = videoStreamsAdapter
                    .getAllSecondary()
                    .get(wrappedVideoStreams.getStreamsList().indexOf(selectedStream));

            if (secondary != null) {
                secondaryStream = secondary.getStream();

                if (selectedStream.getFormat() == MediaFormat.MPEG_4) {
                    psName = Postprocessing.ALGORITHM_MP4_FROM_DASH_MUXER;
                } else {
                    psName = Postprocessing.ALGORITHM_WEBM_MUXER;
                }

                psArgs = null;
                final long videoSize = wrappedVideoStreams
                        .getSizeInBytes((VideoStream) selectedStream);

                // set nearLength, only, if both sizes are fetched or known. This probably
                // does not work on slow networks but is later updated in the downloader
                if (secondary.getSizeInBytes() > 0 && videoSize > 0) {
                    nearLength = secondary.getSizeInBytes() + videoSize;
                }
            }
        } else if (checkedRadioButtonId == R.id.subtitle_button) {
            threads = 1; // use unique thread for subtitles due small file size
            kind = 's';
            selectedStream = subtitleStreamsAdapter.getItem(selectedSubtitleIndex);

            if (selectedStream.getFormat() == MediaFormat.TTML) {
                psName = Postprocessing.ALGORITHM_TTML_CONVERTER;
                psArgs = new String[]{
                        selectedStream.getFormat().getSuffix(),
                        "false" // ignore empty frames
                };
            }
        } else {
            return;
        }

        if (secondaryStream == null) {
            com.muicvtools.mutils.Utils.logDebug(this.getClass(),selectedStream.getUrl());
            urls = new String[]{
                    selectedStream.getUrl()
            };
            recoveryInfo = new MissionRecoveryInfo[]{
                    new MissionRecoveryInfo(selectedStream)
            };
        } else {
            urls = new String[]{
                    selectedStream.getUrl(), secondaryStream.getUrl()
            };
            recoveryInfo = new MissionRecoveryInfo[]{new MissionRecoveryInfo(selectedStream),
                    new MissionRecoveryInfo(secondaryStream)};
        }

        DownloadManagerService.startMission(context, urls, storage, kind, threads,
                currentInfo.getUrl(), psName, psArgs, nearLength, recoveryInfo);

        Toast.makeText(context, getString(R.string.download_has_started),
                Toast.LENGTH_SHORT).show();

        dismiss();
    }

    private void showErrorActivity(String error) {
        Toast.makeText(context, error,
                Toast.LENGTH_SHORT).show();
        //caomui show error activity
//        ErrorActivity.reportError(
//                context,
//                Collections.singletonList(e),
//                null,
//                null,
//                ErrorActivity.ErrorInfo.make(UserAction.SOMETHING_ELSE, "-", "-", R.string.general_error)
//        );
    }
}
