package com.nng.unit.sort.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 整型数排序工具类
 * @author Vijay
 *
 */
public class IntegerSortUtil extends SortUtilTemplate<Integer> {

	@Override
	public void sort(List<Entry<Integer, Integer>> srcDataEntryList, boolean preface) {
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> entryA, Entry<Integer, Integer> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				return preface ? result : -result;
			}
		});
	}

}
