/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
    private int size;
    private int numTrees;
    private int numMarked;
    private int totalLinks;
    private int totalCuts;
    private int totalHeapifyCosts;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.size = 0;
        this.numTrees = 0;
        this.numMarked = 0;
        this.totalLinks = 0;
        this.totalCuts = 0;
        this.totalHeapifyCosts = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {    
        HeapItem item = new HeapItem();
        HeapNode node = new HeapNode();
        item.key = key;
        item.info = info;
        item.node = node;
        node.item = item;
        node.rank = 0;
        node.parent = null;
        node.child = null;
        node.next = node;
        node.prev = node;
        node.mark = false;

        Heap single = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        single.min = item;
        single.size = 1;
        single.numTrees = 1;
        this.meld(single);
        return item;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return this.min;
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        if (this.min == null) {
            return;
        }
        HeapNode minNode = this.min.node;
        HeapNode childList = minNode.child;
        int childCount = 0;
        if (childList != null) {
            HeapNode curr = childList;
            do {
                curr.parent = null;
                if (curr.mark) {
                    curr.mark = false;
                    this.numMarked--;
                }
                childCount++;
                curr = curr.next;
            } while (curr != childList);
        }

        if (minNode.next == minNode) {
            this.min = null;
        } else {
            minNode.prev.next = minNode.next;
            minNode.next.prev = minNode.prev;
            this.min = minNode.next.item;
        }
        minNode.next = minNode;
        minNode.prev = minNode;
        minNode.parent = null;
        minNode.child = null;

        this.size--;
        this.numTrees--;

        boolean linked = false;
        if (childList != null) {
            Heap children = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            children.min = childList.item;
            children.size = 0;
            children.numTrees = childCount;
            this.meld(children);
            linked = !this.lazyMelds;
        }

        if (this.min == null) {
            this.numTrees = 0;
            return;
        }
        if (!linked) {
            successiveLinking();
        }
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) 
    {    
        if (x == null || diff < 0 || diff > x.key) {
            return;
        }
        decreaseKeyTo(x, x.key - diff);
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {    
        if (x == null || this.min == null) {
            return;
        }
        int targetKey = (this.min.key > 0) ? this.min.key - 1 : -1;
        decreaseKeyTo(x, targetKey);
        deleteMin();
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        if (heap2 == null) {
            return;
        }
        this.totalLinks += heap2.totalLinks;
        this.totalCuts += heap2.totalCuts;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;

        if (heap2.min == null) {
            return;
        }
        if (this.min == null) {
            this.min = heap2.min;
        } else {
            spliceLists(this.min.node, heap2.min.node);
            if (heap2.min.key < this.min.key) {
                this.min = heap2.min;
            }
        }

        this.size += heap2.size;
        this.numTrees += heap2.numTrees;
        this.numMarked += heap2.numMarked;

        if (!this.lazyMelds) {
            successiveLinking();
        }
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return this.size;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.numTrees;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.lazyDecreaseKeys ? this.numMarked : 0;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.totalHeapifyCosts;
    }

    private int heapifyUp(HeapNode node)
    {
        int swaps = 0;
        while (node.parent != null && node.item.key < node.parent.item.key) {
            HeapNode parent = node.parent;
            HeapItem tmp = node.item;
            node.item = parent.item;
            parent.item = tmp;
            node.item.node = node;
            parent.item.node = parent;
            node = parent;
            swaps++;
        }
        return swaps;
    }

    private void decreaseKeyTo(HeapItem x, int newKey)
    {
        x.key = newKey;
        HeapNode node = x.node;
        HeapNode parent = node.parent;
        if (parent != null && x.key < parent.item.key) {
            if (this.lazyDecreaseKeys) {
                cut(node, parent);
                cascadingCut(parent);
            } else {
                int swaps = heapifyUp(node);
                this.totalHeapifyCosts += swaps;
            }
        }
        if (this.min == null || x.key < this.min.key) {
            this.min = x;
        }
    }

    private void cut(HeapNode node, HeapNode parent)
    {
        if (node.next == node) {
            parent.child = null;
        } else {
            if (parent.child == node) {
                parent.child = node.next;
            }
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        parent.rank--;
        node.parent = null;
        if (node.mark) {
            node.mark = false;
            this.numMarked--;
        }
        node.next = node;
        node.prev = node;
        this.totalCuts++;

        Heap single = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        single.min = node.item;
        single.size = 0;
        single.numTrees = 1;
        this.meld(single);
    }

    private void cascadingCut(HeapNode node)
    {
        HeapNode parent = node.parent;
        if (parent == null) {
            return;
        }
        if (!node.mark) {
            node.mark = true;
            this.numMarked++;
        } else {
            cut(node, parent);
            cascadingCut(parent);
        }
    }

    private void successiveLinking()
    {
        if (this.min == null) {
            this.numTrees = 0;
            return;
        }
        int bucketsSize = bucketArraySize(this.size);
        HeapNode[] buckets = new HeapNode[bucketsSize];

        HeapNode start = this.min.node;
        HeapNode curr = start;
        do {
            HeapNode next = curr.next;
            curr.next = curr;
            curr.prev = curr;
            int rank = curr.rank;
            while (rank >= buckets.length) {
                buckets = java.util.Arrays.copyOf(buckets, rank + 2);
            }
            while (buckets[rank] != null) {
                HeapNode other = buckets[rank];
                buckets[rank] = null;
                curr = linkTrees(curr, other);
                rank = curr.rank;
                while (rank >= buckets.length) {
                    buckets = java.util.Arrays.copyOf(buckets, rank + 2);
                }
            }
            buckets[rank] = curr;
            curr = next;
        } while (curr != start);

        this.min = null;
        this.numTrees = 0;
        HeapNode root = null;
        for (HeapNode node : buckets) {
            if (node == null) {
                continue;
            }
            if (root == null) {
                root = node;
                node.next = node;
                node.prev = node;
            } else {
                insertBefore(root, node);
            }
            if (this.min == null || node.item.key < this.min.key) {
                this.min = node.item;
            }
            this.numTrees++;
        }
    }

    private HeapNode linkTrees(HeapNode first, HeapNode second)
    {
        if (second.item.key < first.item.key) {
            HeapNode tmp = first;
            first = second;
            second = tmp;
        }
        if (second.mark) {
            second.mark = false;
            this.numMarked--;
        }
        second.parent = first;
        if (first.child == null) {
            first.child = second;
            second.next = second;
            second.prev = second;
        } else {
            insertBefore(first.child, second);
        }
        first.rank++;
        this.totalLinks++;
        return first;
    }

    private static void insertBefore(HeapNode head, HeapNode node)
    {
        node.next = head;
        node.prev = head.prev;
        head.prev.next = node;
        head.prev = node;
    }

    private static void spliceLists(HeapNode a, HeapNode b)
    {
        if (a == null || b == null) {
            return;
        }
        HeapNode aNext = a.next;
        HeapNode bPrev = b.prev;
        a.next = b;
        b.prev = a;
        bPrev.next = aNext;
        aNext.prev = bPrev;
    }

    private static int bucketArraySize(int n)
    {
        int size = 1;
        int value = n;
        while (value > 0) {
            value >>= 1;
            size++;
        }
        return size + 1;
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean mark;
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
    }
}
