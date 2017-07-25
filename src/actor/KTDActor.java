package actor;

import tool.*;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/24.
 */
public class KTDActor {

    private final static int minTermLen = 3; // accept paste_keyword term cnt

    public static void main(String[] args) {
        DocLdaActor.init();

        // before translate, we need ori_title to seperate keyword
        List<WareMsgConventor> wareMsgList = FileSteward.getWareMsgList(DocLdaActor.wkbt_file);
        keywordSeperator(wareMsgList);

        List<WareMsgTranslate> translateWareList = FileSteward.getTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transWare.txt"));
        mergKeyword2TranslateWareList(wareMsgList, translateWareList);

        DescribeActor.index(translateWareList);
        DocLdaActor.actor4Describe();

    }

    public static void keywordSeperator(List<WareMsgConventor> wareMsgList) {
        Map<String, Integer> termMap = new HashMap<>();
        for (WareMsgConventor ware : wareMsgList) {
            titleTermCollector(ware, termMap);
        }
        /*
        int totalTimeTerm = 0;
        for (String term : termMap.keySet()) {
            totalTimeTerm += termMap.get(term);
        }
        totalTimeTerm = totalTimeTerm / termMap.size();
        Iterator<String> it = termMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (FileSteward.HasDigit(key) ||
                    termMap.get(key) < totalTimeTerm) {
                it.remove();//添加此行代码
                termMap.remove(key);
            }
        }
        */
        for (WareMsgConventor ware : wareMsgList) {
            System.out.print(ware.getWareId() + "\t");
            keywordDealer(ware, termMap.keySet());
        }
    }

    public static void mergKeyword2TranslateWareList(List<WareMsgConventor> wareMsgList, List<WareMsgTranslate> translateWareList) {
        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();
        for (int i = 0; i < wareMsgList.size(); i++) {
            String[] keywords = wareMsgList.get(i).getKeywords();
            StringBuffer keywordBuffer = new StringBuffer();
            for (String ele : keywords) {
                ele = IndexSteward.getUniformWord4Keyword(ele, fileSteward, dictMap);
                keywordBuffer.append(ele).append(",");
            }
            translateWareList.get(i).setKeywords(keywordBuffer.toString());
        }
        fileSteward.storeAddTranslateFile();
    }

    public static void titleTermCollector(WareMsgConventor ware, Map<String, Integer> termMap) {
        String title = ware.getTitle();
        if (title != null &&
                !title.trim().isEmpty()) {
            String[] titleTerms = title.replaceAll("[^0-9a-zA-Z]"," ").toLowerCase().split("\\s+");
            for (String term : titleTerms) {
                if (term.length() <= 2) {
                    continue;
                }
                if (termMap.containsKey(term)) {
                    termMap.put(term, termMap.get(term) + 1);
                } else {
                    termMap.put(term, 1);
                }
            }
        }
    }

    public static void keywordDealer(WareMsgConventor ware, Set<String> set) {
        String[] keywords = ware.getKeywords();
        for (String keyword : keywords) {
            System.out.print(keyword + ",");
        }
        System.out.print("\t");

        int same = 1;
        for (int i = 0; i < keywords.length; i++) {
            String keyword = keywords[i].trim().toLowerCase();
            if (set.contains(keyword)) {
                continue;
            }
            String sepKeyword = loopSeperater1(keyword, set);
            if (sepKeyword == null) {
                sepKeyword = loopSeperater2(keyword, set);
            }
            if (sepKeyword == null) {
                sepKeyword = loopSeperater3(keyword, set);
            }
            if (sepKeyword != null) {
                keywords[i] = sepKeyword;
                same = 0;
            }
        }
        ware.setKeywords(keywords);
        for (String keyword : keywords) {
            System.out.print(keyword + ",");
        }
        System.out.print("\t" + same + "\n");
    }

    public static String loopSeperater1(String keyword, Set<String> set) {
        if (keyword.length() >= 2 * minTermLen) {
            for (int i = minTermLen; i < keyword.length() - minTermLen; i++) {
                if (set.contains(keyword.substring(0, i)) &&
                        set.contains(keyword.substring(i))) {
                    return keyword.substring(0, i) + " " + keyword.substring(i);
                }
            }
        }
        return null;
    }

    public static String loopSeperater2(String keyword, Set<String> set) {
        if (keyword.length() >= 3 * minTermLen) {
            for (int i = minTermLen; i < keyword.length() - 2 * minTermLen; i++) {
                for (int j = i + minTermLen; j < keyword.length() - minTermLen; j++) {
                    if (set.contains(keyword.substring(0, i)) &&
                            set.contains(keyword.substring(i, j)) &&
                            set.contains(keyword.substring(j))) {
                        return keyword.substring(0, i) + " " + keyword.substring(i, j) + " " + keyword.substring(j);
                    }
                }
            }
        }
        return null;
    }

    public static String loopSeperater3(String keyword, Set<String> set) {
        if (keyword.length() >= 4 * minTermLen) {
            for (int i = minTermLen; i < keyword.length() - 3 * minTermLen; i++) {
                for (int j = i + minTermLen; j < keyword.length() - 2 * minTermLen; j++) {
                    for (int k = j + minTermLen; k < keyword.length() - minTermLen; k++) {
                        if (set.contains(keyword.substring(0, i)) &&
                                set.contains(keyword.substring(i, j)) &&
                                set.contains(keyword.substring(j, k)) &&
                                set.contains(keyword.substring(k))) {
                            return keyword.substring(0, i) + " " + keyword.substring(i, j) + " " + keyword.substring(j, k) + " " + keyword.substring(k);
                        }
                    }
                }
            }
        }
        return null;
    }
}
