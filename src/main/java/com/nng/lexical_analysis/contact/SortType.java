package com.nng.lexical_analysis.contact;

/**
 * 排序类型
 * @author Vijay
 * 以下编写顺序不能混乱，否则不符合带有变量值的枚举类的写法
 */
public enum SortType {
	ASC("asc"), DESC("desc");		//声明枚举类型
	
	private SortType(String sortType) {		//私有化构造器
		this.sortType = sortType;
	}
	
	public String getValue() {
		return sortType;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	private String sortType;			//声明私有变量
	
}
