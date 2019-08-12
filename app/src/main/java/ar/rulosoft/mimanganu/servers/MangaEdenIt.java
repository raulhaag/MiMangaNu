package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 03/12/2015.
 */
class MangaEdenIt extends MangaEden {

    private static final int[] fltGenre = {
            R.string.flt_tag_adventure, //Avventura
            R.string.flt_tag_action, //Azione
            R.string.flt_tag_bara, //Bara
            R.string.flt_tag_comedy, //Commedia
            R.string.flt_tag_madness, //Demenziale (?)
            R.string.flt_tag_doujinshi, //Dounshinji
            R.string.flt_tag_drama, //Drama
            R.string.flt_tag_ecchi, //Ecchi
            R.string.flt_tag_fantasy, //Fantasy
            R.string.flt_tag_harem, //Harem
            R.string.flt_tag_hentai, //Hentai
            R.string.flt_tag_horror, //Horror
            R.string.flt_tag_josei, //Josei
            R.string.flt_tag_magic, //Magico
            R.string.flt_tag_mecha, //Mecha
            R.string.flt_tag_mystery, //Misteri
            R.string.flt_tag_music, //Musica
            R.string.flt_tag_psychological, //Psicologico
            R.string.flt_tag_collection, //Raccolta (?)
            R.string.flt_tag_romance, //Romantico
            R.string.flt_tag_sci_fi, //Sci-Fi
            R.string.flt_tag_school_life, //Scolastico
            R.string.flt_tag_seinen, //Seinen
            R.string.flt_tag_relation, //Sentimentale (?)
            R.string.flt_tag_shota, //Shota
            R.string.flt_tag_shoujo, //Shoujo
            R.string.flt_tag_shounen, //Shounen
            R.string.flt_tag_supernatural, //Sovrannaturale
            R.string.flt_tag_splatter, //Splatter
            R.string.flt_tag_sports, //Sportivo
            R.string.flt_tag_historical, //Storico
            R.string.flt_tag_slice_of_life, //Vita Quotidiana
            R.string.flt_tag_yaoi, //Yaoi
            R.string.flt_tag_yuri //Yuri
    };

    private static final String[] valGenre = {
            "4e70ea8cc092255ef70073d3",
            "4e70ea8cc092255ef70073c3",
            "4e70ea90c092255ef70074b7",
            "4e70ea8cc092255ef70073d0",
            "4e70ea8fc092255ef7007475",
            "4e70ea93c092255ef70074e4",
            "4e70ea8cc092255ef70073f9",
            "4e70ea8cc092255ef70073cd",
            "4e70ea8cc092255ef70073c4",
            "4e70ea8cc092255ef70073d1",
            "4e70ea90c092255ef700749a",
            "4e70ea8cc092255ef70073ce",
            "4e70ea90c092255ef70074bd",
            "4e70ea93c092255ef700751b",
            "4e70ea8cc092255ef70073ef",
            "4e70ea8dc092255ef700740a",
            "4e70ea8fc092255ef7007456",
            "4e70ea8ec092255ef7007439",
            "4e70ea90c092255ef70074ae",
            "4e70ea8cc092255ef70073c5",
            "4e70ea8cc092255ef70073e4",
            "4e70ea8cc092255ef70073e5",
            "4e70ea8cc092255ef70073ea",
            "4e70ea8dc092255ef7007432",
            "4e70ea90c092255ef70074b8",
            "4e70ea8dc092255ef7007421",
            "4e70ea8cc092255ef70073c6",
            "4e70ea8cc092255ef70073c7",
            "4e70ea99c092255ef70075a3",
            "4e70ea8dc092255ef7007426",
            "4e70ea8cc092255ef70073f4",
            "4e70ea8ec092255ef700743f",
            "4e70ea8cc092255ef70073de",
            "4e70ea9ac092255ef70075d1",
            "4e70ea8cc092255ef70073d3"
    };

    MangaEdenIt(Context context) {
        super(context);
        setFlag(R.drawable.flag_it);
        setIcon(R.drawable.mangaeden);
        setServerName("MangaEdenIt");
        setServerID(MANGAEDENIT);
        setLanguage("it", "it");

        // override super variables
        super.fltGenre = fltGenre;
        super.valGenre = valGenre;
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(manga.getPath());
            // Front
            String image = getFirstMatchDefault("<div class=\"mangaImage2\"><img src=\"(.+?)\"", source, "");
            if (image.length() > 2) {
                image = "http:" + image;
            }
            manga.setImages(image);
            // Summary
            manga.setSynopsis(getFirstMatchDefault("mangaDescription\">(.+?)</h", source, context.getString(R.string.nodisponible)));
            // Status
            manga.setFinished(getFirstMatchDefault("Stato</h(.+?)<h", source, "").contains("Completato"));
            // Author
            manga.setAuthor(getFirstMatchDefault("Autore</h4>(.+?)<h4>", source, context.getString(R.string.nodisponible)));
            // Genres
            manga.setGenre(getFirstMatchDefault("Genere</h4>(.+?)<h4>", source, context.getString(R.string.nodisponible)));
            // Chapters
            Pattern pattern = Pattern.compile("<a href=\"(/it/it-manga/[^\"]+)\" class=\"chapterLink\">(.+?)</a>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2).replaceAll("Capitolo", " Cap "), HOST + matcher.group(1)));
            }
        }
    }
}
