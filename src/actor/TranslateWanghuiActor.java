package actor;

import tool.FileSteward;
import tool.WareMsgConventor;
import tool.WareMsgTranslate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zhangshangzhi on 2017/8/15.
 * Server for Wang Hui
 */
public class TranslateWanghuiActor {

    private static List<FutureTask<List<WareMsgTranslate>>> futureTasks;
    private static Map<String, String> transMap;
    private static ExecutorService executorService;

    public TranslateWanghuiActor () {
        futureTasks = new ArrayList<FutureTask<List<WareMsgTranslate>>>();
        executorService = Executors.newFixedThreadPool(5);
        FileSteward fileSteward = new FileSteward("");
        transMap = fileSteward.dictReader("describ.dic");
    }

    public static void main(String[] args) {
        DocLdaActor.init();

        // get ware set
        Set<Long> wareIdSet = new HashSet<>();
        for (WareMsgConventor ware : FileSteward.getWareMsgList(DocLdaActor.wkbt_file)) {
            wareIdSet.add(ware.getWareId());
        }
        // get Wanghui wareMsg
        List<WareMsgConventor> wareMsgList = getWanghuiWareList(wareIdSet);

        /*
        TranslateWanghuiActor translateWanghuiActor = new TranslateWanghuiActor();

        List<WareMsgTranslate> transWareList = new ArrayList<>();
        List<Integer> indexList = cutPiece(wareMsgList.size(), 10);
        for (int i = 0; i < indexList.size() - 1; i++) {
            int taskStartNum = indexList.get(i);
            int taskEndNum = indexList.get(i + 1);
            List<WareMsgConventor> currentList = wareMsgList.subList(taskStartNum, taskEndNum);
            transWareList.addAll(translateWanghuiActor.exec(currentList));
            System.out.println("### " + (i+1) + "th group finish..");
        }
        executorService.shutdown();
        */

        List<WareMsgTranslate> transWareList = TranslateActor.singleTaskMethod(wareMsgList);

        FileSteward.storeTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transDocDescribe.txt"), transWareList);
    }

    public static List<WareMsgConventor> getWanghuiWareList(Set<Long> wareIdSet) {
        // get wang's ware msg
        List<WareMsgConventor> wareMsgList = new ArrayList<>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String[] eles;
            WareMsgConventor ware;
            fis = new FileInputStream(DocLdaActor.wkbt_file.replace("wkbt.txt", "doc_describ.txt"));
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                ware = new WareMsgConventor();
                eles = str.split("\001", 3);
                ware.setWareId(Long.parseLong(eles[0]));
                if (wareIdSet.contains(Long.parseLong(eles[0]))) {
                    ware.setTitle(eles[1]);
                    ware.setDescribe(eles[2]);
                    wareMsgList.add(ware);
                }
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wareMsgList;
    }

    public List<WareMsgTranslate> exec(List<WareMsgConventor> wareList) {
        for (WareMsgConventor ware : wareList) {
            TransCallable callable = new TransCallable(ware);
            //创建一个异步任务
            FutureTask<List<WareMsgTranslate>> futureTask = new FutureTask<List<WareMsgTranslate>>(callable);
            futureTasks.add(futureTask);
            executorService.submit(futureTask);
        }
        return getThreadResult();
    }

    class TransCallable implements Callable<List<WareMsgTranslate>> {
        private WareMsgConventor ware;

        public TransCallable(WareMsgConventor ware) {
            this.ware = ware;
        }

        @Override
        public List<WareMsgTranslate> call() throws Exception {
            TranslateActor.translate(ware, transMap);
            return new ArrayList<WareMsgTranslate>() {{
                add(new WareMsgTranslate(ware));
            }};
        }
    }

    private List<WareMsgTranslate> getThreadResult() {
        List<WareMsgTranslate> respListList = new ArrayList<WareMsgTranslate>();
        for (FutureTask<List<WareMsgTranslate>> futureTask : futureTasks) {
            //该方法有一个重载get(long timeout, TimeUnit unit) 第一个参数为最大等待时间，第二个为时间的单位
            try {
                respListList.addAll(futureTask.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        futureTasks.clear();
        return respListList;
    }

    private static List<Integer> cutPiece(int totalNum, int listSize) {
        List<Integer> seperateIntList = new ArrayList<Integer>();
        int startIndex = 0;
        while (startIndex < totalNum) {
            seperateIntList.add(startIndex);
            startIndex += listSize;
        }
        seperateIntList.add(totalNum);
        return seperateIntList;
    }
}
