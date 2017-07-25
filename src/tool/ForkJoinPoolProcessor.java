package tool;

import actor.TranslateActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 * Created by zhangshangzhi on 2017/8/10.
 */
public class ForkJoinPoolProcessor extends RecursiveTask<Map<Long, List<Object>>> {

    private List<WareMsgConventor> list;

    private int start;
    private int end;
    private int max;

    public ForkJoinPoolProcessor(int start, int end, int max, List<WareMsgConventor> list) {
        this.list = list;
        this.start = start;
        this.end = end;
        this.max = max;
    }

    @Override
    protected Map<Long, List<Object>> compute() {
        Map<Long, List<Object>> map = new HashMap<>();
        if (end - start < max) {
            List<WareMsgConventor> objects = list.subList(start, end);
            return processor(objects);
        } else {
            // split task to two half
            int middle = (start + end) / 2;
            ForkJoinPoolProcessor left = new ForkJoinPoolProcessor(start, middle, max, list);
            ForkJoinPoolProcessor right = new ForkJoinPoolProcessor(middle, end, max, list);
            left.fork();
            right.fork();
            Map<Long, List<Object>> leftMap = left.join();
            Map<Long, List<Object>> rightMap = right.join();
            leftMap.putAll(rightMap);
            return leftMap;
        }
    }

    public Map<Long, List<Object>> processor(List<WareMsgConventor> list) {
        Map<Long, List<Object>> map = new HashMap<>();
        for (WareMsgConventor ware : list) {
            TranslateActor.translate(ware);
            List<Object> valueList = new ArrayList<>();
            valueList.add(ware);
            map.put(ware.getWareId(), valueList);
        }
        return map;
    }
}
