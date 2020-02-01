package ar.rulosoft.mimanganu.componentes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.UiThread;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.utils.ThemeColors;

/**
 * Series information.
 * Created by Raul on 03/05/2015.
 */

// used in DetailsFragment
public class ControlInfo extends ScrollView implements Imaginable {
    private View blockSummaryView;
    private View lineAuthorView;
    private View lineStatusView;
    private View lineServerView;
    private View lineGenreView;
    private TextView author;
    private TextView status;
    private TextView server;
    private TextView synopsis;
    private TextView genre;
    private TextView authorTitle;
    private TextView statusTitle;
    private TextView serverTitle;
    private TextView genreTitle;
    private ImageView image;

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

    private void initialize() {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.control_info, this, true);
        blockSummaryView = findViewById(R.id.blockSummary);
        lineAuthorView = findViewById(R.id.lineAuthor);
        lineStatusView = findViewById(R.id.lineStatus);
        lineServerView = findViewById(R.id.lineServer);
        lineGenreView = findViewById(R.id.lineGenre);
        authorTitle = findViewById(R.id.titleAuthor);
        statusTitle = findViewById(R.id.titleStatus);
        serverTitle = findViewById(R.id.titleServer);
        genreTitle = findViewById(R.id.titleGenre);
        status = findViewById(R.id.textStatus);
        server = findViewById(R.id.textServer);
        synopsis = findViewById(R.id.sinopsis);
        author = findViewById(R.id.textAuthor);
        genre = findViewById(R.id.textGenre);
        image = findViewById(R.id.image);
    }

    @SuppressWarnings("ResourceAsColor")//lint error
    public void setColor(boolean dark_theme, int color) {
        int mColor = dark_theme ? ThemeColors.brightenColor(color, 150) : color;
        blockSummaryView.setBackgroundColor(color);
        lineAuthorView.setBackgroundColor(color);
        lineStatusView.setBackgroundColor(color);
        lineServerView.setBackgroundColor(color);
        lineGenreView.setBackgroundColor(color);
        authorTitle.setTextColor(mColor);
        statusTitle.setTextColor(mColor);
        serverTitle.setTextColor(mColor);
        genreTitle.setTextColor(mColor);
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
        this.synopsis.setText(synopsis + "\n\n\n\n\n\n\n");
    }

    public void setGenre(String genre) {
        this.genre.setText(genre);
    }

    @UiThread
    public void enableTitleCopy(final Context context, final String name) {
        this.image.setClickable(true);
        this.image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", name);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, R.string.title_copied, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setImageBitmap(Bitmap b) {
        image.setImageBitmap(b);
    }

    @Override
    public void setImageResource(int id) {
        image.setImageResource(id);
    }

}
