package us.shandian.giga.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.muicvtools.mutils.AdOverlayActivity;
import com.muicvtools.mutils.AdsManager;
import com.muicvtools.mutils.Constants;
import com.muicvtools.mutils.R;
import com.muicvtools.mutils.databinding.ActivityDownloaderBinding;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;

import us.shandian.giga.io.StoredDirectoryHelper;
import us.shandian.giga.service.DownloadManager;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.ui.fragment.MissionsFragment;
import us.shandian.giga.util.FilePickerActivityHelper;

public class DownloadActivity extends AdOverlayActivity {

    private static final String MISSIONS_FRAGMENT_TAG = "fragment_tag";
    private TextView ytDownloadPath;
    private ActivityDownloaderBinding downloaderBinding;


    private final ActivityResultLauncher<Intent> requestDownloadPickVideoFolderLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), this::requestDownloadPickVideoFolderResult);

    @Override
    protected boolean isShowOpenAds() {
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // Service
        final Intent i = new Intent();
        i.setClass(this, DownloadManagerService.class);
        startService(i);

//        assureCorrectAppLanguage(this);
//        ThemeHelper.setTheme(this);

        super.onCreate(savedInstanceState);

        downloaderBinding =
                ActivityDownloaderBinding.inflate(getLayoutInflater());
        setContentView(downloaderBinding.getRoot());

//        setSupportActionBar(downloaderBinding.toolbarLayout.toolbar);

//        final ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.downloads_title);
//            actionBar.setDisplayShowTitleEnabled(true);
//        }
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.downloads_title);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        updateFragments();
                        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        setTextDownloadPath();

        downloaderBinding.btnChangeFolder.setVisibility(View.VISIBLE);
        downloaderBinding.btnChangeFolder.setOnClickListener(view ->
        {
//            AdsManager.getInstance().setDoNotShowOpen();
//            launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
            showPopupChooseFolder();
        });
    }

    private void setTextDownloadPath() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = this.getString(R.string.download_path_video_key);
        final String downloadPath = prefs.getString(key, null);

        ytDownloadPath = downloaderBinding.ytDownloadPath;
        if (downloadPath != null)
            ytDownloadPath.setText("Download path: " + Uri.parse(downloadPath).getPath());
        else
            ytDownloadPath.setText("Please choose default download path");
        com.muicvtools.mutils.Utils.logDebug(this.getClass(), downloadPath);
    }

    private void showPopupChooseFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View root = getLayoutInflater().inflate(R.layout.layout_select_folder, null);
        builder.setTitle(R.string.select_folder_title)
                .setView(root)
                .setCancelable(true);
        AlertDialog alertDialog = builder.create();

        Button btn_select_folder = root.findViewById(R.id.btn_select_folder);
        Button btn_default_folder = root.findViewById(R.id.btn_default_folder);
        btn_select_folder.setOnClickListener(view -> {
            alertDialog.dismiss();
            AdsManager.getInstance().setDoNotShowOpen();
            launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
        });

        btn_default_folder.setOnClickListener(view -> {
            alertDialog.dismiss();
            setDefaultDownloadFolder();
        });
        alertDialog.show();
    }

    private void setDefaultDownloadFolder() {
        final String key_audio = getString(R.string.download_path_video_key);
        final String key_video = getString(R.string.download_path_audio_key);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.putString(key_video, new File(getDir(Environment.DIRECTORY_DOWNLOADS), Constants.VIDEO_AUDIO_PATH).toURI().toString());
        spEditor.putString(key_audio, new File(getDir(Environment.DIRECTORY_DOWNLOADS), Constants.VIDEO_AUDIO_PATH).toURI().toString());
        Log.d("adsdk",  new File(getDir(Environment.DIRECTORY_DOWNLOADS), Constants.VIDEO_AUDIO_PATH).toURI().toString());
        spEditor.apply();

        setTextDownloadPath();
    }

    private static File getDir(final String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private void launchDirectoryPicker(final ActivityResultLauncher<Intent> launcher) {
        launcher.launch(StoredDirectoryHelper.getPicker(this));
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
        if (FilePickerActivityHelper.isOwnFileUri(this, uri)) {
            uri = Uri.fromFile(Utils.getFileForUri(uri));
        } else {
            grantUriPermission(getPackageName(), uri,
                    StoredDirectoryHelper.PERMISSION_FLAGS);
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.download_path_video_key), uri.toString()).apply();

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.download_path_audio_key), uri.toString()).apply();

        ytDownloadPath.setText("Download path: " + uri.getPath());
    }

    private void showFailedDialog(@StringRes final int msg) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.general_error)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    private void updateFragments() {
        final MissionsFragment fragment = new MissionsFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment, MISSIONS_FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.download_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
