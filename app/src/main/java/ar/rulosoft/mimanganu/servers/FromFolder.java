package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 14/05/2016.
 */
public class FromFolder extends ServerBase {

    public FromFolder(){
        this.setFlag(R.drawable.noimage);
        this.setIcon(R.drawable.from_folder);
        this.setServerName("FromFolder");
        setServerID(ServerBase.FROMFOLDER);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        manga.setImages(manga.getPath() + "cover.jpg");
        ArrayList<String> folders = Util.dirList(manga.getPath());
        ArrayList<Chapter> chapters = new ArrayList<>();
        folders.remove(0);//remove "."
        for(String folder:folders){
            Chapter chapter = new Chapter(folder,manga.getPath() + folder + "/");
            chapter.setDownloaded(true);
            chapters.add(chapter);
        }
        Collections.sort(chapters,Chapter.Comparators.NUMBERS_DESC);
        manga.setChapters(chapters);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga,forceReload);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        return chapter.getPath() + chapter.getExtra().split("\\|")[page + 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        ArrayList<String> images = Util.imageList(chapter.getPath());
        chapter.setPages(images.size());
        Collections.sort(images,NUMBERS_ASC);
        String save = "";
        for (String image:images){
            save = save + "|" + image;
        }
        chapter.setExtra(save);
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int categorie, int order, int pageNumber) throws Exception {
        return null;
    }

    @Override
    public String[] getCategories() {
        return new String[0];
    }

    @Override
    public String[] getOrders() {
        return new String[0];
    }

    @Override
    public boolean hasList() {
        return false;
    }


    private static final String FLOAT_PATTERN = "([.,0123456789]+)";
    private static final String STRING_END_PATTERN = "[^\\d]\\.";
    private static final String VOLUME_REMOVE_PATTERN = "[v|V][o|O][l|L].{0,1}\\d+";
    public static Comparator<String> NUMBERS_ASC = new Comparator<String>() {
        @Override
        public int compare(String c1, String c2) {
            try {
                    String str1 = c1.replaceAll(VOLUME_REMOVE_PATTERN, " ");
                    str1 = str1.replaceAll(STRING_END_PATTERN, " ");
                    str1 = ServerBase.getFirstMatch(FLOAT_PATTERN, str1, "");
                    Float f1 = Float.parseFloat(str1);
                    String str2 = c2.replaceAll(VOLUME_REMOVE_PATTERN, " ");
                    str2 = str2.replaceAll(STRING_END_PATTERN, " ");
                    str2 = ServerBase.getFirstMatch(FLOAT_PATTERN, str2, "");
                    float f2 = Float.parseFloat(str2);
                return  (int)Math.floor(f1-f2);
            } catch (Exception e) {
                return  0;
            }
        }
    };
}