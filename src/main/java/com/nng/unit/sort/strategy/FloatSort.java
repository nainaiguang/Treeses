package com.nng.unit.sort.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据类型比较策略 -- 单精度浮点型比较
 * @author Vijay
 * @category SortByDataTypeStrategy<Float>
 *
 */
public class FloatSort implements SortUtil<Float> {
	
	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData    数据源
	 * @param preface    序向（当数值比较时，大小数值排序）
	 * @return resultMap
	 */
	@Override
	public Map<Integer, Float> sort(Map<Integer, Float> srcData, boolean preface) {
		Map<Integer, Float> resultMap = new LinkedHashMap<>();
		
		List<Entry<Integer, Float>> srcDataEntryList = new ArrayList<>();
		
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Float>>() {

			@Override
			public int compare(Entry<Integer, Float> entryA, Entry<Integer, Float> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				
				return preface ? result : -result;
			}
		});
		
		for (Entry<Integer, Float> entry : srcDataEntryList) {
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}

}
