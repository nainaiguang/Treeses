package com.nng.unit.sort.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 双精度浮点数排序工具类
 * @author Vijay
 *
 */
public class DoubleSortUtil extends SortUtilTemplate<Double> {

	@Override
	protected void sort(List<Entry<Integer, Double>> srcDataEntryList, boolean preface) {
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Double>>() {

			@Override
			public int compare(Entry<Integer, Double> entryA, Entry<Integer, Double> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				return preface ? result : -result;
			}
		});
	}

}
