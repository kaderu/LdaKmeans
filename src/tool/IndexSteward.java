package tool;

import pic.algorithm.translate.TranslateUtil;

import java.util.*;

import static tool.IndexSteward.INDEX_TYPE.advance_keyword_translate;
import static tool.IndexSteward.INDEX_TYPE.keyword_translate;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class IndexSteward {

    public enum INDEX_TYPE {
        keyword_translate,
        advance_keyword_translate,
    }

    public static void index(String docPath, String dictFilePath, String indexFilePath) {
        List<WareMsgConventor> wareMsgList = FileSteward.getWareMsgList(docPath);
//        indexMaker(wareMsgList, dictFilePath, indexFilePath);
        MultiLayerIndexMap dictMap = kbtIndexMaker(wareMsgList, dictFilePath);
        ldaInputMaker(wareMsgList, dictMap, indexFilePath);
    }

    public static void indexMaker(List<WareMsgConventor> wareMsgList, String dictFilePath, String indexFilePath) {
        // make dict
        Map<Set<String>, Integer> dicSetMap = new HashMap<>();
        Set<String> dictSet = new TreeSet<>();
        for (WareMsgConventor wareMsg : wareMsgList) {
            String[] array = wareMsg.getKeywords();
            dictSet.addAll(new ArrayList<>(Arrays.asList(array)));
        }
        dicSetMap.put(dictSet, 1);
        MultiLayerIndexMap dicMap = dicTranslateDealer(dicSetMap, advance_keyword_translate);
        dicMap.store(dictFilePath);
        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        for (WareMsgConventor wareMsg : wareMsgList) {
            map = new HashMap<>();
            for (String keyword : wareMsg.getKeywords()) { // [mother care, baby bath, baby clean]
                List<Integer> wordIndexList = dicMap.get(keyword);
                for (int wordIndex : wordIndexList) {
                    if (map.containsKey(wordIndex)) {
                        map.put(wordIndex, map.get(wordIndex) + 1);
                    } else {
                        map.put(wordIndex, 1);
                    }
                }
            }
            mapList.add(map);
        }
        FileSteward.storeIndex(mapList, indexFilePath);
    }

    // merge keyword, brandName and title
    public static MultiLayerIndexMap kbtIndexMaker(List<WareMsgConventor> wareMsgList, String dictFilePath) {
        // make dictMap
        Map<Set<String>, Integer> dicSetMap = new HashMap<>();
        Set<String> dictKeywordSet = new TreeSet<>();
        Set<String> dictBrandNameSet = new TreeSet<>();
        Set<String> dictTitleSet = new TreeSet<>();
        for (WareMsgConventor wareMsg : wareMsgList) {
            String[] array = wareMsg.getKeywords();
            dictKeywordSet.addAll(new ArrayList<>(Arrays.asList(array)));
            String branName = wareMsg.getBrandName();
            dictBrandNameSet.add(branName);
            String[] titleCells = wareMsg.getTitleCells();
            dictTitleSet.addAll(new ArrayList<>(Arrays.asList(titleCells)));
        }
        dicSetMap.put(dictKeywordSet, 10 + 2); // we should tell keyword from others
        dicSetMap.put(dictBrandNameSet, 0);
        dicSetMap.put(dictTitleSet, 1);

        MultiLayerIndexMap dicMap = dicTranslateDealer(dicSetMap, advance_keyword_translate);
        dicMap.store(dictFilePath);
        return dicMap;
    }

    // merge keyword, brandName and title
    public static MultiLayerIndexMap kbtdIndexMaker(List<WareMsgTranslate> wareTransList, String dictFilePath) {
        // make dictMap
        Map<Set<String>, Integer> dicSetMap = new HashMap<>();
        Set<String> dictKeywordSet = new TreeSet<>();
        Set<String> dictBrandNameSet = new TreeSet<>();
        Set<String> dictTitleSet = new TreeSet<>();
        Set<String> describeSet = new TreeSet<>();
        for (WareMsgTranslate ware : wareTransList) {
            String[] array = ware.getKeywords().split(",");
            dictKeywordSet.addAll(new ArrayList<>(Arrays.asList(array)));
            String branName = ware.getBrandName();
            dictBrandNameSet.add(branName);
            String[] titleCells = ware.getTitle().split("\\s+");
            dictTitleSet.addAll(new ArrayList<>(Arrays.asList(titleCells)));
            String[] describeCells = ware.getDescribe().split("\\s+");
            describeSet.addAll(new ArrayList<String>(Arrays.asList(describeCells)));
        }
        dicSetMap.put(dictKeywordSet, 10 + 2); // we should tell keyword from others
        dicSetMap.put(dictBrandNameSet, 0);
        dicSetMap.put(dictTitleSet, 0);
        dicSetMap.put(describeSet, 0);

        MultiLayerIndexMap dicMap = dicTranslateDealer(dicSetMap, advance_keyword_translate);
        dicMap.store(dictFilePath);
        return dicMap;
    }

    public static void ldaInputMaker(List<WareMsgConventor> wareMsgList, MultiLayerIndexMap dicMap, String indexFilePath) {
        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        for (WareMsgConventor wareMsg : wareMsgList) {
            map = new HashMap<>();
            String[] keywordsArray = wareMsg.getKeywords();
            String brandName = wareMsg.getBrandName();
            String[] titleCells = wareMsg.getTitleCells();
            String[] totalArray = concatAll(keywordsArray,
//                    new String[]{brandName},
                    titleCells);

            for (String cell : totalArray) {
                if (cell == null ||
                        cell.trim().equals("")) {
                    continue;
                }
                System.out.println(cell);
                List<Integer> wordIndexList = dicMap.get(cell);
                // may we wanna give keyword phrase a bigger weight
                for (int wordIndex : wordIndexList) {
                    int weight = 1;
                    if (map.containsKey(wordIndex)) {
                        map.put(wordIndex, map.get(wordIndex) + weight);
                    } else {
                        map.put(wordIndex, weight);
                    }
                }
            }
            mapList.add(map);
        }
        FileSteward.storeIndex(mapList, indexFilePath, true);
    }

    public static MultiLayerIndexMap dicTranslateDealer(Map<Set<String>, Integer> dicSetMap, Enum methodType) {
        if (methodType.equals(keyword_translate)) {
            return dicTranslateMethodKeywordTranslate(dicSetMap);
        } else if (methodType.equals(advance_keyword_translate)) {
            return dicTranslateMethodAdvanceKeywordTranslate(dicSetMap);
        }
        return null;
    }

    // simply keyword translate
    public static MultiLayerIndexMap dicTranslateMethodKeywordTranslate(Map<Set<String>, Integer> dicSetMap) {
        Map<String, Integer> map = new HashMap<>();
        Map<String, Integer> translateMap = new HashMap<>();
        String uniformWord;

        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();

        for (Map.Entry<Set<String>, Integer> entry : dicSetMap.entrySet()) {
            for (String word : entry.getKey()) {
                if (entry.getValue() == 1) {
                    uniformWord = getUniformWord(word, fileSteward, dictMap);
                } else {
                    uniformWord = word;
                }
                if (!translateMap.containsKey(uniformWord)) {
                    translateMap.put(uniformWord, translateMap.size());
                }
                map.put(word, translateMap.get(uniformWord));
            }
        }

        fileSteward.storeAddTranslateFile();
        return new MultiLayerIndexMap(map);
    }

    public static MultiLayerIndexMap dicTranslateMethodAdvanceKeywordTranslate(Map<Set<String>, Integer> dicSetMap) {
        Map<String, Integer> translateSetMap = new HashMap<>(); // set to collect cell word and make index
        Map<String, List<String>> ori2ExtendMap = new HashMap<>(); // map reflect every oriword to a list of cell word (after translate)
        Map<String, Integer> map = new HashMap<>();

        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();
        // step1. oriword translate
        String uniformWord;
        int index = 0;
        for (Map.Entry<Set<String>, Integer> entry : dicSetMap.entrySet()) {
            for (String word : entry.getKey()) {
                if ("".equals(word.trim())) {
                    continue;
                }
                if (entry.getValue() > 0) {
                    if (entry.getValue() / 10 > 0) { // then we know it's a keyword, we put translate in keyword_translate.dic
                        uniformWord = getUniformWord4Keyword(word, fileSteward, dictMap);
                    } else { // formally put in translate.dic
                        uniformWord = getUniformWord(word, fileSteward, dictMap);
                    }
                    System.out.println("###### translate the " + index++ + "th term finished ..." );
                } else {
                    System.out.println("###### need not translate ..." );
                    uniformWord = word.toLowerCase();
                }
                if (!translateSetMap.containsKey(uniformWord)) {
                    translateSetMap.put(uniformWord, 1);
                } else {
                    translateSetMap.put(uniformWord, translateSetMap.get(uniformWord) + 1);
                }
                final String uniformWordTmp = uniformWord;
                List<String> ori2ExtendList =new ArrayList<String>();
                ori2ExtendList.add(uniformWordTmp);
                if (entry.getValue() % 10 > 1) { // increase size of ori2ExtendList
                    ori2ExtendList.add("");
                }
                ori2ExtendMap.put(word, ori2ExtendList);
            }
        }
        fileSteward.storeAddTranslateFile();

        // step2. with oriword set, we process some popular cell words
        Map<String, Integer> popularCellWordCntMap = new HashMap<>(); // map keep popular cell word (single)
        int kernelCnt = 0;
        // calculate each kernel word weight
        for (String keyword : translateSetMap.keySet()) {
            if (keyword.indexOf(" ") == -1){ // do not deal keywords with several terms
                popularCellWordCntMap.put(keyword, translateSetMap.get(keyword));
                kernelCnt += translateSetMap.get(keyword);
            }
            /*
            for (String ele : keyword.split(" ")) {
                if (ele.equals("and") ||
                        ele.equals("with") ||
                        ele.equals("of") ||
                        ele.equals("in") ||
                        ele.equals("for") ||
                        ele.length() <= 2) {
                    continue;
                }
                if (!popularCellWordCntMap.containsKey(ele)) {
                    popularCellWordCntMap.put(ele, 1);
                } else {
                    popularCellWordCntMap.put(ele, popularCellWordCntMap.get(ele) + 1);
                }
                kernelCnt++;
            }
            */
        }
        double meanCnt = kernelCnt * 1.0 / popularCellWordCntMap.size();
        Iterator it = popularCellWordCntMap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (popularCellWordCntMap.get(key) < meanCnt) {
                it.remove();//添加此行代码
                popularCellWordCntMap.remove(key);
            }
        }

        // step3. extend translate word with popular cell word map
        for (Map.Entry<String, List<String>> entry : ori2ExtendMap.entrySet()) {
            if (entry.getValue().size() > 1) { // size is controlled in step.1, only cell of keyword can go into this logic
                entry.getValue().remove(1);
                List<String> popularCellWordList = getHideKernelWord(popularCellWordCntMap.keySet(), entry.getValue().get(0));
                entry.getValue().addAll(popularCellWordList);
                for (String cell : popularCellWordList) {
                    translateSetMap.put(cell, 1);
                }
            }
        }

        // step4. make index map
        int i = 0;
        for (String keyword : translateSetMap.keySet()) {
            map.put(keyword, i++);
        }

        // step4EX. map blocker from a new game plus
//        int i = 0;
//        Set<String> blockSet = FileSteward.getBlockSet();
//        for (String keyword : translateSet) {
//            if (blockSet.contains(keyword)) {
//                map.put(keyword, i++);
//            }
//        }

        return new MultiLayerIndexMap(ori2ExtendMap, map);
    }

    public static String getUniformWord(String word, FileSteward fileSteward, Map<String, String> dictMap) {
        String uniformWord;
        if (dictMap.containsKey(word)) { // get dict from cache
            uniformWord = dictMap.get(word);
        } else { // get from google.com
            uniformWord = "";
            int i = 0;
            while (i < 3 && uniformWord.isEmpty()) {
                try {
                    uniformWord = TranslateUtil.id2en(word.replaceAll("[-]"," ")).toLowerCase();
                } catch (Exception e) {
                    i++;
                }
            }
            if (uniformWord.isEmpty()) {
                uniformWord = word;
            } else { // add to dictMap and increase_map, later store increasement to file
                dictMap.put(word, uniformWord);
                fileSteward.getAddDictMap().put(word, uniformWord);
            }
        }
        return uniformWord;
    }

    public static String getUniformWord4Keyword(String word, FileSteward fileSteward, Map<String, String> dictMap) {
        String uniformWord;
        if (dictMap.containsKey(word)) { // get dict from cache
            uniformWord = dictMap.get(word);
        } else {  // get from google.com
            uniformWord = "";
            int i = 0;
            while (i < 3 && uniformWord.isEmpty()) {
                try {
                    uniformWord = TranslateUtil.id2en(word.replaceAll("[-]"," ")).toLowerCase();
                } catch (Exception e) {
                    i++;
                }
            }
            if (uniformWord.isEmpty()) {
                uniformWord = word;
            } else { // add to dictMap and increase_map, later store increasement to file
                dictMap.put(word, uniformWord);
                fileSteward.getAddKeywordDictMap().put(word, uniformWord);
            }
        }
        return uniformWord;
    }

    private static List<String> getHideKernelWord(Set<String> wordSet, String keyword) {
        List<String> wordList = new ArrayList<>();
        if (keyword.contains(" ")) { // only deal with pasted word
            for (String ele : keyword.split("\\s+")) {
                if (wordSet.contains(ele)) {
                    wordList.add(ele);
                }
            }
            return wordList;
        }
        for (String word : wordSet) {
            if (word.length() <= 2) {
                continue;
            }
            if (!keyword.equals(word)) {
                if (keyword.indexOf(word) != -1) {
                    String[] eles = keyword.split(word);
                    List<String> cutList = new ArrayList<>();
                    boolean isPaste = true;
                    for (String ele : eles) { // word split this pastestring to several words, we should check them one by one
                        String trimEle = ele.trim();
                        if (trimEle.length() == 0) {
                            continue;
                        } else if (wordSet.contains(trimEle)) {
                            cutList.add(trimEle);
                        } else {
                            isPaste = false;
                            break;
                        }
                    }
                    if (isPaste) { // then we add the elements to wordlist and finish this work
                        wordList.add(word);
                        wordList.addAll(cutList);
                        break;

                        // only add the most important word
//                        if (cutList.size() > 1) {
//                            wordList.add(cutList.get(cutList.size() - 1));
//                        } else if (keyword.endsWith(word)) {
//                            wordList.add(word);
//                        } else {
//                            wordList.add(cutList.get(0));
//                        }
//                        break;
                    }
                }
            }
        }
        return wordList;
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static void main(String[] args) {
        String keyword = "bags";
        String[] arrays = keyword.split(" ");
        System.out.println(arrays[0]);
    }
}
