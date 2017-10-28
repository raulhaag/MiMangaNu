package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

class EsNineManga extends NineManga {
    private static final String HOST = "http://es.ninemanga.com";

    private static final String PATTERN_IMAGE =
            "class=\"pic_download\" href=\"(http://[^/]+/+es_manga/[^\"]+)";

    private static final int[] fltGenre = {
            R.string.flt_tag_4_koma,
            R.string.flt_tag_action,
            R.string.flt_tag_adult,
            R.string.flt_tag_adventure,
            R.string.flt_tag_apocalypse,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_comedy,
            R.string.flt_tag_cyberpunk,
            R.string.flt_tag_gangster,
            R.string.flt_tag_daemons,
            R.string.flt_tag_drama,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_school_life,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_gore,
            R.string.flt_tag_harem,
            R.string.flt_tag_hentai,
            R.string.flt_tag_historical,
            R.string.flt_tag_horror,
            R.string.flt_tag_josei,
            R.string.flt_tag_karate,
            R.string.flt_tag_mature,
            R.string.flt_tag_mafia,
            R.string.flt_tag_magic,
            R.string.flt_tag_makoto,
            R.string.flt_tag_mangasutra,
            R.string.flt_tag_manhwa,
            R.string.flt_tag_mecha,
            R.string.flt_tag_military,
            R.string.flt_tag_mystery,
            R.string.flt_tag_music,
            R.string.flt_tag_none,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_orgy,
            R.string.flt_tag_parody,
            R.string.flt_tag_police,
            R.string.flt_tag_porn,
            R.string.flt_tag_psychological,
            R.string.flt_tag_virtual_reality,
            R.string.flt_tag_reincarnation,
            R.string.flt_tag_romance,
            R.string.flt_tag_samurai,
            R.string.flt_tag_seinen,
            R.string.flt_tag_sex,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_shounen,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_smut,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_sports,
            R.string.flt_tag_super_powers,
            R.string.flt_tag_super_hero,
            R.string.flt_tag_survival,
            R.string.flt_tag_suspense,
            R.string.flt_tag_terror,
            R.string.flt_tag_psychological_terror,
            R.string.flt_tag_thriller,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_transsexual,
            R.string.flt_tag_vampire,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_webcomic,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_yura,
            R.string.flt_tag_yuri
    };
    private static String[] valGenre = {
            "201",
            "69%2C177",
            "193%2C86",
            "179%2C64%2C120",
            "202",
            "66%2C189%2C181",
            "93%2C123",
            "75%2C178",
            "199",
            "125",
            "126",
            "79",
            "65",
            "81%2C176%2C170%2C122",
            "100%2C70%2C180",
            "175",
            "108",
            "78%2C82",
            "83",
            "190%2C95",
            "99",
            "112",
            "113",
            "72",
            "90",
            "172",
            "102",
            "103",
            "94%2C114",
            "115",
            "205",
            "88%2C187",
            "121%2C197",
            "71",
            "184%2C195",
            "91",
            "198",
            "208",
            "109",
            "96%2C192",
            "196",
            "207",
            "67%2C98%2C89",
            "210",
            "73",
            "104",
            "80%2C85",
            "186%2C194%2C173",
            "77%2C68",
            "128%2C174%2C185%2C118",
            "182%2C169",
            "183",
            "74%2C119",
            "188%2C124%2C119%2C76%2C111",
            "206",
            "116",
            "203",
            "171",
            "106",
            "107",
            "204%2C97",
            "87%2C191",
            "117",
            "209",
            "110%2C84",
            "92",
            "200",
            "101",
            "127"
    };

    EsNineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.ninemanga);
        setServerName("EsNineManga");
        setServerID(ESNINEMANGA);

        // override variables in super class
        super.HOST = HOST;
        super.PATTERN_IMAGE = PATTERN_IMAGE;
        super.fltGenre = fltGenre;
        super.valGenre = valGenre;
    }
}
