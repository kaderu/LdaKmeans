package tool;

import actor.DocLdaActor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/4.
 */
public class MergeSteward {

    private LateWork lateWork;
    private double threshold;
    private int cellSizeThreshold;
    private String prefixPath;

    public MergeSteward(String path) {
        lateWork = new LateWork(path);
        prefixPath = path.replace("oriFix.txt", "");
        threshold = 0.6; // TODO
        cellSizeThreshold = 5;
    }

    public MergeSteward() {
        DocLdaActor.init();
        prefixPath = DocLdaActor.wkbt_file.replace(DocLdaActor.wkbt_file_name, "");
    }

    private List<CellCluster> getCellClusterList() {
        List<CellCluster> list = FileSteward.getCellClusterList(prefixPath + "cellClusterTree.txt");
        return list;
    }

    public List<CellCluster> getCellClusterNodeList() {
        List<CellCluster> list = FileSteward.getCellClusterList(prefixPath + "cellClusterNet.txt");

        Map<Integer, Set<CellCluster>> cateMergeMap = new HashMap<>();
        Map<Integer, Set<CellCluster>> topicMergeMap = new HashMap<>();
        for (CellCluster ele : list) {
            if (ele.getCateNode() > 0) {
                if (!cateMergeMap.containsKey(ele.getCateNode())) {
                    cateMergeMap.put(ele.getCateNode(), new HashSet<CellCluster>());
                }
                cateMergeMap.get(ele.getCateNode()).add(ele);
            }
            if (ele.getTopicNode() > 0) {
                if (!topicMergeMap.containsKey(ele.getTopicNode())) {
                    topicMergeMap.put(ele.getTopicNode(), new HashSet<CellCluster>());
                }
                topicMergeMap.get(ele.getTopicNode()).add(ele);
            }
        }

        Set<Set<CellCluster>> set = new HashSet<>();
        Set<CellCluster> subSet;
        for (CellCluster cell : list) {
            subSet = new HashSet<>();
            loopNode(cell, cateMergeMap, topicMergeMap, subSet);
//            System.out.println(subSet.size());
            set.add(subSet);
        }

        Map<Integer, Integer> cellClusterCntMap = new HashMap<>();
        for (Set<CellCluster> ele : set) {
            System.out.println(ele.size());
            for (CellCluster innerEle : ele) {
                if (!cellClusterCntMap.containsKey(innerEle.getId())) {
                    cellClusterCntMap.put(innerEle.getId(), 1);
                } else {
                    cellClusterCntMap.put(innerEle.getId(), cellClusterCntMap.get(innerEle.getId()) + 1);
                }
            }
        }
        Set<Set<CellCluster>> finalSet = new HashSet<>();
        for (int id : cellClusterCntMap.keySet()) {
            if (cellClusterCntMap.get(id) > 1) {
                Set<CellCluster> oriSet = null;
                Iterator<Set<CellCluster>> it = set.iterator();
                while (it.hasNext()) {
                    Set<CellCluster> curSet = it.next();
                    if (containsElement(curSet, id)) {
                        if (oriSet == null) {
                            oriSet = curSet;
                        } else {
                            oriSet.addAll(curSet);
                            it.remove();
                        }
                    }
                }
                finalSet.add(oriSet);
            }
        }
        finalSet.addAll(set);

        List<CellCluster> resultList = new ArrayList<>();
        int i = 1;
        for (Set<CellCluster> innerSet : finalSet) {
            for (CellCluster ele : innerSet) {
                ele.setDisplayNode(i);
                resultList.add(ele);
            }
            i++;
        }

        return resultList;
    }

