package ifmo.cascading;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Arkadii Rost
 */
class Node<T> {
    public static final int UNDEFINED = -1;

    private final T val;
    private final int from;
    private int next;

    public Node(T val, int from) {
        this.val = val;
        this.from = from;
    }

    public Node(T val) {
        this(val, Cascade.SELF_ID);
    }

    public boolean isAlien() {
        return from != Cascade.SELF_ID;
    }

    public boolean hasNext() {
        return next != UNDEFINED;
    }

    public T getVal() {
        return val;
    }

    public int getFrom() {
        return from;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    static <T> List<Node<T>> wrap(T[] data) {
        return Arrays.stream(data).map(Node<T>::new).collect(Collectors.toList());
    }
}
