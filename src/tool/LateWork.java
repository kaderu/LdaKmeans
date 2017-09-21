package tool;

import actor.DocLdaActor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.*;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/3.
 */
public class LateWork {

    private List<Ware4LateWork> wareList;

    private String prefixPath;
    Map<Integer, Map<Integer, Integer>> countIndexMap;
    Map<Integer, String> indexMap;
    MaxentTagger tagger;

    public LateWork(String path) { // oriVsCur.txt
        prefixPath = path.replace("oriFix.txt", "");
        if (wareList == null) {
            initWareList(path);
        }
        countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
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

        // 2017.9.19
        // for manual check, we need tell how each record fit the popular term list.
        // write fitDegree.txt
        Set<String> popularNunTermSet = getNunTermList(map.keySet());

        String file = DocLdaActor.prefix_path + "pic_" + DocLdaActor.categoryId + "\\" + "fitDegree.txt";
        Map<Integer, Long> tmpMap = new TreeMap<>(wareIdMap);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            for (int key : tmpMap.keySet()) {
                Map<Integer, Integer> cellMap = countIndexMap.get(key);
                List<Map.Entry<Integer, Integer>> infos = new ArrayList<>(cellMap.entrySet());
                Collections.sort(infos, new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o2.getValue() - o1.getValue();
                    }
                });
                int num = 0;
                String terms = "";
                for (int index : cellMap.keySet()) {
                    if (popularNunTermSet.contains(indexMap.get(index))) {
                        terms = terms + indexMap.get(index) + ",";
                        num++;
                    }
                }
                bw.write(tmpMap.get(key) + "\t" + num + "\t" + terms + "\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<Integer, Integer> getPopularTermsByWareIdMap(Map<Integer, Long> wareIdMap) {
        Map<Integer, Integer> termCountMap = new HashMap<>();
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

    // work for LateWorkActor check output
    public Map<Integer, Integer> getWareIndexComment(long wareId) {
        Map<Integer, Integer> termCountMap = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getWareId() == wareId) {
                break;
            }
            i++;
        }
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


    // get nun terms for easier manual check
    public Set<String> getNunTermList(Set<String> termSet) {
        // if exist soap and liquid soap, remove soap
        Set<String> collectSet = new HashSet<>();
        Set<String> punishSet = new HashSet<>();
        for (String term : termSet) {
            for (String ele : term.split(" ")) {
                if (collectSet.contains(ele)) {
                    punishSet.add(ele);
                } else {
                    collectSet.add(ele);
                }
            }
        }
        termSet.removeAll(punishSet);

        Set<String> resultSet = new HashSet<>();
        for (String term : termSet) {
            if (isNun(term)) {
                resultSet.add(term);
            }
        }
        return resultSet;
    }

    public boolean isNun(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (String ele : str.split(" ")) {
            String tagged = standfordTagger(ele);
            if (tagged.trim().endsWith("_NN")) {
                return true;
            }
        }
        return false;
    }

    public String standfordTagger(String str) {
        if (tagger == null) {
            try {
                tagger = new MaxentTagger("lib/Stanford Tagger/english-left3words-distsim.tagger");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String tagged = tagger.tagString(str);
        return tagged;
    }

    public static void picturePathReset(String path) {
        // mkdir for picture restore
        File fileRestore = new File(path + "\\picStore_" + DocLdaActor.categoryId);
        fileRestore.mkdir();
        FileSteward.copyFile(path + "\\message.txt", path + "\\picStore_" + DocLdaActor.categoryId + "\\message.txt");
        FileSteward.copyFile(path + "\\fitDegree.txt", path + "\\picStore_" + DocLdaActor.categoryId + "\\fitDegree.txt");
        FileSteward.delete(path + "\\message.txt");
        FileSteward.delete(path + "\\fitDegree.txt");

        List<LateWorkWareMsgModel> modelList = FileSteward.getLateWorkWareMsgModleFromFitDegree(path + "\\picStore_" + DocLdaActor.categoryId + "\\fitDegree.txt");

        Map<Long, String> modelMap = new HashMap<>();
        for (LateWorkWareMsgModel ele : modelList) {
            StringBuffer termsBuffer = new StringBuffer();
            String[] arrays = ele.getNunTerms().split(",");
            int i = 0;
            for (String array : arrays) {
                if (i >= 2) {
                    break;
                }
                termsBuffer.append(array).append("-");
                i++;
            }
            if (termsBuffer.length() > 0) {
                modelMap.put(ele.getWareId(), termsBuffer.substring(0, termsBuffer.length() - 1));
            } else {
                modelMap.put(ele.getWareId(), "");
            }
        }

        Set<Integer> topicIndexSet = new HashSet<>();
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String[] elements = oriFile.getName().split("_");
                if (!topicIndexSet.contains(Integer.parseInt(elements[0]))) { // if not exist, mkdir
                    topicIndexSet.add(Integer.parseInt(elements[0]));
                    fileRestore = new File(path + "\\picStore_" +  + DocLdaActor.categoryId + "\\" + elements[0]);
                    fileRestore.mkdir();
                }
                // copy pic to targ dir
                long wareId = Long.parseLong(elements[elements.length - 1].split("\\.")[0]);
                String newFileName = oriFileName.replace(".", "_" + modelMap.get(wareId) + ".");
                FileSteward.copyFile(path + "\\" + oriFileName, path + "\\picStore_" +  + DocLdaActor.categoryId + "\\" + elements[0] + "\\" + newFileName);
            }
        }

        FileSteward.storeNunTermSummaryFromFitDegree(path + "\\picStore_" + DocLdaActor.categoryId + "\\fitDegree.txt", path + "\\picStore_" + DocLdaActor.categoryId + "\\cellNunSummary.txt");
    }

    public static void main(String[] args) {
        String str = "12_234.jpg";
        String newFileName = str.replaceAll("\\.", "www\\.");
    }
}
