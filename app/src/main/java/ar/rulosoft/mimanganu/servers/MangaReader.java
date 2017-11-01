package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

class MangaReader extends MangaPanda {
    /**
     * Okay, this is almost (except for server and color) the same,
     * so I took the liberty and use MangaPanda as template
     * <p/>
     * If, by any chance, MangaReader should change, then we can fill this
     * with other stuff again.
     * <p/>
     * Previously, this code was the same as MangaPanda either way..
     */
    MangaReader(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangareader);
        setServerName("mangareader.net");
        setServerID(MANGAREADER);

        setHost("http://www.mangareader.net");
    }
}
