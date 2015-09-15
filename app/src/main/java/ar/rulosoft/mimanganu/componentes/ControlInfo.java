package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.ObservableScrollView;

import ar.rulosoft.mimanganu.R;

/**
 * Series information.
 * Created by Raul on 03/05/2015.
 */
public class ControlInfo extends ObservableScrollView implements Imaginable {
    View s1, s2, s3, s4;
    TextView author, status, server, synopsis, title;
    TextView authorTitle, statusTitle, serverTitle;
    ImageView image;

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
        authorTitle = (TextView) findViewById(R.id.autorTitle);
        statusTitle = (TextView) findViewById(R.id.estadoTitle);
        serverTitle = (TextView) findViewById(R.id.servidorTitle);
        status = (TextView) findViewById(R.id.estado);
        server = (TextView) findViewById(R.id.servidor);
        synopsis = (TextView) findViewById(R.id.sinopsis);
        title = (TextView) findViewById(R.id.titulo);
        author = (TextView) findViewById(R.id.autor);
        image = (ImageView) findViewById(R.id.imagen);
    }

    public void setColor(int color) {
        s1.setBackgroundColor(color);
        s2.setBackgroundColor(color);
        s3.setBackgroundColor(color);
        s4.setBackgroundColor(color);
        title.setBackgroundColor(color);
        authorTitle.setTextColor(color);
        statusTitle.setTextColor(color);
        serverTitle.setTextColor(color);
    }

    public void setAuthor(String author) {
        this.author.setText(author);
    }

    public void setStatus(String status) {
        this.status.setText(status);
    }

    public void setServer(String server) {
        this.server.setText(server);
    }

    public void setSynopsis(String synopsis) {
        this.synopsis.setText(synopsis);
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        image.setImageBitmap(b);
    }

    @Override
    public void setImageResource(int id) {
        image.setImageResource(id);
    }

    public void setTitle(String title) {
        this.title.setVisibility(View.VISIBLE);
        this.title.setText(title);
    }
}
