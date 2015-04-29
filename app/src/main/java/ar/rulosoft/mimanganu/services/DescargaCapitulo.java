package ar.rulosoft.mimanganu.services;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.DescargaIndividual.Estados;

public class DescargaCapitulo implements CambioEstado {
    public static int MAX_ERRORS = 5;
    public DescargaEstado estado;
    public Capitulo capitulo;
    OnErrorListener errorListener = null;
    CambioEstado cambioListener = null;
    Estados[] paginasStatus;
    int progreso = 0;

    public DescargaCapitulo(Capitulo capitulo) {
        this.capitulo = capitulo;
        reset();
    }

    public void reset() {
        paginasStatus = new Estados[capitulo.getPaginas()];
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
            } else if (progreso < capitulo.getPaginas()) {
                for (int i = 0; i < capitulo.getPaginas(); i++) {
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
                        errorListener.onError(capitulo);
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

    public Capitulo getCapitulo() {
        return capitulo;
    }

    public void setCapitulo(Capitulo capitulo) {
        this.capitulo = capitulo;
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
        if (progreso == capitulo.getPaginas()) {
            Database.UpdateCapituloDescargado(ServicioColaDeDescarga.actual, capitulo.getId(), 1);
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
            errorListener.onError(capitulo);
        }
    }

    public enum DescargaEstado {
        EN_COLA, DESCARGANDO, DESCARGADO, ERROR
    }

    public interface OnErrorListener {
        void onError(Capitulo capitulo);
    }
}