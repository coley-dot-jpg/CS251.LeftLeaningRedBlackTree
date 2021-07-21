import java.util.*;
import java.io.*;



/**
 * RedBlackBST class
 *
 */
public class RedBlackBST<Key extends Comparable<Key>, Value> {

	private static final boolean RED   = true;
	private static final boolean BLACK = false;
	Node root;     // root of the BST
	private List<Key> inOrder;

	/*************************************************************************
	 *  Node Class and methods - DO NOT MODIFY
	 *************************************************************************/
	public class Node {
		Key key;           // key
		Value val;         // associated data
		Node left, right;  // links to left and right subtrees
		boolean color;     // color of parent link
		int N;             // subtree count

		public Node(Key key, Value val, boolean color, int N) {
			this.key = key;
			this.val = val;
			this.color = color;
			this.N = N;
		}
	}

	// is node x red; false if x is null ?
	private boolean isRed(Node x) {
		if (x == null) return false;
		return (x.color == RED);
	}

	// number of node in subtree rooted at x; 0 if x is null
	private int size(Node x) {
		if (x == null) return 0;
		return x.N;
	}

	// return number of key-value pairs in this symbol table
	public int size() { return size(root); }

	// is this symbol table empty?
	public boolean isEmpty() {
		return root == null;
	}

	public RedBlackBST() {
		this.root = null;
	}

	/*************************************************************************
	 *  Modification Functions
	 *************************************************************************/

	// insert the key-value pair; overwrite the old value with the new value
	// if the key is already present
	public void insert(Key key, Value val) {
		// check if key - value pair is null, if it is do nothing
		if (key == null || val == null) {
			return;
		}
		Node newNode = new Node(key, val, RED, 1);
		newNode.left = null;
		newNode.right = null;
		// if tree is empty, set the root.
		if (isEmpty()) {
			this.root = newNode;
		}
		// start search for where to insert elements passing along the new node to be inserted
		insert(this.root, newNode);
		this.root = balance(this.root);
		this.root.color = BLACK;
	}
	private void insert(Node compare, Node newNode) {
		if (newNode.key.compareTo(compare.key) < 0) {
			// if the new nodes key is less than the key we are currently on, go to the left
			// if its left node is null this is where the new node goes
			if (compare.left == null) {
				compare.left = newNode;
			} else {
				insert(compare.left, newNode);
			}
			// balance on the way back up
			compare.left = balance(compare.left);
		} else if (newNode.key.compareTo(compare.key) > 0) {
			// if the new nodes key is greater than the key we are currently on, go to the right
			// if its left node is null this is where the new node goes
			if (compare.right == null) {
				compare.right = newNode;
			} else {
				insert(compare.right, newNode);
			}
			// balance on the way back up
			compare.right = balance(compare.right);
		} else {
			// the keys are the same so replace the value of that key with the value form the new key
			compare.val = newNode.val;
		}
	}

