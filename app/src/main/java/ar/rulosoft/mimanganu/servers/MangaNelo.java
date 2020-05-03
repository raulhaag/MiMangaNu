package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

public class MangaNelo extends ServerBase {
	private static final String HOST = "https://manganelo.com/";
	
	private static final String PATTERN_COVER =
			"info-image\">\\s*<img[^>]+src=\"([^\"]+?mkklcdnv[^\"]+?)\"[^>]+alt";
	private static final String PATTERN_SUMMARY =
			"Description :</h3>(.+?)</div";
	private static final String PATTERN_COMPLETED =
			"table-value\">Completed";
	private static final String PATTERN_AUTHOR =
			"Author\\(s\\) :</td>\\s*<td[^>]*>(.*?)</td";
	private static final String PATTERN_GENRE =
			"Genres :</td>\\s*<td[^>]*>(.+?)</td";
	private static final String PATTERN_CHAPTER =
			"<li[^>]*>\\s*<a[^>]+[^>]+href=\"([^\"]+?chapter[^\"]+?)\"[^>]+?>(.+?)</a>";
	private static final String PATTERN_IMAGES =
			"<img src=\"([^\"]+?mkklcdnv[^\"]+?chapter[^\"]+?)\"";
	
	private static final String PATTERN_MANGA_SEARCH =
			"search-story-item\">\\s*<a[^>]*href=\"https?://manganelo.com/([^>]+)\"\\s*title=\"([^>]+)\"\\s*>\\s*<img[^>]*?src=\"([^>]*mkkl[^>]*?)\" one";
	private static final String PATTERN_MANGA_LIST =
			"content-genres-item\">\\s*<a[^>]*href=\"https?://manganelo.com/([^>]+)\"\\s*title=\"([^>]+)\"\\s*>\\s*<img[^>]*?src=\"([^>]*mkkl[^>]*?)\" one";
	
	
	
	// filter by status
	private static int[] fltStatus = {
			R.string.flt_status_all,
			R.string.flt_status_completed,
			R.string.flt_status_ongoing,
	};
	private static String[] valStatus = {
			"all", "completed", "ongoing"
	};
	
	// filter by genre
	private static int[] fltGenre = {
			R.string.flt_tag_all,
			R.string.flt_tag_action,
			R.string.flt_tag_adult,
			R.string.flt_tag_adventure,
			R.string.flt_tag_comedy,
			R.string.flt_tag_cooking,
			R.string.flt_tag_doujinshi,
			R.string.flt_tag_drama,
			R.string.flt_tag_ecchi,
			R.string.flt_tag_fantasy,
			R.string.flt_tag_gender_bender,
			R.string.flt_tag_harem,
			R.string.flt_tag_historical,
			R.string.flt_tag_horror,
			R.string.flt_tag_josei,
			R.string.flt_tag_manhua,
			R.string.flt_tag_manhwa,
			R.string.flt_tag_martial_arts,
			R.string.flt_tag_mature,
			R.string.flt_tag_mecha,
			R.string.flt_tag_medical,
			R.string.flt_tag_mystery,
			R.string.flt_tag_one_shot,
			R.string.flt_tag_psychological,
			R.string.flt_tag_romance,
			R.string.flt_tag_school_life,
			R.string.flt_tag_sci_fi,
			R.string.flt_tag_seinen,
			R.string.flt_tag_shoujo,
			R.string.flt_tag_shoujo_ai,
			R.string.flt_tag_shounen,
			R.string.flt_tag_shounen_ai,
			R.string.flt_tag_slice_of_life,
			R.string.flt_tag_smut,
			R.string.flt_tag_sports,
			R.string.flt_tag_supernatural,
			R.string.flt_tag_tragedy,
			R.string.flt_tag_webtoon,
			R.string.flt_tag_yaoi,
			R.string.flt_tag_yuri
	};
	private static String[] valGenre = {
			"all", "2", "3", "4", "6", "7", "9", "10", "11", "12", "13", "14", "15", "16", "45", "17", "44", "43", "19", "20", "21", "22", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42"
	};
	
	// filter by order
	private static int[] fltOrder = {
			R.string.flt_order_views,
			R.string.flt_order_newest,
			R.string.flt_order_last_update
	};
	private static String[] valOrder = {
			"topview", "newest", "latest"
	};
	
	
	MangaNelo(Context context) {
		super(context);
		setFlag(R.drawable.flag_en);
		setIcon(R.drawable.manganelo);
		setServerName("Manganelo (Mangakakalot)");
		setServerID(MANGANELO);
	}
	
