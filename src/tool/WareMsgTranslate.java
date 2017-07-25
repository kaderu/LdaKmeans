package tool;

/**
 * Created by zhangshangzhi on 2017/8/10.
 */
public class WareMsgTranslate {

    // wareId + keyword + brandName + title + describe + detail
    private long wareId;
    private String keywords;
    private String brandName;
    private String title;
    private String describe;
    private String detail;

    public WareMsgTranslate() {

    }

    public WareMsgTranslate(WareMsgConventor ware) {
        wareId = ware.getWareId();

        StringBuffer keywords = new StringBuffer();
        if (ware.getKeywords() != null) {
            for (String keyword : ware.getKeywords()) {
                keywords.append(keyword).append(",");
            }
        }
        this.keywords = keywords.toString();

        if (ware.getWareMsg() == null) {
            brandName = "";
        } else {
            brandName = ware.getBrandName();
        }

//        StringBuffer title = new StringBuffer();
//        for (String cell : ware.getTitleCells()) {
//            title.append(cell).append(" ");
//        }
        this.title = ware.getTitle();

        this.describe = ware.getDescribe();

        // TODO maybe we'll get details from describe
        this.detail = "";
    }

    public long getWareId() {
        return wareId;
    }

    public void setWareId(long wareId) {
        this.wareId = wareId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
