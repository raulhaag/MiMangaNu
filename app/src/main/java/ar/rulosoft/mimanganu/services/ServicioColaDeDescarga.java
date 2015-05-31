package ar.rulosoft.mimanganu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import ar.rulosoft.mimanganu.ActivityLector;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.DescargaCapitulo.DescargaEstado;
import ar.rulosoft.mimanganu.services.DescargaIndividual.Estados;

public class ServicioColaDeDescarga extends Service implements CambioEstado {

    final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 31, 58, 42, 63, 92, 47};
    public static int SLOTS = 2;
    public static ServicioColaDeDescarga actual = null;
    public static boolean intentPrending = false;
    public static ArrayList<DescargaCapitulo> descargas = new ArrayList<DescargaCapitulo>();

    static {
        Arrays.sort(illegalChars);
    }

    public int slots = SLOTS;
    DescargaListener descargaListener = null;

    public static void agregarDescarga(Activity activity, Chapter chapter, boolean lectura) {
        if (!chapter.isDownloaded()) {
            if (descargaNueva(chapter.getId())) {
                DescargaCapitulo dc = new DescargaCapitulo(chapter);
                if (lectura)
                    descargas.add(0, dc);
                else
                    descargas.add(dc);
            } else {
                for (DescargaCapitulo dc : descargas) {
                    if (dc.chapter.getId() == chapter.getId()) {
                        if (dc.estado == DescargaEstado.ERROR) {
                            dc.chapter.borrarImagenes(activity);
                            descargas.remove(dc);
                            dc = null;
                            DescargaCapitulo ndc = new DescargaCapitulo(chapter);
                            if (lectura) {
                                descargas.add(0, ndc);
                            } else {
                                descargas.add(ndc);
                            }
                        } else {
                            if (lectura) {
                                descargas.remove(dc);
                                descargas.add(0, dc);
                            }
                        }
                        break;
                    }
                }
            }
            initValues(activity);
            if (!intentPrending && actual == null) {
                intentPrending = true;
                activity.startService(new Intent(activity, ServicioColaDeDescarga.class));
            }
        }
    }

    public static void initValues(Context context) {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        int descargas = Integer.parseInt(pm.getString("download_threads", "2"));
        int tolerancia = Integer.parseInt(pm.getString("error_tolerancia", "5"));
        int reintentos = Integer.parseInt(pm.getString("reintentos", "4"));
        DescargaCapitulo.MAX_ERRORS = tolerancia;
        DescargaIndividual.REINTENTOS = reintentos;
        ServicioColaDeDescarga.SLOTS = descargas;
    }

    public static boolean descargaNueva(int cid) {
        boolean result = true;
        for (DescargaCapitulo dc : descargas) {
            if (dc.chapter.getId() == cid) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean quitarDescarga(int cid, Context c) {
        boolean result = true;
        for (DescargaCapitulo dc : descargas) {
            if (dc.chapter.getId() == cid) {
                if (dc.estado.ordinal() != DescargaCapitulo.DescargaEstado.DESCARGANDO.ordinal()) {
                    descargas.remove(dc);
                } else {
                    Toast.makeText(c, R.string.quitar_descarga, Toast.LENGTH_LONG).show();
                    result = false;
                }
                break;
            }
        }
        return result;
    }

    public static void attachListener(ActivityLector lector, int cid) {
        for (DescargaCapitulo dc : descargas) {
            if (dc.chapter.getId() == cid) {
                dc.setErrorListener(lector);
                break;
            }
        }
    }

    public static void detachListener(int cid) {
        attachListener(null, cid);
    }

    public static String generarRutaBase(ServerBase s, Manga m, Chapter c, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(s.getServerName()) + "/" + cleanFileName(m.getTitulo()).trim() + "/" + cleanFileName(c.getTitle()).trim();
    }

    public static String generarRutaBase(ServerBase s, Manga m, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dir = prefs.getString("directorio", Environment.getExternalStorageDirectory().getAbsolutePath());
        return dir + "/MiMangaNu/" + cleanFileName(s.getServerName()).trim() + "/" + cleanFileName(m.getTitulo()).trim();
    }

    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int) badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }
        return cleanName.toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        actual = this;
        intentPrending = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                iniciarCola();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCambio(DescargaIndividual descargaIndividual) {
        if (descargaIndividual.estado.ordinal() > Estados.POSTERGADA.ordinal()) {
            slots++;
        }
        if (descargaListener != null) {
            descargaListener.onImagenDescargada(descargaIndividual.cid, descargaIndividual.index);
        }
    }

    public void setDescargaListener(DescargaListener descargaListener) {
        this.descargaListener = descargaListener;
    }

    public void iniciarCola() {
        Manga manga = null;
        ServerBase s = null;
        String ruta = "";
        int lcid = -1;
        while (!descargas.isEmpty()) {
            if (slots > 0) {
                slots--;
                DescargaCapitulo dc = null;
                int sig = 1;
                for (DescargaCapitulo d : descargas) {
                    if (d.estado != DescargaEstado.ERROR) {
                        sig = d.getSiguiente();
                        if (sig > -1) {
                            dc = d;
                            break;
                        }
                    }
                }
                if (dc != null) {
                    if (manga == null || manga.getId() != dc.chapter.getMangaID()) {
                        manga = Database.getManga(actual.getApplicationContext(), dc.chapter.getMangaID());
                        s = ServerBase.getServer(manga.getServerId());
                    }
                    if (lcid != dc.chapter.getId()) {
                        lcid = dc.chapter.getId();
                        ruta = generarRutaBase(s, manga, dc.chapter, getApplicationContext());
                        new File(ruta).mkdirs();
                    }
                    try {
                        String origen = s.getImagen(dc.chapter, sig);
                        String destino = ruta + "/" + sig + ".jpg";
                        DescargaIndividual des = new DescargaIndividual(origen, destino, sig - 1, dc.chapter.getId());
                        des.setCambioListener(dc);
                        dc.setCambioListener(this);
                        new Thread(des).start();
                    } catch (Exception e) {
                        dc.setErrorIdx(sig - 1);
                        slots++;
                    }
                } else if (slots == 1) {
                    break;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    slots++;
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        actual = null;
        stopSelf();
    }
}
