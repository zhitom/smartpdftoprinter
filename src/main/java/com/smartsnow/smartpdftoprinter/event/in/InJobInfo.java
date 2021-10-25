package com.smartsnow.smartpdftoprinter.event.in;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.Setter;

@Getter
@Setter
@ToString
@Builder
public class InJobInfo {
	private String  id;
	private Object  job;
	
	
	@JSONField(serialize = false,deserialize = false)
	public String getKey() {
		return ""+id;
	}
}