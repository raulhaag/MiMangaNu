package ar.rulosoft.mimanganu.services;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import ar.rulosoft.navegadores.Navigator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingleDownload implements Runnable {
    public static int RETRY = 3;
    public boolean referer;
    public Status status = Status.QUEUED;
    int index, cid;
    ChapterDownload cd;
    private String fromURL;
    private String toFile;
    private StateChangeListener changeListener = null;
    private int retry = RETRY;

    public SingleDownload(String fromURL, String toFile, int index, int cid, ChapterDownload cd, boolean referer) {
        super();
        this.fromURL = fromURL;
        this.toFile = toFile;
        this.index = index;
        this.cid = cid;
        this.referer = referer;
        this.cd = cd;
    }

    public int getIndex() {
        return index;
    }


    @Override
    public void run() {
        changeStatus(Status.INIT);
        while (status != Status.DOWNLOAD_OK && retry > 0) {
            File o = new File(toFile);
            File ot = new File(toFile + ".temp");
            if (ot.exists()) {
                ot.delete();
            }
            if (o.length() == 0) {
                InputStream input;
                OutputStream output;
                Response response;
                long contentLength;
                try {
                    OkHttpClient copy;
                    Request request;
                    copy = Navigator.navigator.getHttpClient().newBuilder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .build();
                    if (referer) {
                        request = new Request.Builder().url(fromURL).addHeader("Referer", cd.chapter.getPath()).build();
                    } else {
                        request = new Request.Builder().url(fromURL).build();
                    }
                    response = copy.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            changeStatus(Status.ERROR_404);
                        } else {
                            changeStatus(Status.ERROR_CONNECTION);
                        }
                        retry = 0;
                        ot.delete();
                        writeErrorImage(ot);
                        ot.renameTo(o);
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
                        } catch (InterruptedException e1) {
                        }
                        continue;
                    } else {
                        changeStatus(Status.ERROR_WRITING_FILE);
                        break;
                    }
                } catch (IOException e) {
                    Log.e("SingleDownload", "ERROR_OPENING_FILE");
                    retry--;
                    if (retry > 0) {
                        changeStatus(Status.RETRY);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e1) {
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
                    } catch (InterruptedException e1) {
                    }
                    Log.e("SingleDownload", "ERROR_TIMEOUT");
                } finally {
                    boolean flaggedOk = false;
                    if (status != Status.RETRY) {
                        if (contentLength > ot.length()) {
                            Log.e("SingleDownload", "content length = " + contentLength + " size = " + o.length() + " on = " + o.getPath());
                            ot.delete();
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
                                ot.renameTo(o);
                            } else {
                                ot.delete();
                                writeErrorImage(ot);
                                ot.renameTo(o);
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
        if (DownloadPoolService.actual != null) {
            InputStream ims = DownloadPoolService.actual.getAssets().open("error_image.jpg");
            FileOutputStream output = new FileOutputStream(ot);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ims.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            ims.close();
            output.flush();
            output.close();
        }
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
        ERROR_OPENING_FILE
    }
}