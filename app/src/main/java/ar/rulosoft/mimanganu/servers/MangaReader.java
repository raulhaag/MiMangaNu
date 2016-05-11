package ar.rulosoft.mimanganu.servers;

import ar.rulosoft.mimanganu.R;

public class MangaReader extends MangaPanda {
    /**
     * Okay, this is almost (except for server and color) the same,
     * so I took the liberty and use MangaPanda as template
     * <p/>
     * If, by any chance, MangaReader should change, then we can fill this
     * with other stuff again.
     * <p/>
     * Previously, this code was the same as MangaPanda either way..
     */
    public MangaReader() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.mangareader);
        this.setServerName("Mangareader.net");
        setServerID(ServerBase.MANGAREADER);

        SetHost("http://www.mangareader.net");
    }
}
