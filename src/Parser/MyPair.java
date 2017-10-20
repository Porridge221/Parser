package Parser;

/**
 * Created by ivano on 15.07.2017.
 */
public class MyPair {

    private String key;
    private Double value;

    MyPair(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
