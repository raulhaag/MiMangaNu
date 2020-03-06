package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import com.squareup.duktape.Duktape;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

import static ar.rulosoft.mimanganu.utils.PostProcess.FLAG_PPL90;

/**
 * Created by xtj-9182 on 21.02.2017.
 */
class JapScan extends ServerBase {

    private static final String HOST = "https://www.japscan.co";

    JapScan(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.japscan);
        setServerName("JapScan");
        setServerID(JAPSCAN);
    }

    private String[] letterFilter = new String[]{"All", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private static final String decodeScript = "var window = {\n" +
            "    URL: function (wp) {\n" +
            "        si = wp.indexOf(\"/\", 9);\n" +
            "        this.pathname = wp.substr(si);\n" +
            "        this.origin = wp.substr(0, si);\n" +
            "    }\n" +
            "};\n" +
            "var N = new window.URL(bT).pathname.split('.');\n" +
            "    var mB = N.pop();\n" +
            "    N = N.join('.');\n" +
            "    var zJ = new window.URL(bT).origin + N.split('').map(function(uo) {\n" +
            "        if (uo == '/') {\n" +
            "            return '/';\n" +
            "        } else if (uo == 'o') {\n" +
            "            return 'q';\n" +
            "        } else if (uo == '4') {\n" +
            "            return 'z';\n" +
            "        } else if (uo == '9') {\n" +
            "            return '4';\n" +
            "        } else if (uo == 'u') {\n" +
            "            return 'h';\n" +
            "        } else if (uo == 'z') {\n" +
            "            return 'l';\n" +
            "        } else if (uo == 'h') {\n" +
            "            return 'x';\n" +
            "        } else if (uo == 'e') {\n" +
            "            return 'u';\n" +
            "        } else if (uo == 'a') {\n" +
            "            return 'p';\n" +
            "        } else if (uo == 'i') {\n" +
            "            return 'b';\n" +
            "        } else if (uo == 'q') {\n" +
            "            return '3';\n" +
            "        } else if (uo == 'j') {\n" +
            "            return 'g';\n" +
            "        } else if (uo == 'd') {\n" +
            "            return 'd';\n" +
            "        } else if (uo == '7') {\n" +
            "            return '9';\n" +
            "        } else if (uo == '0') {\n" +
            "            return 's';\n" +
            "        } else if (uo == 'x') {\n" +
            "            return 'c';\n" +
            "        } else if (uo == 'k') {\n" +
            "            return '6';\n" +
            "        } else if (uo == '3') {\n" +
            "            return 'n';\n" +
            "        } else if (uo == 'g') {\n" +
            "            return 'f';\n" +
            "        } else if (uo == '2') {\n" +
            "            return '0';\n" +
            "        } else if (uo == '6') {\n" +
            "            return 'r';\n" +
            "        } else if (uo == 'w') {\n" +
            "            return 'w';\n" +
            "        } else if (uo == 'f') {\n" +
            "            return '1';\n" +
            "        } else if (uo == 'n') {\n" +
            "            return 'i';\n" +
            "        } else if (uo == '8') {\n" +
            "            return '5';\n" +
            "        } else if (uo == 'r') {\n" +
            "            return 't';\n" +
            "        } else if (uo == 'm') {\n" +
            "            return 'e';\n" +
            "        } else if (uo == '1') {\n" +
            "            return '7';\n" +
            "        } else if (uo == 't') {\n" +
            "            return 'v';\n" +
            "        } else if (uo == 'y') {\n" +
            "            return 'y';\n" +
            "        } else if (uo == 'c') {\n" +
            "            return 'k';\n" +
            "        } else if (uo == 'v') {\n" +
            "            return 'a';\n" +
            "        } else if (uo == 'l') {\n" +
            "            return 'j';\n" +
            "        } else if (uo == 's') {\n" +
            "            return '2';\n" +
            "        } else if (uo == '5') {\n" +
            "            return 'o';\n" +
            "        } else if (uo == 'b') {\n" +
            "            return 'm';\n" +
            "        } else if (uo == 'p') {\n" +
            "            return '8';\n" +
            "        } else {\n" +
            "            return uo;\n" +
            "        }\n" +
            "    }).map(function(uo) {\n" +
            "        if (uo == '/') {\n" +
            "            return '/';\n" +
            "        } else if (uo == 'o') {\n" +
            "            return 'q';\n" +
            "        } else if (uo == '4') {\n" +
            "            return 'z';\n" +
            "        } else if (uo == '9') {\n" +
            "            return '4';\n" +
            "        } else if (uo == 'u') {\n" +
            "            return 'h';\n" +
            "        } else if (uo == 'z') {\n" +
            "            return 'l';\n" +
            "        } else if (uo == 'h') {\n" +
            "            return 'x';\n" +
            "        } else if (uo == 'e') {\n" +
            "            return 'u';\n" +
            "        } else if (uo == 'a') {\n" +
            "            return 'p';\n" +
            "        } else if (uo == 'i') {\n" +
            "            return 'b';\n" +
            "        } else if (uo == 'q') {\n" +
            "            return '3';\n" +
            "        } else if (uo == 'j') {\n" +
            "            return 'g';\n" +
            "        } else if (uo == 'd') {\n" +
            "            return 'd';\n" +
            "        } else if (uo == '7') {\n" +
            "            return '9';\n" +
            "        } else if (uo == '0') {\n" +
            "            return 's';\n" +
            "        } else if (uo == 'x') {\n" +
            "            return 'c';\n" +
            "        } else if (uo == 'k') {\n" +
            "            return '6';\n" +
            "        } else if (uo == '3') {\n" +
            "            return 'n';\n" +
            "        } else if (uo == 'g') {\n" +
            "            return 'f';\n" +
            "        } else if (uo == '2') {\n" +
            "            return '0';\n" +
            "        } else if (uo == '6') {\n" +
            "            return 'r';\n" +
            "        } else if (uo == 'w') {\n" +
            "            return 'w';\n" +
            "        } else if (uo == 'f') {\n" +
            "            return '1';\n" +
            "        } else if (uo == 'n') {\n" +
            "            return 'i';\n" +
            "        } else if (uo == '8') {\n" +
            "            return '5';\n" +
            "        } else if (uo == 'r') {\n" +
            "            return 't';\n" +
            "        } else if (uo == 'm') {\n" +
            "            return 'e';\n" +
            "        } else if (uo == '1') {\n" +
            "            return '7';\n" +
            "        } else if (uo == 't') {\n" +
            "            return 'v';\n" +
            "        } else if (uo == 'y') {\n" +
            "            return 'y';\n" +
            "        } else if (uo == 'c') {\n" +
            "            return 'k';\n" +
            "        } else if (uo == 'v') {\n" +
            "            return 'a';\n" +
            "        } else if (uo == 'l') {\n" +
            "            return 'j';\n" +
            "        } else if (uo == 's') {\n" +
            "            return '2';\n" +
            "        } else if (uo == '5') {\n" +
            "            return 'o';\n" +
            "        } else if (uo == 'b') {\n" +
            "            return 'm';\n" +
            "        } else if (uo == 'p') {\n" +
            "            return '8';\n" +
            "        } else {\n" +
            "            return uo;\n" +
            "        }\n" +
            "    }).map(function(uo) {\n" +
            "        if (uo == '/') {\n" +
            "            return '/';\n" +
            "        } else if (uo == 'o') {\n" +
            "            return 'q';\n" +
            "        } else if (uo == '4') {\n" +
            "            return 'z';\n" +
            "        } else if (uo == '9') {\n" +
            "            return '4';\n" +
            "        } else if (uo == 'u') {\n" +
            "            return 'h';\n" +
            "        } else if (uo == 'z') {\n" +
            "            return 'l';\n" +
            "        } else if (uo == 'h') {\n" +
            "            return 'x';\n" +
            "        } else if (uo == 'e') {\n" +
            "            return 'u';\n" +
            "        } else if (uo == 'a') {\n" +
            "            return 'p';\n" +
            "        } else if (uo == 'i') {\n" +
            "            return 'b';\n" +
            "        } else if (uo == 'q') {\n" +
            "            return '3';\n" +
            "        } else if (uo == 'j') {\n" +
            "            return 'g';\n" +
            "        } else if (uo == 'd') {\n" +
            "            return 'd';\n" +
            "        } else if (uo == '7') {\n" +
            "            return '9';\n" +
            "        } else if (uo == '0') {\n" +
            "            return 's';\n" +
            "        } else if (uo == 'x') {\n" +
            "            return 'c';\n" +
            "        } else if (uo == 'k') {\n" +
            "            return '6';\n" +
            "        } else if (uo == '3') {\n" +
            "            return 'n';\n" +
            "        } else if (uo == 'g') {\n" +
            "            return 'f';\n" +
            "        } else if (uo == '2') {\n" +
            "            return '0';\n" +
            "        } else if (uo == '6') {\n" +
            "            return 'r';\n" +
            "        } else if (uo == 'w') {\n" +
            "            return 'w';\n" +
            "        } else if (uo == 'f') {\n" +
            "            return '1';\n" +
            "        } else if (uo == 'n') {\n" +
            "            return 'i';\n" +
            "        } else if (uo == '8') {\n" +
            "            return '5';\n" +
            "        } else if (uo == 'r') {\n" +
            "            return 't';\n" +
            "        } else if (uo == 'm') {\n" +
            "            return 'e';\n" +
            "        } else if (uo == '1') {\n" +
            "            return '7';\n" +
            "        } else if (uo == 't') {\n" +
            "            return 'v';\n" +
            "        } else if (uo == 'y') {\n" +
            "            return 'y';\n" +
            "        } else if (uo == 'c') {\n" +
            "            return 'k';\n" +
            "        } else if (uo == 'v') {\n" +
            "            return 'a';\n" +
            "        } else if (uo == 'l') {\n" +
            "            return 'j';\n" +
            "        } else if (uo == 's') {\n" +
            "            return '2';\n" +
            "        } else if (uo == '5') {\n" +
            "            return 'o';\n" +
            "        } else if (uo == 'b') {\n" +
            "            return 'm';\n" +
            "        } else if (uo == 'p') {\n" +
            "            return '8';\n" +
            "        } else {\n" +
            "            return uo;\n" +
            "        }\n" +
            "    }).map(function(uo) {\n" +
            "        if (uo == '/') {\n" +
            "            return '/';\n" +
            "        } else if (uo == 'o') {\n" +
            "            return 'q';\n" +
            "        } else if (uo == '4') {\n" +
            "            return 'z';\n" +
            "        } else if (uo == '9') {\n" +
            "            return '4';\n" +
            "        } else if (uo == 'u') {\n" +
            "            return 'h';\n" +
            "        } else if (uo == 'z') {\n" +
            "            return 'l';\n" +
            "        } else if (uo == 'h') {\n" +
            "            return 'x';\n" +
            "        } else if (uo == 'e') {\n" +
            "            return 'u';\n" +
            "        } else if (uo == 'a') {\n" +
            "            return 'p';\n" +
            "        } else if (uo == 'i') {\n" +
            "            return 'b';\n" +
            "        } else if (uo == 'q') {\n" +
            "            return '3';\n" +
            "        } else if (uo == 'j') {\n" +
            "            return 'g';\n" +
            "        } else if (uo == 'd') {\n" +
            "            return 'd';\n" +
            "        } else if (uo == '7') {\n" +
            "            return '9';\n" +
            "        } else if (uo == '0') {\n" +
            "            return 's';\n" +
            "        } else if (uo == 'x') {\n" +
            "            return 'c';\n" +
            "        } else if (uo == 'k') {\n" +
            "            return '6';\n" +
            "        } else if (uo == '3') {\n" +
            "            return 'n';\n" +
            "        } else if (uo == 'g') {\n" +
            "            return 'f';\n" +
            "        } else if (uo == '2') {\n" +
            "            return '0';\n" +
            "        } else if (uo == '6') {\n" +
            "            return 'r';\n" +
            "        } else if (uo == 'w') {\n" +
            "            return 'w';\n" +
            "        } else if (uo == 'f') {\n" +
            "            return '1';\n" +
            "        } else if (uo == 'n') {\n" +
            "            return 'i';\n" +
            "        } else if (uo == '8') {\n" +
            "            return '5';\n" +
            "        } else if (uo == 'r') {\n" +
            "            return 't';\n" +
            "        } else if (uo == 'm') {\n" +
            "            return 'e';\n" +
            "        } else if (uo == '1') {\n" +
            "            return '7';\n" +
            "        } else if (uo == 't') {\n" +
            "            return 'v';\n" +
            "        } else if (uo == 'y') {\n" +
            "            return 'y';\n" +
            "        } else if (uo == 'c') {\n" +
            "            return 'k';\n" +
            "        } else if (uo == 'v') {\n" +
            "            return 'a';\n" +
            "        } else if (uo == 'l') {\n" +
            "            return 'j';\n" +
            "        } else if (uo == 's') {\n" +
            "            return '2';\n" +
            "        } else if (uo == '5') {\n" +
            "            return 'o';\n" +
            "        } else if (uo == 'b') {\n" +
            "            return 'm';\n" +
            "        } else if (uo == 'p') {\n" +
            "            return '8';\n" +
            "        } else {\n" +
            "            return uo;\n" +
            "        }\n" +
            "    }).join('') + '.' + mB;\n" +
            "    N = new window['URL'](zJ).pathname.split('.');\n" +
            "    mB = N.pop();\n" +
            "    N = N.join('.');\n" +
            "zJ = new window['URL'](zJ).origin + N.split('').map(function(uo) {\n" +
            "        if (uo == '/') {\n" +
            "            return '/';\n" +
            "        } else if (uo == 'c') {\n" +
            "            return 'm';\n" +
            "        } else if (uo == 'q') {\n" +
            "            return '4';\n" +
            "        } else if (uo == '5') {\n" +
            "            return 'v';\n" +
            "        } else if (uo == 'h') {\n" +
            "            return 'e';\n" +
            "        } else if (uo == '0') {\n" +
            "            return 'p';\n" +
            "        } else if (uo == 'o') {\n" +
            "            return '2';\n" +
            "        } else if (uo == '8') {\n" +
            "            return 'g';\n" +
            "        } else if (uo == 'y') {\n" +
            "            return 't';\n" +
            "        } else if (uo == '2') {\n" +
            "            return 'u';\n" +
            "        } else if (uo == 'f') {\n" +
            "            return '1';\n" +
            "        } else if (uo == 'w') {\n" +
            "            return 'j';\n" +
            "        } else if (uo == 'a') {\n" +
            "            return '8';\n" +
            "        } else if (uo == '4') {\n" +
            "            return 'w';\n" +
            "        } else if (uo == 'x') {\n" +
            "            return '9';\n" +
            "        } else if (uo == '7') {\n" +
            "            return 'k';\n" +
            "        } else if (uo == 'n') {\n" +
            "            return 'q';\n" +
            "        } else if (uo == 'b') {\n" +
            "            return '3';\n" +
            "        } else if (uo == 'i') {\n" +
            "            return '6';\n" +
            "        } else if (uo == 'z') {\n" +
            "            return 'y';\n" +
            "        } else if (uo == 'l') {\n" +
            "            return '7';\n" +
            "        } else if (uo == 'j') {\n" +
            "            return 'f';\n" +
            "        } else if (uo == 'g') {\n" +
            "            return 'h';\n" +
            "        } else if (uo == 's') {\n" +
            "            return 'x';\n" +
            "        } else if (uo == 'm') {\n" +
            "            return '5';\n" +
            "        } else if (uo == 'd') {\n" +
            "            return '0';\n" +
            "        } else if (uo == '3') {\n" +
            "            return 'l';\n" +
            "        } else if (uo == '1') {\n" +
            "            return 'b';\n" +
            "        } else if (uo == 'p') {\n" +
            "            return 'z';\n" +
            "        } else if (uo == '6') {\n" +
            "            return 'c';\n" +
            "        } else if (uo == 'u') {\n" +
            "            return 'a';\n" +
            "        } else if (uo == 'k') {\n" +
            "            return 's';\n" +
            "        } else if (uo == 'e') {\n" +
            "            return 'd';\n" +
            "        } else if (uo == 'v') {\n" +
            "            return 'i';\n" +
            "        } else if (uo == '9') {\n" +
            "            return 'n';\n" +
            "        } else if (uo == 'r') {\n" +
            "            return 'o';\n" +
            "        } else if (uo == 't') {\n" +
            "            return 'r';\n" +
            "        } else {\n" +
            "            return uo;\n" +
            "        }\n" +
            "    }).join('') + '.' + mB;\n" +
            "\n" +
            "zJ.toString();";

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("search", URLEncoder.encode(search, "UTF-8"));
        String source = nav.post(HOST + "/search/");
        if (source.length() > 2) {
            JSONArray jsonArray = new JSONArray(source);
            JSONObject item;
            for (int i = 0; i < jsonArray.length(); i++) {
                item = (JSONObject) jsonArray.get(i);
                mangas.add(new Manga(getServerID(), item.getString("name"), item.getString("url"), false));
            }
        }
        return mangas;

    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public synchronized void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Cover Image
            // JapScan has no cover images ...
            manga.setImages(HOST + getFirstMatchDefault("<div class=\"m-2\">[\\s\\S]+?src=\"([^\"]+)", source, ""));

            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis:</div>[\\s\\S]+?<p[^>]+>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(getFirstMatchDefault("Statut:</span>([^<]+)", source, "").contains("Terminé"));

            // Author
            manga.setAuthor(getFirstMatchDefault("Auteur\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)).trim());

            // Genres
            manga.setGenre(getFirstMatchDefault("Type\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern pattern = Pattern.compile("<div class=\"chapters_list text-truncate\">[\\s\\S]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
            }
        }
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data = getNavigatorAndFlushParameters().get(HOST + chapter.getPath() + page + ".html");
        String eimg = getFirstMatch("<div id=\"image\" data-src=\"([^\"]+)", data, context.getString(R.string.error_downloading_image));
        Duktape duktape = Duktape.create();
        String result = "";
        boolean error = false;
        try {
            result = duktape.evaluate("var bT = '" + eimg + "';\n" + decodeScript).toString();
        } catch (Exception e) {
            error = true;
        } finally {
            duktape.close();
        }
        if (error) throw new Exception(context.getString(R.string.error_downloading_image));
        return result + (chapter.getExtra().contains(FLAG_PPL90) ? FLAG_PPL90 : "");
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("\"img-fluid\" src=\"([^\"]+)[\\s\\S]+?<a[^>]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), HOST + matcher.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    private String[] pageFilter = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120"};

    @Override
    public synchronized ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
       /* String extra = "";
        if (filters[0][0] != 0) {
            extra = letterFilter[filters[0][0]] + "/";
        }*/
        String source = getNavigatorAndFlushParameters().get(HOST + "/mangas/" + (pageNumber - 1 + (filters[0][0] * 10)));
        return getMangasFromSource(source);
    }

    @Override
    public synchronized void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
            String pages = getFirstMatch("Page (\\d+)</option>\\s*</select>", source,
                    context.getString(R.string.server_failed_loading_image));
            String extra = "";
            if (source.contains("iYFbYi_UibMqYb.js")) {
                extra = "|" + FLAG_PPL90;
            }
            chapter.setExtra(extra);
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public boolean hasSearch() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_page), pageFilter, ServerFilter.FilterType.SINGLE),

                //  new ServerFilter(context.getString(R.string.flt_alpha), letterFilter, ServerFilter.FilterType.SINGLE),
        };
    }
}