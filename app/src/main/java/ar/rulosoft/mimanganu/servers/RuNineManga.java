package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 29/11/2015.
 */
class RuNineManga extends NineManga {
    private static final String HOST = "http://ru.ninemanga.com";

    private static final String PATTERN_COVER =
            "<img itemprop=\"image\".+?src=\"(.+?)\"";
    private static final String PATTERN_IMAGE =
            "(http[^\"]+comics[^\"]+)";

    private static final int[] fltGenre = {
            // assignment was done using Google Translate
            // so if you are a native speaker and have some improvements for the category assignment
            // please do not hesitate to raise an issue
            R.string.flt_tag_art, // арт
            R.string.flt_tag_thriller, // боевик
            R.string.flt_tag_martial_arts, // боевыеискусства
            R.string.flt_tag_vampire, // вампиры
            R.string.flt_tag_harem, // гарем
            R.string.flt_tag_gender_bender, // гендернаяинтрига
            R.string.flt_tag_fantasy, // фэнтези, фантастика
            R.string.flt_tag_detective, // детектив
            R.string.flt_tag_josei, // дзёсэй
            R.string.flt_tag_doujinshi, // додзинси
            R.string.flt_tag_drama, // драма
            R.string.flt_tag_game, // игра
            R.string.flt_tag_historical, // история
            R.string.flt_tag_kodomo, // кодомо
            R.string.flt_tag_comedy, // комедия
            R.string.flt_tag_maho_shoujo, // махо-сёдзё
            R.string.flt_tag_mecha, // меха
            R.string.flt_tag_mystery, // мистика
            R.string.flt_tag_sci_fi, // научнаяфантастика
            R.string.flt_tag_daily_life, // повседневность
            R.string.flt_tag_apocalypse, // постапокалиптика
            R.string.flt_tag_adventure, // приключения
            R.string.flt_tag_psychological, // психология
            R.string.flt_tag_romance, // романтика
            R.string.flt_tag_samurai, // самурайскийбоевик
            R.string.flt_tag_supernatural, // сверхъестественное
            R.string.flt_tag_shoujo, // сёдзё
            R.string.flt_tag_shoujo_ai, // сёдзё-ай
            R.string.flt_tag_shounen, // сёнэн
            R.string.flt_tag_shounen_ai, // сёнэн-ай
            R.string.flt_tag_sports, // спорт
            R.string.flt_tag_seinen, // сэйнэн
            R.string.flt_tag_tragedy, // трагедия
            R.string.flt_tag_suspense, // триллер
            R.string.flt_tag_horror, // ужасы
            R.string.flt_tag_school_life, // школа
            R.string.flt_tag_ecchi, // этти
            R.string.flt_tag_yuri, // юри
    };
    private static final String[] valGenre = {
            "90",
            "53",
            "58",
            "85",
            "73",
            "81",
            "68%2C56%2C77",
            "72",
            "64",
            "62",
            "51",
            "76",
            "75",
            "89",
            "57",
            "88",
            "84",
            "71",
            "79",
            "65",
            "87",
            "59",
            "54",
            "61",
            "82",
            "55",
            "67",
            "78",
            "52",
            "63",
            "69",
            "74",
            "70",
            "83",
            "86",
            "66",
            "60",
            "80"
    };

    RuNineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_ru);
        setIcon(R.drawable.ninemanga);
        setServerName("RuNineManga");
        setServerID(RUNINEMANGA);

        // override variables in super class
        super.HOST = HOST;
        super.PATTERN_COVER = PATTERN_COVER;
        super.PATTERN_IMAGE = PATTERN_IMAGE;
        super.fltGenre = fltGenre;
        super.valGenre = valGenre;
    }
}
