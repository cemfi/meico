package meico.supplementary;

import java.util.Map;

/**
 * A helper class for key value pairings, used in class AccentuationPatternDef, and all the ...Map classes.
 * Source: https://stackoverflow.com/questions/2973041/a-keyvaluepair-in-java
 * @param <K>
 * @param <V>
 */
public class KeyValue<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public K setKey(K key) {
        return this.key = key;
    }

    public V setValue(V value) {
        return this.value = value;
    }
}

