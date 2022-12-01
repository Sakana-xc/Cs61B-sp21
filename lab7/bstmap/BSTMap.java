package  bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>,V> implements  Map61B<K,V>  {
    private BSTNode root;
    private int size = 0;
    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left, right;
        private int size;

        public BSTNode(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }

    @Override
    public void clear(){
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key){
        return containsKeyHelper(root, key);

    }
    private boolean containsKeyHelper(BSTNode node, K key){
        if(node == null){
            return false;
        }
        int cmp = key.compareTo(node.key);
        if(cmp > 0){
            return containsKeyHelper(node.right, key);
        }
        else if (cmp <0){
            return containsKeyHelper(node.left,key);
        }
        else{
            return true;
        }
    }

    @Override
    public V get(K key){
        return get(root,key);
    }
    private V get(BSTNode node, K key)  {
        if(node == null){return null;}
        int cmp = key.compareTo(node.key);
        if (cmp <0){return get(node.left, key);}
        else if (cmp > 0){return get(node.right, key);}
        else{return node.value;}

    }


    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value){
        put(root, key, value);
        size += 1;
    }

    private BSTNode put(BSTNode node, K key, V value){
        if(node == null){return new BSTNode(key, value,1);}
        int cmp = key.compareTo(node.key);
        if(cmp <0){put(node.left, key,value);}
        else if (cmp > 0){put(node.right, key,value);}
        else {node.value = value;}
        return node;

    }

    @Override
    ///* Returns a Set view of the keys contained in this map. Not required for Lab 7.
    //     * If you don't implement this, throw an UnsupportedOperationException. */
    public Set<K> keySet(){
        HashSet<K> set = new HashSet<>();
        addKeys(root,set);
        return set;
    }

    private void addKeys(BSTNode node, HashSet set){
        if(node ==null){return;
    }
        set.add(node.key);
        addKeys(node.left,set);
        addKeys(node.right,set);}

    public void printInOrder(){
        printInOrder(root);
    }
    /* in order of increasing key */
    private void printInOrder(BSTNode node){
        if(node == null){return;}
        printInOrder(node.left);
        System.out.println(node.key.toString() +"->" + node.value.toString());
        printInOrder(node.right);

    }
    @Override
    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    public V remove(K key){
        if(containsKey(key)){
            V removed = get(key);
            root = remove(root, key);
            size -= 1;
            return removed;
        }
        return null;
    }
    private BSTNode remove(BSTNode node,K key){
        int cmp = key.compareTo(node.key);
        if(cmp <0){remove(node.left, key);}
        else if (cmp >0){remove(node.right, key);}
        /* find the node with rightKey, replace and restructure, involves deleting*/
        else{if(node.left == null){return node.right;}
             if (node.right == null){return node.left;
             }
             else{
                 BSTNode tmp = node;
                 node = getMinChild(node.right);
                 node.left = tmp.left;
                 /* because takes nodes to replace tmp, so it's a remove(subtree root, minChild.key)*/
                 node.right = remove(tmp.right,node.key);
             }
        }
        return node;
    }
    private BSTNode getMinChild(BSTNode node) {
        if (node.left == null) {
            return node;
        }
        return getMinChild(node.left);
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. So if there is not a key - value pair , no need to remove*/
    @Override
    public V remove(K key, V value){
        if (containsKey(key)){
            V targetValue = get(key);
            if(targetValue.equals(key)){
                root = remove(root,key);
                size -= 1;
            }

        }
        return null;
    }

    public Iterator<K> iterator(){
        return keySet().iterator();
    }




}