package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import ar.rulosoft.mimanganu.R;

public class FanFoxNet extends MTownBase {
    private static final int[] fltGenre = {
            R.string.flt_tag_action,
            R.string.flt_tag_adventure,
            R.string.flt_tag_comedy,
            R.string.flt_tag_drama,
            R.string.flt_tag_fantasy,
            R.string.flt_tag_martial_arts,
            R.string.flt_tag_shounen,
            R.string.flt_tag_horror,
            R.string.flt_tag_supernatural,
            R.string.flt_tag_harem,
            R.string.flt_tag_psychological,
            R.string.flt_tag_romance,
            R.string.flt_tag_school_life,
            R.string.flt_tag_shoujo,
            R.string.flt_tag_mystery,
            R.string.flt_tag_sci_fi,
            R.string.flt_tag_seinen,
            R.string.flt_tag_tragedy,
            R.string.flt_tag_ecchi,
            R.string.flt_tag_sports,
            R.string.flt_tag_slice_of_life,
            R.string.flt_tag_mature,
            R.string.flt_tag_shoujo_ai,
            R.string.flt_tag_webtoon,
            R.string.flt_tag_doujinshi,
            R.string.flt_tag_one_shot,
            R.string.flt_tag_smut,
            R.string.flt_tag_yaoi,
            R.string.flt_tag_josei,
            R.string.flt_tag_historical,
            R.string.flt_tag_shounen_ai,
            R.string.flt_tag_gender_bender,
            R.string.flt_tag_adult,
            R.string.flt_tag_yuri,
            R.string.flt_tag_mecha,
            R.string.flt_tag_lolicon,
            R.string.flt_tag_shotacon
    };

    private static boolean cookieInit = false;

    /**
     * Construct a new ServerBase object.
     *
     * @param context the context for this object
     */
    FanFoxNet(Context context) {
        super(context);
        setFlag(R.drawable.flag_en);
        setIcon(R.drawable.mangafox_icon);
        setServerName("FanFox");
        setServerID(FANFOXNET);
    }

    @Override
    public String getHost() {
        return "https://fanfox.net";
    }

    @Override
    public String getDomain() {
        return "fanfox.net";
    }

    @Override
    public int[] getFilter() {
        return fltGenre;
    }

    @Override
    public boolean getCookieInit() {
        return cookieInit;
    }

    @Override
    public void setCookieInit(boolean state) {
        cookieInit = state;
    }

}
