package actor;

import tool.FileSteward;

/**
 * Created by zhangshangzhi on 2017/9/7.
 */
public class BatchActor {

    public static void main(String[] args) {

//        PictureSteward.main(new String[0]);
//
//        DocLdaActor.watchActor();
//
//        TranslateActor.main(new String[0]);
//
//        KTDActor.main(new String[0]);
//
//        KmeansActor.main(new String[0]);

        DocLdaActor.main(new String[0]);


    }

    public static void test() {
        DocLdaActor.init();
        String path = DocLdaActor.prefix_path + "pic_" + DocLdaActor.categoryId + "\\picStore_" + DocLdaActor.categoryId;
        FileSteward.storeNunTermSummaryFromFitDegree(path + "\\fitDegree.txt", path + "\\cellNunSummary.txt");
    }
}
