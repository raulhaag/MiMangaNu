package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

class ItNineManga extends NineManga {
    private static final String HOST = "http://it.ninemanga.com";

    private static final String PATTERN_IMAGE =
            "class=\"pic_download\" href=\"(http[s]?://[^/]+/+it_manga/pic/[^\"]+)\"";

    private static int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_action,
            R.string.flt_tag_bara,
            R.string.flt_tag_comedy,
            R.string.flt_tag_madness,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_harem,
            R.string.flt_tag_hentai,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_magic,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_mature,
            R.string.flt_tag_mecha,
            R.string.flt_tag_mystery,
            R.string.flt_tag_music,
            R.string.flt_tag_psychological,
            R.string.flt_tag_collection,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_relation,
            R.string.flt_tag_shota,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shounen,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_smut,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_splatter,
            R.string.flt_tag_sports,
            R.string.flt_tag_historical,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_daily_life,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_yuri

    };
    private static String[] valGenre = {
            "98",
            "113",
            "108%2C63",
            "65",
            "88",
            "101%2C71",
            "79",
            "114%2C92",
            "82",
            "70",
            "74",
            "109",
            "76",
            "90",
            "107",
            "80",
            "95",
            "91",
            "99",
            "106",
            "68",
            "87%2C105",
            "96",
            "83%2C97",
            "93",
            "104%2C75",
            "103%2C64",
            "66",
            "67",
            "72",
            "89",
            "73",
            "69",
            "102",
            "111",
            "78%2C100",
            "81",
            "85%2C110",
            "84",
            "112",
            "77",
            "86",
            "94"
    };

    ItNineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_it);
        setIcon(R.drawable.ninemanga);
        setServerName("ItNineManga");
        setServerID(ITNINEMANGA);

        // override variables in super class
        super.HOST = HOST;
        super.PATTERN_IMAGE = PATTERN_IMAGE;
        super.fltGenre = fltGenre;
        super.valGenre = valGenre;
    }
}
