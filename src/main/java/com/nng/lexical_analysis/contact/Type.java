package com.nng.lexical_analysis.contact;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Vijay
 *
 */
enum Type {
	INT("int", Integer.class), 
	INTEGER("integer", Integer.class), 
	VARCHAR("string", String.class), 
	CHARACTER("char", Character.class), 
	DOUBLE("double", Double.class), 
	FLOAT("float", Float.class);
	
	private Type(String type, Class<? extends Object> clazz) {
		this.type = type;
		this.clazz = clazz;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	public Map<String, Class<? extends Object>> getValueMap() {
		Map<String, Class<? extends Object>> valueMap = new HashMap<>();
		for (Type typeItem : Type.values()) {
			valueMap.put(typeItem.type, typeItem.clazz);
		}
		
		return valueMap;
	}
	
	public String getTypeValue() {
		return type;
	}
	
	public Class<? extends Object> getClazzValue() {
		return clazz;
	}
	
	private String type;
	private Class<? extends Object> clazz;
}
