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
import ar.rulosoft.mimanganu.utils.ThemeColors;

/**
 * Series information.
 * Created by Raul on 03/05/2015.
 */
public class ControlInfo extends ObservableScrollView implements Imaginable {
    View s1, s2, s3, b1;
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
        b1 = findViewById(R.id.blockSummary);
        s1 = findViewById(R.id.lineAuthor);
        s2 = findViewById(R.id.lineStatus);
        s3 = findViewById(R.id.lineServer);
        authorTitle = (TextView) findViewById(R.id.titleAuthor);
        statusTitle = (TextView) findViewById(R.id.titleStatus);
        serverTitle = (TextView) findViewById(R.id.titleServer);
        status = (TextView) findViewById(R.id.textStatus);
        server = (TextView) findViewById(R.id.textServer);
        synopsis = (TextView) findViewById(R.id.sinopsis);
        title = (TextView) findViewById(R.id.titulo);
        author = (TextView) findViewById(R.id.textAuthor);
        image = (ImageView) findViewById(R.id.imagen);
    }

    public void setColor(boolean dark_theme, int color) {
        int mColor = dark_theme ? ThemeColors.brightenColor(color, 150) : color;
        b1.setBackgroundColor(color);
        s1.setBackgroundColor(color);
        s2.setBackgroundColor(color);
        s3.setBackgroundColor(color);
        title.setBackgroundColor(mColor);
        authorTitle.setTextColor(mColor);
        statusTitle.setTextColor(mColor);
        serverTitle.setTextColor(mColor);
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
