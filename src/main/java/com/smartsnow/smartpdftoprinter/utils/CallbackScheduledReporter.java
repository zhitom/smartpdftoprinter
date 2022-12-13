package com.smartsnow.smartpdftoprinter.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

/**
 * {@link com.codahale.metrics.ConsoleReporter}
 * */
public class CallbackScheduledReporter extends ScheduledReporter {
	private CallbackScheduledFunction callbackFunction;
	
	@FunctionalInterface
	public static interface CallbackScheduledFunction{
		void apply(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers);
	}
	public static CallbackScheduledReporter.Builder forRegistry(MetricRegistry registry,CallbackScheduledFunction callbackFunction) {
		return new Builder(registry,callbackFunction);
	}
	public static CallbackScheduledReporter.Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	public static class Builder {
		private final MetricRegistry registry;
		private TimeZone timeZone;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;
		private ScheduledExecutorService executor;
		private boolean shutdownExecutorOnStop;
		private Set<MetricAttribute> disabledMetricAttributes;
		private CallbackScheduledFunction callbackFunction;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.timeZone = TimeZone.getDefault();
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
			this.executor = null;
			this.shutdownExecutorOnStop = true;
			this.disabledMetricAttributes = Collections.emptySet();
			this.callbackFunction=null;
		}
		private Builder(MetricRegistry registry,CallbackScheduledFunction callbackFunction) {
			this.registry = registry;
			this.timeZone = TimeZone.getDefault();
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
			this.executor = null;
			this.shutdownExecutorOnStop = true;
			this.disabledMetricAttributes = Collections.emptySet();
			this.callbackFunction=callbackFunction;
		}

		public CallbackScheduledReporter.Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
			this.shutdownExecutorOnStop = shutdownExecutorOnStop;
			return this;
		}

		public CallbackScheduledReporter.Builder scheduleOn(ScheduledExecutorService executor) {
			this.executor = executor;
			return this;
		}

		public CallbackScheduledReporter.Builder formattedFor(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public CallbackScheduledReporter.Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		public CallbackScheduledReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		public CallbackScheduledReporter.Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		public CallbackScheduledReporter.Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
			this.disabledMetricAttributes = disabledMetricAttributes;
			return this;
		}
		public CallbackScheduledReporter.Builder callbackFunction(CallbackScheduledFunction callbackFunction) {
			this.callbackFunction = callbackFunction;
			return this;
		}
		public CallbackScheduledReporter build() {
			return new CallbackScheduledReporter(registry, timeZone, rateUnit, durationUnit, filter, executor,
					shutdownExecutorOnStop, disabledMetricAttributes,callbackFunction);
		}
	}

	private CallbackScheduledReporter(MetricRegistry registry, TimeZone timeZone, TimeUnit rateUnit, TimeUnit durationUnit,
			MetricFilter filter, ScheduledExecutorService executor, boolean shutdownExecutorOnStop,
			Set<MetricAttribute> disabledMetricAttributes,CallbackScheduledFunction callbackFunction) {
		super(registry, "callback-scheduled-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
				disabledMetricAttributes);
		Objects.requireNonNull(callbackFunction, "need CallbackFunction");
		this.callbackFunction=callbackFunction;
	}

	@Override
	public void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		
		callbackFunction.apply(gauges, counters, histograms, meters, timers);
	}
}