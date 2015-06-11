package ar.rulosoft.mimanganu.services;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SingleDownload implements Runnable {
    public static int RETRY = 3;
    String fromURL, toFile;
    StateChange changeListener = null;
    int index, cid;
    Status status = Status.QUEUED;
    int retry = RETRY;

    public SingleDownload(String fromURL, String toFile, int index, int cid) {
        super();
        this.fromURL = fromURL;
        this.toFile = toFile;
        this.index = index;
        this.cid = cid;
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
                int contentLenght;
                try {
                    URL url = new URL(fromURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    int code = con.getResponseCode();
                    if (code != 200) {
                        if (code == 404) {
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
                    contentLenght = con.getContentLength();
                    input = con.getInputStream();
                    output = new FileOutputStream(ot);
                } catch (MalformedURLException e) {
                    changeStatus(Status.ERROR_INVALID_URL);
                    retry = 0;
                    break;
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
                    int bytesRead = 0;
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
                            Log.e("MIMANGA DOWNLOAD", "content lenght =" + contentLenght + " size =" + o.length() + " on =" + o.getPath());
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

    void writeErrorImage(File ot) throws IOException {
        if (DownloadPoolService.actual != null) {
            InputStream ims = DownloadPoolService.actual.getAssets().open("error_image.jpg");
            FileOutputStream output = new FileOutputStream(ot);
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = ims.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
            ims.close();
            output.flush();
            output.close();
        }
    }

    void changeStatus(Status status) {
        this.status = status;
        if (changeListener != null && status.ordinal() > Status.POSTPONED.ordinal()) {
            changeListener.onChange(this);
        }
    }

    public void setChangeListener(StateChange changeListener) {
        this.changeListener = changeListener;
    }

    enum Status {
        QUEUED, INIT, DOWNLOADING, RETRY, POSTPONED, DOWNLOAD_OK, ERROR_CONNECTION, ERROR_404, ERROR_TIMEOUT, ERROR_ON_UPLOAD, ERROR_INVALID_URL, ERROR_WRITING_FILE, ERROR_OPENING_FILE
    }
}