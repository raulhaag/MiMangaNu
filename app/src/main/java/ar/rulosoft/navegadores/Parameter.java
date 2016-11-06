package ar.rulosoft.navegadores;

/**
 * Created by Raul on 01/11/2016.
 */

public class Parameter {
    String key;
    String value;

    public Parameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
