package com.smartsnow.smartpdftoprinter.event.in;

import java.time.ZonedDateTime;

import com.smartsnow.smartpdftoprinter.bean.EventStatusEnum;
import com.smartsnow.smartpdftoprinter.bean.EventTypeEnum;
import com.smartsnow.smartpdftoprinter.utils.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Data;

/**
 * 环形队列的消息结构,消息对象会复用,所以需要注意重新初始化
 * 
 * */
public class InEventWrapper{
	protected EventStatusEnum eventStatus=EventStatusEnum.EVENT_STATUS_SUCCESS;
	protected ZonedDateTime curZonedDateTime=null;//减少当前时间的转换开销
	protected String currentTimeStr="";//减少当前时间的转换开销
	
	@JSONField(serialize = false) 
	protected Object eventContent=null;
	protected InEvent inEvent=new InEvent();
	protected String cause="";	
	
	@Data
	public static class InEvent{
		private Object content;
		private EventTypeEnum eventType=EventTypeEnum.EVENT_TYPE_LOGIN_TO_JOB;
		
		@JSONField(serialize = false,deserialize = false)
		public void setKey(Object eventContent) {
			switch (eventType) {
			case EVENT_TYPE_LOGIN_TO_JOB:
				content=eventContent;
				break;
			default:
				break;
			}
		}
		@JSONField(serialize = false,deserialize = false)
		public String getKey() {
			switch (eventType) {
			case EVENT_TYPE_LOGIN_TO_JOB:
				return ((InJobInfo)content).getKey();
			default:
				return ((InJobInfo)content).getKey();
			}
		}
	}
	
	//重置消息
	public void reset(Object object) {
		eventStatus=EventStatusEnum.EVENT_STATUS_SUCCESS;
		eventContent=object;
		//积压时时间就不准确了
//		curZonedDateTime=DateUtil.getZonedDateTime();
//		currentTimeStr=DateUtil.toDateTimeStr(curZonedDateTime);
	}
	//第一个消费者的key: tenant+objId+gatewayFlag
	@JSONField(serialize = false) 
	public String getFirstConsumerKey() {
		inEvent.setKey(eventContent);
		return inEvent.getKey();
	}
	//解析消息,仅重置当前类的字段,将输入事件进行转换缓存,避免每次查询时都耗时转换.
	@JSONField(serialize = false) 
	public void parse() {
		curZonedDateTime=DateUtil.getZonedDateTime();
		currentTimeStr=DateUtil.toDateTimeStr(curZonedDateTime);
		if (inEvent.content == null) {
			return;
		}
	}
	
	public EventTypeEnum getEventType() {
		return inEvent.getEventType();
	}
	public void setEventType(EventTypeEnum eventType) {
		inEvent.setEventType(eventType);
	}
	public InEvent getInEvent(){
		return inEvent;
	}
	public String getCause() {
		return cause;
	}
	public String getCurrentTimeStr() {
		return currentTimeStr;
	}
	public ZonedDateTime getCurZonedDateTime() {
		return curZonedDateTime;
	}
	public EventStatusEnum getEventStatus() {
		return eventStatus;
	}
	public void setEventStatus(EventStatusEnum eventStatus) {
		this.eventStatus = eventStatus;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this,SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
	}	
}
