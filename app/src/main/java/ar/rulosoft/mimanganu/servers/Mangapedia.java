package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;
import okhttp3.MultipartBody;

/**
 * Created by Raúl on 15/07/2017.
 */

public class Mangapedia extends ServerBase {

    private static final String[] genre = {"Shonen", "Seinen", "Shonen Ai", "Josei"
            , "Shojo", "Shojo Ai", "Yaoi", "Yuri"};

    private static final String[] genreV = {
            "typeID[1]","typeID[2]","typeID[3]","typeID[4]","typeID[5]","typeID[6]",
            "typeID[7]","typeID[8]",
    };

    private static final String[] subGenre = {
            "Action","Aventure","Comédie","Drame","Fantasie","Arts Martiaux",
            "Mystères","Surnaturel","Adulte","Doujinshi","Ecchi","Harem",
            "Historique","Horreur","Mature","Mecha","One Shot","Psychologie",
            "Romance","School Life","Sci-Fi","Tranche de vie","Sports","Tragédie",

    };
    private static final String[] subGenreV = {
            "categoryID[1]","categoryID[2]","categoryID[3]","categoryID[4]","categoryID[5]","categoryID[6]",
            "categoryID[7]","categoryID[8]","categoryID[9]","categoryID[10]","categoryID[11]","categoryID[12]",
            "categoryID[13]","categoryID[14]","categoryID[15]","categoryID[16]","categoryID[17]","categoryID[18]",
            "categoryID[19]","categoryID[20]","categoryID[21]","categoryID[22]","categoryID[23]","categoryID[24]"
    };

    private static final String[] type = {"Manga", "Manhwa", "Manhua", "Webcomics"};

    private static final String[] typeV = {
            "typeBookID[1]", "typeBookID[2]", "typeBookID[4]", "typeBookID[5]"
    };

    private static final String[] sortBy = {"Default", "Titre", "Auteur", "Illustrateur", "Vues", "Derniere MAJ"};

    private static final String[] sortByV = {
            "0", "1", "2", "3", "4", "5"
    };

    private static final String[] sortOrder = {"Default", "Croissant", "Décroissant"};

    private static final String[] sortOrderV = {
            "0", "1", "2"
    };