	// delete the key-value pair with the given key
	public void delete(Key key) {
		// check if the tree is empty and if the node is in the tree
		if (isEmpty() || !contains(key)) {
			return;
		}
		// start moving red down
		if (!isRed(root.left) && !isRed(root.right)) {
			this.root.color = RED;
		}
		// find the node and delete it
		this.root = delete(this.root, key);
		// if the tree is not empty set the color of the root back to black
		if (!isEmpty()) {
			this.root.color = BLACK;
		}
	}
	private Node delete(Node compare, Key deleteIt) {
		//if the key we searching for is less than the current nodes key go to the left
		if (deleteIt.compareTo(compare.key) < 0) {
			// if needed, move red left as we go down
			if (!isRed(compare.left) && !isRed(compare.left.left)) {
				compare = moveRedLeft(compare);
			}
			// go left to continue search
			compare.left = delete(compare.left, deleteIt);
		} else if (deleteIt.compareTo(compare.key) > 0) {
			// if the key we searching for is greater than the current nodes key go to the right

			// if needed, move red right as we go down
			if (isRed(compare.left)) {
				compare = rotateRight(compare);
			}
			if (!isRed(compare.right) && !isRed(compare.right.left)) {
				compare = moveRedRight(compare);
			}
			// go right to continue search
			compare.right = delete(compare.right, deleteIt);
		} else {

			// if node is at the bottom and is red with we just delete it by setting to null
			if (isRed(compare) && compare.left == null && compare.right == null) {
				// setting to null
				return null;
			} else { // node is not at bottom of tree so we must find a replacement
				// its replacement will be in the left most node it its right subtree,
				// if no right child then we replace it with the left child
				if (compare.right == null) {
					return compare.left;
				}
				// find the key and value of the node we are replacing it with
				inOrder = new ArrayList<>();
				getElements(compare.right);
				Key replaceKey = inOrder.get(0);
				// set the deleted nodes key and value equal to the replacements and delete the replacement node
				compare.right = findReplacement(compare.right, replaceKey);
				compare.key = replaceKey;
				compare.val = search(replaceKey);
			}
		}
		// balance on the way back up
		return balance(compare);
	}
	private Node findReplacement(Node deleted, Key replacekey) {
		// if we found the replacement then delete it
		if (deleted.key == replacekey) {
			return null;
		}
		// move red to the left if needed
		if (!isRed(deleted.left) && !isRed(deleted.left.left)) {
				deleted = moveRedLeft(deleted);
			}
		// keep going left ot find the replacement if not found
		deleted.left = findReplacement(deleted.left, replacekey);
		// balance tree on way back up
		return balance(deleted);
	}

	/*************************************************************************
	 *  Search Functions
	 *************************************************************************/

	// value associated with the given key; null if no such key
	public Value search(Key key) {
		Node current = this.root;
		while (current != null) {
			if (key.compareTo(current.key) < 0) {
				// if the key we searching for is less than the current nodes key go to the left
				// if its left node is null this is where the new node goes
				current = current.left;
			} else if (key.compareTo(current.key) > 0) {
				// if the key we searching for is greater than the current nodes key go to the right
				current = current.right;
			} else if(key.compareTo(current.key) == 0) {
				// the keys are the same return its value
				return current.val;
			}
		}
		// if the while loop went through the whole tree without finding the key then return null
		return null;
	}

	// is there a key-value pair with the given key?
	public boolean contains(Key key) {
		return (search(key) != null);
	}



	/*************************************************************************
	 *  Utility Functions
	 *************************************************************************/

	// height of tree (1-node tree has height 0)
	public int height() { return height(root); }
	private int height(Node x) {
		if (x == null) return -1;
		return 1 + Math.max(height(x.left), height(x.right));
	}

	/*************************************************************************
	 *  Rank Methods
	 *************************************************************************/

	// the key of rank k
	public Key getValByRank(int k) {
		// reset static list of all elements
		inOrder = new ArrayList<Key>();
		getElements(root);
		// if index is out of range then return null
		if (k >= this.root.N) {
			return null;
		}
		return inOrder.get(k);
	}

	// number of keys less than key
	public int rank(Key key) {
		// reset static list of all elements
		int keys = 0;
		inOrder = new ArrayList<>();
		getElements(this.root);
		// count the number of keys less than the given keys
		for (int i = 0; i < this.inOrder.size(); i++) {
			if (this.inOrder.get(i).compareTo(key) < 0) {
				keys++;
			}
		}
		// return rank/index of key
		return keys;
	}



	/***********************************************************************
	 *  Range count and range search.
	 ***********************************************************************/

	private void getElements(Node node) {
		if (node == null){
			return;
		}
		getElements(node.left);
		this.inOrder.add(node.key);
		getElements(node.right);
	}
	public List<Key> getElements(int a, int b) {
		List<Key> interval = new ArrayList<Key>();
		this.inOrder = new ArrayList<>();
		getElements(this.root);
		// if range is invalid then return empty list
		if (a < 0 || b >= this.inOrder.size()) {
			return interval;
		}
		// add elements only in the interval to the interval list from list of all elements
		for (int i = a; i <= b; i++) {
			interval.add(this.inOrder.get(i));
		}
		return interval;
	}

	/*************************************************************************
	 *  red-black tree helper functions
	 *************************************************************************/

	// make a left-leaning link lean to the right
	private Node rotateRight(Node h) {
		// assert (h != null) && isRed(h.left);
		Node x = h.left;
		h.left = x.right;
		x.right = h;
		x.color = x.right.color;
		x.right.color = RED;
		x.N = h.N;
		h.N = size(h.left) + size(h.right) + 1;
		return x;
	}

