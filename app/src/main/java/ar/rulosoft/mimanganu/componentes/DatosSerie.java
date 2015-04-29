package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

public class DatosSerie extends ImageView implements Imaginable {

    public static int TEXT_HEIGHT = 20;
    public Paint pTxt, pBack, pBlack, pTitle;
    public String text = "";
    public String titulo = "";
    int paddings = 15;
    Bitmap imagen = null;
    int anchoB, altoB;
    float fEscalaAlto, fEscalaAncho;
    Matrix m;
    Rect rOrigen;
    ArrayList<String> lineasTit;
    ArrayList<String> lineasLat;
    ArrayList<String> lineasAbajo;
    boolean wfm = false, inicializada = false;
    // movimiento
    float mDy, ultimaY;
    int mActivePointer;
    private float escala, ancho, alto, imgAncho, imgAlto, anchoT1, anchoT2, altoT1, altoControl, finControl;

	/*
     * x1 margen de saparacion titulo img y texto abajo x2 margen para testo al
	 * lado y1 altura de inicio del titulo y2 altura de inicio de la img y3
	 * altura de inicio del texto al lado y4 altula de inicio del texto abajo
	 */

    public DatosSerie(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inicializacion();
    }

    public DatosSerie(Context context) {
        super(context);
        inicializacion();
    }

    public DatosSerie(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicializacion();
    }

