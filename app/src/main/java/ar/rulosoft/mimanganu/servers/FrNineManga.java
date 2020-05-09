package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

class FrNineManga extends NineManga {
    private static final String HOST = "http://fr.ninemanga.com";

    private static final String PATTERN_IMAGE =
            "src='([^']+)";

    private static final String PATTERN_CHAPTER =
            "<a class=\"chapter_list_a\" href=\".*?(/chapter[^<\"]+)\" title=\"([^\"]+)\">([^<]+)</a>";


    private static final String[] fltGenre = {
            "Action", "Adventure", "Anges", "Arts Martiaux", "Aventure", "ComÉDie", "ComéDie", "Comedy", "Crime", "Drama", "Drame", "Ecchi", "Fantastique", "Fantasy", "Gender Bender", "Harem", "Histoire", "Historical", "Historique", "Horreur", "Horror", "Isekai", "Josei", "Magical Girls", "Magie", "Mature", "Mecha", "Medical", "MystÈRe", "MystèRe", "Mystery", "One Shot", "Philosophical", "Post-Apocalyptique", "Psychological", "Psychologique", "Romance", "School Life", "Sci-Fi", "Seinen", "Shojo", "Shonen", "Shonen Ai", "Shoujo Ai", "Shounen Ai", "Slice Of Life", "Sport", "Sports", "Surnaturel", "Suspense", "Thriller", "Tragedy", "Tranche De Vie", "Webtoon", "Yaoi", "Yuri"
    };
    private static String[] valGenre = {
            "5", "27", "98", "24", "11", "81", "6", "25", "66", "35", "2", "47", "1", "28", "51", "50", "154", "41", "76", "19", "63", "36", "94", "286", "8", "82", "68", "65", "85", "3", "40", "173", "64", "113", "61", "42", "26", "43", "44", "88", "101", "80", "240", "45", "39", "29", "102", "67", "4", "75", "128", "37", "48", "32", "38", "60"
    };

    FrNineManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.ninemanga);
        setServerName("FrNineManga");
        setServerID(FRNINEMANGA);

        // override variables in super class
        super.HOST = HOST;
        super.PATTERN_IMAGE = PATTERN_IMAGE;
        super.valGenre = valGenre;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(
                        context.getString(R.string.flt_genre),
                        fltGenre, ServerFilter.FilterType.MULTI_STATES),
                new ServerFilter(
                        context.getString(R.string.flt_status),
                        buildTranslatedStringArray(NineManga.fltStatus), ServerFilter.FilterType.SINGLE),
                // new ServerFilter(
                //         context.getString(R.string.flt_category),
                //         buildTranslatedStringArray(fltCategory), ServerFilter.FilterType.SINGLE)
        };
    }
}
