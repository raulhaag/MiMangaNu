package ar.rulosoft.mimanganu.services;

import android.util.Log;
import ar.rulosoft.navegadores.Navegador;
import ar.rulosoft.navegadores.RefererInterceptor;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class SingleDownload implements Runnable {
    public static int RETRY = 3;
    public static Navegador NAVEGADOR = null;
    private String fromURL;
    private String toFile;
    private StateChange changeListener = null;
    int index, cid;
    ChapterDownload cd;
    Status status = Status.QUEUED;
    private int retry = RETRY;
    public String referer;

    public SingleDownload(String fromURL, String toFile, int index, int cid, ChapterDownload cd,String referer) {
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

    public static Navegador initAndGetNavegador() {
        if (NAVEGADOR == null) NAVEGADOR = new Navegador();
        return NAVEGADOR;
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
                long contentLenght;
                try {
                    OkHttpClient client = initAndGetNavegador().getHttpClient();
                    client.setConnectTimeout(3, TimeUnit.SECONDS);
                    client.setReadTimeout(3, TimeUnit.SECONDS);
                    client.networkInterceptors().add(new RefererInterceptor(referer));
                    Response response = client.newCall(new Request.Builder().url(fromURL).build()).execute();
                    if(!response.isSuccessful()) {
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
                    contentLenght = response.body().contentLength();
                    input = response.body().byteStream();
                    output = new FileOutputStream(ot);
                } catch (FileNotFoundException e) {
                    changeStatus(Status.ERROR_WRITING_FILE);
                    retry = 0;
                    break;
                } catch (IOException e) {
                    changeStatus(Status.ERROR_OPENING_FILE);
                    retry = 0;
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
                } finally {
                    boolean flagedOk = false;
                    if (status != Status.RETRY) {
                        if (contentLenght > ot.length()) {
                            Log.e("MIMANGA DOWNLOAD", "content lenght =" + contentLenght +
                                    " size =" + o.length() + " on =" + o.getPath());
                            ot.delete();
                            retry--;
                            changeStatus(Status.RETRY);
                        } else {
                            flagedOk = true;
                        }
                    }
                    try {
                        output.flush();
                        output.close();
                        input.close();
                        if (flagedOk) {
                            if (ot.length() > 0) {
                                ot.renameTo(o);
                            } else {
                                ot.delete();
                                writeErrorImage(ot);
                                ot.renameTo(o);
                            }
                            Log.i("MIMANGA DOWNLOAD", "download ok =" + o.getPath());
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

    public void setChangeListener(StateChange changeListener) {
        this.changeListener = changeListener;
    }

    enum Status {
        QUEUED, INIT, DOWNLOADING, RETRY, POSTPONED, DOWNLOAD_OK, ERROR_CONNECTION,
        ERROR_404, ERROR_TIMEOUT, ERROR_ON_UPLOAD, ERROR_INVALID_URL, ERROR_WRITING_FILE,
        ERROR_OPENING_FILE
    }
}