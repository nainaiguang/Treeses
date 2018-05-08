package com.nng.unit.sort.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 排序工具接口
 * 使用 模板模式 管理
 * @author Administrator
 * 
 * @param <T>
 */
public abstract class SortUtilTemplate<T> implements SortUtil<T> {
	private Map<Integer, T> resultMap =  new LinkedHashMap<>();		//结果集合
	
	//protected 防止接口 被使用者违规使用，仅允许继承的具体实现工具类使用
	//preface : true --> 正向  || false --> 反向
	protected abstract void sort(List<Entry<Integer, T>> srcDataEntryList, boolean preface);
	
	//排序工具类 统一调用的接口
	@Override
	public Map<Integer, T> rank(Map<Integer, T> srcData, boolean preface) {
		Map<Integer, T> nullMap = new HashMap<>();				//装载空值的集合
		
		// 校验 null 值，找到后，集中放置到 nullMap 中
		for (Entry<Integer, T> entry : srcData.entrySet()) {
			if(entry.getValue() == null) {
				nullMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		// 根据 nullMap中的 key, 清除掉原数据中，带有 null值的键值对
		for (Integer key : nullMap.keySet()) {
			srcData.remove(key);
		}
		
		//装载已过滤  null 的原数据 (必须在过滤之后装载 !!)
		List<Entry<Integer, T>> srcDataEntryList = new ArrayList<>(srcData.entrySet());
		
		// 根据正反向要求进行排序, 并装载 已排序的 和 null 的数据到 resultMap
		if(preface) {		//正向排序，null值靠前
			resultMap.putAll(nullMap);
			sort(srcDataEntryList, preface);
			for (Entry<Integer, T> entry : srcDataEntryList) {		//装载已排序的数据到结果集合
				resultMap.put(entry.getKey(), entry.getValue());
			}
		} else if(!preface) {	//反向排序，null值靠后
			sort(srcDataEntryList, preface);
			for (Entry<Integer, T> entry : srcDataEntryList) {
				resultMap.put(entry.getKey(), entry.getValue());
			}
			resultMap.putAll(nullMap);
		}
		
		return resultMap;
	}
}
