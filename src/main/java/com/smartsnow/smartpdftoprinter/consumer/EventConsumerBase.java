package com.smartsnow.smartpdftoprinter.consumer;

import com.smartsnow.smartpdftoprinter.event.in.InEventWrapper;
import com.smartsnow.smartpdftoprinter.utils.SerialConsumersRouteGroupQueueThreadPool.RouteGroupQueueWorkerWithRandomNextKey;
import com.codahale.metrics.Timer;

public abstract class EventConsumerBase 
					implements RouteGroupQueueWorkerWithRandomNextKey<String,InEventWrapper> {
	protected int routeId=-1;
	protected Timer timer = null;
	protected InEventWrapper event=null;
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	@Override
	public void setRouteId(int routeId) {
		this.routeId=routeId;
	}
	
	//需要通知
	@Override
	public boolean isNeedNotify() {
		return false;
	}

	@Override
	public void doEvent(InEventWrapper event) throws Exception {
		this.event=event;
		Timer.Context ctx=timer.time();
		consumer(event);
		ctx.stop();
	}
	protected abstract void consumer(InEventWrapper event) throws Exception;
	@Override
	public void end() {
		
	}
}
