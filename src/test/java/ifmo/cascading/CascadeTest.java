package ifmo.cascading;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CascadeTest {
    @Test
    public void arrayCascade() {
        List<List<Float>> data =  Arrays.asList(
                Arrays.asList(2.4f, 6.4f, 6.5f, 8.0f, 9.3f),
                Arrays.asList(2.3f, 2.5f, 2.6f),
                Arrays.asList(1.3f, 4.4f, 6.2f, 6.6f),
                Arrays.asList(1.1f, 3.5f, 4.6f, 7.9f, 8.1f)
        );

        Cascade<Float> cascade = Cascade.cascade(Float::compare, data);
        int[] path = {0, 0, 0};
        assertEquals(Arrays.asList(6.4f, null, 6.2f, 7.9f), cascade.search(5f, path));
        assertEquals(Arrays.asList(9.3f, null, null, 8.1f), cascade.search(8.05f, path));
        assertEquals(Arrays.asList(8f, null, null, 8.1f), cascade.search(8f, path));
        assertEquals(Arrays.asList(2.4f, 2.3f, 4.4f, 3.5f), cascade.search(2f, path));
    }
}