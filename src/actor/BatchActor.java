package actor;

import tool.FileSteward;

/**
 * Created by zhangshangzhi on 2017/9/7.
 * This is final.
 */
public class BatchActor {

    public static void main(String[] args) {

        DocLdaActor.init(75061132);

//        PictureSteward.main(new String[0]);

//        DocLdaActor.watchActor();
//
//        TranslateActor.main(new String[0]);
//
//        KTDActor.main(new String[0]);
//
//        KmeansActor.main(new String[0]);
//
//        DocLdaActor.main(new String[0]);

        test2();
    }

    public static void test() {
        DocLdaActor.init();
        String path = DocLdaActor.prefix_path + "pic_" + DocLdaActor.categoryId + "\\picStore_" + DocLdaActor.categoryId;
        FileSteward.storeNunTermSummaryFromFitDegree(path + "\\fitDegree.txt", path + "\\cellNunSummary.txt");
    }

    public static void test2() {
        DocLdaActor.init(75061132);
        DocLdaActor.kmeansWatchActor();
    }
}
