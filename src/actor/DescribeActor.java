package actor;

import tool.DescriptionMsgGrabber;
import tool.FileSteward;
import tool.Tokenizer;
import tool.WareMsgTranslate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/11.
 */
public class DescribeActor {

    private static final Set<String> prepSet = new HashSet<String>() {{
        add("in");
        add("on");
        add("with");
        add("and");
        add("by");
        add("to");
        add("for");
        add("from");
        add("at");
        add("about");
        add("under");
        add("of");
        add("into");
        add("within");
        add("throughout");
        add("through");
        add("inside");
        add("outside");
        add("without");
        add("that");
        add(",");
        add(".");
        add("or");
        add("as");
        add("made");
        add("designed");
        add("used");
        add("equipped");
        add("provide");
        add("offered");
    }};

    public static void main(String[] args) {
        DocLdaActor.init();
        List<WareMsgTranslate> translateWareList = FileSteward.getTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transWare.txt"));

        // make output
        for (WareMsgTranslate ware : translateWareList) {
            System.out.print(ware.getWareId() + "\t");
            System.out.print(ware.getKeywords() + "\t");
            System.out.print((ware.getKeywords().trim().isEmpty() ? 1 : 0) + "\t");
            String describe = ware.getDescribe();
            if (describe == null ||
                    describe.trim().isEmpty()) {
                System.out.println("\t0");
                continue;
            }
            String[] describeArrays = describe.toLowerCase().trim().replaceAll(ware.getBrandName() + " ", " ").split("\001");

            // [special] rule 0: focus on [Contents:]
            boolean stop = false;
            boolean aimAt = false;
            for (String ele : describeArrays) {
                if (ele.endsWith("contents:") ||
                        ele.endsWith("content:")) { // we focus on next line
                    aimAt = true;
                    continue;
                } else if (ele.contains("contents:") ||
                        ele.contains("content:")) { // we focus on current line
                    aimAt = true;
                }
                if (aimAt) {
                    String contentKernel = getContentKernel(ele);
                    System.out.println("[C]" + contentKernel);
                    stop = true;
                    break;
                }
            }
            if (!stop) {
                int shortPhraseNum = 0;
                int innerStop = 0;
                for (String centence : describeArrays) {
                    // rule 1: drop details
                    if ("details:".equals(centence.trim()) ||
                            "details".equals(centence.trim())) {
                        break;
                    }
                    // rule 2: // pity but we have walk into short describe sentence jungle
                    if (describeArrays.length > 1 &&
                            centence.split(" ").length < 10) {
                        shortPhraseNum++;
                        if (shortPhraseNum == 3) {
                            break;
                        }
                        continue;
                    }
                    if (innerStop == 1) {
                        break;
                    }
                    // XX is a YY
                    String result;
                    for (String ele : centence.split("\\.")) {
                        if (ele.trim().isEmpty()) {
                            continue;
                        }
                        result = returnSubjectByIsA(ele);
                        if (result != null) {
                            System.out.println(result);
                            innerStop = 1;
                            break;
                        }
                    }
                    if (innerStop == 1) {
                        break;
                    }
                    // A XX ...
                    for (String ele : centence.split("\\.")) {
                        if (ele.trim().isEmpty()) {
                            continue;
                        }
                        result = returnSubjectByA(ele);
                        if (result != null) {
                            System.out.println(result);
                            innerStop = 1;
                            break;
                        }
                    }
                    if (innerStop == 1) {
                        break;
                    }
                    // XX is made ..
                    for (String ele : centence.split("\\.")) {
                        if (ele.trim().isEmpty()) {
                            continue;
                        }
                        result = returnSubjectByIsMade(ele);
                        if (result != null) {
                            System.out.println(result);
                            innerStop = 1;
                            break;
                        }
                    }
                    if (innerStop == 1) {
                        break;
                    }

                }
                if (innerStop == 0) {
                    for (String ele : describeArrays) {
                        if (ele.endsWith(":") ||
                                ele.split("\\s+").length <= 3) {
                            continue;
                        }
                        System.out.println(ele.split("\\.")[0] + "\t1");
                        break;
                    }
                }
            }
        }

        // ori
        /*
        index(translateWareList);
        DocLdaActor.actor4Describe();
        */
    }

    public static void index(List<WareMsgTranslate> translateWareList) {
        index(translateWareList, 0);
    }

