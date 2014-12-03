package ifmo.cascading;

/**
 * @author Arkadii Rost
 */
class Cascade<T> {
    public static final int SELF_ID = -1;

    private final T[] val;

    public Cascade(T[] val) {
        this.val = val;
    }
}