    private boolean containsElement(Set<CellCluster> set, int id) {
        for (CellCluster ele : set) {
            if (ele.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void loopNode(CellCluster cell, Map<Integer, Set<CellCluster>> cateMergeMap, Map<Integer, Set<CellCluster>> topicMergeMap, Set<CellCluster> subSet) {
        if (subSet.contains(cell)) {
            return;
        }
        subSet.add(cell);
        if (cell.getCateNode() != 0) {
            for (CellCluster cell1 : cateMergeMap.get(cell.getCateNode())) {
                loopNode(cell1, cateMergeMap, topicMergeMap, subSet);
            }
        }
        if (cell.getTopicNode() != 0) {
            for (CellCluster cell2 : topicMergeMap.get(cell.getTopicNode())) {
                loopNode(cell2, cateMergeMap, topicMergeMap, subSet);
            }
        }
    }

    private Map<Integer, Integer> indexMapMaid(long leafCateId, long topicId) {
        return lateWork.getPopularTerms(leafCateId, topicId);
    }

    private Map<Integer, Integer> indexMapMaid(List<Long> wareIdList) {
        return lateWork.getPopularTermsByWareIdList(wareIdList);
    }

    private Map<String, Double> indexMapMaidWithName(List<Long> wareIdList) {
        return lateWork.getPopularTermsNameByWareIdList(wareIdList);
    }

    public double scoreMaid(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        return  MapSimilaritySteward.getSimilarityScore(map1, map2);
    }

    public void maidCaptain(String columnStr) {
        List<CellCluster> list = getCellClusterList();

        Map<Integer, List<Integer>> map = new TreeMap<>();
        CellCluster cluster1;
        CellCluster cluster2;
        Map<Integer, Integer> indexMap1;
        Map<Integer, Integer> indexMap2;
        double score;
        if ("cate".equals(columnStr)) {
            Map<Long, List<CellCluster>> leafCateMap = new HashMap<>();
            for (CellCluster cluster : list) {
//            if (cluster.getSize() < 5) { // drop pieces too small
//                continue;
//            }
                if (!leafCateMap.containsKey(cluster.getLeafCateId())) {
                    leafCateMap.put(cluster.getLeafCateId(), new ArrayList<CellCluster>());
                }
                leafCateMap.get(cluster.getLeafCateId()).add(cluster);
            }
            for (Map.Entry<Long, List<CellCluster>> entry : leafCateMap.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    for (int j = 0; j < i; j++) {
                        cluster1 = entry.getValue().get(i);
                        cluster2 = entry.getValue().get(j);
                        indexMap1 = indexMapMaid(cluster1.getLeafCateId(), cluster1.getTopicId());
                        indexMap2 = indexMapMaid(cluster2.getLeafCateId(), cluster2.getTopicId());
                        score = scoreMaid(indexMap1, indexMap2);
                        if (score >= threshold) {
                            if (!map.containsKey(cluster2.getId())) {
                                map.put(cluster2.getId(), new ArrayList<Integer>());
                            }
                            map.get(cluster2.getId()).add(cluster1.getId());
                        }
                    }
                }
            }
        } else if ("topic".equals(columnStr)) {
            Map<Integer, List<CellCluster>> topicMap = new HashMap<>();
            for (CellCluster cluster : list) {
                if (!topicMap.containsKey(cluster.getTopicId())) {
                    topicMap.put(cluster.getTopicId(), new ArrayList<CellCluster>());
                }
                topicMap.get(cluster.getTopicId()).add(cluster);
            }
            for (Map.Entry<Integer, List<CellCluster>> entry : topicMap.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    for (int j = 0; j < i; j++) {
                        cluster1 = entry.getValue().get(i);
                        cluster2 = entry.getValue().get(j);
                        indexMap1 = indexMapMaid(cluster1.getLeafCateId(), cluster1.getTopicId());
                        indexMap2 = indexMapMaid(cluster2.getLeafCateId(), cluster2.getTopicId());
                        score = scoreMaid(indexMap1, indexMap2);
                        if (score >= threshold) {
                            if (!map.containsKey(cluster2.getId())) {
                                map.put(cluster2.getId(), new ArrayList<Integer>());
                            }
                            map.get(cluster2.getId()).add(cluster1.getId());
                        }
                    }
                }
            }
        }

        Set<Set<Integer>> finalSet = getMergeSet(map);

//        for (Set<Integer> set : finalList) {
//            for (int id : set) {
//                System.out.print(id + " ");
//            }
//            System.out.println("");
//        }

        Map<Integer, Integer> printMap = new HashMap<>();
        int ind = 0;
        for (Set<Integer> set : finalSet) {
            ind++;
            for (int id : set) {
                printMap.put(id, ind);
            }
        }
        for (int i = 1; i < list.size() + 1; i++) {
            if (printMap.containsKey(i)) {
                System.out.println(printMap.get(i));
            } else {
                System.out.println(0);
            }

        }
    }

    public static Set<Set<Integer>> getMergeSet(Map<Integer, List<Integer>> map) {
        Set<Set<Integer>> setList = new HashSet<>();
        Set<Integer> sinSet = new HashSet<>();
        Map<Integer, Integer> idMap = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            if (sinSet.contains(entry.getKey())) {
                continue;
            }
            Set<Integer> curSet = new TreeSet<>();
            curSet.add(entry.getKey());
            loopSin(map, entry.getValue(), curSet);
            setList.add(curSet);
            sinSet.addAll(curSet);

            for (int curId : curSet) {
                if (!idMap.containsKey(curId)) {
                    idMap.put(curId, 1);
                } else {
                    idMap.put(curId, idMap.get(curId) + 1);
                }
            }
        }
//        */

//        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
//            Set<Integer> curSet = new TreeSet<>();
//            curSet.add(entry.getKey());
//            curSet.addAll(entry.getValue());
//            setList.add(curSet);
//            for (int curId : curSet) {
//                if (!idMap.containsKey(curId)) {
//                    idMap.put(curId, 1);
//                } else {
//                    idMap.put(curId, idMap.get(curId) + 1);
//                }
//            }
//        }

        Set<Set<Integer>> finalSet = new HashSet<>();
        for (int cellId : idMap.keySet()) {
            if (idMap.get(cellId) > 1) {
                Set<Integer> oriSet = null;
                Iterator<Set<Integer>> it = setList.iterator();
                while (it.hasNext()) {
                    Set<Integer> set = it.next();
                    if (set.contains(cellId)) {
                        if (oriSet == null) {
                            oriSet = set;
                        } else {
                            oriSet.addAll(set);
                            it.remove();
                        }
                    }
                }
                finalSet.add(oriSet);
            }
        }
        finalSet.addAll(setList);
        return finalSet;
    }


