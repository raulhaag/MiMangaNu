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

public class DescargaIndividual implements Runnable {
    public static int REINTENTOS = 3;
    String origen, destino;
    CambioEstado cambioListener = null;
    int index, cid;
    Estados estado = Estados.EN_COLA;
    int reintentos = REINTENTOS;

    public DescargaIndividual(String origen, String destino, int index, int cid) {
        super();
        this.origen = origen;
        this.destino = destino;
        this.index = index;
        this.cid = cid;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void run() {
        changeStatus(Estados.INICIADA);
        while (estado != Estados.DESCARGA_OK && reintentos > 0) {
            File o = new File(destino);
            File ot = new File(destino + ".temp");
            if (ot.exists()) {
                ot.delete();
            }
            if (o.length() == 0) {
                InputStream input;
                OutputStream output;
                int contentLenght;
                try {
                    URL url = new URL(origen);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    int code = con.getResponseCode();
                    if (code != 200) {
                        if (code == 404) {
                            changeStatus(Estados.ERROR_404);
                        } else {
                            changeStatus(Estados.ERROR_CONECCION);
                        }
                        reintentos = 0;
                        ot.delete();
                        writeErrorImage(ot);
                        ot.renameTo(o);
                        break;
                    }
                    contentLenght = con.getContentLength();
                    input = con.getInputStream();
                    output = new FileOutputStream(ot);
                } catch (MalformedURLException e) {
                    changeStatus(Estados.ERROR_URL_INVALIDA);
                    reintentos = 0;
                    break;
                } catch (FileNotFoundException e) {
                    changeStatus(Estados.ERROR_ESCRIBIR_ARCHIVO);
                    reintentos = 0;
                    break;
                } catch (IOException e) {
                    changeStatus(Estados.ERROR_ABRIR_ARCHIVO);
                    reintentos = 0;
                    break;
                }
                try {
                    changeStatus(Estados.DESCARGANDO);
                    byte[] buffer = new byte[4096];
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (Exception e) {
                    reintentos--;
                    if (reintentos > 0) {
                        changeStatus(Estados.REINTENTANDO);
                    } else {
                        changeStatus(Estados.ERROR_TIMEOUT);
                    }
                } finally {
                    boolean flagedOk = false;
                    if (estado != Estados.REINTENTANDO) {
                        if (contentLenght > ot.length()) {
                            Log.e("MIMANGA DOWNLOAD", "content lenght =" + contentLenght + " tamaño =" + o.length() + " en =" + o.getPath());
                            ot.delete();
                            reintentos--;
                            changeStatus(Estados.REINTENTANDO);
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
                            Log.i("MIMANGA DOWNLOAD", "descarga ok =" + o.getPath());
                            changeStatus(Estados.DESCARGA_OK);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                changeStatus(Estados.DESCARGA_OK);
            }
        }
    }

    void writeErrorImage(File ot) throws IOException {
        if (ServicioColaDeDescarga.actual != null) {
            InputStream ims = ServicioColaDeDescarga.actual.getAssets().open("error_image.jpg");
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

    void changeStatus(Estados estado) {
        this.estado = estado;
        if (cambioListener != null && estado.ordinal() > Estados.POSTERGADA.ordinal()) {
            cambioListener.onCambio(this);
        }
    }

    public void setCambioListener(CambioEstado cambioListener) {
        this.cambioListener = cambioListener;
    }

    enum Estados {
        EN_COLA, INICIADA, DESCARGANDO, REINTENTANDO, POSTERGADA, DESCARGA_OK, ERROR_CONECCION, ERROR_404, ERROR_TIMEOUT, ERROR_SUBIDA, ERROR_URL_INVALIDA, ERROR_ESCRIBIR_ARCHIVO, ERROR_ABRIR_ARCHIVO
    }
}