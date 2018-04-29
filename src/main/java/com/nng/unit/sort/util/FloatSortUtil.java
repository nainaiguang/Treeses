package com.nng.unit.sort.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 单精度浮点数排序工具类
 * @author Vijay
 *
 */
public class FloatSortUtil extends SortUtilTemplate<Float> {

	@Override
	protected void sort(List<Entry<Integer, Float>> srcDataEntryList, boolean preface) {
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Float>>() {

			@Override
			public int compare(Entry<Integer, Float> entryA, Entry<Integer, Float> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				return preface ? result : -result;
			}
		});
	}

}
