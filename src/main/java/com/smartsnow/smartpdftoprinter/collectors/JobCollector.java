package com.smartsnow.smartpdftoprinter.collectors;

import java.io.File;
import java.util.List;

import com.smartsnow.smartpdftoprinter.event.in.InJobInfo;

public interface JobCollector {
	void prepare();
	List<InJobInfo> getJobs();
	void apply(File f);
}
