package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

class DeNineManga extends NineManga {
    private static final String HOST = "http://de.ninemanga.com";

    private static final String PATTERN_IMAGE =
            "(http[^\"]+comics[^\"]+)";

    private static final int[] fltGenre = {
            R.string.flt_tag_adventure,
            R.string.flt_tag_action,
            R.string.flt_tag_kitchen_sink_drama,
            R.string.flt_tag_daemons,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_erotic,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_card_game,
            R.string.flt_tag_comedy,
            R.string.flt_tag_magic,
            R.string.flt_tag_mecha,
            R.string.flt_tag_military,
            R.string.flt_tag_music,
            R.string.flt_tag_mystery,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shounen,
            R.string.flt_tag_game,
            R.string.flt_tag_sports,
            R.string.flt_tag_super_powers,
            R.string.flt_tag_thriller,
            R.string.flt_tag_vampire,
            R.string.flt_tag_video_game,
            R.string.flt_tag_yaoi
    };
    private static final String[] valGenre = {
            "63",
            "64",
            "82",
            "76",
            "65",
            "79",
            "88",
            "66",
            "91",
            "73",
            "84",
            "72",
            "95",
            "81",
            "78",
            "67",
            "68",
            "89",
            "90",
            "83",
            "69",
            "74",
            "70",
            "86",
            "85",
            "75",
            "92",
            "87",
            "80",
            "94",
            "71",
            "77",
            "93"
    };

    DeNineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_de);
        setIcon(R.drawable.ninemanga);
        setServerName("DeNineManga");
        setServerID(DENINEMANGA);

        // override variables in super class
        super.HOST = HOST;
        super.PATTERN_IMAGE = PATTERN_IMAGE;
        super.fltGenre = fltGenre;
        super.valGenre = valGenre;
    }
}
