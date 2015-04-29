package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

/**
 * TODO: document your custom view class.
 */
public class ControlTapaSerie extends RelativeLayout implements Imaginable {

    ImageView imagen;
    TextView texto;

    public ControlTapaSerie(Context context, AttributeSet attrs) {
        super(context, attrs);
        Inicializar();
    }

    public ControlTapaSerie(Context context) {
        super(context);
        Inicializar();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int alto = MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec) * 1.3), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, alto);

    }

    private void Inicializar() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.control_tapa_serie, this, true);
        imagen = (ImageView) findViewById(R.id.imagen_portada);
        texto = (TextView) findViewById(R.id.texto);
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        if (imagen != null) {
            imagen.setImageBitmap(b);
        } else {
            Log.w("CONTROLTAPASERIE", "imagen no inicializada");
        }
    }

    @Override
    public void setImageResource(int id) {
        if (imagen != null) {
            imagen.setImageResource(id);
        } else {
            Log.w("CONTROLTAPASERIE", "imagen no inicializada");
        }
    }

    public void setText(String text) {
        if (texto != null) {
            texto.setText(text);
        } else {
            Log.w("CONTROLTAPASERIE", "texto no inicializado");
        }
    }

    public void setImageLeft(int dra) {
        texto.setCompoundDrawablesWithIntrinsicBounds(dra, 0, 0, 0);
    }

}
