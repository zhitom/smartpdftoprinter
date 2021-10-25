package com.smartsnow.smartpdftoprinter.bean;

/**枚举值决定了消费顺序*/
public enum EventConsumerEnum {
	EVENT_CONSUMER_JOB,
	EVENT_CONSUMER_MAX;
	
	public static int size() {
		return EVENT_CONSUMER_MAX.ordinal();
	}
}
