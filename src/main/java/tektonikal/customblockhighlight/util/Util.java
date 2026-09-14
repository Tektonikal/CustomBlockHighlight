package tektonikal.customblockhighlight.util;

import java.util.Iterator;
import java.util.List;

public class Util {
	public static <E> Iterable<E> concat(List<E> a, List<E> b) {
		return () -> new Iterator<>() {
			private final Iterator<E> first = a.iterator();
			private final Iterator<E> second = b.iterator();

			@Override
			public boolean hasNext() {
				return first.hasNext() || second.hasNext();
			}

			@Override
			public E next() {
				if (first.hasNext())
					return first.next();
				return second.next();
			}
		};
	}
}
