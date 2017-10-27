package actor;

import tool.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/9/21.
 */
public class TermGatherActor {

    public static void main(String[] args) {
        DocLdaActor.init(75061316);

        long targLeafCateId = 75061359;

        // read file oriFix.txt, we get ware set under leaf_cate XXX
        LateWork lateWork = new LateWork(DocLdaActor.ori_fix_file);
        List<Ware4LateWork> ware4OriFixList = lateWork.getWareList();
        Set<Long> wareIdSet = new HashSet<>();
        for (Ware4LateWork ware : ware4OriFixList) {
            if (ware.getLeafCateId() == targLeafCateId) {
                wareIdSet.add(ware.getWareId());
            }
        }

        // read file transWare.txt, get describe from it
        List<WareMsgTranslate> translateWareList = FileSteward.getTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transWare.txt"));
        Map<Long, WareMsgTranslate> transMap = new HashMap<>();
        for (WareMsgTranslate ware : translateWareList) {
            if (wareIdSet.contains(ware.getWareId())) {
                transMap.put(ware.getWareId(), ware);
            }
        }

        // read file normalize.txt, get keyword & title msg from it
        List<WareMsg> wareNormalizeList = FileSteward.getWareMsgFromNormalize(DocLdaActor.wkbt_file.replace("wkbt.txt", "normalize.txt"));
        for (WareMsg wareMsg : wareNormalizeList) {
            long wareId = wareMsg.getWareId();
            if (transMap.containsKey(wareId)) {
                transMap.get(wareId).setKeywords(wareMsg.getKeywords());
                transMap.get(wareId).setTitle(wareMsg.getTitle());
            }
        }

        Map<String, Integer> termCollectMap = new HashMap<>();
        for (long wareId : transMap.keySet()) {
            // deal with keyword, we take both pharse and term into consider
            String keywords = transMap.get(wareId).getKeywords();
            if (!keywords.trim().isEmpty()) {
                String[] keywordEles = keywords.split(",");
                for (String ele : keywordEles) {
                    String eleToken = Tokenizer.token(ele); // rooten
                    putOrAdd2Map(termCollectMap, eleToken);
                    if (eleToken.trim().contains(" ")) {
                        for (String term : ele.split("\\s+")) {
                            putOrAdd2Map(termCollectMap, term);
                        }
                    }
                }
            }
            // deal with title
            String title = transMap.get(wareId).getTitle();
            if (!title.trim().isEmpty()) {
                String[] titleTerms = title.split(" ");
                for (String term : titleTerms) {
                    String termToken = Tokenizer.token(term);
                    putOrAdd2Map(termCollectMap, termToken);
                }
            }
        }

        // before output term map, show term attribute
        List<Map.Entry<String, Integer>> infos = new ArrayList<>(termCollectMap.entrySet());
        Collections.sort(infos, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        String writeFile = DocLdaActor.wkbt_file.replace("wkbt.txt", "termCollector.txt");
        FileSteward.delete(writeFile);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(writeFile);
            bw = new BufferedWriter(fw);

            for (Map.Entry<String, Integer> entry : infos) {
                String term = entry.getKey();
                if (!term.contains(" ")) {
                    term = lateWork.standfordTagger(term);
                }
                double ratio = entry.getValue() / (double) termCollectMap.size();
                if (ratio < 0.001) {
                    break;
                }
                bw.write(term + "\t" + ratio + "\n");
            }

            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void putOrAdd2Map(Map<String, Integer> map, String key) {
        key = key.trim();
        if (map == null) {
            return;
        }
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

}
