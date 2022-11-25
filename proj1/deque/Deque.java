package deque;

public interface Deque<T> {
    void addFirst(T item);
    void addLast(T item);
    default boolean isEmpty(){
        return size() == 0;
    }
    int size();
    void printDeque();

    /* Removes and returns the item at the front of the deque. If no such item exists, returns null.*/
    T removeFirst();

    T removeLast();
    T get(int index);
    boolean equal(Object o);

}