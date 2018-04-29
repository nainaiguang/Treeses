package com.nng.unit.sort.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 字符串排序工具类
 * @author Vijay
 *
 */
public class StringSortUtil extends SortUtilTemplate<String> {
	@Override
	public void sort(List<Entry<Integer, String>> srcDataEntryList, boolean preface) {
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, String>>() {
			@Override
			public int compare(Entry<Integer, String> entryA, Entry<Integer, String> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				return preface ? result : -result;
			}
		});
	}
}
