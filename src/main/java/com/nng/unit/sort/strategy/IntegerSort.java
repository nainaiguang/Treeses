package com.nng.unit.sort.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据类型比较策略 -- 整型比较
 * @author Vijay
 * @category SortByDataTypeStrategy<Integer>
 *
 */
public class IntegerSort implements SortUtil<Integer> {
	
	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData    数据源
	 * @param preface    序向（当数值比较时，大小数值排序）
	 * @return resultMap
	 */
	@Override
	public Map<Integer, Integer> sort(Map<Integer, Integer> srcData, boolean preface) {
		Map<Integer, Integer> resultMap = new LinkedHashMap<>();
		
		List<Entry<Integer, Integer>> srcDataEntryList = new ArrayList<>(srcData.entrySet());
		
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> entrytA, Entry<Integer, Integer> entryB) {
				int result = entrytA.getValue().compareTo(entryB.getValue());
				
				return preface ? result : -result;
			}
		});
		
		for (Entry<Integer, Integer> entry : srcDataEntryList) {
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}

}
