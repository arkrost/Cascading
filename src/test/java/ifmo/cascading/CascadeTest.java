package ifmo.cascading;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CascadeTest {

    /*
     * 0:[2.4, 6.4, 6.5, 8.0, 9.3]
     * 1: [2.3, 2.5, 2.6]
     * 2: [1.3, 4.4, 6.2, 6.6]
     * 3: [1.1, 3.5, 4.6, 7.9, 8.1]
     */
    @Test
    public void arrayCascade() {
        List<List<Float>> data = getData();
        Cascade<Float> cascade = Cascade.cascade(Float::compare, data);
        int[] path = {0, 0, 0};
        assertEquals(Arrays.asList(6.4f, null, 6.2f, 7.9f), cascade.search(5f, path));
        assertEquals(Arrays.asList(9.3f, null, null, 8.1f), cascade.search(8.05f, path));
        assertEquals(Arrays.asList(8f, null, null, 8.1f), cascade.search(8f, path));
        assertEquals(Arrays.asList(2.4f, 2.3f, 4.4f, 3.5f), cascade.search(2f, path));
    }

    @Test
    public void graphCascade() {
        List<List<Float>> data = getData();
        /*
         *  0:[2.4, 6.4, 6.5, 8.0, 9.3]   1: [2.3, 2.5, 2.6]
         *  |----------------------------->|
         *  |                              |
         *  v ---------------------------->v
         *  2: [1.3, 4.4, 6.2, 6.6]      3: [1.1, 3.5, 4.6, 7.9, 8.1]
         *
         */
        boolean[][] edges = {
            {false, true, true, false},
            {false, false, false, true},
            {false, false, false, true},
            {false, false, false, false}
        };
        List<Cascade<Float>> cascades = Cascade.cascade(Float::compare, data, edges);
        // search 2.45 by path 0 -> 1 -> 3
        assertEquals(Arrays.asList(6.4f, 2.5f, 3.5f), cascades.get(0).search(2.45f, new int[2]));
        // search 2.45 by path 1 -> 3
        assertEquals(Arrays.asList(2.5f, 3.5f), cascades.get(1).search(2.45f, new int[1]));
        // search 2.45 by path 0 -> 2 -> 3
        assertEquals(Arrays.asList(6.4f, 4.4f, 3.5f), cascades.get(0).search(2.45f, new int[] {1, 0}));
        /*
         *  Simulate list
         *  0:[2.4, 6.4, 6.5, 8.0, 9.3] ->  1: [2.3, 2.5, 2.6]
         *                                  |
         *                                  v
         *  3: [1.1, 3.5, 4.6, 7.9, 8.1] <- 2: [1.3, 4.4, 6.2, 6.6]
         */
        edges = new boolean[][]{
                {false, true, false, false},
                {false, false, true, false},
                {false, false, false, true},
                {false, false, false, false}
        };
        cascades = Cascade.cascade(Float::compare, data, edges);
        assertEquals(Arrays.asList(6.4f, null, 6.2f, 7.9f), cascades.get(0).search(5f, new int[3]));
        assertEquals(Arrays.asList(9.3f, null, null, 8.1f), cascades.get(0).search(8.05f, new int[3]));
        assertEquals(Arrays.asList(8f, null, null, 8.1f), cascades.get(0).search(8f, new int[3]));
        assertEquals(Arrays.asList(2.4f, 2.3f, 4.4f, 3.5f), cascades.get(0).search(2f, new int[3]));

        /*
         * 2: [1.3, 4.4, 6.2, 6.6]         1: [2.3, 2.5, 2.6]        3: [1.1, 3.5, 4.6, 7.9, 8.1]
         * ------------------------------->|------------------------>
         *                                 |
         *                                 v
         *                                 0:[2.4, 6.4, 6.5, 8.0, 9.3]
         */
        edges = new boolean[][] {
                {false, false, false, false},
                {true, false, false, true},
                {false, true, false, false},
                {false, false, false, false}
        };
        cascades = Cascade.cascade(Float::compare, data, edges);
        assertEquals(Arrays.asList(6.2f, null, 6.4f), cascades.get(2).search(5f, new int[2]));
        assertEquals(Arrays.asList(6.2f, null, 7.9f), cascades.get(2).search(5f, new int[] {0, 1}));
        assertEquals(Arrays.asList(null, null, 9.3f), cascades.get(2).search(8.05f, new int[2]));
        assertEquals(Arrays.asList(null, null, 8.1f), cascades.get(2).search(8.05f, new int[] {0, 1}));
        assertEquals(Arrays.asList(null, null, 8.f), cascades.get(2).search(8f, new int[2]));
        assertEquals(Arrays.asList(null, 8.1f), cascades.get(1).search(8f, new int[] {1}));
        assertEquals(Arrays.asList(4.4f, 2.3f, 2.4f), cascades.get(2).search(2f, new int[2]));
    }

    private List<List<Float>> getData() {
        return Arrays.asList(
                Arrays.asList(2.4f, 6.4f, 6.5f, 8.0f, 9.3f),
                Arrays.asList(2.3f, 2.5f, 2.6f),
                Arrays.asList(1.3f, 4.4f, 6.2f, 6.6f),
                Arrays.asList(1.1f, 3.5f, 4.6f, 7.9f, 8.1f)
        );
    }
}