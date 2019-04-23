package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

public class FromCBZ extends FromFolder {
    FromCBZ(Context context) {
        super(context);
        setIcon(R.drawable.from_folder);
        setServerName("FromCBZ");
        setServerID(FROMCBZ);
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
        File dir = new File(manga.getPath());
        ArrayList<Chapter> chapters = new ArrayList<>();
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (child.getName().toLowerCase().endsWith("cbz") || child.getName().toLowerCase().endsWith("zip")) {
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
        return "zip|" + chapter.getPath() + "|" + chapter.getExtra().split("\\|")[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        ZipFile zipFile = new ZipFile(chapter.getPath());
        ArrayList<String> files = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            files.add(zipEntry.getName());
        }
        zipFile.close();
        Collections.sort(files);
        StringBuilder sb = new StringBuilder(chapter.getPath());
        int pages = 0;
        for (String f : files) {
            if (f.toLowerCase().endsWith("jpg")
                    || f.toLowerCase().endsWith("png")
                    || f.toLowerCase().endsWith("bmp")) {
                sb.append("|").append(f);
                pages++;
            }
        }
        chapter.setPages(pages);
        chapter.setExtra(sb.toString());
        chapter.setDownloaded(true);
    }

    @Override
    public boolean hasList() {
        return false;
    }
}
