package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

class MintManga extends ReadMangaMe {
    /*
     Reuse ReadMangaMe class as the page layout is identical.
     */
    MintManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_ru);
        setIcon(R.drawable.mintmanga);
        setServerName("MintManga");
        setServerID(MINTMANGA);
        setHost("https://mintmanga.live");
    }
}
