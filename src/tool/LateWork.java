package tool;

import java.io.File;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/3.
 */
public class LateWork {

    private List<Ware4LateWork> wareList;

    private String prefixPath;

    public LateWork(String path) { // oriVsCur.txt
        prefixPath = path.replace("oriFix.txt", "");
        if (wareList == null) {
            initWareList(path);
        }
    }

    private void initWareList(String path) {
        wareList = FileSteward.getWare4LateWorkList(path);
    }

    public Map<Integer, Integer> getPopularTerms(long leafCateId, long topicId) {
        Map<Integer, Long> wareIdList = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getLeafCateId() == leafCateId &&
                    ware.getTopicId() == topicId) {
                wareIdList.put(i, ware.getWareId());
            }
            i++;
        }
        return getPopularTermsByWareIdMap(wareIdList);
    }

    public Map<Integer, Integer> getPopularTermsByWareIdList(List<Long> wareIdList) {
        Map<Integer, Long> wareIdMap = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (wareIdList.indexOf(ware.getWareId()) != -1) {
                wareIdMap.put(i, ware.getWareId());
            }
            i++;
        }
        return getPopularTermsByWareIdMap(wareIdMap);
    }

    public Map<String, Double> getPopularTermsNameByWareIdList(List<Long> wareIdList) {
        Map<Integer, Long> wareIdMap = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (wareIdList.indexOf(ware.getWareId()) != -1) {
                wareIdMap.put(i, ware.getWareId());
            }
            i++;
        }
        return getPopularTermsNameByWareIdMap(wareIdMap);
    }

    public Map<String, Double> getPopularTermsNameByWareIdMap(Map<Integer, Long> wareIdMap) {
        Map<Integer, Integer> termCountMap = getPopularTermsByWareIdMap(wareIdMap);
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        Map<String, Double> map = new HashMap<>();
        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<>(termCountMap.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : infoIds) {
            if (count < 10) {
                map.put(indexMap.get(entry.getKey()), entry.getValue() / (double) wareIdMap.size());
            }
            count++;
        }

        return map;
    }

    public Map<Integer, Integer> getPopularTermsByWareIdMap(Map<Integer, Long> wareIdMap) {
        Map<Integer, Integer> termCountMap = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        for (int ind : wareIdMap.keySet()) {
            Map<Integer, Integer> cellMap = countIndexMap.get(ind);
            for (int termIndex : cellMap.keySet()) {
                if (termCountMap.containsKey(termIndex)) {
                    termCountMap.put(termIndex, termCountMap.get(termIndex) + cellMap.get(termIndex));
                } else {
                    termCountMap.put(termIndex, cellMap.get(termIndex));
                }
            }
        }

        // output
        /*
        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<>(termCountMap.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue());
//                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });
        System.out.println("### size of ware list is: " + wareIdMap.size());
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : infoIds) {
            if (count < 30) {
                System.out.println(indexMap.get(entry.getKey()) + "\t" + entry.getValue());
            }
            count++;
        }
//        */

        return termCountMap;
    }

    public Map<Integer, Integer> getWareIndexComment(long wareId) {
        Map<Integer, Integer> termCountMap = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getWareId() == wareId) {
                break;
            }
            i++;
        }
        Map<Integer, Map<Integer, Integer>> countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        Map<Integer, Integer> cellMap = countIndexMap.get(i);
        for (int termIndex : cellMap.keySet()) {

            if (termCountMap.containsKey(termIndex)) {
                termCountMap.put(termIndex, termCountMap.get(termIndex) + cellMap.get(termIndex));
            } else {
                termCountMap.put(termIndex, cellMap.get(termIndex));
            }
        }

        for (Map.Entry<Integer, Integer> entry : termCountMap.entrySet()) {
            System.out.println(indexMap.get(entry.getKey()) + "\t" + entry.getValue());
        }
        return termCountMap;
    }

    public List<Ware4LateWork> getWareList() {
        return wareList;
    }

    public static void main(String[] args) {
        String str = "s      213    we 2  ";
//        str = str.toLowerCase();
        String[] strA = str.split("\\s+");
    }

    public static void picturePathReset(String path) {
        // mkdir for picture restore
        File fileRestore = new File(path + "\\picStore");
        fileRestore.mkdir();

        Set<Integer> topicIndexSet = new HashSet<>();
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("_", 2);
                if (!topicIndexSet.contains(Integer.parseInt(elements[0]))) { // if not exist, mkdir
                    topicIndexSet.add(Integer.parseInt(elements[0]));
                    fileRestore = new File(path + "\\picStore\\" + elements[0]);
                    fileRestore.mkdir();
                }
                // copy pic to targ dir
                FileSteward.copyFile(path + "\\" + oriFileName, path + "\\picStore\\" + elements[0] + "\\" + oriFileName);
            }
        }
    }
}
