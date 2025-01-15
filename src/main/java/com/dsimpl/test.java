package com.dsimpl;

import com.dsimpl.balanceTree.BPlusTree;

import java.util.Map;
import java.util.TreeMap;

public class test {
    public static void main(String[] args) {
        BPlusTree<Integer, Integer> bp = new BPlusTree<>(5);
        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
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
        Map<Integer, Integer> range = bp.range(25, 50);
        System.out.println();
    }
}
