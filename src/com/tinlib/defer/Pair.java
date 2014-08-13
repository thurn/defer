package com.tinlib.defer;

public final class Pair<A,B> {
  final A first;
  final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }
}
