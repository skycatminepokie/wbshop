package com.skycat.wbshop;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {
    /**
     * Create a Codec to store HashMaps as a List of Pairs.
     *
     * @param <K>            The key type
     * @param <V>            The value type
     * @param keyCodec       The codec for the key type
     * @param keyFieldName   The name to use for the first field
     * @param valueCodec     The codec for the value type
     * @param valueFieldName The name to use for the second field
     * @return A new codec
     */
    public static <K, V> Codec<HashMap<K, V>> hashMapCodec(Codec<K> keyCodec, String keyFieldName, Codec<V> valueCodec, String valueFieldName) { // This and all related methods are from my project mystical. Should be moved to an api sometime.
        return Utils.pairCodec(keyCodec, keyFieldName, valueCodec, valueFieldName).listOf().xmap(Utils::pairsToMap, Utils::mapToPairs);
    }

    public static boolean log(String msg) {
        return log(msg, LogLevel.INFO, WBShop.LOGGER);
    }

    public static boolean log(String msg, Logger logger) {
        return log(msg, LogLevel.INFO, logger);
    }

    public static boolean log(String msg, LogLevel level) {
        return log(msg, level, WBShop.LOGGER);
    }

    public static boolean log(String msg, LogLevel level, Logger logger) { // This and all overloads come from Mystical
        switch (level) {
            case INFO:
                logger.info(msg);
                return logger.isInfoEnabled();
            case DEBUG:
                logger.debug(msg);
                return logger.isDebugEnabled();
            case WARN:
                logger.warn(msg);
                return logger.isWarnEnabled();
            case ERROR:
                logger.error(msg);
                return logger.isErrorEnabled();
            default:
                return false; // case OFF
        }
    }

    public static <K, V> Pair<K, V> mapEntryToPair(Map.Entry<K, V> entry) {
        return Pair.of(entry.getKey(), entry.getValue());
    }

    public static <K, V> LinkedList<Pair<K, V>> mapToPairs(Map<K, V> map) {
        LinkedList<Pair<K, V>> list = new LinkedList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            list.add(mapEntryToPair(entry));
        }
        return list;
    }

    public static <F, S> Codec<Pair<F, S>> pairCodec(Codec<F> firstCodec, String firstFieldName, Codec<S> secondCodec, String secondFieldName) {
        return Codec.pair(firstCodec.fieldOf(firstFieldName).codec(), secondCodec.fieldOf(secondFieldName).codec());
    }

    private static <K, V> HashMap<K, V> pairsToMap(List<Pair<K, V>> pairs) {
        HashMap<K, V> map = new HashMap<>(/*pairs.size()*/);
        for (Pair<K, V> pair : pairs) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }
}
