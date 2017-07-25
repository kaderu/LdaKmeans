package tool;

/**
 * Created by zhangshangzhi on 2017/8/24.
 */
public class DescriptionMsgGrabber {

    public enum GrabType {
        startWith,
        contains_former,
        contains_later,
        endWith
    }

    public DescriptionMsgGrabber(GrabType type, String fitString, int sort) {
        this.type = type;
        this.fitString = fitString;
        this.sort = sort;
    }

    private GrabType type;
    private String fitString;
    private int sort;

    public GrabType getType() {
        return type;
    }

    public void setType(GrabType type) {
        this.type = type;
    }

    public String getFitString() {
        return fitString;
    }

    public void setFitString(String fitString) {
        this.fitString = fitString;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
