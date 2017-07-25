package actor;

import tool.CosineDistance;
import tool.FileSteward;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by zhangshangzhi on 2017/8/8.
 */
public class KmeansActor {

    public static final int clusterNum = 150;

    public static void main(String[] args) {

        DocLdaActor.init();

        Instances ins = null;
        SimpleKMeans KM = null;
        DistanceFunction disFun = null;

        try {
            // 读入样本数据
            String normalizeGammaFilePath = FileSteward.getTargKmeansInputFilePath(DocLdaActor.da_model_path, true); // normalize here
            File file = new File(normalizeGammaFilePath);
            ArffLoader loader = new ArffLoader();
            loader.setFile(file);
            ins = loader.getDataSet();

            // 初始化聚类器 （加载算法）
            KM = new SimpleKMeans();
            KM.setDistanceFunction(new CosineDistance());
            KM.setNumClusters(clusterNum);       //设置聚类要得到的类别数量
            KM.buildClusterer(ins);     //开始进行聚类

//            KM.setDisplayStdDevs(false);
//            KM.setDistanceFunction(new EuclideanDistance());
//            KM.setMaxIterations(500);
//            KM.setDontReplaceMissingValues(true);
//            KM.setPreserveInstancesOrder(false);
//            KM.setSeed(100);



            System.out.println(KM.preserveInstancesOrderTipText());
            // 打印聚类结果
            System.out.println(KM.toString());
            dealPrintResult(KM.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> idList = DocLdaActor.ldaPlusKmeans();
        FileSteward.mergTopic2LeafCateId(DocLdaActor.wkbt_file, DocLdaActor.ori_fix_file, idList);
        DocLdaActor.kmeansWatchActor();
    }

    private static void dealPrintResult(String result) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(DocLdaActor.wkbt_file.replace("wkbt.txt", "kmeansKernel.txt"));
            bw = new BufferedWriter(fw);

            String[] eles = result.split("\\n");
            int start = 0;
            for (String ele : eles) {
                if (ele.contains("=================")) {
                    start = 1;
                    continue;
                }
                if (start == 1 &&
                        !"".equals(ele.trim())) {
                    bw.write(ele + "\n");
                }
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
