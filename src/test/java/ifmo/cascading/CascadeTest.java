package ifmo.cascading;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CascadeTest {
    @Test
    public void arrayCascade() {
        Cascade<Float> cascade = Cascade.cascade(Float::compare, getListsData());
        assertEquals(Arrays.asList(2.4f, 2.5f, 3.5f, 6.4f, 6.5f, 7.9f, 8.0f, 9.3f), cascade.getVal());
    }

    private List<List<Float>> getListsData() {
        return Arrays.asList(
                Arrays.asList(2.4f, 6.4f, 6.5f, 8.0f, 9.3f),
                Arrays.asList(2.3f, 2.5f, 2.6f),
                Arrays.asList(1.3f, 4.4f, 6.2f, 6.6f),
                Arrays.asList(1.1f, 3.5f, 4.6f, 7.9f, 8.1f)
        );
    }
}