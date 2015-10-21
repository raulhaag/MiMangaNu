package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

/**
 * serie cover by Ra�l
 */

public class Cover extends RelativeLayout implements Imaginable {
    private ImageView image;
    private TextView text;

    public Cover(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public Cover(Context context) {
        super(context);
        initialize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec) * 1.3), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, height);
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
            if (b == null) {
                image.setVisibility(INVISIBLE);
            } else {
                image.setImageBitmap(b);
                image.setVisibility(VISIBLE);
            }
            image.invalidate();
        }
    }

    @Override
    public void setImageResource(int id) {
        if (image != null) {
            image.setImageResource(id);
        }
    }

    public void setText(String text) {
        if (this.text != null) {
            this.text.setText(text);
        }
    }

    public void setImageLeft(int dra) {
        text.setCompoundDrawablesWithIntrinsicBounds(dra, 0, 0, 0);
    }


}
