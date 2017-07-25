package tool;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/4.
 */
public class MapSimilaritySteward {

    public static double getSimilarityScore(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        double max1 = getMaxValue(map1);
        double max2 = getMaxValue(map2);
        // real map
        Map<Integer, Double> map = new HashMap<>();
        // ideal worse map
        Map<Integer, Double> iMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : map1.entrySet()) {
            map.put(entry.getKey(), entry.getValue() / max1);
            iMap.put(entry.getKey(), entry.getValue() / max1);
        }
        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
            if (map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), map.get(entry.getKey()) - entry.getValue() / max2);
            } else {
                map.put(entry.getKey(), - entry.getValue() / max2);
            }
            iMap.put(-entry.getKey(), entry.getValue() / max2);
        }

        double realScore = getScore(map);
        double iScore = getScore(iMap);

        return 1 - realScore / iScore;
    }

    public static double getMaxValue(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<>(map.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue());
//                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });
        return infoIds.get(0).getValue();
    }

    public static double getScore(Map<Integer, Double> map) {
        double score = 0;
        for (int key : map.keySet()) {
            score += map.get(key) * map.get(key);
        }
        return score;
    }
}
