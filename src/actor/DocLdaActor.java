package actor;

import org.knowceans.lda.LdaEstimate;
import tool.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class DocLdaActor {

    public static final String prefix_path = "C:\\Users\\zhangshangzhi\\Desktop\\pic\\";
    public static final String wkbt_file_name = "wkbt.txt";
    public static final String wkbt_dict_file_name = "word_index.txt";
    public static final String lda_input_file_name = "wkbtLda.dat";
    public static final String lda_model_path_name = "wkbtLda.model";
    public static final String ori_vs_cur_file_name = "oriVsCur.txt";
    public static final String ori_fix_file_name = "oriFix.txt";

    public static final int clusterNum = 120;

    public static String wkbt_file;
    public static String wkbt_dict_file;
    public static String lda_input_file;
    public static String da_model_path;
    public static String ori_vs_cur_file;
    public static String ori_fix_file;

    public static long categoryId;

    public static void init() {
        // step.1 determine a category_id, let's say, 75061382
        categoryId = 75061316;
        initalPath(categoryId);
        System.out.println("**************************");
        System.out.println("step.1 initalPath finish...");
    }

    public static void init(long categoryId) {
        // step.1 determine a category_id, let's say, 75061382
        initalPath(categoryId);
        System.out.println("**************************");
        System.out.println("step.1 initalPath finish...");
    }


    public static void main(String[] args) {
        init();
        // provide us cellClusterTree.txt
        FileSteward.storeCellClusterTree(ori_fix_file);

        mergeNodeIndx();
        watchRootNodeActor();

//        LateWork.picturePathReset(prefix_path + "pic_" + categoryId);

//        rootNodeTermCollector();


//        rootNodeTermCollector();

//          actor();
//        watchActor();
//          lateWorkActor();

//          merge();
//        watchNodeActor();

//          watchMergeCellActor();

//        kmeansWatchActor();

//        ldaPlusKmeans();

//        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
//        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);

//        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
//        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);

//        lateWorkActor();
    }

    public static void actor() {

        // step.2 get wareId_keyword_brandName_title from mysql online, store as wkbt.txt in PATH C:\Users\zhangshangzhi\Desktop\pic\pic_75061382
        // help yourself do this.

        // step.3 gain index file named wkbtLda.dat as input of LDA model
//        IndexSteward.index(wkbt_file, wkbt_dict_file, lda_input_file);
        IndexSteward.index(wkbt_file, wkbt_dict_file, lda_input_file);
        System.out.println("**************************");
        System.out.println("step.3 gain index file finish...");

        // step.4 work LDA and get lda.model
        FileSteward.delete(da_model_path);
        String[] args = new String[]{"est", "0.5", String.valueOf(clusterNum), "settings.txt",
                lda_input_file, "seeded", da_model_path};
        LdaEstimate.main(args);
        System.out.println("**************************");
        System.out.println("step.4 LDA work finish...");

        // step.5 if we have prepared picture locally, then this method will help group them.
        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
        System.out.println("**************************");
        System.out.println("step.5 picture group finish...");

        // step.6 aim to compare status of clustering, put orignal and current status to an file, namely, oriVsCur.csv
        FileSteward.mergTopic2LeafCateId(wkbt_file, ori_vs_cur_file, map);
        System.out.println("**************************");
        System.out.println("step.6 compare status finish, see in oriVsCur.csv ...");
    }

    public static void actor4Describe() {
        // step.4 work LDA and get lda.model
        FileSteward.delete(da_model_path);
        String[] args = new String[]{"est", "0.5", String.valueOf(clusterNum), "settings.txt",
                lda_input_file, "seeded", da_model_path};
        LdaEstimate.main(args);
        System.out.println("**************************");
        System.out.println("step.4 LDA work finish...");

        // step.5 if we have prepared picture locally, then this method will help group them.
        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
        System.out.println("**************************");
        System.out.println("step.5 picture group finish...");

        // step.6 aim to compare status of clustering, put orignal and current status to an file, namely, oriVsCur.csv
        FileSteward.mergTopic2LeafCateId(wkbt_file, ori_vs_cur_file, map);
        System.out.println("**************************");
        System.out.println("step.6 compare status finish, see in oriVsCur.csv ...");
    }

    // this actor use to rename picture with leaf_category_id
    public static void watchActor() {
        Map<Long, Number> map = FileSteward.mergLeafCate2WareId(wkbt_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
    }

    // watcher for kmeans, before which we'd replace topic_id with kmeans_cluster_id
    public static void kmeansWatchActor() {
        LateWork lateWork = new LateWork(ori_fix_file);
        List<Ware4LateWork> wareList = lateWork.getWareList();
        Map<Long, Number> map = new HashMap<>();
        for (Ware4LateWork ware : wareList) {
            map.put(ware.getWareId(), ware.getTopicId());
        }
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
    }


    public static void watchMergeCellActor() {
        long categoryId = 75061333;
        initalPath(categoryId);
//        Map<String, Integer> map = FileSteward.getMergeCellMap(wkbt_file.replace("wkbt.txt", "mergeCateCell.txt"));
        Map<String, Integer> map = FileSteward.getMergeCellMap(wkbt_file.replace("wkbt.txt", "mergeCateCell.txt"), "cate");
        PictureSteward.picturesRenameMergeCell(prefix_path + "pic_" + categoryId, map);
    }

    public static void initalPath(long categoryId) {
        wkbt_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_file_name;
        wkbt_dict_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_dict_file_name;
        lda_input_file = prefix_path + "pic_" + categoryId + "\\" + lda_input_file_name;
        da_model_path = prefix_path + "pic_" + categoryId + "\\" + lda_model_path_name;
        ori_vs_cur_file = prefix_path + "pic_" + categoryId + "\\" + ori_vs_cur_file_name;
        ori_fix_file = prefix_path + "pic_" + categoryId + "\\" + ori_fix_file_name;
    }

    // get dealed title wanghui need
    public static void wareTitleFile() {
        FileSteward.dealWareTitleFile(wkbt_file, wkbt_file.replace("wkbt.txt", "wkbt_ex.txt"));
    }

    // put piece together
    public static void merge() {
        MergeSteward mergeSteward = new MergeSteward(ori_fix_file);
//        mergeSteward.maidCaptain("topic");
        mergeSteward.maidCaptain("cate");
    }

    // provide node index
    public static void mergeNodeIndx() {
        MergeSteward mergeSteward = new MergeSteward(ori_fix_file);
//        mergeSteward.mergeNode();
        mergeSteward.mergeNodeEx(); // ex will merge dush by leaf_category and brandName
    }



    public static void watchNodeActor() {
        // ask for cellClusterNode.txt copy from cellClusterTree.txt which add 2 node columns to the tail
        Map<Long, Number> map = FileSteward.mergeNode2WareId(ori_fix_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
    }

    public static void watchRootNodeActor() {
        // merge dush cell to root cell, then we wanna have a look
        Map<Long, Number> map = FileSteward.mergeRootCell2WareId(ori_fix_file);
        mapReindex(map);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);

        // TODELETE
        MergeSteward mergeSteward = new MergeSteward(ori_fix_file);
        Map<Integer, Integer> linkIdMap = mergeSteward.rootCellTermCollector(map);
        for (long wareId : map.keySet()) {
            map.put(wareId, linkIdMap.get(map.get(wareId)));
        }

    }

    // an ex version on watchRootNodeActor(), return final cluster after merge dush and root_inner_merge
    public static void rootNodeTermCollector() {
        Map<Long, Number> map = FileSteward.mergeRootCell2WareId(ori_fix_file);
        MergeSteward mergeSteward = new MergeSteward(ori_fix_file);
        Map<Integer, Integer> linkIdMap = mergeSteward.rootCellTermCollector(map);
        for (long wareId : map.keySet()) {
            map.put(wareId, linkIdMap.get(map.get(wareId)));
        }
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
    }

    public static List<Integer> ldaPlusKmeans() {
        List<double[]> kernelList = FileSteward.getKmeansKernelList(wkbt_file.replace("wkbt.txt", "kmeansKernel.txt"));
        List<double[]> gammaList = FileSteward.getGammaTopicSimlarList(FileSteward.getTargGammaFilePath(da_model_path));
        List<Integer> kmeansClusterIdList = new ArrayList<>();
        for (double[] gammaArray : gammaList) {
            int kmeansClusterId = tellClusterId(gammaArray, kernelList);
//            System.out.println(kmeansClusterId);
            kmeansClusterIdList.add(kmeansClusterId);
        }
        return kmeansClusterIdList;
    }

    private static int tellClusterId(double[] gammaArray, List<double[]> kernelList) {
        double distance = 0;
        int index = 0;
        for (int i = 0; i < kernelList.size(); i++) {
            double curDistance = getDistance(gammaArray, kernelList.get(i));
            if (distance > curDistance ||
                    distance == 0) {
                distance = curDistance;
                index = i;
            }
        }
        return index;
    }

    private static double getDistance(double[] gammaArray, double[] kernelArray) {
        if (gammaArray.length != kernelArray.length) {
            return 0;
        } else {
            double distance = 0;
            for (int i = 0; i < gammaArray.length; i++) {
                distance += (gammaArray[i] - kernelArray[i]) * (gammaArray[i] - kernelArray[i]);
            }
            return distance;
        }
    }

    private static void mapReindex(Map<Long, Number> map) {
        int index = 0;
        Map<Number, Number> reIndexMap = new HashMap<>();
        for (long wareId : map.keySet()) {
            if (!reIndexMap.containsKey(map.get(wareId))) {
                reIndexMap.put(map.get(wareId), index);
                index++;
            }
            map.put(wareId, reIndexMap.get(map.get(wareId)));
        }
    }

}
