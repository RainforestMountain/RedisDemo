package com.example.redisdemo_1.skiplist;

class Skiplist {
    /**
     * 最大层数
     */
    private static int DEFAULT_MAX_LEVEL = 32;
    /**
     * 随机层数概率，也就是随机出的层数，在第一层以上（不包括第一层）的概率，层数不超过maxLevel, 层数的起始号是1
     * 1 < level <= max_level
     * 大于这个概率就是说，选择这个层数
     */
    private static double DEFAULT_P_FACTOR = 0.25;

    /**
     * 头节点
     */
    Node head = new Node(null, DEFAULT_MAX_LEVEL);

    /**
     * 当前nodes的实际层数
     */
    int currentLevel = 1;

    public Skiplist() {

    }

    /**
     * 查找，要调用新的方法，findClosest(), 找最近小于value的node
     *
     * @param target
     * @return
     */
    public boolean search(int target) {
        Node searchNode = head;
        for (int i = currentLevel - 1; i >= 0; i--) {
            //查找的是目标的前驱节点
            searchNode = findClosest(searchNode, i, target);
            if (searchNode.next[i] != null && searchNode.next[i].value == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加一个节点，要添加索引到随机的一层
     *
     * @param num
     */
    public void add(int num) {
        int level = randomLevel();
        Node updateNode = head;
        Node newNode = new Node(num, level);
        //找到最右边小于num的节点
        for (int i = currentLevel - 1; i >= 0; i--) {
            updateNode = findClosest(updateNode, i, num);
            if (i < level) {

                if (updateNode.next[i] == null) {
                    //更新位置在末尾了
                    updateNode.next[i] = newNode;
                } else {
                    //不在末尾
                    Node next = updateNode.next[i];
                    updateNode.next[i] = newNode;
                    newNode.next[i] = next;
                }
            }
        }

        if (level > currentLevel) {
            //随机出来的层数比当前的层数还要大，
            // 那么超过currentLevel的head 直接指向newNode, 建立这些层的索引
            for (int i = currentLevel; i < level; i++) {
                head.next[i] = newNode;
            }
            currentLevel = level;
        }
    }

    public boolean erase(int num) {
        boolean flag = false;
        Node searchNode = head;
        //删除目标节点，还要删除索引（指针数组的形式）
        for (int i = currentLevel - 1; i >= 0; i--) {
            //查找的是目标的前驱节点
            searchNode = findClosest(searchNode, i, num);
            if (searchNode.next[i] != null && searchNode.next[i].value == num) {
                //找到该层的节点
                searchNode.next[i] = searchNode.next[i].next[i];
                flag = true;
                continue;
            }
        }
        //只要删除过一次，就说明删除了
        return flag;
    }


    /**
     * 从node开始，在level层, 最后一个，最右边小于value（value更大）的节点，并且更新node
     * 放弃了查找开始的节点，因为在查找中并不重要
     *
     * @param node
     * @param levelIndex
     * @param value
     * @return
     */
    private Node findClosest(Node node, int levelIndex, int value) {
        while ((node.next[levelIndex] != null) && value > node.next[levelIndex].value) {
            node = node.next[levelIndex];
        }
        return node;
    }

    /**
     * 插入值的时候，索引随机到, 一个层数区间内，其中所有层都要有，应该是为了查询效率
     * （1,randomval],小于等于这个随机值并且大于1就行
     *
     * @return
     */
    private static int randomLevel() {
        int level = 1;
        while (Math.random() < DEFAULT_P_FACTOR && level < DEFAULT_MAX_LEVEL) {
            level++;
        }
        return level;
    }

    /**
     * 标准跳表中，“同一值的节点在多层索引中复用”（即一个节点包含多层指针），本质是用最小的空间成本维护层级关联：
     * 一个节点的 next[] 数组直接存储各层的后继指针，天然实现 “高层索引指向同值在下层的位置”;
     * 插入 / 删除时只需操作一个节点，避免冗余维护;
     * 空间复杂度仍为 O (n)（平均每个节点的层级为 2~4），兼顾效率与复杂度;
     */
    class Node {
        Integer value;
        /**
         * 跳表 Node.next 用数组，本质是用数组下标映射 “索引层级”，让一个节点能高效存下 “多层索引的后继指针”。
         */
        Node[] next;

        public Node(Integer value, int size) {
            this.value = value;
            this.next = new Node[size];
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}

/**
 * Your Skiplist object will be instantiated and called as such:
 * Skiplist obj = new Skiplist();
 * boolean param_1 = obj.search(target);
 * obj.add(num);
 * boolean param_3 = obj.erase(num);
 */