	// make a right-leaning link lean to the left
	private Node rotateLeft(Node h) {
		// assert (h != null) && isRed(h.right);
		Node x = h.right;
		h.right = x.left;
		x.left = h;
		x.color = x.left.color;
		x.left.color = RED;
		x.N = h.N;
		h.N = size(h.left) + size(h.right) + 1;
		return x;
	}

	// flip the colors of a node and its two children
	private void flipColors(Node h) {
		// h must have opposite color of its two children
		// assert (h != null) && (h.left != null) && (h.right != null);
		// assert (!isRed(h) &&  isRed(h.left) &&  isRed(h.right))
		//     || (isRed(h)  && !isRed(h.left) && !isRed(h.right));
		h.color = !h.color;
		h.left.color = !h.left.color;
		h.right.color = !h.right.color;
	}

	// Assuming that h is red and both h.left and h.left.left
	// are black, make h.left or one of its children red.
	private Node moveRedLeft(Node h) {
		// assert (h != null);
		// assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

		flipColors(h);
		if (isRed(h.right.left)) {
			h.right = rotateRight(h.right);
			h = rotateLeft(h);
		}
		return h;
	}

	// Assuming that h is red and both h.right and h.right.left
	// are black, make h.right or one of its children red.
	private Node moveRedRight(Node h) {
		// assert (h != null);
		// assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
		flipColors(h);
		if (isRed(h.left.left)) {
			h = rotateRight(h);
		}
		return h;
	}

	// restore red-black tree invariant
	private Node balance(Node h) {
		// assert (h != null);

		if (isRed(h.right))                      h = rotateLeft(h);
		if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
		if (isRed(h.left) && isRed(h.right))     flipColors(h);

		h.N = size(h.left) + size(h.right) + 1;
		return h;
	}



	/*************************************************************************
	 *  The main function
	 Use this for testing
	 *************************************************************************/
	public static void main(String[] args) {

		Scanner readerTest = null;

		try {
			//Change File name to test other test files.
			readerTest = new Scanner(new File(args[0]));
		} catch (IOException e) {
			System.out.println("Reading Oops");
		}

		RedBlackBST<Integer, Integer> test = new RedBlackBST<>();

		while(readerTest.hasNextLine()){
			String[] input  =readerTest.nextLine().split(" ");

			for(String x: input){
				System.out.print(x+" ");
			}

			System.out.println();
			switch (input[0]){
				case "insert":
					Integer key = Integer.parseInt(input[1]);
					Integer val = Integer.parseInt(input[2]);
					test.insert(key,val);
					printTree(test.root);
					System.out.println();
					break;

				case "delete":
					Integer key1 = Integer.parseInt(input[1]);
					test.delete(key1);
					printTree(test.root);
					System.out.println();
					break;

				case "search":
					Integer key2 = Integer.parseInt(input[1]);
					Integer ans2 = test.search(key2);
					System.out.println(ans2);
					System.out.println();
					break;

				case "getval":
					Integer key3 = Integer.parseInt(input[1]);
					Integer ans21 = test.getValByRank(key3);
					System.out.println(ans21);
					System.out.println();
					break;

				case "rank":
					Integer key4 = Integer.parseInt(input[1]);
					Object ans22 = test.rank(key4);
					System.out.println(ans22);
					System.out.println();
					break;

				case "getelement":
					Integer low = Integer.parseInt(input[1]);
					Integer high = Integer.parseInt(input[2]);
					List<Integer> testList = test.getElements(low,high);

					for(Integer list : testList){
						System.out.println(list);
					}

					break;

				default:
					System.out.println("Error, Invalid test instruction! "+input[0]);
			}
		}

	}


	/*************************************************************************
	 *  Prints the tree
	 *************************************************************************/
	public static void printTree(RedBlackBST.Node node){

		if (node == null){
			return;
		}

		printTree(node.left);
		System.out.print(((node.color == true)? "Color: Red; ":"Color: Black; ") + "Key: " + node.key + "\n");
		printTree(node.right);
	}
}