    public void inicializar(String titulo, String texto, int imageW, int imageH) {

        this.titulo = titulo;
        this.text = texto;
        this.altoB = imageH;
        this.anchoB = imageW;

        if (ancho == 0) {
            wfm = true;
        } else {
            this.imgAlto = imageH * escala;
            this.imgAncho = imageW * escala;

            float aux = (ancho / 2) - paddings * 1.5f;
            if (imgAncho > aux) {
                this.imgAlto = this.imgAlto / this.imgAncho * aux;
                this.imgAncho = aux;
            }

            this.anchoT1 = this.ancho - this.imgAncho - paddings * 3;
            this.altoT1 = this.imgAlto + paddings;
            this.anchoT2 = this.ancho - 2 * paddings;

            actualizarDatos();
            inicializada = true;
            invalidate();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        alto = MeasureSpec.getSize(heightMeasureSpec);
        ancho = MeasureSpec.getSize(widthMeasureSpec);
        if (wfm) {
            inicializar(titulo, text, anchoB, altoB);
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    void initRecOrigen() {
        if (imagen != null)
            rOrigen = new Rect(0, 0, imagen.getWidth(), imagen.getHeight());
        else
            rOrigen = new Rect(0, 0, 10, 10);

    }

    @Override
    public void draw(Canvas canvas) {

        if (inicializada && imagen != null) {
            // variables
            float x1, x2, y1, y2, y3, y4;
            x1 = paddings;
            x2 = x1 + imgAncho + paddings;
            y1 = paddings + pTitle.getTextSize() + mDy;
            y2 = paddings + pTitle.getTextSize() * lineasTit.size() + 2 * paddings + mDy;
            y3 = y2 + pTxt.getTextSize();
            y4 = y3 + pTxt.getTextSize() * lineasLat.size();

            fEscalaAlto = imgAlto / imagen.getHeight();
            fEscalaAncho = imgAncho / imagen.getWidth();

            if (lineasTit != null) {
                for (int i = 0; i < lineasTit.size(); i++) {
                    // calcular alineacion
                    float subx = (ancho - pTitle.measureText(lineasTit.get(i))) / 2;
                    // dibujar
                    canvas.drawText(lineasTit.get(i), subx, y1 + (i) * (pTitle.getTextSize()), pTitle);
                }
            }

            if (lineasLat != null) {
                for (int i = 0; i < lineasLat.size(); i++) {
                    // dibujar
                    canvas.drawText(lineasLat.get(i), x2, y3 + (i) * (pTxt.getTextSize()), pTxt);
                }
            }

            if (lineasAbajo != null) {
                for (int i = 0; i < lineasAbajo.size(); i++) {
                    // dibujar
                    canvas.drawText(lineasAbajo.get(i), x1, y4 + (i) * (pTxt.getTextSize()), pTxt);
                }
            }

			/*
             * canvas.drawRect(new Rect((int) x1, (int) y2, (int) (x1 +
			 * imgAncho), (int) (y2 + imgAlto)), pTxt); /
			 */
            if (imagen != null) {
                m.reset();
                m.postScale(fEscalaAncho, fEscalaAlto);
                m.postTranslate(x1, y2);
                canvas.drawBitmap(imagen, m, null);
                // canvas.drawBitmap(imagen, rOrigen, new Rect((int) x1, (int)
                // y2,
                // (int) (x1 + imgAncho), (int) (y2 + imgAlto)), pBack);
            }
        } else {
            // fondo
            // canvas.drawRect(0, 0, ancho, alto, pBlack);
        }
    }

    private void inicializacion() {

        escala = getResources().getDisplayMetrics().density;

        pBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBlack.setColor(Color.BLACK);
        pBlack.setStyle(Style.FILL);

        pBack = new Paint();
        pBack.setAntiAlias(true);
        pBack.setFilterBitmap(false);
        pBack.setColor(Color.BLACK);
        pBack.setStyle(Style.FILL);
        // pBack.setAlpha(100);

        pTxt = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTxt.setColor(Color.WHITE);
        pTxt.setTextSize(TEXT_HEIGHT * escala);

        pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTitle.setColor(Color.WHITE);
        pTitle.setTextSize((TEXT_HEIGHT + 6) * escala);

        m = new Matrix();

    }

    public Respuesta getLines(String texto, float w, float h, Paint p) {
        Respuesta rta = new Respuesta();
        ArrayList<String> res = new ArrayList<String>();
        if (texto == null) {
            texto = "";
        }
        if (p.measureText(texto) < w) {
            res.add(texto);
            rta.setResto("");
        } else {
            int lineas = (int) (h / (p.getTextSize()));
            String[] palabras = texto.split(" ");
            int lineaActual = 1;
            int i;
            String temp = "";
            String ultima_agregada = "";
            for (i = 0; i < palabras.length; i++) {
                if (p.measureText(temp + " " + palabras[i]) < w) {
                    if (temp.length() > 0) {
                        temp = temp + " " + palabras[i];
                        ultima_agregada = palabras[i];
                    } else {
                        // TODO palabras largas
                        temp = palabras[i];
                        ultima_agregada = palabras[i];
                    }
                } else {
                    res.add(temp);
                    temp = palabras[i];
                    lineaActual++;
                }
                if (lineaActual > lineas) {
                    if (temp == palabras[i])
                        i--;
                    break;
                }
            }

            if (i == palabras.length && temp != "") {
                res.add(temp);
            } else if (palabras.length < i && ultima_agregada == palabras[i]) {
                i++;
            }

            if (i + 1 < palabras.length) {
                String ll = palabras[i + 1];
                for (int j = i + 2; j < palabras.length; j++) {
                    ll = ll + " " + palabras[j];
                }
                rta.setResto(ll);
            }
        }
        rta.setResultado(res);
        return rta;
    }

    public void setImageSizeDp(float w, float h) {
        this.imgAlto = h * escala;
        this.imgAncho = w * escala;
        float aux = (ancho / 2) - paddings * 1.5f;

        if (imgAncho > aux) {
            this.imgAlto = this.imgAlto / this.imgAncho * aux;
            this.imgAncho = aux;
        }

        this.anchoT1 = this.ancho - this.imgAncho - paddings * 3;
        this.altoT1 = this.imgAlto + paddings;
        actualizarDatos();
    }

    public void actualizarDatos() {
        Respuesta rta = getLines(text, anchoT1, altoT1 + paddings, pTxt);
        lineasLat = rta.getResultado();
        lineasAbajo = getLines(rta.getResto(), anchoT2, Integer.MAX_VALUE, pTxt).getResultado();
        lineasTit = getLines(titulo, anchoT2, Integer.MAX_VALUE, pTitle).getResultado();
        altoControl = lineasTit.size() * pTitle.getTextSize() + (lineasAbajo.size() + lineasLat.size() + 1) * pTxt.getTextSize() + 3 * paddings;
        finControl = alto - altoControl + dpToPx(48);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imagen != null) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    ultimaY = event.getY();
                    mActivePointer = event.getPointerId(0);
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int pointerIndex = event.findPointerIndex(mActivePointer);
                    final float y = event.getY(pointerIndex);
                    final float dy = y - ultimaY;
                    ultimaY = y;
                    if ((mDy + dy) > 0 || (mDy + dy) < finControl) {
                        break;
                    }
                    mDy += dy;

                default:
                    break;
            }
            invalidate();
            altoControl = lineasTit.size() * pTitle.getTextSize() + (lineasAbajo.size() + lineasLat.size() + 1) * pTxt.getTextSize() + 3 * paddings;
            finControl = alto - altoControl - dpToPx(48);
            return true;
        }
        return false;
    }

    @Override
    public void setImageResource(int resId) {
        imagen = BitmapFactory.decodeResource(getResources(), resId);
        initRecOrigen();
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        imagen = bm;
        initRecOrigen();
        invalidate();
    }

    public ImageView getImageView() {
        return this;
    }

    private class Respuesta {
        private ArrayList<String> resultado;
        private String resto = "";

        public ArrayList<String> getResultado() {
            return resultado;
        }

        public void setResultado(ArrayList<String> resultado) {
            this.resultado = resultado;
        }

        public String getResto() {
            return resto;
        }

        public void setResto(String resto) {
            this.resto = resto;
        }
    }

}
