package com.dsimpl.balanceTree;

public interface TreeMethodInterface<K extends Comparable<K>, V> {
    void put();
    V get(K key);
    void remove(K key);
    boolean containsKey(K key);
    K floorKey(K key);
    K ceilingKey(K key);
    K firstKey();
    K lastKey();
    int size();
}
