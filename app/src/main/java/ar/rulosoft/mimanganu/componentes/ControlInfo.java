package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.ObservableScrollView;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 03/05/2015.
 */
public class ControlInfo extends ObservableScrollView implements Imaginable {
    View s1, s2, s3, s4;
    // TODO: Needs translation. Possibly breaks databse?
    TextView autor, estado, servidor, sinopsis, titulo;
    TextView autorTitle, estadoTitle, servidorTitle;
    ImageView imagen;

    public ControlInfo(Context context) {
        super(context);
        initialize();
    }

    public ControlInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ControlInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void initialize() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.control_info, this, true);
        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);
        autorTitle = (TextView) findViewById(R.id.autorTitle);
        estadoTitle = (TextView) findViewById(R.id.estadoTitle);
        servidorTitle = (TextView) findViewById(R.id.servidorTitle);
        estado = (TextView) findViewById(R.id.estado);
        servidor = (TextView) findViewById(R.id.servidor);
        sinopsis = (TextView) findViewById(R.id.sinopsis);
        titulo = (TextView)findViewById(R.id.titulo);
        autor = (TextView) findViewById(R.id.autor);
        imagen = (ImageView)findViewById(R.id.imagen);
    }

    public void setColor(int color) {
        Drawable colorDrawable = new ColorDrawable(color);
        if (Build.VERSION.SDK_INT >= 16) {
            s1.setBackground(colorDrawable);
            s2.setBackground(colorDrawable);
            s3.setBackground(colorDrawable);
            s4.setBackground(colorDrawable);
            titulo.setBackground(colorDrawable);
        } else {
            s1.setBackgroundDrawable(colorDrawable);
            s2.setBackgroundDrawable(colorDrawable);
            s3.setBackgroundDrawable(colorDrawable);
            s4.setBackgroundDrawable(colorDrawable);
            titulo.setBackgroundDrawable(colorDrawable);
        }
        autorTitle.setTextColor(color);
        estadoTitle.setTextColor(color);
        servidorTitle.setTextColor(color);
    }

    public void setAutor(String autor) {
        this.autor.setText(autor);
    }

    public void setEstado(String estado) {
        this.estado.setText(estado);
    }

    public void setServidor(String servidor) {
        this.servidor.setText(servidor);
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis.setText(sinopsis);
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        imagen.setImageBitmap(b);
    }

    @Override
    public void setImageResource(int id) {
        imagen.setImageResource(id);
    }

    public void setTitulo(String titulo) {
        this.titulo.setVisibility(View.VISIBLE);
        this.titulo.setText(titulo);
    }
}
