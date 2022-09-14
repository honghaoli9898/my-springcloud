package com.seaboxdata.sdps.uaa.example;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Test {

	public static void main(String[] args) {
		List<Pair<String, Integer>> list = new ArrayList<>();
		list.add(new ImmutablePair<>("TR", 90));
		list.add(new ImmutablePair<>("TX", 10));
		WeightRandomStrategy<String, Integer> strategy = new WeightRandomStrategy<>(
				list);
		int a = 0, b = 0;
		for (int i = 0; i < 10000; i++) {
			switch (strategy.random()) {
			case "TR":
				a++;
				break;
			case "TX":
				b++;
				break;
			default:
				break;
			}
		}
		System.out.println("a=" + a + ", b=" + b);
		System.out.println("a+b=" + (a + b));
	}

}

class WeightRandomStrategy<K, V extends Number> {
	private TreeMap<Double, K> weightMap = new TreeMap<>();

	public WeightRandomStrategy(List<Pair<K, V>> list) {
		for (Pair<K, V> pair : list) {
			Double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap
					.lastKey();
			this.weightMap.put(pair.getValue().doubleValue() + lastWeight,
					pair.getKey());
		}
	}

	public K random() {
		Double randomWeight = this.weightMap.lastKey() * Math.random();
		SortedMap<Double, K> tailMap = this.weightMap.tailMap(randomWeight,
				false);
		return this.weightMap.get(tailMap.firstKey());
	}
}
