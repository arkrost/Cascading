package ifmo.cascading;

import java.util.*;

/**
 * @author Arkadii Rost
 */
public class Cascade<T> {
    private static final int UNDEFINED = -1;
    private final Comparator<T> comparator;
    private final List<T> val;
    private final int[] cascadeIndex;
    private final int[] innerIndexes;
    private final List<Cascade<T>> next;
    private final int loadFactor;
    private final int[] selfLinks;
    private final int[][] nextLinks;

    /*
     * Create leaf cascading node.
     *
     * Assume that {@code val} is not sorted by comparator.
     */
    private Cascade(Comparator<T> comparator, int loadFactor, List<T> val) {
        this.comparator = comparator;
        this.loadFactor = loadFactor;
        this.val = getSortedCopy(val);
        cascadeIndex = new int[val.size()];
        Arrays.fill(cascadeIndex, UNDEFINED);
        innerIndexes = new int[val.size()];
        Arrays.fill(innerIndexes, UNDEFINED);
        next = Collections.emptyList();
        nextLinks = new int[][]{};
        selfLinks = new int[val.size()];
        selfLinks[selfLinks.length - 1] = UNDEFINED;
        for (int i = 0; i < selfLinks.length - 1; i++)
            selfLinks[i] = i + 1;
    }


    /*
     * Create cascade node by cascading values from nodes {@code next} into self values {@val}.
     *
     * Assume that {@code val} is not sorted by comparator.
     */
    private Cascade(Comparator<T> comparator, int loadFactor, List<T> val, List<Cascade<T>> next) {
        this.comparator = comparator;
        this.loadFactor = loadFactor;
        this.next = next;
        int cascadedLength = val.size();
        for (Cascade<T> c : next)
            cascadedLength += c.val.size() / c.loadFactor;
        this.val = new ArrayList<>(cascadedLength);
        this.cascadeIndex = new int[cascadedLength];
        this.innerIndexes = new int[cascadedLength];
        // cascading
        int selfIndex = 0;
        List<T> sortedVal = getSortedCopy(val);
        int[] indexes = new int[next.size()];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = next.get(i).loadFactor - 1;
        for (int i = 0; i < cascadedLength; i++) {
            T minVal = null;
            int minCascadeInd = UNDEFINED;
            int minValInnerIndex = UNDEFINED;
            // check self value
            if (selfIndex < val.size())
                minVal = sortedVal.get(selfIndex);
            for (int j = 0; j < next.size(); j++) {
                List<T> nextVal = next.get(j).val;
                if (indexes[j] < nextVal.size()) { // has next value
                    T guess = nextVal.get(indexes[j]);
                    if (minVal == null || comparator.compare(guess, minVal) < 0) {
                        minVal = guess;
                        minCascadeInd = j;
                        minValInnerIndex = indexes[j];
                    }
                }
            }
            this.val.add(minVal);
            this.cascadeIndex[i] = minCascadeInd;
            this.innerIndexes[i] = minValInnerIndex;
            // update indexes
            if (minCascadeInd == UNDEFINED) { //self update
                selfIndex++;
            } else {
                indexes[minCascadeInd] += next.get(minCascadeInd).loadFactor;
            }
        }
        // linking
        selfLinks = new int[cascadedLength];
        nextLinks = new int[cascadedLength][next.size()];
        selfLinks[cascadedLength - 1] = UNDEFINED;
        Arrays.fill(nextLinks[cascadedLength - 1], UNDEFINED);
        for (int j = cascadedLength - 2; j >= 0; j--) {
            System.arraycopy(nextLinks[j + 1], 0, nextLinks[j], 0, next.size());
            if (cascadeIndex[j + 1] == UNDEFINED) { // self value
                selfLinks[j] = j + 1;
            } else {
                selfLinks[j] = selfLinks[j + 1];
                nextLinks[j][cascadeIndex[j + 1]] = j + 1;
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

    public static <T> List<Cascade<T>> cascade(Comparator<T> comparator, List<List<T>> vertexes,
                                               boolean[][] edges)
    {
        int vertexCount = vertexes.size();
        List<Cascade<T>> cascades = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++)
            cascades.add(null);
        boolean[] cascaded = new boolean[vertexCount];
        boolean stop = false;
        while (!stop) {
            stop = true;
            for (int u = 0; u < vertexCount; u++) {
                if (cascaded[u])
                    continue;
                boolean canBeCascaded = true;
                List<Cascade<T>> next = new ArrayList<>();
                int incomingEdgesCount = 0;
                for (int v = 0; v < vertexCount; v++) {
                    if (edges[v][u])
                        incomingEdgesCount++;
                    if (!edges[u][v])
                        continue;
                    if (cascaded[v]) {
                        next.add(cascades.get(v));
                    } else {
                        canBeCascaded = false;
                        break;
                    }
                }
                if (canBeCascaded) {
                    cascaded[u] = true;
                    cascades.set(u, new Cascade<>(comparator, 2 * incomingEdgesCount, vertexes.get(u), next));
                    stop = false;
                    break;
                }
            }
        }
        return cascades;
    }

    public List<T> search(T x, int[] path) {
        int index = 0;
        int offset = val.size();
        while (offset > 0) {
            int guess = index + (offset >> 1);
            if (comparator.compare(val.get(guess), x) < 0) {
                index = guess + 1;
                offset -= 1;
            }
            offset >>= 1;
        }
        List<T> res = new ArrayList<>();
        if (index == val.size()) { // value not found :(
            res.add(null);
            spread(x, UNDEFINED, res, path, 0);
        } else {
            res.add(getSelfValue(index));
            spread(x, index, res, path, 0);
        }
        return res;
    }

    private T getSelfValue(int index) {
        T selfVal = null;
        if (cascadeIndex[index] == UNDEFINED) { // is it self value?
            selfVal = val.get(index);
        } else if (selfLinks[index] != UNDEFINED) { // has link for self value
            selfVal = val.get(selfLinks[index]);
        }
        return selfVal;
    }

    private void spread(T x, int index, List<T> acc, int[] path, int pathIndex) {
        if (pathIndex >= path.length)
            return;
        int ci = path[pathIndex]; // next cascade index
        int innerIndex = UNDEFINED;
        if (index != UNDEFINED) {
            if (cascadeIndex[index] == ci) {
                innerIndex = innerIndexes[index];
            } else if (nextLinks[index][ci] != UNDEFINED) {
                innerIndex = innerIndexes[nextLinks[index][ci]];
            }
        }
        next.get(ci).cascadeSearch(innerIndex, x, acc, path, pathIndex + 1);
    }

    private void cascadeSearch(int index, T x, List<T> acc, int[] path, int pathIndex) {
        if (index == UNDEFINED)
            index = val.size();
        int endIndex = Math.max(-1, index - loadFactor);
        for (index--; index > endIndex; index--) { // first value suit well
            if (comparator.compare(x, val.get(index)) > 0) // shift too much
                break;
        }
        index++;
        if (index == val.size()) { // no self value
            acc.add(null);
            spread(x, UNDEFINED, acc, path, pathIndex);
        } else {
            acc.add(getSelfValue(index));
            spread(x, index, acc, path, pathIndex);
        }
    }

    private List<T> getSortedCopy(List<T> val) {
        List<T> sorted = new ArrayList<>(val);
        sorted.sort(comparator);
        return sorted;
    }
}
