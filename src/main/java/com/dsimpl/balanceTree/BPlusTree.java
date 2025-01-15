package com.dsimpl.balanceTree;

import java.util.*;

/**
 * 参考看图写代码,可视化数据结构网站:https://www.cs.usfca.edu/~galles/visualization/Algorithms.html
 * 1: B+树的节点由关键字(排序字段,可以是任何实现了比较手段的数据类型)与数据组成的
 * 2: 非叶子节点中,数组中最前面与最后面的元素,就是其所有的所属叶子节点中元素的最大值和最小值
 * 3: 在非叶子节点对应的叶子节点中,非叶子节点中的关键字个数等于其对应叶子结点的数据个数
 * 4: B+树数据存在底层叶子节点,所有非叶子节点的关键字都会出现在叶子节点,叶子节点用链表链接,且叶子节点个数等于分支数
 */
public class BPlusTree<K extends Comparable<K>, V> {
    //B+树的阶
    public int degree;
    public int UPPER_BOUND;
    public int UNDER_BOUND;
    public BPlusNode root;
    private BPlusNode head;
    private BPlusNode tail;
    private int size;
    private int height = 0;

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

    public V get(K key) {
        if (key == null) {
            return null;
        }
        return root.get(key);
    }

    public Map<K, V> range(K start, K end) {
        if (start.compareTo(end) < 0 && start != null && end != null) {
            return root.range(start, end);
        } else {
            return new HashMap<>();
        }
    }

    public int size() {
        return size;
    }

    public int height() {
        return height;
    }

    public boolean containsKey(K key) {
        if (key == null) return false;
        return root.containsKey(key);
    }

    /**
     * @param key
     * @return 返回小于等于给定元素中最大的元素,不存在返回null
     */
    public K floorKey(K key) {
        if (key == null) return null;
        return root.floorKey(key);
    }

    /**
     * @param key
     * @return 返回大于等于给定元素中最大的元素,不存在返回null
     */
    public K ceilingKey(K key) {
        if (key == null) return null;
        return root.ceilingKey(key);
    }

    public K firstKey() {
        return head.keys.get(0);
    }

    public K lastKey() {
        return tail.keys.get(tail.keys.size() - 1);
    }

