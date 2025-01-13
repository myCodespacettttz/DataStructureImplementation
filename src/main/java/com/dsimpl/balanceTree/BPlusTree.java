package com.dsimpl.balanceTree;

import java.util.ArrayList;
import java.util.List;

public class BPlusTree<K extends Comparable<K>, V> {
    //B+树的阶
    public int degree;
    public int UPPER_BOUND;
    public int UNDER_BOUND;
    public BPlusNode root;
    private BPlusNode head;
    private BPlusNode tail;
    private int size;

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
        head = root;
        tail = root;
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
            this(isLeaf);
            this.isRoot = isRoot;
        }

        public BPlusNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            keys = new ArrayList<>();
            if (isLeaf) {
                datas = new ArrayList<>();
            } else {
                childen = new ArrayList<>();
            }
        }

        public int findCeilingKeyIndex(K key) {
            int left = 0;
            int right = keys.size() - 1;
            int resIndex = keys.size();
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) <= 0) {
                    resIndex = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return resIndex;
        }

        public void put(K key, V value) {
            //1:找到key在当前树需要添加的位置
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            //2:迭代到需要插入的底层叶子节点
            while (!this.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            cur.doPut(key, value);
        }

        public void putKeyAndValue(K key, V value) {
            int left = 0;
            int right = keys.size() - 1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) == 0) {
                    datas.set(mid, value);
                    break;
                } else if (key.compareTo(keys.get(mid)) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            //超过keys最大值
            if (left > right) {
                size++;
                keys.add(left, key);
                datas.add(left, value);
            }
        }

        private void doPut(K key, V value) {
            int index = contains(key);
            if (index != -1 || keys.size() < UPPER_BOUND) {
                putKeyAndValue(key, value);
            } else {
                size++;
                spite(key, value);
            }
        }

        /**
         * 叶子节点添加元素后所处的位置需要进行分裂
         * @param key
         */
        public void spite(K key, V value) {
            //1:将当前数据列表分裂为两个节点
            BPlusNode left = new BPlusNode(true);
            BPlusNode right = new BPlusNode(true);

            //2:处理叶子节点链表的插入操作,分别考虑当前节点分裂为两个节点后的前后指针的重新指向,并且需要考虑当前节点是否是头尾的情况
            if (pre != null) {
                pre.next = left;
                left.pre = pre;
            } else {
                head = left;
            }
            if (next != null) {
                next.pre = right;
                right.next = next;
            } else {
                tail = right;
            }
            left.next = right;
            right.pre = left;

            //3：将分裂出的两个节点插入到叶子节点链表之后,将原节点置空
            pre = null;
            next = null;

            //4：将原节点数据插入新的链表
            int mid = UPPER_BOUND / 2;
            left.keys = new ArrayList<>(keys.subList(0, mid));
            left.datas = new ArrayList<>(datas.subList(0, mid));
            right.keys = new ArrayList<>(keys.subList(mid, keys.size()));
            right.datas = new ArrayList<>(datas.subList(mid, keys.size()));

            //5:查找在原节点中的插入位置,决定本次插入的key在新链表中的插入节点位置
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            if (ceilingKeyIndex < mid) {
                left.keys.add(ceilingKeyIndex, key);
                left.datas.add(ceilingKeyIndex, value);
            } else {
                int newIndex = ceilingKeyIndex - mid;
                left.keys.add(newIndex, key);
                left.datas.add(newIndex, value);
            }
        }

        /**
         * @param key
         * @return 返回叶子节点中是否存在传入元素
         */
        private int contains(K key) {
            int left = 0;
            int right = keys.size() - 1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) <= 0) {
                    return mid;
                } else if (key.compareTo(keys.get(mid)) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return -1;
        }

        private int findChildIndexByCeilingKeyIndex(int keyIndex, K key) {
            return (keyIndex == keys.size() || key.compareTo(keys.get(keyIndex)) < 0) ? keyIndex : keyIndex + 1;
        }
    }

    public static void main(String[] args) {
        BPlusTree<Integer, Integer> bp = new BPlusTree<>(5);
        bp.put(1, 1);
        bp.put(2, 2);
        bp.put(3, 3);
        bp.put(4, 4);
        bp.put(5, 5);
        bp.put(6, 6);

    }
}
