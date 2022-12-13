package com.smartsnow.smartpdftoprinter.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.smartsnow.smartpdftoprinter.utils.CallbackScheduledReporter.CallbackScheduledFunction;


/**
 * 
 */
public class MetricsUtil {
	//private static Logger logger = LoggerFactory.getLogger(MetricsUtil.class);
	
	private static Map<String,MetricsReporter> reports=new ConcurrentHashMap<>();

	private MetricsUtil() {}
	public static MetricsReporter newReporter() {
		return newReporter("MetricsUtil",MetricsUtil.class);
	}
	public static MetricsReporter newReporter(String reportId) {
		return newReporter(reportId,MetricsUtil.class);
	}
	public static MetricsReporter newReporter(String reportId, Class<?> loggerClazz) {
		return newReporter(reportId,loggerClazz,LoggingLevel.WARN); 
	}
	public static MetricsReporter newReporter(String reportId, Class<?> loggerClazz,LoggingLevel level) {
		MetricsReporter reporterById=reports.get(reportId);
		if(reporterById==null) {
			reporterById=new MetricsReporter(reportId,loggerClazz,level);
			reports.put(reportId,reporterById);
		}
		return reporterById; 
	}
	public static class MetricsReporter{
		/**统一一个registry*/
		private static MetricRegistry registry = new MetricRegistry();
		/** 打印日志的对象 */
		private final Logger yourLogger;
		private Slf4jReporter reporter = null;
		private JmxReporter jmxReporter = JmxReporter.forRegistry(registry).build();
		private CallbackScheduledReporter callbackReporter=null;
		
		@SuppressWarnings("unchecked")
		public <T> Gauge<T> newGauge(String nameId,Supplier<T> supplier) {
			return registry.gauge(nameId,() -> (() -> supplier.get()));
		}
		public Counter newCounter(String nameId) {
			return registry.counter(nameId);
		}
		public Meter newMeter(String nameId) {
			return registry.meter(nameId);
		}
		public Histogram newHistogram(String nameId) {
			return registry.histogram(nameId);
		}
		public Timer newTimer(String nameId) {
			return registry.timer(nameId,() -> new Timer(new SlidingTimeWindowReservoir(60, TimeUnit.SECONDS)));
		}
		public Metric get(String nameId) {
			return registry.getMetrics().get(nameId);
		}
		public boolean remove(String nameId) {
			return registry.remove(nameId);
		}
		public void stopLogReporter() {
			if(reporter!=null) {
				reporter.stop();
				reporter=null;
			}
		}
		public void stopJmxReporter() {
			if(jmxReporter!=null) {
				jmxReporter.stop();
				jmxReporter=null;
			}
		}
		public void stopCallbackScheduledReporter() {
			if(callbackReporter!=null) {
				callbackReporter.stop();
				callbackReporter=null;
			}
		}
		public void start(long period, TimeUnit unit) {
			if(reporter!=null) {
				reporter.start(period, unit);
			}
			if(jmxReporter!=null) {
				jmxReporter.start(); 
			}
		}
		public void start(long period, TimeUnit unit,CallbackScheduledFunction callbackFunction) {
			if(reporter!=null) {
				reporter.start(period, unit);
			}
			if(jmxReporter!=null) {
				jmxReporter.start(); 
			}
			callbackReporter=CallbackScheduledReporter.forRegistry(registry, callbackFunction).build();
			callbackReporter.start(period, unit);
		}
		public void stop() {
			stopLogReporter();
			stopJmxReporter();
			stopCallbackScheduledReporter();
		}
		private MetricsReporter(String reportId, Class<?> loggerClazz,LoggingLevel level) {
			this.yourLogger = LoggerFactory.getLogger(loggerClazz);
			reporter=Slf4jReporter.forRegistry(registry).
					withLoggingLevel(level).outputTo(yourLogger).
					prefixedWith(reportId).build();
		}
		
	}	
}