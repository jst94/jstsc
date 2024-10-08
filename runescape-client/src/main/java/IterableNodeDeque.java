import java.util.Collection;
import java.util.Iterator;
import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("pf")
@Implements("IterableNodeDeque")
public class IterableNodeDeque implements Iterable, Collection {
	@ObfuscatedName("ab")
	@ObfuscatedSignature(
		descriptor = "Ltp;"
	)
	@Export("sentinel")
	Node sentinel;
	@ObfuscatedName("ay")
	@ObfuscatedSignature(
		descriptor = "Ltp;"
	)
	Node field4629;

	public IterableNodeDeque() {
		this.sentinel = new Node();
		this.sentinel.previous = this.sentinel;
		this.sentinel.next = this.sentinel;
	}

	@ObfuscatedName("ab")
	@Export("rsClear")
	public void rsClear() {
		while (this.sentinel.previous != this.sentinel) {
			this.sentinel.previous.remove();
		}

	}

	@ObfuscatedName("ay")
	@ObfuscatedSignature(
		descriptor = "(Ltp;)V"
	)
	@Export("addFirst")
	public void addFirst(Node var1) {
		if (var1.next != null) {
			var1.remove();
		}

		var1.next = this.sentinel.next;
		var1.previous = this.sentinel;
		var1.next.previous = var1;
		var1.previous.next = var1;
	}

	@ObfuscatedName("an")
	@ObfuscatedSignature(
		descriptor = "(Ltp;)V"
	)
	@Export("addLast")
	public void addLast(Node var1) {
		if (var1.next != null) {
			var1.remove();
		}

		var1.next = this.sentinel;
		var1.previous = this.sentinel.previous;
		var1.next.previous = var1;
		var1.previous.next = var1;
	}

	@ObfuscatedName("ax")
	@ObfuscatedSignature(
		descriptor = "()Ltp;"
	)
	@Export("last")
	public Node last() {
		return this.method7577((Node)null);
	}

	@ObfuscatedName("ao")
	@ObfuscatedSignature(
		descriptor = "(Ltp;)Ltp;"
	)
	Node method7577(Node var1) {
		Node var2;
		if (var1 == null) {
			var2 = this.sentinel.previous;
		} else {
			var2 = var1;
		}

		if (var2 == this.sentinel) {
			this.field4629 = null;
			return null;
		} else {
			this.field4629 = var2.previous;
			return var2;
		}
	}

	@ObfuscatedName("am")
	@ObfuscatedSignature(
		descriptor = "()Ltp;"
	)
	@Export("previous")
	public Node previous() {
		Node var1 = this.field4629;
		if (var1 == this.sentinel) {
			this.field4629 = null;
			return null;
		} else {
			this.field4629 = var1.previous;
			return var1;
		}
	}

	@ObfuscatedName("ac")
	int method7574() {
		int var1 = 0;

		for (Node var2 = this.sentinel.previous; var2 != this.sentinel; var2 = var2.previous) {
			++var1;
		}

		return var1;
	}

	@ObfuscatedName("ae")
	public boolean method7575() {
		return this.sentinel.previous == this.sentinel;
	}

	@ObfuscatedName("ad")
	@ObfuscatedSignature(
		descriptor = "()[Ltp;"
	)
	Node[] method7576() {
		Node[] var1 = new Node[this.method7574()];
		int var2 = 0;

		for (Node var3 = this.sentinel.previous; var3 != this.sentinel; var3 = var3.previous) {
			var1[var2++] = var3;
		}

		return var1;
	}

	@ObfuscatedName("aq")
	@ObfuscatedSignature(
		descriptor = "(Ltp;)Z"
	)
	boolean method7566(Node var1) {
		this.addFirst(var1);
		return true;
	}

	public Iterator iterator() {
		return new IterableNodeDequeDescendingIterator(this);
	}

	public int size() {
		return this.method7574();
	}

	public boolean isEmpty() {
		return this.method7575();
	}

	public boolean contains(Object var1) {
		throw new RuntimeException();
	}

	public boolean add(Object var1) {
		return this.method7566((Node)var1);
	}

	public Object[] toArray(Object[] var1) {
		int var2 = 0;

		for (Node var3 = this.sentinel.previous; var3 != this.sentinel; var3 = var3.previous) {
			var1[var2++] = var3;
		}

		return var1;
	}

	public boolean remove(Object var1) {
		throw new RuntimeException();
	}

	public boolean containsAll(Collection var1) {
		throw new RuntimeException();
	}

	public boolean addAll(Collection var1) {
		throw new RuntimeException();
	}

	public boolean removeAll(Collection var1) {
		throw new RuntimeException();
	}

	public boolean retainAll(Collection var1) {
		throw new RuntimeException();
	}

	public void clear() {
		this.rsClear();
	}

	public Object[] toArray() {
		return this.method7576();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object var1) {
		return super.equals(var1);
	}

	@ObfuscatedName("au")
	@ObfuscatedSignature(
		descriptor = "(Ltp;Ltp;)V"
	)
	@Export("IterableNodeDeque_addBefore")
	public static void IterableNodeDeque_addBefore(Node var0, Node var1) {
		if (var0.next != null) {
			var0.remove();
		}

		var0.next = var1;
		var0.previous = var1.previous;
		var0.next.previous = var0;
		var0.previous.next = var0;
	}
}
