package com.nng.unit.sort.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据类型比较策略 -- 双精度浮点型比较
 * @author Vijay
 * @category SortByDataTypeStrategy<Double>
 *
 */
public class DoubleSort implements SortUtil<Double> {

	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData    数据源
	 * @param preface    序向（当数值比较时，大小数值排序）
	 * @return resultMap
	 */
	@Override
	public Map<Integer, Double> sort(Map<Integer, Double> srcData, boolean preface) {
		Map<Integer, Double> resultMap = new LinkedHashMap<>();
		
		List<Entry<Integer, Double>> srcDataEntryList = new ArrayList<>(srcData.entrySet());
		
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Double>>() {

			@Override
			public int compare(Entry<Integer, Double> entryA, Entry<Integer, Double> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				
				return preface ? result : -result;
			}
		});
		
		for (Entry<Integer, Double> entry : srcDataEntryList) {
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}

}
