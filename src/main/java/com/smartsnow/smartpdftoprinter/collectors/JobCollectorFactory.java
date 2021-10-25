package com.smartsnow.smartpdftoprinter.collectors;

import org.springframework.stereotype.Component;

import com.smartsnow.smartpdftoprinter.bean.JobTypeEnum;
import com.smartsnow.smartpdftoprinter.utils.SpringContextUtils;


@Component
public class JobCollectorFactory {
	private JobCollectorFactory() {}
	
	public static JobCollector getEventHandler(JobTypeEnum jobTypeEnum) {
		switch (jobTypeEnum) {
		case JOB_TYPE_PDF:
			return SpringContextUtils.getBean(PdfToPrinterCollector.class);
		case JOB_TYPE_UNKNOWN:
		default:
			return null;
		}
	}
}