	@Override
	public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
		loadChapters(manga, forceReload);
	}
	
	@Override
	public void loadChapters(Manga manga, boolean forceReload) throws Exception {
		if (manga.getChapters().isEmpty() || forceReload) {
			String data = getNavigatorAndFlushParameters().get(manga.getPath());
			
			// cover image
			manga.setImages(getFirstMatchDefault(PATTERN_COVER, data, ""));
			// summary
			manga.setSynopsis(getFirstMatchDefault(PATTERN_SUMMARY, data,
					context.getString(R.string.nodisponible)));
			// ongoing or completed
			manga.setFinished(data.contains(PATTERN_COMPLETED));
			// author
			manga.setAuthor(getFirstMatchDefault(PATTERN_AUTHOR, data,
					context.getString(R.string.nodisponible)));
			// genre
			manga.setGenre(getFirstMatchDefault(PATTERN_GENRE, data, context.getString(R.string.nodisponible)));
			String message = getFirstMatchDefault("<div style=\"text-align: center;\">(The series .+? available in MangaTown)", data, "");
			if (!"".equals(message)) {
				Util.getInstance().toast(context, message, Toast.LENGTH_LONG);
				return;
			}
			// chapter
			Pattern p = Pattern.compile(PATTERN_CHAPTER, Pattern.DOTALL);
			Matcher m = p.matcher(data);
			while (m.find()) {
				manga.addChapterFirst(new Chapter(m.group(2), m.group(1)));
			}
		}
	}
	
	@Override
	public String getImageFrom(Chapter chapter, int page) throws Exception {
		assert chapter.getExtra() != null;
		return chapter.getExtra().split("\\|")[page - 1];
	}
	
	@Override
	public void chapterInit(Chapter chapter) throws Exception {
		if (chapter.getPages() == 0) {
			String data = getNavigatorAndFlushParameters().get(chapter.getPath());
			
			ArrayList<String> page_links = getAllMatch(PATTERN_IMAGES, data);
			if (page_links.isEmpty()) {
				throw new Exception(context.getString(R.string.server_failed_loading_page_count));
			}
			
			chapter.setExtra(TextUtils.join("|", page_links));
			chapter.setPages(page_links.size());
		}
	}
	
	@Override
	public boolean hasList() {
		return false;
	}
	
	@Override
	public ArrayList<Manga> getMangas() throws Exception {
		return null;
	}
	
	@Override
	public ArrayList<Manga> search(String term) throws Exception {
		ArrayList<Manga> mangas = new ArrayList<>();
		
		String data = getNavigatorAndFlushParameters().get(
				HOST + "search/" + Pattern.compile("[^\\d\\p{L}]",Pattern.UNICODE_CASE).matcher(term).replaceAll("_"));
		
		
		Pattern p = Pattern.compile(PATTERN_MANGA_SEARCH, Pattern.DOTALL);
		Matcher m = p.matcher(data);
		while (m.find()) {
			mangas.add(new Manga(getServerID(), m.group(2), HOST + m.group(1), m.group(3)));
		}
		return mangas;
	}
	
	
	
	@Override
	public boolean hasFilteredNavigation() {
		return true;
	}
	
	@Override
	public ServerFilter[] getServerFilters() {
		return new ServerFilter[]{
				new ServerFilter(
						context.getString(R.string.flt_status),
						buildTranslatedStringArray(fltStatus), ServerFilter.FilterType.SINGLE),
				new ServerFilter(
						context.getString(R.string.flt_genre),
						buildTranslatedStringArray(fltGenre), ServerFilter.FilterType.SINGLE),
				new ServerFilter(
						context.getString(R.string.flt_order),
						buildTranslatedStringArray(fltOrder), ServerFilter.FilterType.SINGLE)
		};
	}
	
	@Override
	public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
		ArrayList<Manga> mangas = new ArrayList<>();
		String query = String.format(
//				"manga_list?type=%s&category=%s&state=%s&page=%s",
				"genre-%s/%s?type=%s&state=%s",
				valGenre[filters[1][0]],
				pageNumber,
				valOrder[filters[2][0]],
				valStatus[filters[0][0]]
		);
		
		String data = getNavigatorAndFlushParameters().get(
				HOST + query);
		Pattern p = Pattern.compile(PATTERN_MANGA_LIST, Pattern.DOTALL);
		Matcher m = p.matcher(data);
		while (m.find()) {
			mangas.add(new Manga(getServerID(), m.group(2), HOST + m.group(1), m.group(3)));
		}
		return mangas;
	}
}
