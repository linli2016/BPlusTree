
enum TreeNodeType {
	InnerNode,
	LeafNode
}

abstract class BTreeNode<TKey extends Comparable<TKey>> {
	protected Object[] keys;
	protected int keyCount;
	protected BTreeNode<TKey> parentNode;
	protected BTreeNode<TKey> leftSibling;
	protected BTreeNode<TKey> rightSibling;
	

	protected BTreeNode() {
		this.keyCount = 0;
		this.parentNode = null;
		this.leftSibling = null;
		this.rightSibling = null;
	}

	public int getKeyCount() {
		return this.keyCount;
	}
	
	@SuppressWarnings("unchecked")
	public TKey getKey(int index) {
		return (TKey)this.keys[index];
	}

	public void setKey(int index, TKey key) {
		this.keys[index] = key;
	}

	public BTreeNode<TKey> getParent() {
		return this.parentNode;
	}

	public void setParent(BTreeNode<TKey> parent) {
		this.parentNode = parent;
	}	
	
	public abstract TreeNodeType getNodeType();
	
	
	/**
	 * Search a key on current node, if found the key then return its position,
	 * otherwise return -1 for a leaf node, 
	 * return the child node index which should contain the key for a internal node.
	 */
	public abstract int search(TKey key);
	
	
	
	/* The codes below are used to support insertion operation */
	
	public boolean isOverflow() {
		return this.getKeyCount() == this.keys.length;
	}
	
	public BTreeNode<TKey> dealOverflow() {
		int midIndex = this.getKeyCount() / 2;
		TKey upKey = this.getKey(midIndex);
		
		BTreeNode<TKey> newRNode = this.split();
				
		if (this.getParent() == null) {
			this.setParent(new BTreeInnerNode<TKey>());
		}
		newRNode.setParent(this.getParent());
		
		// maintain links of sibling nodes
		newRNode.setLeftSibling(this);
		newRNode.setRightSibling(this.rightSibling);
		if (this.getRightSibling() != null)
			this.getRightSibling().setLeftSibling(newRNode);
		this.setRightSibling(newRNode);
		
		// push up a key to parent internal node
		return this.getParent().pushUpKey(upKey, this, newRNode);
	}
	
	protected abstract BTreeNode<TKey> split();
	
	protected abstract BTreeNode<TKey> pushUpKey(TKey key, BTreeNode<TKey> leftChild, BTreeNode<TKey> rightNode);
	
	
	
	
	
	
	/* The codes below are used to support deletion operation */
	
	public boolean isUnderflow() {
		return this.getKeyCount() < (this.keys.length / 2);
	}
	
	public boolean canLendAKey() {
		return this.getKeyCount() > (this.keys.length / 2);
	}
	
	public BTreeNode<TKey> getLeftSibling() {
		if (this.leftSibling != null && this.leftSibling.getParent() == this.getParent())
			return this.leftSibling;
		return null;
	}

	public void setLeftSibling(BTreeNode<TKey> sibling) {
		this.leftSibling = sibling;
	}

	public BTreeNode<TKey> getRightSibling() {
		if (this.rightSibling != null && this.rightSibling.getParent() == this.getParent())
			return this.rightSibling;
		return null;
	}

	public void setRightSibling(BTreeNode<TKey> silbling) {
		this.rightSibling = silbling;
	}
	
	public BTreeNode<TKey> dealUnderflow() {
		if (this.getParent() == null)
			return null;
		
		// try to borrow a key from sibling
		BTreeNode<TKey> leftSibling = this.getLeftSibling();
		if (leftSibling != null && leftSibling.canLendAKey()) {
			this.getParent().processChildrenTransfer(this, leftSibling, leftSibling.getKeyCount() - 1);
			return null;
		}
		
		BTreeNode<TKey> rightSibling = this.getRightSibling();
		if (rightSibling != null && rightSibling.canLendAKey()) {
			this.getParent().processChildrenTransfer(this, rightSibling, 0);
			return null;
		}
		
		// Can not borrow a key from any sibling, then do fusion with sibling
		if (leftSibling != null) {
			return this.getParent().processChildrenFusion(leftSibling, this);
		}
		else {
			return this.getParent().processChildrenFusion(this, rightSibling);
		}
	}
	
	protected abstract void processChildrenTransfer(BTreeNode<TKey> borrower, BTreeNode<TKey> lender, int borrowIndex);
	
	protected abstract BTreeNode<TKey> processChildrenFusion(BTreeNode<TKey> leftChild, BTreeNode<TKey> rightChild);
	
	protected abstract void fusionWithSibling(TKey sinkKey, BTreeNode<TKey> rightSibling);
	
	protected abstract TKey transferFromSibling(TKey sinkKey, BTreeNode<TKey> sibling, int borrowIndex);
}