package org.tuc.btree;

import org.tuc.utils.MultiCounter;

/**
 * Represents a B+ tree data structure.
 * The structures and behaviors between internal nodes and external nodes are different,
 * so there are separate classes for each kind of node.
 *
 * @param <TKey>   the data type of the key
 * @param <TValue> the data type of the value
 */
public class BTree<TKey extends Comparable<TKey>, TValue> {

    /** The root node of the B+ tree. */
    private BTreeNode<TKey> root;

    /** The order (maximum number of keys) for the leaf nodes. */
    protected static int leafOrder;

    /**
     * Instantiates a new BTree with a custom leaf order.
     *
     * @param leafOrder the leaf order
     */
    public BTree(int leafOrder) {
        BTree.leafOrder = leafOrder;
        this.root = new BTreeLeafNode<>(leafOrder);
    }

    /**
     * Inserts a new key and its associated value into the B+ tree.
     *
     * @param key   the key
     * @param value the value
     */
    public void insert(TKey key, TValue value) {
        BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
        leaf.insertKey(key, value);

        if (leaf.isOverflow()) {
            BTreeNode<TKey> n = leaf.dealOverflow();
            if (n != null)
                this.root = n;
        }
    }

    /**
     * Searches for a key in the B+ tree and returns its associated value.
     *
     * @param key the key
     * @return the value associated with the key, or null if the key is not found
     */
    public TValue search(TKey key) {
        BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);

        int index = leaf.search(key);
        return (index == -1) ? null : leaf.getValue(index);
    }

    /**
     * Deletes a key and its associated value from the B+ tree.
     *
     * @param key the key to be deleted
     */
    public void delete(TKey key) {
        BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);

        if (leaf.delete(key) && leaf.isUnderflow()) {
            BTreeNode<TKey> n = leaf.dealUnderflow();
            if (n != null)
                this.root = n;
        }
    }

    /**
     * Searches for the leaf node that should contain the specified key.
     *
     * @param key the key
     * @return the leaf node that should contain the key
     */
    @SuppressWarnings("unchecked")
    private BTreeLeafNode<TKey, TValue> findLeafNodeShouldContainKey(TKey key) {
        BTreeNode<TKey> node = this.root;
        // in case root is the only node, the counter will still get increased.
        while (MultiCounter.increaseCounter(1) && node.getNodeType() == TreeNodeType.InnerNode) {
            node = ((BTreeInnerNode<TKey>) node).getChild(node.search(key));
        }

        return (BTreeLeafNode<TKey, TValue>) node;
    }

    /**
     * Retrieves the leaf order of the B+ tree.
     *
     * @return the leaf order
     */
    public static int getLeafOrder() {
        return leafOrder;
    }
}
