package ar.rulosoft.mimanganu.services;

import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.DescargaIndividual.Estados;

public class DescargaCapitulo implements CambioEstado {
    public static int MAX_ERRORS = 5;
    public DescargaEstado estado;
    public Chapter chapter;
    OnErrorListener errorListener = null;
    CambioEstado cambioListener = null;
    Estados[] paginasStatus;
    int progreso = 0;

    public DescargaCapitulo(Chapter chapter) {
        this.chapter = chapter;
        reset();
    }

    public void reset() {
        paginasStatus = new Estados[chapter.getPaginas()];
        for (int i = 0; i < paginasStatus.length; i++) {
            paginasStatus[i] = Estados.EN_COLA;
        }
        estado = DescargaEstado.EN_COLA;
    }

    public void cambiarEstado(DescargaEstado nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public int getSiguiente() {
        int j = -2;
        if (estado.ordinal() < DescargaEstado.DESCARGADO.ordinal()) {
            if (estado == DescargaEstado.EN_COLA)
                cambiarEstado(DescargaEstado.DESCARGANDO);
            if (hayErrores()) {
                j = -11;
            } else if (progreso < chapter.getPaginas()) {
                for (int i = 0; i < chapter.getPaginas(); i++) {
                    if (paginasStatus[i] == Estados.EN_COLA || paginasStatus[i] == Estados.POSTERGADA) {
                        paginasStatus[i] = Estados.INICIADA;
                        j = i;
                        break;
                    }
                }
            }
        }
        return (j + 1);
    }

    public boolean hayErrores() {
        int errors = 0;
        for (Estados e : paginasStatus) {
            if (e.ordinal() > Estados.DESCARGA_OK.ordinal()) {
                errors++;
                if (errors > MAX_ERRORS) {
                    cambiarEstado(DescargaEstado.ERROR);
                    if (errorListener != null) {
                        errorListener.onError(chapter);
                    }
                    break;
                }
            }
        }
        return errors > MAX_ERRORS;
    }

    public boolean estaDescargando() {
        boolean ret = false;
        for (Estados e : paginasStatus) {
            if (e.ordinal() < Estados.POSTERGADA.ordinal()) {
                ret = true;
                break;
            }
        }
        if (!ret)
            cambiarEstado(DescargaEstado.DESCARGADO);
        return ret;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void setCambioListener(CambioEstado cambioListener) {
        this.cambioListener = cambioListener;
    }

    public void setErrorIdx(int idx) {
        paginasStatus[idx] = Estados.ERROR_SUBIDA;
        progreso++;
        hayErrores();
        chackProgreso();
    }

    public void chackProgreso() {
        if (progreso == chapter.getPaginas()) {
            Database.UpdateCapituloDescargado(ServicioColaDeDescarga.actual, chapter.getId(), 1);
            cambiarEstado(DescargaEstado.DESCARGADO);
        }
    }

    @Override
    public void onCambio(DescargaIndividual descargaIndividual) {
        paginasStatus[descargaIndividual.index] = descargaIndividual.estado;
        progreso++;
        chackProgreso();
        if (cambioListener != null)
            cambioListener.onCambio(descargaIndividual);
    }

    public void setErrorListener(OnErrorListener errorListener) {
        this.errorListener = errorListener;
        if (this.estado == DescargaEstado.ERROR && errorListener != null) {
            errorListener.onError(chapter);
        }
    }

    public enum DescargaEstado {
        EN_COLA, DESCARGANDO, DESCARGADO, ERROR
    }

    public interface OnErrorListener {
        void onError(Chapter chapter);
    }
}