    public Mangapedia(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_fr);
        this.setIcon(R.drawable.mangapedia);
        this.setServerName("Mangapedia");
        setServerID(ServerBase.MANGAPEDIA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String web = "http://mangapedia.fr/project_code/script/moreMangas.php";
        if(pageNumber == 1){
            web = "http://mangapedia.fr/project_code/script/search.php";
        }

        Navigator nav = getNavigatorAndFlushParameters();
        String boundary = Navigator.getNewBoundary();
        nav.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        MultipartBody.Builder mBodyBuilder = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM);
        mBodyBuilder.addFormDataPart("artist","");
        mBodyBuilder.addFormDataPart("searchType","advance");
        mBodyBuilder.addFormDataPart("pageNumber", "" + pageNumber);
        mBodyBuilder.addFormDataPart("searchTerm","");
        mBodyBuilder.addFormDataPart("searchByLetter","");
        for(int i = 0; i < genreV.length; i++){
            if(Util.getInstance().contains(filters[0],i)){
                mBodyBuilder.addFormDataPart(genreV[i],"1");
            }else{
                mBodyBuilder.addFormDataPart(genreV[i],"0");
            }
        }
        for(int i = 0; i < subGenreV.length; i++){
            if(Util.getInstance().contains(filters[1],i)){
                mBodyBuilder.addFormDataPart(subGenreV[i],"1");
            }else{
                mBodyBuilder.addFormDataPart(subGenreV[i],"0");
            }
        }
        for(int i = 0; i < typeV.length; i++){
            if(Util.getInstance().contains(filters[2],i)){
                mBodyBuilder.addFormDataPart(typeV[i],"1");
            }else{
                mBodyBuilder.addFormDataPart(typeV[i],"0");
            }
        }
        mBodyBuilder.addFormDataPart("sortBy", sortByV[filters[3][0]]);
        mBodyBuilder.addFormDataPart("sortOrder", sortOrderV[filters[4][0]]);
        return getMangasString(nav.post(web, mBodyBuilder.build()));
    }

    private ArrayList<Manga> getMangasString(String data){
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"([^\"]+)\".+?src=\"([^\"]+)\"\\/>.+?>(.+?)<");
        Matcher m = p.matcher(data);
        while (m.find()) {
            Manga manga = new Manga(getServerID(), m.group(3), m.group(1), false);
            manga.setImages(m.group(2).replaceAll("-thumb","")+"|http://mangapedia.fr/mangas");
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return  new ServerFilter[]{
                new ServerFilter("Genres", genre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Sub Genre", subGenre, ServerFilter.FilterType.MULTI),
                new ServerFilter("Type", type, ServerFilter.FilterType.MULTI),
                new ServerFilter("Classé par", sortBy, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Classé par", sortOrder , ServerFilter.FilterType.SINGLE)};
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        Navigator nav = getNavigatorAndFlushParameters();
        String boundary = Navigator.getNewBoundary();
        nav.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        nav.addHeader("X-Requested-With", "XMLHttpRequest");
        MultipartBody.Builder mBodyBuilder = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM);
        mBodyBuilder.addFormDataPart("artist",term);
        mBodyBuilder.addFormDataPart("searchType","advance");
        mBodyBuilder.addFormDataPart("pageNumber", "1");
        mBodyBuilder.addFormDataPart("searchTerm","");
        mBodyBuilder.addFormDataPart("searchByLetter","");
        for(int i = 0; i < genreV.length; i++){
            mBodyBuilder.addFormDataPart(genreV[i],"0");
        }
        for(int i = 0; i < subGenreV.length; i++){
            mBodyBuilder.addFormDataPart(subGenreV[i],"0");
        }
        for(int i = 0; i < typeV.length; i++){
            mBodyBuilder.addFormDataPart(typeV[i],"0");

        }
        mBodyBuilder.addFormDataPart("sortBy", "0");
        mBodyBuilder.addFormDataPart("sortOrder", "0");
        return getMangasString(nav.post("http://mangapedia.fr/project_code/script/search.php", mBodyBuilder.build()));    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga,forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().size() == 0 || forceReload) {
            String data = getNavigatorAndFlushParameters().get(manga.getPath());
            //Autor
            manga.setAuthor(Util.getInstance().fromHtml(
                    getFirstMatchDefault("Auteur : (.+?)</div>", data,
                            context.getString(R.string.nodisponible))).toString());
            //Generos
            manga.setGenre(Util.getInstance().fromHtml(
                    getFirstMatchDefault("Sous-genres : (.+?)<\\/div>", data,
                            context.getString(R.string.nodisponible))).toString());

            //Sinopsis
            manga.setSynopsis(Util.getInstance().fromHtml(
                    getFirstMatchDefault("Synopsis : (.+?)<\\/div>", data,
                            context.getString(R.string.nodisponible))).toString());
            //Estado
            manga.setFinished(!getFirstMatchDefault("Statut : (.+?)<\\/div>", data, "en cours")
                    .contains("en cours"));
            //Capítulos
            manga.getChapters().clear();
            Pattern pattern = Pattern.compile("<a href=\"(http[s]*://mangapedia.fr/lel[^\"]+).+?\"nameChapter\">(.+?)<");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                manga.addChapter(new Chapter(matcher.group(2).trim().replaceAll("<.*?>", ""),
                        matcher.group(1)));
            }
        }
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        Pattern p = Pattern.compile("['|\"](http[s]*://mangapedia.fr/project_code/script/image.php\\?path=.+?)['|\"]");
        Matcher m = p.matcher(data);
        int i = 0;
        String images = "";
        while(m.find()){
            i++;
            images = images + "|" + m.group(1);
        }
        chapter.setPages(i);
        chapter.setExtra(images);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }
}
