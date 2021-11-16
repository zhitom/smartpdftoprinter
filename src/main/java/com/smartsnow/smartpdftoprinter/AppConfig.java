package com.smartsnow.smartpdftoprinter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.smartsnow.smartpdftoprinter.bean.EventConsumerEnum;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class AppConfig {
    
	@Value("${smartpdftoprinter.in.dir}")
    private String inDir;
	
	@Value("${smartpdftoprinter.in.pattern}")
    private String inPattern;
	
	@Value("${smartpdftoprinter.in.orderby}")
    private String inOrderBy;
	
	@Value("${smartpdftoprinter.out.dir}")
    private String outDir;
	
	@Value("${smartpdftoprinter.job.interval}")
    private int jobInterval;
	
	@Value("${smartpdftoprinter.job.type}")
    private String jobType;
	
	@Value("${smartpdftoprinter.job.maxRemainJobs}")
    private int jobMaxRemainJobs;
	
    @Value("${smartpdftoprinter.batch.size}")
    private int inBatchSize;
	
	@Value("${smartpdftoprinter.consumers.thread.num}")
    private List<Integer> consumerThreadNumList;
    
	@Value("${smartpdftoprinter.onceFlag}")
    private boolean onceFlag;
	
	@Value("${smartpdftoprinter.printer.name}")
    private String printerName;
	
	@Value("${smartpdftoprinter.printer.copies}")
    private int printerCopies;
	
	@Value("${smartpdftoprinter.printer.scaling}")
    private int printerScaling;
	
	@Value("${smartpdftoprinter.printer.scalingValue}")
    private int printerScalingValue;
	
	@Value("${smartpdftoprinter.printer.pageFormat}")
    private int printerPageFormat;
	
	@Value("${smartpdftoprinter.printer.pageNumList}")
    private String printerPageNumListStr;
	
	@Value("${smartpdftoprinter.printer.width}")
    private int printerPageWidth;
	
	@Value("${smartpdftoprinter.printer.height}")
    private int printerPageHeight;
	
	@Value("${smartpdftoprinter.printer.marginTop}")
    private int printerPageMarginTop;
	
	@Value("${smartpdftoprinter.printer.marginBottom}")
    private int printerPageMarginBottom;
	
	@Value("${smartpdftoprinter.printer.marginLeft}")
    private int printerPageMarginLeft;
	
	@Value("${smartpdftoprinter.printer.marginRight}")
    private int printerPageMarginRight;
	
	@Value("${smartpdftoprinter.printer.sides}")
    private int printerSides;
	
	public List<SimpleEntry<Integer,Integer>> getPageNumList(){
		if(printerPageNumListStr==null||printerPageNumListStr.isEmpty()) {
			return Collections.emptyList();
		}
		List<SimpleEntry<Integer,Integer>> rets=new ArrayList<>();
		for(String s:printerPageNumListStr.split("\\,")) {
			if(s.trim().isEmpty()) {
				continue;
			}
			String[] pnums=s.split("\\-");
			if(pnums.length<2) {
				rets.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(pnums[0]), Integer.parseInt(pnums[0])));
			}else {
				rets.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(pnums[0]), Integer.parseInt(pnums[1])));
			}
		}
		return rets;
	}
    public int getInBatchSize() {
    	if(inBatchSize<=0) {
    		return Runtime.getRuntime().availableProcessors();
    	}
		return inBatchSize;
    }
    public int getConsumerThreadNum(EventConsumerEnum consumer) {
		if(consumerThreadNumList==null||consumerThreadNumList.isEmpty())
			return 1;
		else {
			
			if(consumerThreadNumList.size()<=consumer.ordinal()) {
				return 1;
			}
			return (int) consumerThreadNumList.get(consumer.ordinal());
		}
	}
    
}
