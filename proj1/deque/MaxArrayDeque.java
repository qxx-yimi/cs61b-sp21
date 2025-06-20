package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    public T max() {
        return maxHelp(comparator);
    }

    public T max(Comparator<T> c) {
        return maxHelp(c);
    }

    private T maxHelp(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T curItem = get(i);
            if(c.compare(curItem, maxItem) > 0) {
                maxItem = curItem;
            }
        }
        return maxItem;
    }
}
