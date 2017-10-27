package actor;

import tool.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/9/21.
 */
public class CellTermGatherActor {

    private static LateWork lateWork;
    private static String writeFile;

    public static void main(String[] args) {
        DocLdaActor.init(75061316);
        List<Long> list = new ArrayList<Long>(){{
            add(75061319L);
            add(75061320L);
            add(75061321L);
            add(75061322L);
            add(75061325L);
            add(75061383L);
            add(75061384L);
            add(75061385L);
            add(75061330L);
            add(75061331L);
            add(75061340L);
            add(75061333L);
            add(75061334L);
            add(75061335L);
            add(75061336L);
            add(75061337L);
            add(75061360L);
            add(75061361L);
            add(75061374L);
            add(75061380L);
            add(75061394L);
            add(75062014L);
            add(75061339L);
            add(75061366L);
            add(75061378L);
            add(75061387L);
            add(75061401L);
            add(75061402L);
            add(75061403L);
            add(75061404L);
            add(75061407L);
            add(75061419L);
            add(75061342L);
            add(75061405L);
            add(75061612L);
            add(75061613L);
            add(75061617L);
            add(75061867L);
            add(75061916L);
            add(75061986L);
            add(75061348L);
            add(75061362L);
            add(75061375L);
            add(75061350L);
            add(75061351L);
            add(75061373L);
            add(75061511L);
            add(75061355L);
            add(75061371L);
            add(75061372L);
            add(75061377L);
            add(75061391L);
            add(75061392L);
            add(75061399L);
            add(75061614L);
            add(75061354L);
            add(75061396L);
            add(75061586L);
            add(75061619L);
            add(75061620L);
            add(75061359L);
            add(75061365L);
            add(75061367L);
            add(75061368L);
            add(75061376L);
            add(75061379L);
            add(75061381L);
            add(75061382L);
            add(75061386L);
            add(75061388L);
            add(75061410L);
            add(75061492L);
            add(75061390L);
            add(75061397L);
            add(75061398L);
            add(75061605L);
            add(75061618L);
            add(75061621L);
            add(75061973L);
            add(75061486L);
            add(75061487L);
            add(75061488L);
            add(75061489L);
            add(75061490L);
            add(75061493L);
        }};

        for (long leafCateId : list) {
            mainLeafCate(leafCateId);
            System.out.println("leaf cate id " + leafCateId + " finished. ");
        }
    }

    private static void mainLeafCate(long targLeafCateId) {
        writeFile = DocLdaActor.wkbt_file.replace("wkbt.txt", "cellTermCollector_" + targLeafCateId  + ".txt");

        // read file oriFix.txt, we get ware set under leaf_cate XXX
        lateWork = new LateWork(DocLdaActor.ori_fix_file);
        List<Ware4LateWork> ware4OriFixList = lateWork.getWareList();

        Set<Long> wareIdSet = new HashSet<>();
        Map<Long, List<Long>> collectMap = new HashMap<>();
        for (Ware4LateWork ware : ware4OriFixList) {
            if (ware.getLeafCateId() == targLeafCateId) {
                wareIdSet.add(ware.getWareId());
                if (!collectMap.containsKey(ware.getTopicId())) {
                    collectMap.put(ware.getTopicId(), new ArrayList<Long>());
                }
                collectMap.get(ware.getTopicId()).add(ware.getWareId());
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

        FileSteward.delete(writeFile);

        for (long topicId : collectMap.keySet()) {
            List<Long> wareIdList = collectMap.get(topicId);
            Map<Long, WareMsgTranslate> subTransMap = new HashMap<>();
            for (long wareId : wareIdList) {
                subTransMap.put(wareId, transMap.get(wareId));
            }
            Map<String, Integer> termSubCollectMap = dealTokenize(subTransMap);
            resultWriter(termSubCollectMap, topicId);
        }

    }

    private static Map<String, Integer> dealTokenize(Map<Long, WareMsgTranslate> transMap) {
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
            if (title != null &&
                    !title.trim().isEmpty()) {
                String[] titleTerms = title.split(" ");
                for (String term : titleTerms) {
                    String termToken = Tokenizer.token(term);
                    putOrAdd2Map(termCollectMap, termToken);
                }
            }
        }
        return termCollectMap;
    }

    private static void resultWriter(Map<String, Integer> termCollectMap, long topicId) {
        // before output term map, show term attribute
        Iterator<Map.Entry<String, Integer>> it = termCollectMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> term = it.next();
            if (term.getValue() < 0.01) {
                it.remove();
            }
        }
        List<Map.Entry<String, Integer>> infos = new ArrayList<>(termCollectMap.entrySet());
        Collections.sort(infos, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

//        String writeFile = DocLdaActor.wkbt_file.replace("wkbt.txt", "cellTermCollector.txt");
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(writeFile, true);
            bw = new BufferedWriter(fw);

            bw.write("\n### topicId: " + topicId + "\n");

            for (Map.Entry<String, Integer> entry : infos) {
                String term = entry.getKey();
                if (!term.contains(" ")) {
                    term = lateWork.standfordTagger(term);
                }
                double ratio = entry.getValue() / (double) termCollectMap.size();
                if (ratio < 0.01) {
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
