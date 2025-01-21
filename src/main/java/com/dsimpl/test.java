package com.dsimpl;

import com.dsimpl.balanceTree.BPlusTree;

import java.util.Map;
import java.util.TreeMap;

public class test {
    public static void main(String[] args) {
        BPlusTree<Integer, Integer> b = new BPlusTree<>(5);
        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            b.put(i, i);
        }
        System.out.println(b.printTree());
        boolean b2 = b.containsKey(70);
        b.remove(70);
        boolean b1 = b.containsKey(70);
        System.out.println(b.printTree());
    }

    public static void checkRandom(BPlusTree<Integer, Integer> bp, TreeMap<Integer, Integer> treeMap) {
        System.out.println("start");
        int times = 50;
        int maxKey = 50;
        int maxValue = 1000000;
        for (int i = 0; i < times; i++) {
            int key = (int) (Math.random() * maxKey);
            int value = (int) (Math.random() * maxValue);
            if (Math.random() > 0.1) {
                treeMap.put(key, value);
                bp.put(key, value);
            }
        }
        int getKey = (int) (Math.random() * maxKey);
        Integer treeMapGetKey = treeMap.get(getKey);
        Integer avlGetKey = bp.get(getKey);
        if ((treeMapGetKey == null && avlGetKey != null) || (treeMapGetKey != null && avlGetKey == null)) {
            System.out.println("get-> error");
        }
        if ((treeMapGetKey != null && avlGetKey != null) && !treeMapGetKey.equals(avlGetKey)) {
            System.out.println("get-> error");
        }
    }
}
