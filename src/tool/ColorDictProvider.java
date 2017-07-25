package tool;

import java.util.Set;

/**
 * Created by zhangshangzhi on 2017/8/3.
 */
public class ColorDictProvider {

    private Set<String> colorSet;
    private static ColorDictProvider colorDictProvider;

    private ColorDictProvider() {
        FileSteward fileSteward = new FileSteward(" ");
        colorSet = fileSteward.getColorSet();
    }

    public static ColorDictProvider getInstance() {
        if (colorDictProvider == null) {
            colorDictProvider = new ColorDictProvider();
        }
        return colorDictProvider;
    }

    public Set<String> getColorSet() {
        return colorSet;
    }
}
