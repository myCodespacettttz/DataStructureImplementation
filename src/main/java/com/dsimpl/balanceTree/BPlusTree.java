package com.dsimpl.balanceTree;

import java.util.*;

/**
 * 参考看图写代码,可视化数据结构网站:https://www.cs.usfca.edu/~galles/visualization/Algorithms.html
 * 1: B+树的节点由关键字(排序字段,可以是任何实现了比较手段的数据类型)与数据组成的
 * 2: 非叶子节点中,数组中最前面与最后面的元素,就是其所有的所属叶子节点中元素的最大值和最小值
 * 3: 除根节点以外,每个节点至少有m/2个子节点,根节点如果有子节点的话至少有两个子节点
 * 3: 有N个子女的节点必须是N个关键字
 * 4: B+树数据存在底层叶子节点,所有非叶子节点的关键字都会出现在叶子节点,叶子节点用链表链接,且叶子节点个数等于分支数
 */
public class BPlusTree<K extends Comparable<K>, V> {
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

    public void remove(K key) {
        root.remove(key);
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

        /**
         * @param key
         * @return 返回传入关键字所在的叶子节点
         */
        public BPlusNode findChildNode(K key) {
            int ceilingKeyIndex = findCeilingKeyIndex(key);
            int childIndex = findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            BPlusNode cur = root;
            while (!cur.isLeaf) {
                cur = cur.childen.get(childIndex);
                ceilingKeyIndex = cur.findCeilingKeyIndex(key);
                childIndex = cur.findChildIndexByCeilingKeyIndex(ceilingKeyIndex, key);
            }
            return cur;
        }

        public void put(K key, V value) {
            findChildNode(key).doPut(key, value);
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

        public V get(K key) {
            return findChildNode(key).doGet(key);
        }

        public boolean containsKey(K key) {
            BPlusNode cur = findChildNode(key);
            return cur.contains(key) != -1;
        }

        public K floorKey(K key) {
            return findChildNode(key).doFloorKey(key);
        }

        public K ceilingKey(K key) {
            return findChildNode(key).doCeilingKey(key);
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
            BPlusNode cur = findChildNode(start);
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

        public void remove(K key) {
            findChildNode(key).doRemove(key);
        }

        private void doRemove(K key) {
            if (containsKey(key)) {
                return;
            }
            size--;
            //1:如果给定的key位于root节点且是叶子节点,直接删除即可
            if (this.isLeaf && this.isRoot) {
                //只有一个根节点且关键字总数等于1,需要重置高度
                if (this.keys.size() == 1) {
                    height = 0;
                }
                removeKeyAndValue(key);
            }
            //2:关键字个数大于下界,即使删除后也不会低于下界,直接删除即可,若没有触发12,则说明删除后元素小于下界,需要左右借值操作
            if (keys.size() > UNDER_BOUND && keys.size() > 2) {
                removeKeyAndValue(key);
                return;
            }
            //3:判断当前节点的是否存在前驱节点,若存在且前驱节点元素大于下界,并且前驱节点与当前节点拥有同样的父节点,则向前借
            if (pre != null && pre.keys.size() > UNDER_BOUND && pre.parent == parent && pre.keys.size() > 2) {
                //将前一个节点的最后一个元素删除后加入当前节点
                keys.add(0, pre.keys.remove(pre.keys.size() - 1));
                datas.add(0, pre.datas.remove(pre.datas.size() - 1));
                //拿到前驱节点在父节点中的下标,将本次需要加入的前驱节点
                int index = parent.childen.indexOf(pre);
                parent.keys.set(index, keys.get(0));
                removeKeyAndValue(key);
            } else if (next != null && next.keys.size() > UNDER_BOUND && next.parent == parent && next.keys.size() > 2) {
                //4:前驱节点借不了值的情况下向后驱节点借
                keys.add(next.keys.remove(0));
                datas.add(next.datas.remove(0));
                int index = parent.childen.indexOf(this);
                parent.keys.set(index, keys.get(0));
                removeKeyAndValue(key);
            } else if (pre != null && pre.parent == parent && (pre.keys.size() <= UNDER_BOUND || pre.keys.size() <= 2)) {
                //5:前驱后继都借不了的情况下,将当前节点的对应关键字删除后,剩余关键字向前驱合并,随后将前驱与后继相连
                removeKeyAndValue(key);
                pre.keys.addAll(keys);
                pre.datas.addAll(datas);
                keys = pre.keys;
                datas = pre.datas;
                parent.childen.remove(pre);
                pre.parent = null;
                pre.keys = null;
                pre.datas = null;
                //6:前驱节点如果还存在前驱,则将前驱的前驱与当前节点相连接,断开当前节点的前驱节点
                if (pre.pre != null) {
                    BPlusNode temp = pre;
                    temp.pre.next = this;
                    pre = temp.pre;
                    temp.pre = null;
                    temp.next = null;
                } else {
                    //前驱没有前驱节点,说明前驱自身就是头节点
                    head = this;
                    pre.next = null;
                    pre = null;
                }
                //7:如果删除的关键字在父结点中也存在,则删除父节点中对应的关键字
                parent.keys.remove(parent.childen.indexOf(this));
                if (parent.keys.size() > UPPER_BOUND && parent.keys.size() >= 2) {
                    return;
                }
                parent.removeMatain();
            } else if (next != null && next.parent == parent && (next.keys.size() <= UNDER_BOUND || next.keys.size() <= 2)) {
                removeKeyAndValue(key);
                keys.addAll(next.keys);
                datas.addAll(next.datas);
                next.parent = null;
                next.keys = null;
                next.datas = null;
                parent.childen.remove(next);
                if (next.next != null) {
                    BPlusNode temp = next;
                    temp.next.pre = this;
                    next = temp.next;
                    temp.pre = null;
                    temp.next = null;
                } else {
                    tail = this;
                    next.pre = null;
                    next = null;
                }
                parent.keys.remove(parent.childen.indexOf(this));
                if (parent.keys.size() > UPPER_BOUND && parent.keys.size() >= 2) {
                    return;
                }
                parent.removeMatain();
            }

        }

        private void removeMatain() {
            if (!(childen.size() < UNDER_BOUND || childen.size() < 2)) {
                return;
            }
            //8:如果父节点是根节点,就将合并后的叶子节点链表替换为父节点即可
            if (isRoot) {
                if (childen.size() > 2) {
                    return;
                }
                root = childen.get(0);
                root.parent = null;
                root.isRoot = true;
                keys = null;
                childen = null;
                height--;
                return;
            }
            //9:说明父亲节点是非叶子节点,也就是说父亲节点在中间层
            int index = parent.childen.indexOf(this);
            int leftIndex = index - 1;
            int rightIndex = index + 1;
            BPlusNode leftBro = null;
            BPlusNode rightBro = null;
            if (leftIndex >= 0) {
                leftBro = parent.childen.get(leftIndex);
            }
            if (rightIndex < parent.childen.size()) {
                rightBro = parent.childen.get(rightIndex);
            }
            if (leftBro != null && leftBro.childen.size() > UNDER_BOUND && leftBro.childen.size() > 2) {
                //10:删掉左边的最大的孩子,然后将其变成当前节点的孩子
                BPlusNode brrow = leftBro.childen.get(leftBro.childen.size() - 1);
                leftBro.childen.remove(leftBro.childen.size() - 1);
                brrow.parent = this;
                childen.add(0, brrow);
                //11:从父亲节点中拿到左兄弟的下标
                int preIndex = parent.childen.indexOf(leftBro);
                keys.add(0, parent.keys.get(preIndex));
                parent.keys.set(preIndex, leftBro.keys.remove(leftBro.keys.size() - 1));
            } else if (rightBro != null && rightBro.childen.size() > UNDER_BOUND && rightBro.childen.size() > 2) {
                //12:左边借不了再试试右边能否满足借值条件
                BPlusNode brrow = rightBro.childen.get(0);
                rightBro.childen.remove(0);
                brrow.parent = this;
                childen.add(0, brrow);
                int nextIndex = parent.childen.indexOf(this);
                keys.add(parent.keys.get(nextIndex));
                parent.keys.set(nextIndex, leftBro.keys.remove(0));
            } else if (leftBro != null && leftBro.childen.size() <= UNDER_BOUND && leftBro.childen.size() <= 2) {
                //13:左右都无法满足借值条件则合并当前节点与左叶子节点
                leftBro.childen.addAll(childen);
                for (int i = 0; i < leftBro.childen.size(); i++) {
                    leftBro.childen.get(i).parent = this;
                }
                int indexPost = parent.childen.indexOf(leftBro);
                leftBro.keys.add(parent.keys.get(indexPost));
                leftBro.keys.addAll(keys);
                childen = leftBro.childen;
                keys = leftBro.keys;
                parent.childen.remove(leftBro);
                leftBro.parent = null;
                leftBro.keys = null;
                leftBro.childen = null;
                parent.keys.remove(parent.childen.indexOf(this));
                if (parent.isRoot && (parent.childen.size() >= UNDER_BOUND && parent.childen.size() >= 2)) {
                    return;
                }
                parent.removeMatain();
            } else if (rightBro != null && rightBro.childen.size() <= UNDER_BOUND && rightBro.childen.size() <= 2) {
                childen.addAll(rightBro.childen);
                for (BPlusNode child : rightBro.childen) {
                    child.parent = this;
                }
                int preIndex = parent.childen.indexOf(this);
                keys.add(parent.keys.get(preIndex));
                keys.addAll(rightBro.keys);
                parent.childen.remove(rightBro);
                rightBro.parent = null;
                rightBro.keys = null;
                rightBro.childen = null;
                parent.keys.remove(parent.childen.indexOf(this));
                if (parent.childen.size() >= UNDER_BOUND && parent.childen.size() >= 2) {
                    return;
                }
                parent.removeMatain();
            }
        }

        private void removeKeyAndValue(K key) {
            int left = 0;
            int right = keys.size() - 1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                int compare = key.compareTo(keys.get(mid));
                if (compare == 0) {
                    keys.remove(mid);
                    datas.remove(mid);
                    break;
                } else if (compare > 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }

    }
}
