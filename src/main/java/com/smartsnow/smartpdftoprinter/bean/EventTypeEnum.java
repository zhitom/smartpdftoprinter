package com.smartsnow.smartpdftoprinter.bean;

public enum EventTypeEnum {
	EVENT_TYPE_LOGIN_TO_JOB;
	public static EventTypeEnum get(int i) {
		for(EventTypeEnum v:values()) {
			if(v.ordinal()==i) {
				return v;
			}
		}
		return EVENT_TYPE_LOGIN_TO_JOB;
	}
}
