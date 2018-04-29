package com.nng.unit.sort.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 字符排序工具类
 * @author Vijay
 *
 */
public class CharacterSortUtil extends SortUtilTemplate<Character> {

	@Override
	protected void sort(List<Entry<Integer, Character>> srcDataEntryList, boolean preface) {
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Character>>() {
			@Override
			public int compare(Entry<Integer, Character> entryA, Entry<Integer, Character> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				return preface ? result : -result;
			}
		});
	}
}
