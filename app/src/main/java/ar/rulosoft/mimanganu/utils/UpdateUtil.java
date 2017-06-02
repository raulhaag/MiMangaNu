package ar.rulosoft.mimanganu.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.mimanganu.MessageActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static ar.rulosoft.mimanganu.MainActivity.pm;

/**
 * Created by Jordy on 23/04/2017.
 */

public class UpdateUtil {
    private static final String TAG = "UpdateUtils";
    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/raulhaag/MiMangaNu/releases/latest";
    private static File UPDATE_FILE_CACHE = new File(Environment.getExternalStorageDirectory() + "/download", "update.apk");
    private static int prog = 0;

    public static void checkAppUpdates(Context context) {
        new CheckForAppUpdates(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void generatUpdateDialog(final Context context) {
        if (NetworkUtilsAndReceiver.isConnectedNonDestructive(context))
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        final OkHttpClient client = Navigator.navigator.getHttpClient().newBuilder()
                                .connectTimeout(3, TimeUnit.SECONDS)
                                .readTimeout(3, TimeUnit.SECONDS)
                                .build();
                        Response response = client.newCall(new Request.Builder().url(LATEST_RELEASE_URL).build()).execute();
                        final JSONObject object = new JSONObject(response.body().string());
                        String version_name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                        if (!version_name.equals(object.getString("tag_name"))) {            //Test <-----
                            ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View rootView = inflater.inflate(R.layout.dialog_update, null);
                                    final TextView desc = (TextView) rootView.findViewById(R.id.descrption);
                                    final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
                                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                    try {
                                        desc.setText(object.getString("body"));
                                        dialogBuilder.setTitle(context.getString(R.string.new_version) + " " + object.getString("tag_name"));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error reading source");
                                    }
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
                                                        download(activity, object.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"), progressBar, desc, dialog);
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
                        } else {
                            Log.i(TAG, "App is up to date!!!!");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error while searching for new update");
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static void download(final AppCompatActivity activity, final String url, final ProgressBar bar, final TextView desc, final DialogInterface dialog) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    UPDATE_FILE_CACHE = new File(PreferenceManager.getDefaultSharedPreferences(activity).getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/", "update.apk");
                    if (UPDATE_FILE_CACHE.exists()) UPDATE_FILE_CACHE.delete();
                    final OkHttpClient client = Navigator.navigator.getHttpClient().newBuilder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .build();
                    Response response = client.newCall(new Request.Builder().url(url).build()).execute();
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream outputStream = new FileOutputStream(UPDATE_FILE_CACHE);
                    long lenghtOfFile = response.body().contentLength();
                    int count;
                    byte data[] = new byte[1024 * 6];
                    long total = 0;
                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        int tprog = (int) ((total * 100) / lenghtOfFile);
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
                    activity.startActivity(getUpdateIntent());
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

    private static Intent getUpdateIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(UPDATE_FILE_CACHE), "application/vnd.android.package-archive");
        return intent;
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
                if (Navigator.navigator == null)
                    Navigator.navigator = new Navigator(context);
                Navigator.navigator.flushParameter();
                String source = Navigator.navigator.get("https://api.github.com/repos/raulhaag/MiMangaNu/releases/latest");
                final JSONObject jsonObject = new JSONObject(source);

                int onlineVersionMinor = Integer.parseInt(Util.getInstance().getFirstMatchDefault("\"tag_name\": \"\\d+\\.(\\d+)\"", source, ""));
                int onlineVersionMajor = Integer.parseInt(Util.getInstance().getFirstMatchDefault("\"tag_name\": \"(\\d+)\\.\\d+\"", source, ""));
                //String body = getFirstMatchDefault("\"body\": \"(.+?)\"", source, "").replaceAll("\\\\r\\\\n","").trim().replaceAll("  "," ");
                final String download_url = Util.getInstance().getFirstMatchDefault("\"browser_download_url\": \"(.+?)\"", source, "");
                String currentVersionTmp = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                int currentVersionMinor = Integer.parseInt(Util.getInstance().getFirstMatchDefault("\\d+\\.(\\d+)", currentVersionTmp, ""));
                int currentVersionMajor = Integer.parseInt(Util.getInstance().getFirstMatchDefault("(\\d+)\\.\\d+", currentVersionTmp, ""));

                if (currentVersionMinor != onlineVersionMinor || currentVersionMajor != onlineVersionMajor) {
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(MessageActivity.MESSAGE_VALUE, MessageActivity.MESSAGE_UPDATE);
                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), intent, context.getString(R.string.app_update), context.getString(R.string.app_name) + " v" + onlineVersionMajor + "." + onlineVersionMinor + " " + context.getString(R.string.is_available));
                    pm.edit().putBoolean("on_latest_app_version", false).apply();
                    //Only display update if user tap on notification
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