package com.nng.unit.sort.util;

import java.util.ArrayList;
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
	private List<Entry<Integer, T>> srcDataEntryList;				//原数据的Entry集合
	
	//protected 防止接口 被使用者违规使用，仅允许继承的具体实现工具类使用
	protected abstract void sort(List<Entry<Integer, T>> srcDataEntryList, boolean preface);
	
	//排序工具类 统一调用的接口
	@Override
	public Map<Integer, T> rank(Map<Integer, T> srcData, boolean preface) {
		srcDataEntryList = new ArrayList<>(srcData.entrySet());		//装载原数据
		sort(srcDataEntryList, preface);		//排序
		
		for (Entry<Integer, T> entry : srcDataEntryList) {		//装载已排序的数据到结果集合
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}
}