    /**
     * type 0: full text index
     * type 1: ori index -- without describe
     * @param translateWareList
     */
    public static void index(List<WareMsgTranslate> translateWareList, int type) {
        List<Map<Integer, Integer>> indexWareList = new ArrayList<>();

        Set<String> stopSet = FileSteward.readStopSet();
        Map<String, Integer> dictMap = new HashMap<>();




        FileWriter fw;
        BufferedWriter bw;
        StringBuffer buffer;
        try {
            fw = new FileWriter(DocLdaActor.wkbt_file.replace(DocLdaActor.wkbt_file_name, "nromalize.txt"));
            bw = new BufferedWriter(fw);

            for (WareMsgTranslate ware : translateWareList) {
                buffer = new StringBuffer();
                buffer.append(ware.getWareId()).append("\t");

                System.out.println("########### " + ware.getWareId());
                Map<Integer, Integer> wareTermMap = new HashMap<>();
                int index;

            /*
            let's make a interval discussion here for what count most for a ware doc.
             If keywords is of high-quanlity, then keyword count the most. Then is brandName, which connect similar wares together.
             If both above lose efficacy (like K:lego B:lego case), then we bank on title.
             But sometimes, title contribute little -- maybe it is just brandName plus version-number plus color. This case we should ask DESCRIBE for help.
             Then how ?
             Full-text helps little as a conclution of our test. But a CONTENT label always tell us what this ware is on earth. We like this label.
             At worset, nothing help above. Then we have to focus on describe content. As a commen sence, the first centence would told us cat or dog, that's enough.
            */

                boolean msgEnough = false;
                // keyword
                String keywords = ware.getKeywords();
                if (keywords != null &&
                        !keywords.trim().isEmpty()) {
                    keywords = keywords.toLowerCase().trim();
                    for (String ele : new HashSet<String>(Arrays.asList(keywords.split("[\\s]{0,},[\\s]{0,}")))) {
                        ele = dealBrandName(ele, ware.getBrandName());
                        ele = castrate(ele, stopSet);
                        if (!ele.isEmpty()) {

                            buffer.append(ele).append(",");

                            index = getTermIndex(ele, dictMap); // this will add ele to dicMap
                            addToMap(index, wareTermMap);
//                        msgEnough = true; // we get kernel msg
                            if (ele.contains(" ")) {
                                for (String cell : ele.split("\\s+")) {
                                    if (FileSteward.HasDigit(cell) ||
                                            stopSet.contains(cell)) {
                                        continue;
                                    }
                                    index = getTermIndex(cell, dictMap);
                                    addToMap(index, wareTermMap);
                                }
                            }
                        }
                    }
                }
                buffer.append("\t");

                // brandName
//                index = getTermIndex(ware.getBrandName().toLowerCase(), dictMap);
//                addToMap(index, wareTermMap);

                // title
                if (!msgEnough &&
                        ware.getTitle() != null) {
                    String title = ware.getTitle().toLowerCase().trim();
                    title = dealBrandName(title, ware.getBrandName()); // TODO more strict drop
                    for (String ele : castrate(title).split("\\s+")) {
                        if (ele.isEmpty() ||
                                FileSteward.HasDigit(ele) ||
                                stopSet.contains(ele) || // here we drop color
                                ele.length() > 20) {
                            continue;
                        }

                        buffer.append(ele).append(" ");

                        index = getTermIndex(ele, dictMap);
                        addToMap(index, wareTermMap);
                        msgEnough = true;
                    }
                    buffer.append("\t");
                }
                // describe
                String describe = ware.getDescribe();
                if (describe != null &&
                        !describe.trim().isEmpty()) {
                    describe = describe.toLowerCase().trim();
                    describe = dealBrandName(describe, ware.getBrandName());

                    String[] describeArrays = describe.split("\001");
                    boolean stop = false;
                    boolean aimAt = false;
                    for (String ele : describeArrays) {
                        if (ele.endsWith("contents:") ||
                                ele.endsWith("content:")) { // we focus on next line
                            aimAt = true;
                            continue;
                        } else if (ele.contains("contents:") ||
                                ele.contains("content:")) { // we focus on current line
                            aimAt = true;
                        }
                        if (aimAt) {
                            String contentKernel = getContentKernel(ele);
                            describeIndexWorker(contentKernel, stopSet, dictMap, type, wareTermMap, buffer);
                            stop = true;
                            break;
                        }
                    }
                    buffer.append("\t");

                    if (!(stop && msgEnough)) {
                        int shortPhraseNum = 0;
                        int innerStop = 0;
                        for (String centence : describeArrays) {
                            // rule 1: drop details
                            if ("details:".equals(centence.trim()) ||
                                    "details".equals(centence.trim())) {
                                break;
                            }
                            // rule 2: // pity but we have walk into short describe sentence jungle
                            if (describeArrays.length > 1 &&
                                    centence.split(" ").length < 10) {
                                shortPhraseNum++;
                                if (shortPhraseNum == 3) {
                                    break;
                                }
                                continue;
                            }
                            if (innerStop == 1) {
                                break;
                            }
                            // XX is a YY
                            String result;
                            for (String ele : centence.split("\\.")) {
                                if (ele.trim().isEmpty()) {
                                    continue;
                                }
                                result = returnSubjectByIsA(ele);
                                if (result != null) {
                                    describeIndexWorker(result, stopSet, dictMap, type, wareTermMap, buffer);
                                    innerStop = 1;
                                    break;
                                }
                            }
                            if (innerStop == 1) {
                                break;
                            }
                            // A XX ...
                            for (String ele : centence.split("\\.")) {
                                if (ele.trim().isEmpty()) {
                                    continue;
                                }
                                result = returnSubjectByA(ele);
                                if (result != null) {
                                    describeIndexWorker(result, stopSet, dictMap, type, wareTermMap, buffer);
                                    innerStop = 1;
                                    break;
                                }
                            }
                            if (innerStop == 1) {
                                break;
                            }
                            // XX is made ..
                            for (String ele : centence.split("\\.")) {
                                if (ele.trim().isEmpty()) {
                                    continue;
                                }
                                result = returnSubjectByIsMade(ele);
                                if (result != null) {
                                    describeIndexWorker(result, stopSet, dictMap, type, wareTermMap, buffer);
                                    innerStop = 1;
                                    break;
                                }
                            }
                            if (innerStop == 1) {
                                break;
                            }

                        }
                    }
                }

                bw.write(buffer.toString() + "\n");

                indexWareList.add(wareTermMap);
            }

            bw.close();
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        FileSteward.storeDict(dictMap, DocLdaActor.wkbt_dict_file);
        if (type == 0) {
            FileSteward.storeIndex(indexWareList, DocLdaActor.lda_input_file, false);

            try {
                fw = new FileWriter(DocLdaActor.wkbt_dict_file.replace(DocLdaActor.wkbt_dict_file_name, "nodeIndex4WH_ex.txt"));
                bw = new BufferedWriter(fw);
                for (int i = 0; i < indexWareList.size(); i++) {
                    bw.write(translateWareList.get(i).getWareId() + "\t");
                    for (int j = 0; j < 5943; j++) {
                        if (indexWareList.get(i).containsKey(j)) {
                            bw.write(indexWareList.get(i).get(j) + "\t");
                        } else {
                            bw.write(0 + "\t");
                        }
                    }
                    bw.write("\r\n");
                }
                bw.close();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            FileSteward.storeIndex(indexWareList, DocLdaActor.lda_input_file.replace("wkbtLda.dat", "wkbtLdaWithoutDescribe.dat"));
        }
    }

    // brand name may be a phrase with space, take into consideration
    private static String dealBrandName(String sentence, String brandName) {
        sentence = sentence.replaceAll(brandName + " ", " ");
        if (brandName.contains(" ") &&
                !sentence.isEmpty()) {
            String[] brandArrays = brandName.split("\\s+");
            for (String ele : brandArrays) {
                if (sentence.contains(" ")) {
                    sentence = sentence.replaceAll(ele + " ", " ");
                } else {
                    sentence = sentence.replaceAll(ele, " ");
                }
            }
        }
        return sentence;
    }

    public static void describeIndexWorker(String contentKernel, Set<String> stopSet, Map<String, Integer> dictMap, int type, Map<Integer, Integer> wareTermMap, StringBuffer buffer) {
        for (String term : castrate(contentKernel).split("\\s+")) {
            if (term.trim().isEmpty() ||
                    stopSet.contains(term) ||
                    FileSteward.HasDigit(term) ||
                    term.length() > 20) {
                continue;
            }
            int index = getTermIndex(term, dictMap);
            if (type == 0) {
                addToMap(index, wareTermMap);
            }
            buffer.append(term).append(" ");
        }
    }

    public static void describeIndexWorker(String contentKernel, Set<String> stopSet, Map<String, Integer> dictMap, int type, Map<Integer, Integer> wareTermMap) {
        for (String term : castrate(contentKernel).split("\\s+")) {
            if (term.trim().isEmpty() ||
                    stopSet.contains(term) ||
                    FileSteward.HasDigit(term) ||
                    term.length() > 20) {
                continue;
            }
            int index = getTermIndex(term, dictMap);
            if (type == 0) {
                addToMap(index, wareTermMap);
            }
        }
    }

    public static int getTermIndex(String term, Map<String, Integer> indexMap) {
        term = Tokenizer.token(term); // get root term
        if (indexMap.containsKey(term)) {
            return indexMap.get(term);
        } else {
//            int index = indexMap.size() + 1; // so we stipulate index start from 1
            int index = indexMap.size(); // so we stipulate index start from 1
            indexMap.put(term, index);
            return index;
        }
    }

    public static void addToMap(int termIndex, Map<Integer, Integer> map) {
        if (!map.containsKey(termIndex)) {
            map.put(termIndex, 1);
        } else {
            map.put(termIndex, map.get(termIndex) + 1);
        }
    }

    public static String castrate(String input) {
        return input.replaceAll("amp;"," ").replaceAll("[^0-9a-zA-Z]"," ").trim();
    }

    // rewrite for keyword phrase, drop stop word and sort by alphabet, so similar phrase may be normalize to one standard
    public static String castrate(String input, Set<String> set) {
        input = castrate(input);
        String[] inputArray = input.split(" ");
        for (int i = 0; i < inputArray.length; i++) {
            if (set.contains(inputArray[i])) {
                inputArray[i] = "";
            }
        }
        Arrays.sort(inputArray);
        StringBuffer result = new StringBuffer();
        for (String ele : inputArray) {
            if (!ele.trim().isEmpty()) {
                result.append(ele.trim()).append(" ");
            }
        }
        return result.toString().trim();
    }

    public static String getContentKernel(String content) {
        if (content.indexOf("contents:") != -1 ||
                content.indexOf("content:") != -1) {
            content = content.split(":", 2)[1].trim(); // case -- Contents: Milk powder container 1 piece
        }
        if (content.contains(",")) {
            content = content.split(",")[0].trim(); // case -- Contents: 1 bottle Wide Neck Milk with Dot Size 2-M, 1 Bottle Bottle with Soft Silicone Spout
        }
        // TODO more ..
//        System.out.println("content:\t" + content);
        return content;
    }

    public static String getsentenceKernel(String sentence, WareMsgTranslate ware) {
        StringBuffer result = new StringBuffer();
        sentence = sentence.replace(ware.getBrandName(), " ");
        String[] sentenceArray = sentence.split("\\.");
        // first sentence
        if (sentenceArray[0].contains(" is a")) { // case -- Silvercross ... Britannia is a baby stroller made from quality materials so durable and durable.
            String str = sentenceArray[0].split(" is ", 2)[1];
            str = stopByPrep(str);
            result.append(str).append(" ");
        } else if (sentenceArray[0].startsWith("a ") ||
                sentenceArray[0].contains("an ")) {
            result.append(stopByPrep(sentenceArray[0])).append(" ");
        } else { // do not get a "is"
            int titleTermLen = ware.getTitle().split("\\s+").length;
            String[] firstsentenceArray = sentenceArray[0].split("\\s+");
            if (titleTermLen >= firstsentenceArray.length) { // sentence short than title, we guess it is useless
                return "";
            }
            for (int i = titleTermLen; i < firstsentenceArray.length; i++) { // TODO we'd better limit times of this loop
                if (!prepSet.contains(firstsentenceArray[i])) {
                    result.append(firstsentenceArray[i]).append(" ");
                } else {
                    break;
                }
            }
        }
        System.out.println("sentence 1st:\t" + result.toString().trim());
        // secend sentence
        if (sentenceArray.length > 1 &&
                sentenceArray[1].contains(" is ") &&
                sentenceArray[1].indexOf(" is ") <= 20) {
            result.append(sentenceArray[1].substring(0, sentenceArray[1].indexOf(" is ")));
            System.out.println("sentence 2nd:\t" + sentenceArray[1].substring(0, sentenceArray[1].indexOf(" is ")).trim());
        } else if (sentenceArray.length > 1 &&
                sentenceArray[1].contains(" are ") &&
                sentenceArray[1].indexOf(" are ") <= 20) {
            result.append(sentenceArray[1].substring(0, sentenceArray[1].indexOf(" are ")));
            System.out.println("sentence 2nd:\t" + sentenceArray[1].substring(0, sentenceArray[1].indexOf(" are ")).trim());
        }


        return result.toString().trim();
    }

    public static String stopByPrep(String sentence) {
        StringBuffer result = new StringBuffer();
//        sentence = sentence.split("\\.|,")[0];
        String[] arrays = sentence.split("\\s+");
        int i = 0;
        for (String ele :arrays) {
            if (!prepSet.contains(ele) ||
                    i < 5) { // protect size
                i++;
                result.append(ele).append(" ");
            } else {
                return result.toString().trim();
            }
        }
        return result.toString().trim();
    }

    // method list to judge kernel
    public static String returnSubjectByIsA(String sentence) {
        DescriptionMsgGrabber dmg1 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.contains_later, " is a ", 1);
        DescriptionMsgGrabber dmg2 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.contains_later, " is an ", 2);
        List<DescriptionMsgGrabber> dmgList = new ArrayList<>();
        dmgList.add(dmg1);
        dmgList.add(dmg2);
        return returnSubjectFromDmgList(dmgList, sentence);
    }

