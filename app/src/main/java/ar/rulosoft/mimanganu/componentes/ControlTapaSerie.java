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

    ImageView image;
    TextView text;

    // TODO: Needs translation. Not sure what this is supposed to mean ~.~
    public ControlTapaSerie(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ControlTapaSerie(Context context) {
        super(context);
        initialize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int alto = MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec) * 1.3), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, alto);

    }

    private void initialize() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.control_tapa_serie, this, true);
        image = (ImageView) findViewById(R.id.imagen_portada);
        text = (TextView) findViewById(R.id.texto);
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        if (image != null) {
            image.setImageBitmap(b);
        } else {
            Log.w("CONTROLTAPASERIE", "imagen no inicializada");
        }
    }

    @Override
    public void setImageResource(int id) {
        if (image != null) {
            image.setImageResource(id);
        } else {
            Log.w("CONTROLTAPASERIE", "imagen no inicializada");
        }
    }

    public void setText(String text) {
        if (this.text != null) {
            this.text.setText(text);
        } else {
            Log.w("CONTROLTAPASERIE", "texto no inicializado");
        }
    }

    public void setImageLeft(int dra) {
        text.setCompoundDrawablesWithIntrinsicBounds(dra, 0, 0, 0);
    }

}
