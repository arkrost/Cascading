package ifmo.cascading;

import java.util.*;

/**
 * @author Arkadii Rost
 */
public class Cascade<T> {
    private final Comparator<T> comparator;
    private final List<T> val;
    private final int[] from;
    private final List<Cascade<T>> next;
    private final int loadFactor;

    /*
     * Create leaf cascading node.
     *
     * Assume that {@code val} is not sorted by comparator.
     */
    public Cascade(Comparator<T> comparator, int loadFactor, List<T> val) {
        this.comparator = comparator;
        this.loadFactor = loadFactor;
        this.val = getSortedCopy(val, comparator);
        from = new int[val.size()];
        next = Collections.emptyList();
    }


    /*
     * Create cascade node by cascading values from nodes {@code next} into self values {@val}.
     *
     * Assume that {@code val} is not sorted by comparator.
     */
    public Cascade(Comparator<T> comparator, int loadFactor, List<T> val, List<Cascade<T>> next) {
        this.comparator = comparator;
        this.loadFactor = loadFactor;
        this.next = next;
        int cascadedLength = val.size();
        for (Cascade<T> c : next)
            cascadedLength += c.val.size() / c.loadFactor;
        this.val = new ArrayList<>(cascadedLength);
        this.from = new int[cascadedLength];
        // cascading
        int selfIndex = 0;
        List<T> sortedVal = getSortedCopy(val, comparator);
        int[] indexes = new int[next.size()];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = next.get(i).loadFactor - 1;
        for (int i = 0; i < cascadedLength; i++) {
            T minVal = null;
            int minInd = -1;
            // check self value
            if (selfIndex < val.size()) {
                minInd = 0;
                minVal = sortedVal.get(selfIndex);
            }
            for (int j = 0; j < next.size(); j++) {
                List<T> nextVal = next.get(j).val;
                if (indexes[j] < nextVal.size()) { // has next value
                    T guess = nextVal.get(indexes[j]);
                    if (minVal == null || comparator.compare(guess, minVal) < 0) {
                        minVal = guess;
                        minInd = j + 1;
                    }
                }
            }
            this.val.add(minVal);
            this.from[i] = minInd;
            // update indexes
            if (minInd == 0) { //self update
                selfIndex++;
            } else {
                int j = minInd - 1;
                indexes[j] += next.get(j).loadFactor;
            }
        }
    }

    public static <T> Cascade<T> cascade(Comparator<T> comparator, List<List<T>> values) {
        if (values == null || values.isEmpty())
            return null;
        int loadFactor = 2;
        int lastInd = values.size() - 1;
        Cascade<T> cur = new Cascade<>(comparator, loadFactor, values.get(lastInd));
        ListIterator<List<T>> iterator = values.listIterator(lastInd);
        while (iterator.hasPrevious())
            cur = new Cascade<>(comparator, loadFactor, iterator.previous(), Collections.singletonList(cur));
        return cur;
    }

    public List<T> getVal() {
        return val;
    }

    private List<T> getSortedCopy(List<T> val, Comparator<T> comparator) {
        List<T> sorted = new ArrayList<>(val);
        sorted.sort(comparator);
        return sorted;
    }
}