    private static void loopSin(Map<Integer, List<Integer>> map, List<Integer> oriSinList, Set<Integer> set) {
        for (int oriSin : oriSinList) {
            set.add(oriSin);
            if (map.containsKey(oriSin)) {
                loopSin(map, map.get(oriSin), set);
            }
        }
    }

    public Map<Integer, Integer> rootCellTermCollector(Map<Long, Number> map) {
        Map<Integer, List<Long>> wareIdListMap = new TreeMap<>();
        for (long wareId : map.keySet()) {
            if (!wareIdListMap.containsKey(map.get(wareId))) {
                wareIdListMap.put((int) map.get(wareId), new ArrayList<Long>());
            }
            wareIdListMap.get(map.get(wareId)).add(wareId);
        }

        // sout
        List<String> writeList = new ArrayList<>();
        for (int cellId : wareIdListMap.keySet()) {
            FileSteward.writeCellNum(cellId);
            Map<String, Double> curIndexCellNameMap = indexMapMaidWithName(wareIdListMap.get(cellId));
            Set<String> popularTermSet = curIndexCellNameMap.keySet();

            // 2017.9.19
            // here we only want noun terms, check by standford jar and model
            popularTermSet = lateWork.getNunTermList(popularTermSet);

            writeList.add(cellId + "\t" + popularTermSet);
            System.out.println(cellId + "\t" + popularTermSet);
        }

        // write message.txt
        String file = DocLdaActor.prefix_path + "pic_" + DocLdaActor.categoryId + "\\" + "message.txt";
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(new File(file));
            bw = new BufferedWriter(fw);
            for (String content : writeList) {
                bw.write(content + "\r\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // need not logic behind
        return new HashMap<>();

        /*
        Map<Integer, Map<Integer, Integer>> collectMap = new HashMap<>();
        Map<Integer, List<Integer>> cellIdMergeMap = new TreeMap<>();
        for (int cellId : wareIdListMap.keySet()) {
            Map<Integer, Integer> curIndexMap = indexMapMaid(wareIdListMap.get(cellId));
            collectMap.put(cellId, curIndexMap);
        }
        List<Map.Entry<Integer, Map<Integer, Integer>>> entrys = new ArrayList<>(collectMap.entrySet());
        Collections.sort(entrys, new Comparator<Map.Entry<Integer, Map<Integer, Integer>>>() {
            @Override
            public int compare(Map.Entry<Integer, Map<Integer, Integer>> o1, Map.Entry<Integer, Map<Integer, Integer>> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        for (int i = 0; i < entrys.size(); i++) {
            for (int j = 0; j < i; j++) {
                double score = scoreMaid(entrys.get(i).getValue(), entrys.get(j).getValue());
                if (score >= threshold) {
                    if (!cellIdMergeMap.containsKey(entrys.get(i).getKey())) {
                        cellIdMergeMap.put(entrys.get(i).getKey(), new ArrayList<Integer>());
                    }
                    cellIdMergeMap.get(entrys.get(i).getKey()).add(entrys.get(j).getKey());
                }
            }
        }

        Set<Set<Integer>> finalSet = getMergeSet(cellIdMergeMap);
        Map<Integer, Integer> printMap = new HashMap<>();
        int ind = 0;
        for (Set<Integer> set : finalSet) {
            ind++;
            for (int id : set) {
                printMap.put(id, ind);
            }
        }
        Map<Integer, Integer> idLinkMap = new HashMap<>();
        for (int key : wareIdListMap.keySet()) {
            if (printMap.containsKey(key)) {
                System.out.println(key + "\t" + printMap.get(key));
                idLinkMap.put(key, printMap.get(key));
            } else {
                ind++;
                System.out.println(key + "\t" + ind);
                idLinkMap.put(key, ind);
            }
        }

        return idLinkMap;
        */
    }

    public void mergeNodeEx() {
        List<CellCluster> list = getCellClusterList();
        LateWork lateWork = new LateWork(DocLdaActor.ori_fix_file);
        List<Ware4LateWork> wareList = lateWork.getWareList();
        Map<String, Map<Long, Integer>> brandCateMap = new HashMap<>();
        for (Ware4LateWork ware : wareList) {
            String key = ware.getLeafCateId() + "_" + ware.getBrandName();
            if (!brandCateMap.containsKey(key)) {
                brandCateMap.put(key, new HashMap<Long, Integer>());
            }
            Map<Long, Integer> curMap = brandCateMap.get(key);
            if (!curMap.containsKey(ware.getTopicId())) {
                curMap.put(ware.getTopicId(), 1);
            } else {
                curMap.put(ware.getTopicId(), curMap.get(ware.getTopicId()) + 1);
            }
        }
        Map<String, Integer> cellSizeMap = new HashMap<>();
        for (CellCluster cc : list) {
            String key = cc.getLeafCateId() + "_" + cc.getTopicId();
            cellSizeMap.put(key, cc.getSize());
        }
        for (Ware4LateWork ware : wareList) {
            String key = ware.getLeafCateId() + "_" + ware.getTopicId();
            if (cellSizeMap.get(key) < 5) {
                String key2 = ware.getLeafCateId() + "_" + ware.getBrandName();
                long targTopicId = -1;
                int count = 0;
                for (long topicId : brandCateMap.get(key2).keySet()) {
                    if (brandCateMap.get(key2).get(topicId) > count) {
                        targTopicId = topicId;
                        count = brandCateMap.get(key2).get(topicId);
                    }
                }
                if (targTopicId == ware.getTopicId()) {
                    continue;
                } else {
                    ware.setTopicId(targTopicId);
                }
            }
        }

        // to store oriFile
        FileSteward.storeOriFix(wareList, DocLdaActor.ori_fix_file);
        FileSteward.storeCellClusterTree(DocLdaActor.ori_fix_file);
        mergeNode();
    }

    public void mergeNode() {
        int termCnt = FileSteward.countTerm(DocLdaActor.wkbt_dict_file);
        List<CellCluster> list = getCellClusterList();
        double[][] result = new double[list.size()][termCnt + 2]; // [0] is index start from 1, [1] is cell_size

        Map<Integer, Integer> indexMap = null;
        for (int i = 0; i < list.size(); i++) {
            indexMap = indexMapMaid(list.get(i).getLeafCateId(), list.get(i).getTopicId());
            result[i][0] = i;
            result[i][1] = list.get(i).getSize();
            for (int index : indexMap.keySet()) {
                result[i][index + 2] = indexMap.get(index);
            }
        }

        // here do dush merge
//        result = mergeDustAndNormalize(result, list);

        result = mergeDustByIFIDF(result, list);

        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(prefixPath + "nodeIndex4WH.txt");
            bw = new BufferedWriter(fw);
            for (int i = 0; i < result.length; i++) {
//                bw.write(i + "\t");
                for (int j = 0; j < result[0].length; j++) {
                    bw.write(result[i][j] + "\t");
                }
                bw.write("\r\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double[][] mergeDustAndNormalize(double[][] result, List<CellCluster> list) {
        for (int i = 0; i < result.length; i++) {
            if (result[i][1] < cellSizeThreshold) { // it is a dush cell
                double cosinDistance = 1;
                int targId = 1;
                for (int j = 0; j < result.length; j++) { // loop
                    if (result[j][1] >= cellSizeThreshold && // root cell
                            list.get(j).getSecondCateId() == list.get(i).getSecondCateId()) { // same second_category
                        double curCD = cosinDistance(Arrays.copyOfRange(result[i], 2, result[i].length), Arrays.copyOfRange(result[j], 2, result[i].length));
                        if (cosinDistance == 1 ||
                                cosinDistance < curCD) {
                            cosinDistance = curCD;
                            targId = (int) result[j][0];
                        }
                    }
                }
                result[i][0] = targId; // tag it
                for (int k = 1; k < result[i].length; k++) {
                    result[targId][k] += result[i][k];
                }
            }
        }
        for (double[] curArray : result) {
            double[] subArray = Arrays.copyOfRange(curArray, 2, curArray.length);
            subArray = normalize(subArray);
            System.arraycopy(subArray, 0, curArray, 2, subArray.length);
        }
        return result;
    }

    public double[][] mergeDustByIFIDF(double[][] result, List<CellCluster> list) {
        double[][] tfIdfMatrix = getIFIDFMatrix(result);
        for (int i = 0; i < result.length; i++) {
            if (result[i][1] < cellSizeThreshold) { // it is a dush cell
                double oDistance = 0;
                int targId = 1;
                for (int j = 0; j < result.length; j++) { // loop
                    if (result[j][1] >= cellSizeThreshold && // root cell
                            list.get(j).getSecondCateId() == list.get(i).getSecondCateId()) { // same second_category
                        double curOD = oDistance(Arrays.copyOfRange(tfIdfMatrix[i], 2, result[i].length), Arrays.copyOfRange(result[j], 2, tfIdfMatrix[i].length));
                        if (oDistance == 0 ||
                                oDistance > curOD) {
                            oDistance = curOD;
                            targId = (int) result[j][0];
                        }
                    }
                }
                result[i][0] = targId; // tag it
                for (int k = 1; k < result[i].length; k++) {
                    result[targId][k] += result[i][k];
                }
            }
        }
        return result;
    }

    public double[][] getIFIDFMatrix(double[][] result) {
        Map<Integer, Integer> termDocCountMap = new HashMap<>();
        for (double[] array : result) {
            for (int i = 0; i < array.length - 2; i++) {
                if (array[i + 2] == 0) {
                    continue;
                }
                if (!termDocCountMap.containsKey(i)) {
                    termDocCountMap.put(i, 1);
                } else {
                    termDocCountMap.put(i, termDocCountMap.get(i) + 1);
                }
            }
        }
        for (double[] array : result) {
            int termCnt = 0;
            for (int i = 2; i < array.length; i++) {
                termCnt += array[i];
            }
            for (int i = 0; i < array.length - 2; i++) {
                if (array[i + 2] == 0) {
                    continue;
                }
                double tfidf = array[i + 2] / (double)termCnt * Math.log(result.length / (double)(termDocCountMap.get(i) + 1));
                array[i + 2] = tfidf;
            }
        }
        return result;
    }

    public double[] normalize(double[] array) {
        double[] result = new double[array.length];
        double squareSum = 0;
        for (int i = 0; i < array.length; i++) {
            squareSum += array[i] * array[i];
        }
        double distance = Math.sqrt(squareSum);
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] / squareSum;
        }
        return result;
    }

    public double cosinDistance(double[] cc1, double[] cc2) {
        cc1 = normalize(cc1);
        cc2 = normalize(cc2);
        double pointPlus = 0;
        int len = cc1.length;
        for (int i = 0; i < len; i++) {
            pointPlus += cc1[i] * cc2[i];
        }
        return pointPlus;
    }

    public double oDistance(double[] cc1, double[] cc2) {
        double pointPlus = 0;
        int len = cc1.length;
        for (int i = 0; i < len; i++) {
            pointPlus += (cc1[i] - cc2[i]) * (cc1[i] - cc2[i]);
        }
        return Math.sqrt(pointPlus);
    }

    public static void main(String[] args) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        List<Integer> list1 = new ArrayList<Integer>(){{
            add(10);
            add(11);
        }};
        List<Integer> list2 = new ArrayList<Integer>(){{
            add(20);
            add(21);
        }};
        map.put(1, list1);
        map.put(10, list2);
        loopSin(map, new ArrayList<Integer>(){{add(1);}}, new HashSet<Integer>());
    }
}
