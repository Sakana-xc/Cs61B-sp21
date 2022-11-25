package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    private T[] items = (T []) new Object[8];
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque(){
        nextFirst = 4;
        nextLast = 5;
        size = 0;
    }

    public  int addOne(int p){
        return (p + 1) % items.length;
    }

    public int minOne(int p) {
        return (p+ items.length-1)%items.length;
    }
    public int size(){
        return size;
    }

    private void resize(int capacity){
        T [] newItems = (T[]) new Object[capacity];
        int index = addOne(nextFirst);
        for(int i = 0; i <size; i ++){
            newItems[i]= items[index];
            addOne(index);
        }
        nextFirst = capacity -1;
        nextLast = size;
        items = newItems;
    }

    private void checkRoom(){
        if(size==items.length){
            resize(size * 2);
        }
    }

    private void efficiency(){
        if(size < items.length/4 && items.length >=16){
            resize(items.length/4);
        }
    }

    @Override
    public void addFirst(T item){
        checkRoom();
        items[nextFirst]=item;
        nextFirst= minOne(nextFirst);
        size += 1;

    }
    @Override
    public void addLast(T item){
        checkRoom();
        items[nextLast] = item;
        nextLast = addOne(nextLast);
        size += 1;
    }
    @Override
    public T get(int index){
        if(index<0 || index > size -1){
            return null;
        }
        return items[(nextFirst+1+index)%items.length];
        //deque starts at nextFirst +1

    }

    @Override
    public boolean equal(Object o) {
        return false;
    }

    @Override
    public T removeFirst(){
        if(isEmpty()){
            return null;
        }
        nextFirst += 1;
        T item = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        efficiency();
        return item;

    }


    @Override
    public T removeLast(){
        if(isEmpty()){
            return null;
        }
        nextLast -= 1;
        T item = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        efficiency();
        return item;
    }
    @Override
    public void printDeque(){
        String[] tmp = new String [size];
        int index = addOne(nextFirst);
        for(int i =0; i <size; i ++){
            tmp[i]= items[index].toString();
            index = addOne(index);
        }
        System.out.println(String.join(""+tmp));

    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    protected class ArrayDequeIterator implements Iterator<T> {
        private int index;
        ArrayDequeIterator(){
            index = 0;
        }
        public boolean hasNext(){
            return index < size;
        }

        public T next(){
            T item = get(index);
            index = addOne(index);
            return item;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Deque)){
            return false;
        }
        //next to cast o cuz o type is object;
        Deque other = (Deque) o;
        if(o== null){
            return false;
        }
        if(o == this){
            return true;
        }
        int index = addOne(nextFirst);
        for(int i = 0; i < size; i++){
            if(!items[index].equals(other.get(i))){
                return false;//get method
            }
            index = addOne((index));

        }
        if(other.size() != this.size() ) {
            return false;
        }
        return true;
    }

}