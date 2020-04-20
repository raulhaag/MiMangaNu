package ar.rulosoft.mimanganu.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.BuildConfig;
import ar.rulosoft.mimanganu.MessageActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jordy on 23/04/2017.
 */

public class AppUpdateUtil {
    private static final String TAG = "UpdateUtils";
    private static final String LATEST_RELEASE_URL = "https://github.com/raulhaag/MiMangaNu/releases/latest";
    private static final String LATEST_RELEASE_URL_API = "https://api.github.com/repos/raulhaag/MiMangaNu/releases/latest";

    private static File UPDATE_FILE_CACHE = new File(Environment.getExternalStorageDirectory() + "/download", "update.apk");
    private static int prog = 0;

    public static void checkAppUpdates(Context context) {
        new CheckForAppUpdates(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    public static void generateUpdateDialog(final Context context) {
        if (NetworkUtilsAndReceiver.isConnectedNonDestructive(context))
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        final Triple<String, String, String> info = getCurrentVersion();
                        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View rootView = inflater.inflate(R.layout.dialog_update, null);
                                final TextView desc = rootView.findViewById(R.id.descrption);
                                final ProgressBar progressBar = rootView.findViewById(R.id.progress);
                                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                desc.setText(info.getThird());
                                dialogBuilder.setTitle(context.getString(R.string.new_version) + " " + info.getFirst());
                                dialogBuilder.setView(rootView);
                                dialogBuilder.setPositiveButton(context.getString(R.string.download), null);
                                dialogBuilder.setNegativeButton(context.getString(R.string.close), null);
                                AlertDialog dialog = dialogBuilder.create();
                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(final DialogInterface dialog) {
                                        final Button cancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                                if (context instanceof MessageActivity) {
                                                    ((MessageActivity) context).onBackPressed();
                                                }
                                            }
                                        });
                                        final Button accept = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                        accept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                try {
                                                    AppCompatActivity activity = (AppCompatActivity) context;
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ((AlertDialog) dialog).setCancelable(false);
                                                            String init_download_text = context.getString(R.string.downloading) + " 0%";
                                                            desc.setText(init_download_text);
                                                            accept.setEnabled(false);
                                                            cancel.setEnabled(false);
                                                            progressBar.setVisibility(View.VISIBLE);
                                                            progressBar.setIndeterminate(true);
                                                        }
                                                    });
                                                    download(activity, info.getSecond(), progressBar, desc, dialog);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error while starting download");
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });
                                dialog.show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error while searching for new update");
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private static void download(final AppCompatActivity activity, final String url, final ProgressBar bar, final TextView desc, final DialogInterface dialog) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    UPDATE_FILE_CACHE = new File(PreferenceManager.getDefaultSharedPreferences(activity).getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/", "update.apk");
                    if (UPDATE_FILE_CACHE.exists()) UPDATE_FILE_CACHE.delete();
                    final OkHttpClient client = Navigator.getInstance().getHttpClient().newBuilder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .build();
                    Response response = client.newCall(new Request.Builder().url(url).build()).execute();
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream outputStream = new FileOutputStream(UPDATE_FILE_CACHE);
                    long lengthOfFile = response.body().contentLength();
                    int count;
                    byte[] data = new byte[1024 * 6];
                    long total = 0;
                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        int tprog = (int) ((total * 100) / lengthOfFile);
                        if (tprog > prog) {
                            prog = tprog;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String downloading_text = activity.getString(R.string.downloading) + " " + prog + "%";
                                    desc.setText(downloading_text);
                                    bar.setIndeterminate(false);
                                    bar.setProgress(prog);
                                }
                            });
                        }
                        outputStream.write(data, 0, count);
                        outputStream.flush();
                    }
                    outputStream.close();
                    inputStream.close();
                    activity.startActivity(getAndPrepareUpdateIntent(activity));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error while downloading update");
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, R.string.update_error, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Intent getAndPrepareUpdateIntent(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(UPDATE_FILE_CACHE), "application/vnd.android.package-archive");
            return intent;
        } else {
            Uri contentUri = FileProvider.getUriForFile(context, "ar.rulosoft.provider", UPDATE_FILE_CACHE);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            return intent;
        }
    }

    private static Triple<String, String, String> getCurrentVersion() {
        Navigator.getInstance().flushParameter();
        try {
            final JSONObject object = new JSONObject(Navigator.getInstance().get(LATEST_RELEASE_URL_API));
            String version = object.getString("tag_name");
            String data = object.getString("body");
            String link = object.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            return new Triple<>(version, link, data);
        } catch (Exception e) {
        }
        try {
            String source = Navigator.getInstance().get(LATEST_RELEASE_URL);
            String link = "https://github.com" + Util.getInstance().getFirstMatchDefault("(/raulhaag/MiMangaNu[^\"]+apk)", source, "");
            if (link.equals("https://github.com")) {
                return null;
            }
            String version = Util.getInstance().getFirstMatchDefault("tree/([^\"]+)", source, "");
            String data = Util.getInstance().getFirstMatchDefault("<div class=\"markdown-body\">([\\s\\S]+?)</div>", source, "")
                    .replaceAll("<li>(.+)</li>", "* $1")
                    .replaceAll("\\s*<p>([^<]+)</p>", "$1")
                    .replaceAll("<[^>]+>", "");
            return new Triple<>(version, link, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInstallerPackagename(Context context) {
        String installer = (context.getPackageManager().getInstallerPackageName(context.getPackageName()));
        return installer != null ? installer : "com.none.found";
    }

    public static class CheckForAppUpdates extends AsyncTask<Void, Integer, Void> {
        private String error = "";
        private Context context;

        CheckForAppUpdates(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Triple<String, String, String> info = getCurrentVersion();
                if (!BuildConfig.VERSION_NAME.replace("-github", "").equals(info.getFirst())) {
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(MessageActivity.MESSAGE_VALUE, MessageActivity.MESSAGE_UPDATE);
                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(),
                            intent, context.getString(R.string.app_update), context.getString(R.string.app_name) +
                                    " v" + info.getFirst() + " " + context.getString(R.string.is_available));
                } else {
                    Log.i("Util", "App is up to date. No update necessary");
                }
            } catch (Exception e) {
                Log.e("Util", "checkAppUpdates Exception");
                e.printStackTrace();
                error = Log.getStackTraceString(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!error.isEmpty())
                Log.e("Util", error);
            super.onPostExecute(result);
        }
    }
}