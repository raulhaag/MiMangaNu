package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
                float f1 = Float.parseFloat(str1);
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
        if (new File(manga.getPath() + "cover.jpg").exists()) {
            manga.setImages(manga.getPath() + "cover.jpg");
        }
        ArrayList<String> folders = Util.getInstance().dirList(manga.getPath());
        ArrayList<Chapter> chapters = new ArrayList<>();
        folders.remove(0);//remove "."
        for (String folder : folders) {
            Chapter chapter = new Chapter(folder, manga.getPath() + folder + "/");
            chapter.setDownloaded(true);
            chapters.add(chapter);
        }
        File dir = new File(manga.getPath());
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (child.getName().toLowerCase().endsWith("cbz") ||
                        // child.getName().toLowerCase().endsWith("cbr") ||
                        // child.getName().toLowerCase().endsWith("rar") ||
                        child.getName().toLowerCase().endsWith("zip")) {
                    Chapter chapter = new Chapter(child.getName(), child.getAbsolutePath());
                    chapter.setDownloaded(true);
                    chapters.add(chapter);
                }
            }
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
        if (chapter.getExtra().startsWith("zip")) {
            return "zip|" + chapter.getPath() + "|" + chapter.getExtra().split("\\|")[page];
        }/* else if (chapter.getExtra().startsWith("rar")) {
            return "rar|" + chapter.getPath() + "|" + chapter.getExtra().split("\\|")[page];
        }*/
        return chapter.getPath() + chapter.getExtra().split("\\|")[page - 1];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            ArrayList<String> images = Util.getInstance().imageList(chapter.getPath());
            if (!images.isEmpty()) {
                Collections.sort(images, NUMBERS_ASC);
                chapter.setExtra(TextUtils.join("|", images));
                chapter.setPages(images.size());
                return;
            }
            ArrayList<String> files = new ArrayList<>();
            String id = "rar";
            if (chapter.getPath().toLowerCase().endsWith(".zip") || chapter.getPath().toLowerCase().endsWith(".cbz")) {
                id = "zip";
                ZipFile zipFile = new ZipFile(chapter.getPath());
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    files.add(zipEntry.getName());
                }
                zipFile.close();
            } /*else if (chapter.getPath().toLowerCase().endsWith(".rar") || chapter.getPath().toLowerCase().endsWith(".cbr")) {
                Archive rar = new Archive(new FileInputStream(chapter.getPath()));
                FileHeader fh;
                while (null != (fh = rar.nextFileHeader())) {
                    files.add(fh.getFileNameString());
                }
            }*/
            Collections.sort(files);
            StringBuilder sb = new StringBuilder(id);
            int pages = 0;
            for (String f : files) {
                if (f.toLowerCase().endsWith(".jpg")
                        || f.toLowerCase().endsWith(".jpeg")
                        || f.toLowerCase().endsWith(".png")
                        || f.toLowerCase().endsWith(".bmp")) {
                    sb.append("|").append(f);
                    pages++;
                }
            }
            chapter.setPages(pages);
            chapter.setExtra(sb.toString());
            chapter.setDownloaded(true);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
