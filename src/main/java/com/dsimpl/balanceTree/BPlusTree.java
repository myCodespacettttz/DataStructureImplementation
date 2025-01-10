package com.dsimpl.balanceTree;

import java.util.List;

public class BPlusTree<K extends Comparable<K>, V> {
    //B+树的阶
    public int degree;
    public int UPPER_BOUND;
    public int UNDER_BOUND;
    public BPlusNode root;
    private BPlusNode head;
    private BPlusNode tail;

    public BPlusTree(int degree) {
        if (degree < 3) {
            degree = 3;
        } else {
            this.degree = degree;
        }
        this.UPPER_BOUND = degree - 1;
        this.UNDER_BOUND = UPPER_BOUND / 2;
        //初始化时根节点同时为根节点与叶子节点
        this.root = new BPlusNode(true, true);
    }

    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        root.put(key, value);
    }

    public class BPlusNode {
        private boolean isRoot;

        private boolean isLeaf;

        //排序依据/关键字
        private List<K> keys;

        //叶子节点/数据
        private List<V> datas;

        //孩子节点
        private List<BPlusNode> childen;

        private BPlusNode next;

        private BPlusNode pre;

        public BPlusNode(boolean isRoot, boolean isLeaf) {
            this.isRoot = isRoot;
            this.isLeaf = isLeaf;
        }

        public void put(K key, V value) {
            //1:找到key在当前树中所处的位置
        }
    }
}
