package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

class EsNineManga extends NineManga {
    private static final String HOST = "http://es.ninemanga.com";

    private static final String PATTERN_IMAGE =
            "class=\"pic_download\" href=\"(https*://[^/]+/+es_manga/[^\"]+)";

    private static final String PATTERN_CHAPTER =
            "<a class=\"chapter_list_a\" href=\".*?(/chapter[^<\"]+)\" title=\"([^\"]+)\">([^<]+)</a>";


    private static final String[] fltGenre = {
            "4-Koma", "AcciÓN", "AccióN", "Action", "Adult", "Adulto", "Adventure", "AnimacióN", "ApocalíPtico", "Artes Marciales", "Aventura", "Aventuras", "BL (Boys Love)", "Boys Love", "Ciberpunk", "Ciencia FiccióN", "Comedia", "Comedy", "Crimen", "Cyberpunk", "Demonios", "Deporte", "Deportes", "Doujinshi", "Drama", "Ecchi", "Escolar", "EspañOl", "Extranjero", "Familia", "Fantacia", "FantasÍA", "FantasíA", "Fantasy", "Gender Bender", "GéNero Bender", "Girls Love", "GL (Girls Love)", "Gore", "Guerra", "Harem", "Hentai", "Historia", "Historical", "HistóRico", "Horror", "Isekai", "Josei", "Maduro", "Magia", "Magical Girls", "Manga", "Martial", "Martial Arts", "Mecha", "Medical", "Militar", "Misterio", "Music", "MúSica", "Musical", "Mystery", "NiñOs", "Oeste", "One Shot", "One-Shot", "Oneshot", "Parodia", "Philosophical", "PolicíAca", "Policiaco", "Policial", "PsicolóGica", "PsicolóGico", "Psychological", "Realidad", "Realidad Virtual", "Recuentos De La Vida", "ReencarnacióN", "Romance", "Samurai", "School Life", "Sci-Fi", "Seinen", "Shojo", "Shojo Ai", "Shojo-Ai (Yuri Soft)", "Shonen", "Shonen Ai", "Shonen-Ai", "Shonen-Ai (Yaoi Soft)", "Shota", "Shoujo", "Shoujo Ai", "Shoujo-Ai", "Shounen", "Shounen Ai", "Slice Of Life", "Smut", "Sobrenatural", "Sports", "Super Natural", "Super Poderes", "Superhero", "Supernatural", "Superpoderes", "Supervivencia", "Suspense", "Telenovela", "Thiller", "Thriller", "Tragedia", "Tragedy", "Vampiros", "Ver En Lectormanga", "Vida Cotidiana", "Vida Escolar", "Vida Escolar.", "Webcomic", "Webtoon", "Wuxia", "Yaoi", "Yaoi (Soft)", "Yonkoma", "Yuri", "Yuri (Soft)"
    };
    private static String[] valGenre = {
            "201", "213", "69", "177", "193", "86", "179", "229", "202", "66", "64", "120", "223", "228", "225", "93", "75", "178", "227", "199", "126", "76", "111", "216", "79", "65", "81", "249", "238", "237", "100", "214", "70", "180", "175", "230", "226", "222", "108", "234", "78", "83", "233", "190", "95", "99", "240", "112", "72", "172", "248", "251", "189", "181", "115", "247", "205", "88", "241", "121", "197", "187", "235", "239", "184", "221", "195", "198", "252", "220", "236", "208", "219", "96", "192", "231", "196", "169", "207", "67", "210", "176", "123", "73", "80", "186", "218", "77", "128", "174", "217", "224", "85", "194", "173", "68", "185", "182", "183", "74", "188", "124", "206", "246", "119", "215", "203", "171", "242", "204", "97", "87", "191", "209", "243", "84", "170", "122", "92", "200", "244", "105", "211", "232", "127", "212"
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
        super.valGenre = valGenre;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String id = getFirstMatch("\\/(\\d+)\\.", chapter.getPath(), context.getString(R.string.error));
        String path = chapter.getPath().replaceAll("\\/\\d+", "")
                .replace("chapter", "manga");
        nav.addHeader("Referer", HOST + path);
        String data = nav.getRedirectWeb(HOST + chapter.getPath());
        nav.addHeader("Referer", HOST + chapter.getPath());
        data = nav.getRedirectWeb(data);
        nav.addHeader("Referer", HOST + chapter.getPath());
        String sid = getFirstMatch("\\/(\\d+)\\.", data, context.getString(R.string.error));
        nav.addHeader("Cookie", "lrgarden_visit_check_" + sid + "=" + id + ";");
        data = nav.get("https://www.gardenmanage.com" + data);
        data = getFirstMatch("all_imgs_url: \\[([^\\]]+)", data, context.getString(R.string.error));
        ArrayList<String> pages = getAllMatch("\"([^\"]+)\"", data);
        if (pages.size() != 0) {
            chapter.setPages(pages.size());
            chapter.setExtra("https://www.gardenmanage.com/c/esninemanga/" + id + "/|" + TextUtils.join("|", pages));
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] parts = chapter.getExtra().split("\\|");
        return parts[page] + "|" + parts[0];
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