    public static String returnSubjectByA(String sentence) {
        DescriptionMsgGrabber dmg1 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.startWith, "a ", 1);
        DescriptionMsgGrabber dmg2 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.startWith, "an ", 1);
        DescriptionMsgGrabber dmg3 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.startWith, "is a ", 2);
        DescriptionMsgGrabber dmg4 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.startWith, "is an ", 2);
        List<DescriptionMsgGrabber> dmgList = new ArrayList<>();
        dmgList.add(dmg1);
        dmgList.add(dmg2);
        dmgList.add(dmg3);
        dmgList.add(dmg4);
        return returnSubjectFromDmgList(dmgList, sentence);
    }

    public static String returnSubjectByIsMade(String sentence) {
        DescriptionMsgGrabber dmg1 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.contains_former, " is made ", 1);
        DescriptionMsgGrabber dmg2 = new DescriptionMsgGrabber(DescriptionMsgGrabber.GrabType.contains_former, " are made ", 2);
        List<DescriptionMsgGrabber> dmgList = new ArrayList<>();
        dmgList.add(dmg1);
        dmgList.add(dmg2);
        return returnSubjectFromDmgList(dmgList, sentence);
    }

    public static String returnSubject(DescriptionMsgGrabber dmg, String sentence) {
        if (dmg.getType() == DescriptionMsgGrabber.GrabType.startWith) {
            if (sentence.startsWith(dmg.getFitString())) {
                return stopByPrep(sentence);
            }
            return null;
        }
        if (dmg.getType() == DescriptionMsgGrabber.GrabType.contains_former) {
            if (sentence.contains(dmg.getFitString())) {
                return stopByPrep(sentence);
            }
            return null;
        }
        if (dmg.getType() == DescriptionMsgGrabber.GrabType.contains_later) {
            if (sentence.contains(dmg.getFitString())) {
                return stopByPrep(sentence.substring(sentence.indexOf(dmg.getFitString())));
            }
            return null;
        }
        if (dmg.getType() == DescriptionMsgGrabber.GrabType.endWith) {
            // TODO
            return null;
        }
        return null;
    }


    public static String returnSubjectFromDmgList(List<DescriptionMsgGrabber> dmgList, String sentence) {
        Collections.sort(dmgList, new Comparator<DescriptionMsgGrabber>() {
            @Override
            public int compare(DescriptionMsgGrabber o1, DescriptionMsgGrabber o2) {
                return o1.getSort() - o2.getSort();
            }
        });
        String result = null;
        for (DescriptionMsgGrabber dmg : dmgList) {
            result = returnSubject(dmg, sentence);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
