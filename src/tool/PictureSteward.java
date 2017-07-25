package tool;

import actor.DocLdaActor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class PictureSteward {

    public final static String picture_prefix_path = "http://img20.jd.id/Indonesia/s172x172_///img20.jd.id/Indonesia/";

    public static void main(String[] args) {
//        long categoryId = 75061382;
        long categoryId = 75061316;
        DocLdaActor.initalPath(categoryId);

        // rename all picture 2 orignal
        picturesRename(DocLdaActor.prefix_path + "pic_" + categoryId);

        List<WareMsgConventor> wareMsgList = FileSteward.getWareMsgList(DocLdaActor.wkbt_file);
        System.out.println("Begin picture download, wareMsgList size is " + wareMsgList.size());
        String url = "";
        String path = "";
        int i = 0;
        for (WareMsgConventor wareMsg : wareMsgList) {
            url = picture_prefix_path + wareMsg.getImgUri();
            path = DocLdaActor.prefix_path + "pic_" + categoryId + "\\" + wareMsg.getWareId() + ".jpg";

            int time = 0;
            if (!(new File(path).exists())) {
                while (!(new File(path).exists()) &&
                        time < 3) {
                    downloadPicture(url, path);
                }
                if (new File(path).exists()) {
                    System.out.println("picture " + i++ + " download to local finished.");
                } else {
                    System.out.println("picture " + i++ + " download to local finished. WareId is " + wareMsg.getWareId());
                }
            }
        }
    }
    //链接url下载图片
    private static void downloadPicture(String onlineUrl, String localPath) {
        URL url = null;

        try {
            url = new URL(onlineUrl);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());

            String imageName = localPath; // like "F:/test.jpg"

            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            byte[] context=output.toByteArray();
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void picturesRename(String path, Map<Long, Number> map) {
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("\\_|\\.");
                if (elements.length == 3) {
                    newFileName = map.get(Long.parseLong(elements[1]))
                            + "_"+ elements[0] // leafcate_topid_wareId.jpg
                            + "_" + elements[1] + "." + elements[2];
//                    newFileName = elements[1] + "." + elements[2];
                } else if (elements.length == 2) {
                    newFileName = map.get(Long.parseLong(elements[0])) + "_" + elements[0] + "." + elements[1];
//                    newFileName = elements[0] + "." + elements[1];
                } else {
                    int size = elements.length;
//                    newFileName = elements[0]+ "_" + map.get(Long.parseLong(elements[size - 2])) + "_" + elements[size - 2] + "." + elements[size - 1];
                    newFileName = map.get(Long.parseLong(elements[size - 2])) + "_" + elements[size - 3] + "_" + elements[size - 2] + "." + elements[size - 1];
//                    newFileName = map.get(Long.parseLong(elements[size - 2])) + "_" + elements[size - 2] + "." + elements[size - 1];
                }
                File newFile = new File(path + "\\" + newFileName);
                oriFile.renameTo(newFile);
            }
        }
    }

    public static void picturesRename2Ori(String path) {
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("\\_|\\.");
                int size = elements.length;
                newFileName = elements[size - 2] + "." + elements[size - 1];
                File newFile = new File(path + "\\" + newFileName);
                oriFile.renameTo(newFile);
            }
        }
    }

    public static void picturesRenameMergeCell(String path, Map<String, Integer> map) {
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("\\_");
                if (elements.length == 3) {
                    newFileName = map.get(elements[0] + "_" + elements[1])
                            + "_"+ elements[0]
                            + "_" + elements[1]
                            + "_" + elements[2];
                } else if (elements.length == 4) {
                    newFileName = map.get(elements[1] + "_" + elements[2])
                            + "_"+ elements[1]
                            + "_" + elements[2]
                            + "_" + elements[3];
                }
                File newFile = new File(path + "\\" + newFileName);
                oriFile.renameTo(newFile);
            }
        }
    }


    public static void picturesRename(String path) {
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("\\_|\\.");
                if (elements.length == 3) {
                    newFileName = elements[1] + "." + elements[2];
                } else if (elements.length == 2) {
                    newFileName = elements[0] + "." + elements[1];
                } else {
                    continue;
                }
                File newFile = new File(path + "\\" + newFileName);
                oriFile.renameTo(newFile);
            }
        }
    }
}
