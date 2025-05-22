package org.eldrygo.XMusic.Utils;

import java.util.Map;
import java.util.Objects;

public class OtherUtils {
    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        return null; // No se encontró la clave con ese valor
    }
}
