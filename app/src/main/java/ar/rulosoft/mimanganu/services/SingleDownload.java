package ar.rulosoft.mimanganu.services;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import ar.rulosoft.mimanganu.utils.PostProcess;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static ar.rulosoft.mimanganu.utils.PostProcess.FLAG_PPL90;

public class SingleDownload implements Runnable {
    public static int RETRY = 3;
    private String fromURL;
    private final String toFile;
    private final String inRef;
    public boolean referer;
    public Status status = Status.QUEUED;
    int index, cid;
    ChapterDownload cd;
    private StateChangeListener changeListener = null;
    private int retry = RETRY;
    private Context context;
    private boolean needPP = false;

    public SingleDownload(Context context, String fromURL, String toFile, int index, int cid, ChapterDownload cd, boolean referer) {
        super();
        if (fromURL.contains("|")) {
            String[] parts = fromURL.split("\\|");
            this.fromURL = parts[0];
            inRef = parts[1];
        } else {
            this.fromURL = fromURL;
            inRef = null;
        }
        if (fromURL.contains(FLAG_PPL90)) {
            needPP = true;
            fromURL = fromURL.replace("[L90]", "");
            this.fromURL = fromURL;
        }
        this.toFile = toFile;
        this.index = index;
        this.cid = cid;
        this.referer = referer;
        this.cd = cd;
        this.context = context;
    }

    public int getIndex() {
        return index;
    }


    @Override
    public void run() {
        changeStatus(Status.INIT);
        while (status != Status.DOWNLOAD_OK && retry >= 0) {
            File o = new File(toFile);
            File ot = new File(toFile + ".temp");
            if (ot.exists()) {
                if (!ot.delete()) {
                    Log.e("SingleDownload", "failed to delete temporary file");
                }
            }
            if (o.length() == 0) {
                InputStream input;
                OutputStream output;
                Response response;
                long contentLength;
                try {
                    OkHttpClient copy;
                    Request request;
                    Request.Builder rBuilder;
                    copy = Navigator.getInstance().getHttpClient().newBuilder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .build();
                    rBuilder = new Request.Builder()
                            .addHeader("User-Agent", Navigator.USER_AGENT)
                            .addHeader("Connection", "keep-alive");
                    if (referer) {
                        if (inRef != null) {
                            rBuilder.addHeader("Referer", inRef);
                        } else {
                            rBuilder.addHeader("Referer", cd.chapter.getPath());
                        }
                    }
                    request = rBuilder.url(fromURL).build();
                    response = copy.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            changeStatus(Status.ERROR_404);
                        } else {
                            changeStatus(Status.ERROR_CONNECTION);
                        }
                        retry = 0;
                        //noinspection ResultOfMethodCallIgnored
                        ot.delete();
                        writeErrorImage(ot);
                        if (!ot.renameTo(o)) {
                            Log.e("SingleDownload", "failed to rename temporary file");
                        }
                        break;
                    }
                    contentLength = response.body().contentLength();
                    input = response.body().byteStream();
                    output = new FileOutputStream(ot);
                } catch (FileNotFoundException e) {
                    Log.e("SingleDownload", "ERROR_WRITING_FILE");
                    retry--;
                    if (retry > 0) {
                        changeStatus(Status.RETRY);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    } else {
                        changeStatus(Status.ERROR_WRITING_FILE);
                        break;
                    }
                } catch (IOException e) {
                    Log.e("SingleDownload", "ERROR_OPENING_FILE" + e.getLocalizedMessage());
                    retry--;
                    if (retry > 0) {
                        changeStatus(Status.RETRY);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    } else {
                        changeStatus(Status.ERROR_OPENING_FILE);
                        break;
                    }
                } catch (Exception e) {
                    retry = 0;
                    Log.e("SingleDownload", "ERROR_CONNECTION " + e.getMessage());
                    e.printStackTrace();
                    changeStatus(Status.ERROR_CONNECTION);
                    break;
                }
                try {
                    changeStatus(Status.DOWNLOADING);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (Exception e) {
                    retry--;
                    if (retry > 0) {
                        changeStatus(Status.RETRY);
                    } else {
                        changeStatus(Status.ERROR_TIMEOUT);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {
                    }
                    Log.e("SingleDownload", "ERROR_TIMEOUT");
                } finally {
                    boolean flaggedOk = false;
                    if (status != Status.RETRY) {
                        if (contentLength > ot.length()) {
                            Log.e("SingleDownload", "content length = " + contentLength + " size = " + o.length() + " on = " + o.getPath());
                            if (!ot.delete()) {
                                Log.e("SingleDownload", "failed to delete temporary file");
                            }
                            retry--;
                            changeStatus(Status.RETRY);
                        } else {
                            flaggedOk = true;
                        }
                    }
                    try {
                        output.flush();
                        output.close();
                        input.close();
                        response.body().close();
                        if (flaggedOk) {
                            if (ot.length() > 0) {
                                if (needPP) {
                                    PostProcess.l90(ot.getAbsolutePath());
                                }
                                if (!ot.renameTo(o)) {
                                    Log.e("SingleDownload", "failed to rename temporary file");
                                }
                            } else {
                                if (!ot.delete()) {
                                    Log.e("SingleDownload", "failed to delete temporary file");
                                }
                                writeErrorImage(ot);
                                if (!ot.renameTo(o)) {
                                    Log.e("SingleDownload", "failed to rename temporary file");
                                }
                            }

                            //  Log.i("SingleDownload", "download ok =" + o.getPath());
                            changeStatus(Status.DOWNLOAD_OK);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                changeStatus(Status.DOWNLOAD_OK);
            }
        }
    }

    private void writeErrorImage(File ot) throws IOException {
        InputStream is = context.getAssets().open("error_image.jpg");
        FileOutputStream os = new FileOutputStream(ot);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
    }

    private void changeStatus(Status status) {
        this.status = status;
        if (changeListener != null && status.ordinal() > Status.POSTPONED.ordinal()) {
            changeListener.onChange(this);
        }
    }

    public void setChangeListener(StateChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public enum Status {
        QUEUED, INIT, DOWNLOADING, RETRY, POSTPONED, DOWNLOAD_OK, ERROR_CONNECTION,
        ERROR_404, ERROR_TIMEOUT, ERROR_ON_UPLOAD, ERROR_INVALID_URL, ERROR_WRITING_FILE,
        ERROR_OPENING_FILE, POSTPROCESSING
    }
}