    public String printTree() {
        return root.printTree();
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
        private BPlusNode parent;

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

        public void put(K key, V value) {
            //1:找到key在当前树需要添加的位置
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            //2:迭代到需要插入的底层叶子节点
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            cur.doPut(key, value);
        }

        public V get(K key) {
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            return cur.doGet(key);
        }

        public boolean containsKey(K key) {
            //1:找到key在当前树需要添加的位置
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            //2:迭代到需要插入的底层叶子节点
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            return cur.contains(key) == -1;
        }

        public K floorKey(K key) {
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            return cur.doFloorKey(key);
        }

        public K ceilingKey(K key) {
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            return cur.doCeilingKey(key);
        }

        private K doCeilingKey(K key) {
            for (int i = 0; i < keys.size(); i++) {
                if (key.compareTo(keys.get(i)) <= 0) {
                    return keys.get(i);
                }
            }
            BPlusNode cur = this.next;
            if (cur == null) {
                return null;
            }
            return cur.keys.get(0);
        }

        private K doFloorKey(K key) {
            for (int i = keys.size() - 1; i <= 0; i--) {
                if (key.compareTo(keys.get(i)) <= 0) {
                    return keys.get(i);
                }
            }
            //由于叶子节点是有序链表,所以这个节点中不存在,那就一定是上一个节点的最后一个元素
            BPlusNode cur = this.pre;
            if (cur == null) {
                return null;
            }
            return cur.keys.get(cur.keys.size() - 1);
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
                if (key.compareTo(keys.get(mid)) == 0) {
                    return mid;
                } else if (key.compareTo(keys.get(mid)) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return -1;
        }

        private V doGet(K key) {
            int left = 0;
            int right = keys.size() - 1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) == 0) {
                    return datas.get(mid);
                } else if (key.compareTo(keys.get(mid)) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return null;
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
                if (height == 0) {
                    height = 1;
                }
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

            handlePreAndNext(left, right);
            copyKeyAndValueToNewNode(key, value, left, right);

            if (parent != null) {
                //6:在根节点的孩子节点列表中删除原节点,同时将分裂出的两个节点插入父节点的孩子节点列表,并将分裂前的节点置空
                int index = parent.childen.indexOf(this);
                left.parent = parent;
                right.parent = parent;
                parent.childen.remove(this);
                parent.childen.add(index, left);
                parent.childen.add(index + 1, right);
                parent.keys.add(index, right.keys.get(0));
                keys = null;
                childen = null;
                parent.insertMatain();
                parent = null;
            } else {
                isRoot = false;
                height++;
                root = new BPlusNode(true, false);
                left.parent = root;
                right.parent = root;
                root.childen.add(left);
                root.childen.add(right);
                root.keys.add(right.keys.get(0));
                keys = null;
                childen = null;
            }
        }

        private void insertMatain() {
            if (keys.size() <= UPPER_BOUND) {
                return;
            }
            BPlusNode left = new BPlusNode(false);
            BPlusNode right = new BPlusNode(false);
            int downMinIndex = UPPER_BOUND / 2 + 1;
            left.childen = new ArrayList<>(childen.subList(0, downMinIndex));
            for (BPlusNode child : left.childen) {
                child.parent = left;
            }
            right.childen = new ArrayList<>(childen.subList(downMinIndex, childen.size()));
            for (BPlusNode child : right.childen) {
                child.parent = right;
            }
            left.keys = new ArrayList<>(keys.subList(0, downMinIndex - 1));
            right.keys = new ArrayList<>(keys.subList(downMinIndex, keys.size()));
            if (parent != null) {
                int index = parent.childen.indexOf(this);
                parent.childen.remove(this);
                left.parent = parent;
                right.parent = parent;
                parent.childen.add(index, left);
                parent.childen.add(index + 1, right);
                parent.keys.add(index, keys.get(downMinIndex - 1));
                keys = null;
                childen = null;
                parent.insertMatain();
                parent = null;
            } else {
                isRoot = false;
                height++;
                root = new BPlusNode(true, false);
                left.parent = root;
                right.parent = root;
                root.childen.add(left);
                root.childen.add(right);
                root.keys.add(keys.get(downMinIndex - 1));
                keys = null;
                childen = null;
            }
        }

        private void handlePreAndNext(BPlusNode left, BPlusNode right) {
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

            //3：将分裂出的两个节点插入到叶子节点链表之后,将分裂前节点的前后引用置空
            pre = null;
            next = null;
        }

        private void copyKeyAndValueToNewNode(K key, V value, BPlusNode left, BPlusNode right) {
            int mid = UPPER_BOUND / 2;
            //4：将分裂前节点中的数据插入分裂后的两个节点中
            left.keys = new ArrayList<>(keys.subList(0, mid));
            left.datas = new ArrayList<>(datas.subList(0, mid));
            right.keys = new ArrayList<>(keys.subList(mid, keys.size()));
            right.datas = new ArrayList<>(datas.subList(mid, keys.size()));

            //5:查找分裂前节点中的插入位置,决定本次插入的key在新链表中的插入节点位置
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            if (ceilingKeyIndex < mid) {
                left.keys.add(ceilingKeyIndex, key);
                left.datas.add(ceilingKeyIndex, value);
            } else {
                int newIndex = ceilingKeyIndex - mid;
                right.keys.add(newIndex, key);
                right.datas.add(newIndex, value);
            }
        }

        private int findChildIndexByCeilingKeyIndex(int keyIndex, K key) {
            return (keyIndex == keys.size() || key.compareTo(keys.get(keyIndex)) < 0) ? keyIndex : keyIndex + 1;
        }


        public String printTree() {
            StringBuilder sb = new StringBuilder();
            LinkedList<BPlusNode> queue = new LinkedList<>();
            queue.add(this);
            int level = 1;
            while (!queue.isEmpty()) {
                int size = queue.size();
                for (int i = 1; i <= size; i++) {
                    BPlusNode poll = queue.poll();
                    sb.append(poll.keys).append(" ");
                    if (!poll.isLeaf && poll.childen != null) {
                        queue.addAll(poll.childen);
                    }
                }
                sb.append(System.lineSeparator());
                sb.append("第" + level++ + "行结束");
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        }

        /**
         * 范围查找可以直接迭代整个链表,时间复杂度O(n),也可以利用多级索引的优势,结合ceiling或者floor
         * 方法的特点得到范围中的头或者尾,然后从前往后或者从后往前都可以,时间复杂度O(log(n)) + length
         * @param start 起始位置,不包含
         * @param end 截止位置,包含
         */
        public Map<K, V> range(K start, K end) {
            Map<K, V> result = new HashMap<>();
            //1:先找到底层的start所处的叶子节点
            int ceilingKeyIndex = findCeilingKeyIndex(start);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, start);
            BPlusNode cur = root;
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(start);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, start);
            }
            //2:寻找第一个大于start的元素并记录其索引
            int startIndex = -1;
            for (int i = 0; i < cur.keys.size(); i++) {
                if (cur.keys.get(i).compareTo(start) > 0) {
                    startIndex = i;
                    break;
                }
            }
            //3:叶子节点中如果没有找到,那么本节点的下一个节点的起始位置就一定大于start
            if (startIndex == -1) {
                startIndex = 0;
                cur = cur.next;
            }
            //4:从start位置开始迭代到end,当找到链表结尾或找到end后退出
            while (cur != null && cur.keys.get(startIndex).compareTo(end) <= 0) {
                result.put(cur.keys.get(startIndex), cur.datas.get(startIndex));
                if (startIndex == cur.keys.size() - 1) {
                    cur = cur.next;
                    startIndex = 0;
                } else {
                    startIndex++;
                }
            }
            return result;
        }
    }
}
