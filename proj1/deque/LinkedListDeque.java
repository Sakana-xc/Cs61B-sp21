package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> ,Iterable<T> {
    private int size;
    private Node head;

    private class Node {
        private T item;
        private Node prev;
        private Node next;

        Node() {
            //constructor of Node
            item = null;
            prev = next = null;
        }

        Node(T i, Node p, Node q) {
            this.item = i;
            this.prev = p;
            this.next = q;
        }

        @Override
        public String toString() {
            if (item == null) {
                return "null";
            }
            return item.toString();
        }
    }

    public LinkedListDeque() {
        head = new Node();
        head.next = head;
        head.prev = head;
        size = 0;

    }

    @Override
    public void addFirst(T item) {
        Node tmp = new Node(item, head, head.next);
        //update Node head.next's pointer
        head.next.prev = tmp;
        head.next = tmp;
        size += 1;

    }

    @Override
    public void addLast(T item) {
        Node tmp = new Node(item, head.prev, head);
        head.prev.next = tmp;
        head.prev = tmp;
        size += 1;
    }

    public int size() {
        return size;

    }

    @Override
    public void printDeque() {
//        Node p = head.next;
//        while(p != head){
//            System.out.print(p.item + "");
//            p = p.next;
//        }
//        System.out.println();
        String[] items = new String[size];
        Node p = head.next;
        if (p == head) {
            return;
        }
        for (int i = 0; i < size; i++) {
            items[i] = p.item.toString();
            p = p.next;
        }
        System.out.println(String.join("", items));

    }

    /* Removes and returns the item at the front of the deque. If no such item exists, returns null.*/
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = head.next.item;
        head.next = head.next.next;
        head.next.prev = head;
        size -= 1;
        return item;
    }

    //Removes and returns the item at the back of the deque. If no such item exists, returns null.
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T item = head.prev.item;
        head.prev = head.prev.prev;
        head.prev.next = head;
        size -= 1;
        return item;

    }


    //Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth. If no such item exists, returns null. Must not alter the deque!
    @Override
    public T get(int index) {
        int mid = (size - 1) / 2;
        if (index < 0 || index >= size) {
            return null;
        }
        if (index <= mid) {
            Node p = head.next;
            while (index != 0) {
                p = p.next;
                index -= 1;
            }
            return p.item;
        } else {
            int rest = size - 1 - index;
            Node p = head.prev;
            while (rest != 0) {
                p = p.prev;
                rest -= 1;
            }

            return p.item;
        }

    }

    @Override
    public boolean equal(Object o) {
        return false;
    }

    //recursive
    public T getRecursive(int index){
        if(index <0 || index >=size ){
            return null;
        }
        return getRecursiveHelper(index,head.next);

    }
    private T getRecursiveHelper(int index, Node curr){
        if(index ==0){
            return curr.item;
        }
        return getRecursiveHelper(index-1,curr.next);

    }

    @Override
    public Iterator<T> iterator() {

        return new linkedListDequeIterator();
    }
    private class linkedListDequeIterator implements Iterator<T> {
        private Node p;
        linkedListDequeIterator(){
            p = head.next;
        }
        public boolean hasNext(){
           return  p != head;
        }
        public T next(){
            T item = p.item;
            p = p.next;
            return item;
        }

    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedListDeque)){
            return false;
        }
        //next to cast o cuz o type is object;
        LinkedListDeque other = (LinkedListDeque) o;
        Node p = head.next;
        for(int i = 0; i < size; i++){
            if(other.get(i)!= p.item ){
                return false;//get method
            }
            p = p.next;

        }
        if(other.size() != size() ) {
            return false;
        }
        return true;
    }




}

