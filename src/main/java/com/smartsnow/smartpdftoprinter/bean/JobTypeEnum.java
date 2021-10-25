package com.smartsnow.smartpdftoprinter.bean;

/**
 * 
 * 顺序不能颠倒,和物理库的对应
 * @author Shandy<shanzq@aivanlink.com>
 *
 */
public enum JobTypeEnum {
	JOB_TYPE_PDF("pdf"),//0
	JOB_TYPE_UNKNOWN("unknown");
	private String s;
	private JobTypeEnum(String s) {
		this.s=s;
	}
	public static JobTypeEnum get(int i) {
		for(JobTypeEnum v:values()) {
			if(v.ordinal()==i) {
				return v;
			}
		}
		return JOB_TYPE_UNKNOWN;
	}
	public static JobTypeEnum get(String s) {
		for(JobTypeEnum v:values()) {
			if(v.s.equalsIgnoreCase(s)) {
				return v;
			}
		}
		return JOB_TYPE_UNKNOWN;
	}
	public String getYourString() {
		return s;
	}
}
