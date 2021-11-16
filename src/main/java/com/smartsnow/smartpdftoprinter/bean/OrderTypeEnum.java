package com.smartsnow.smartpdftoprinter.bean;

/**
 * 
 * 顺序不能颠倒,和物理库的对应
 * @author Shandy<shanzq@aivanlink.com>
 *
 */
public enum OrderTypeEnum {
	SIZE("size"),
	TIME("time"),
	NAME("name");
	private String s;
	private OrderTypeEnum(String s) {
		this.s=s;
	}
	public static OrderTypeEnum get(int i) {
		for(OrderTypeEnum v:values()) {
			if(v.ordinal()==i) {
				return v;
			}
		}
		return NAME;
	}
	public static OrderTypeEnum get(String s) {
		for(OrderTypeEnum v:values()) {
			if(v.s.equalsIgnoreCase(s)) {
				return v;
			}
		}
		return NAME;
	}
	public String getYourString() {
		return s;
	}
}
