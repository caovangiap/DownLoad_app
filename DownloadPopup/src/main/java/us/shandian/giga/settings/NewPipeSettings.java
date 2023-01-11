package us.shandian.giga.settings;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.muicvtools.mutils.R;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.Localization;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import us.shandian.giga.get.DownloaderImpl;
import us.shandian.giga.util.DeviceUtils;
import us.shandian.giga.util.ExceptionUtils;

/*
 * Created by k3b on 07.01.2016.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * NewPipeSettings.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Helper class for global settings.
 */
public final class NewPipeSettings {
    private NewPipeSettings() {
    }

    public static void initSettings(final Context context) {
        // check if there are entries in the prefs to determine whether this is the first app run
        Boolean isFirstRun = null;
        final Set<String> prefsKeys = PreferenceManager.getDefaultSharedPreferences(context)
                .getAll().keySet();
        for (final String key : prefsKeys) {
            // ACRA stores some info in the prefs during app initialization
            // which happens before this method is called. Therefore ignore ACRA-related keys.
            if (!key.toLowerCase().startsWith("acra")) {
                isFirstRun = false;
                break;
            }
        }
        if (isFirstRun == null) {
            isFirstRun = true;
        }

//        saveDefaultVideoDownloadDirectory(context, download_folder);
//        saveDefaultAudioDownloadDirectory(context, download_folder);

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = context.getResources().getConfiguration().locale;
        }

        NewPipe.init(getDownloader(),
                new Localization(locale.getCountry(), locale.getLanguage()));

//        SettingMigrations.initMigrations(context, isFirstRun);
        configureRxJavaErrorHandler();
        initNotificationChannels(context);

    }

    private static void configureRxJavaErrorHandler() {
        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull final Throwable throwable) {
                Log.e("adsdk", "RxJavaPlugins.ErrorHandler called with -> : "
                        + "throwable = [" + throwable.getClass().getName() + "]");

                final Throwable actualThrowable;
                if (throwable instanceof UndeliverableException) {
                    // As UndeliverableException is a wrapper,
                    // get the cause of it to get the "real" exception
                    actualThrowable = throwable.getCause();
                } else {
                    actualThrowable = throwable;
                }

                final List<Throwable> errors;
                if (actualThrowable instanceof CompositeException) {
                    errors = ((CompositeException) actualThrowable).getExceptions();
                } else {
                    errors = Collections.singletonList(actualThrowable);
                }

                for (final Throwable error : errors) {
                    if (isThrowableIgnored(error)) {
                        return;
                    }
                    if (isThrowableCritical(error)) {
                        reportException(error);
                        return;
                    }
                }

                // Out-of-lifecycle exceptions should only be reported if a debug user wishes so,
                // When exception is not reported, log it
                if (isDisposedRxExceptionsReported()) {
                    reportException(actualThrowable);
                } else {
                    Log.e("adsdk", "RxJavaPlugin: Undeliverable Exception received: ", actualThrowable);
                }
            }

            private boolean isThrowableIgnored(@NonNull final Throwable throwable) {
                // Don't crash the application over a simple network problem
                return ExceptionUtils.hasAssignableCause(throwable,
                        // network api cancellation
                        IOException.class, SocketException.class,
                        // blocking code disposed
                        InterruptedException.class, InterruptedIOException.class);
            }

            private boolean isThrowableCritical(@NonNull final Throwable throwable) {
                // Though these exceptions cannot be ignored
                return ExceptionUtils.hasAssignableCause(throwable,
                        NullPointerException.class, IllegalArgumentException.class, // bug in app
                        OnErrorNotImplementedException.class, MissingBackpressureException.class,
                        IllegalStateException.class); // bug in operator
            }

            private void reportException(@NonNull final Throwable throwable) {
                // Throw uncaught exception that will trigger the report system
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), throwable);
            }
        });
    }

    private static void initNotificationChannels(Context context) {
        // Keep the importance below DEFAULT to avoid making noise on every notification update for
        // the main and update channels
        final NotificationChannelCompat mainChannel = new NotificationChannelCompat
                .Builder(context.getString(R.string.notification_channel_id),
                NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.notification_channel_name))
                .setDescription(context.getString(R.string.notification_channel_description))
                .build();

//        final NotificationChannelCompat appUpdateChannel = new NotificationChannelCompat
//                .Builder(getString(R.string.app_update_notification_channel_id),
//                NotificationManagerCompat.IMPORTANCE_LOW)
//                .setName(getString(R.string.app_update_notification_channel_name))
//                .setDescription(getString(R.string.app_update_notification_channel_description))
//                .build();

//        final NotificationChannelCompat hashChannel = new NotificationChannelCompat
//                .Builder(getString(R.string.hash_channel_id),
//                NotificationManagerCompat.IMPORTANCE_HIGH)
//                .setName(getString(R.string.hash_channel_name))
//                .setDescription(getString(R.string.hash_channel_description))
//                .build();

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannelsCompat(Arrays.asList(mainChannel));
    }

    protected static boolean isDisposedRxExceptionsReported() {
        return false;
    }

    private static DownloaderImpl getDownloader() {
        return DownloaderImpl.init(null);
    }

    static void saveDefaultVideoDownloadDirectory(final Context context, String download_folder) {
        saveDefaultDirectory(context, R.string.download_path_video_key,
                Environment.DIRECTORY_DOWNLOADS, download_folder);
    }

    static void saveDefaultAudioDownloadDirectory(final Context context, String download_folder) {
        saveDefaultDirectory(context, R.string.download_path_audio_key,
                Environment.DIRECTORY_DOWNLOADS, download_folder);
    }

    private static void saveDefaultDirectory(final Context context, final int keyID,
                                             final String defaultDirectoryName, String download_folder) {
        if (download_folder != null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String key = context.getString(keyID);
            final String downloadPath = prefs.getString(key, null);
            if (!isNullOrEmpty(downloadPath)) {
                return;
            }

            final SharedPreferences.Editor spEditor = prefs.edit();
//            spEditor.putString(key, getNewPipeChildFolderPathForDir(getDir(defaultDirectoryName)));
            spEditor.putString(key, new File(getDir(defaultDirectoryName), download_folder).toURI().toString());
            spEditor.apply();
        } else if (!useStorageAccessFramework(context)) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String key = context.getString(keyID);
            final String downloadPath = prefs.getString(key, null);
            if (!isNullOrEmpty(downloadPath)) {
                return;
            }

            final SharedPreferences.Editor spEditor = prefs.edit();
//            spEditor.putString(key, getNewPipeChildFolderPathForDir(getDir(defaultDirectoryName)));
            spEditor.putString(key, new File(getDir(defaultDirectoryName), download_folder).toURI().toString());
            spEditor.apply();
        }
    }

    @NonNull
    public static File getDir(final String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private static String getNewPipeChildFolderPathForDir(final File dir) {
        return new File(dir, "NewPipe").toURI().toString();
    }

    public static boolean useStorageAccessFramework(final Context context) {
        // There's a FireOS bug which prevents SAF open/close dialogs from being confirmed with a
        // remote (see #6455).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || DeviceUtils.isFireTv(context)) {
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        final String key = context.getString(R.string.storage_use_saf);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, true);
    }
}
