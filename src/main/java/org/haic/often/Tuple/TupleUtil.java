package org.haic.often.Tuple;

public final class TupleUtil {

	private TupleUtil() {
	}

	public static <A, B> TwoTuple<A, B> Tuple(A a, B b) {
		return new TwoTuple<>(a, b);
	}

	public static <A, B, C> ThreeTuple<A, B, C> Tuple(A a, B b, C c) {
		return new ThreeTuple<>(a, b, c);
	}

	public static <A, B, C, D> FourTuple<A, B, C, D> Tuple(A a, B b, C c, D d) {
		return new FourTuple<>(a, b, c, d);
	}

	public static <A, B, C, D, E> FiveTuple<A, B, C, D, E> Tuple(A a, B b, C c, D d, E e) {
		return new FiveTuple<>(a, b, c, d, e);
	}

}
