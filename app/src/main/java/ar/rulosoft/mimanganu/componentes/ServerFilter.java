package ar.rulosoft.mimanganu.componentes;

/**
 * Created by Raul on 29/10/2016.
 */

public class ServerFilter {
    private String[] options;
    private FilterType filterType;
    private String title;

    public ServerFilter(String title, String[] options, FilterType filterType) {
        this.title = title;
        this.options = options;
        this.filterType = filterType;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public String getTitle() {
        return title;
    }

    public String[] getOptions() {
        return options;
    }

    public enum FilterType {MULTI, SINGLE, MULTI_STATES}
}
