package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2017/7/26.
 */
public class MultiLayerIndexMap extends HashMap {

    private Map<String, List<String>> advanceTranslateMap;

    private Map<String, Integer> indexMap;

    public MultiLayerIndexMap(Map<String, List<String>> advanceTranslateMap, Map<String, Integer> indexMap) {
        this.advanceTranslateMap = advanceTranslateMap;
        this.indexMap = indexMap;
    }

    public MultiLayerIndexMap(Map<String, Integer> indexMap) {
        this.indexMap = indexMap;
    }

    @Override
    public List<Integer> get(Object key) {
        if (key == null ||
                String.valueOf(key).trim().equals("")) {
            return null;
        }
        final String oriWord = String.valueOf(key);
        List<String> cellwordList;
        if (advanceTranslateMap != null) {
            cellwordList = advanceTranslateMap.get(oriWord);
        } else {
            cellwordList = new ArrayList<String>() {{
                add(oriWord);
            }};
        }

        List<Integer> indexList = new ArrayList<>();
        for (String cell : cellwordList) {
            Integer ind = indexMap.get(cell);
            if (ind != null) {
                indexList.add(ind);
            }
        }
        return indexList;
    }

    public void store(String path) {
        // store dict
        FileSteward.storeDict(indexMap, path);
    }
}
