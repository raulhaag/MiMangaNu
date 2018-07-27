package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

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

    private static final String FLOAT_PATTERN = "([.,0123456789]+)";
    private static final String STRING_END_PATTERN = "[^\\d]\\.";
    private static final String VOLUME_REMOVE_PATTERN = "[v|V][o|O][l|L]\\.?\\s*\\d+";
    private static Comparator<String> NUMBERS_ASC = new Comparator<String>() {
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
                return (int) Math.floor(f1 - f2);
            } catch (Exception e) {
                return 0;
            }
        }
    };

    FromFolder(Context context) {
        super(context);
        setFlag(R.drawable.noimage);
        setIcon(R.drawable.from_folder);
        setServerName("FromFolder");
        setServerID(FROMFOLDER);
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
        ArrayList<String> folders = Util.getInstance().dirList(manga.getPath());
        ArrayList<Chapter> chapters = new ArrayList<>();
        folders.remove(0);//remove "."
        for (String folder : folders) {
            Chapter chapter = new Chapter(folder, manga.getPath() + folder + "/");
            chapter.setDownloaded(true);
            chapters.add(chapter);
        }
        Chapter.Comparators.setManga_title(manga.getTitle());
        Collections.sort(chapters, Chapter.Comparators.NUMBERS_ASC);
        manga.setChapters(chapters);
        manga.setAuthor(context.getString(R.string.nodisponible));
        manga.setGenre(context.getString(R.string.nodisponible));
        manga.setSynopsis(context.getString(R.string.nodisponible));
        manga.setFinished(true);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload);
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        assert chapter.getExtra() != null;
        return chapter.getPath() + chapter.getExtra().split("\\|")[page - 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if(chapter.getPages() == 0) {
            ArrayList<String> images = Util.getInstance().imageList(chapter.getPath());
            Collections.sort(images, NUMBERS_ASC);
            chapter.setExtra(TextUtils.join("|", images));
            chapter.setPages(images.size());
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
