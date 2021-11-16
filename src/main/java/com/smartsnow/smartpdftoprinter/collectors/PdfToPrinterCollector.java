package com.smartsnow.smartpdftoprinter.collectors;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Sides;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.smartsnow.smartpdftoprinter.AppConfig;
import com.smartsnow.smartpdftoprinter.bean.OrderTypeEnum;
import com.smartsnow.smartpdftoprinter.event.in.InJobInfo;
import com.smartsnow.smartpdftoprinter.utils.SleepUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Shandy
 *
 */
@Slf4j
@Component
public class PdfToPrinterCollector implements JobCollector {

	@Autowired
	private AppConfig appConfig;
	private String completedSuffix=null;//结束文件名后缀，此文件一般无需处理
	private PathMatcher includePattern=null;//包含文件的正则表达式
	private PathMatcher ignorePattern=null;//忽略文件的正则表达式
	private boolean recursiveDirectorySearch=false;//是否递归
	private String  inChildOutDirName=null;//输入目录下的输出目录名
	private Map<String,File> printedFiles=new ConcurrentHashMap<>();
	private PrintService printService=null;
	private static boolean existFlag=false;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				existFlag=true;
			}
		}));
	}
	@Override
	public void prepare() {
		setYourPrinter();
//		org.apache.pdfbox.rendering.UsePureJavaCMYKConversion=true
		System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		if(appConfig.getOutDir()!=null&&!appConfig.getOutDir().isEmpty()) {
			if(!appConfig.getOutDir().endsWith("/")) {
				appConfig.setOutDir(appConfig.getOutDir()+File.separator);
			}
			if(appConfig.getOutDir().startsWith(appConfig.getInDir())) {
				//输出目录为子目录，需要避免扫描
				String outDir=appConfig.getOutDir().substring(appConfig.getInDir().length());
				if(outDir.startsWith("/")) {
					outDir=outDir.substring(1);
				}
				inChildOutDirName=outDir.indexOf('/')<0?outDir:outDir.substring(0,outDir.indexOf('/'));
			}
			File outDirObj = new File(appConfig.getOutDir());
			if(!outDirObj.exists()) {
				outDirObj.mkdirs();
			}
		}
		includePattern=FileSystems.getDefault().getPathMatcher(appConfig.getInPattern());
	}

	@Override
	public List<InJobInfo> getJobs() {
		return getCandidateFiles(appConfig.getInDir(),recursiveDirectorySearch)
				.stream()
				.filter(v->!printedFiles.containsKey(v.getAbsolutePath()))
				.map(v->{
					printedFiles.put(v.getAbsolutePath(), v);
					log.info("file={}",v.getName());
					return InJobInfo.builder()
						.id(v.getName())
						.job(v)
						.build();
					})
				.collect(Collectors.toList());
	}
	protected List<File> getCandidateFiles(String d,boolean recursiveFlag) {
		List<File> candidateFiles = new ArrayList<File>();
		try {
			File directory=new File(d);
			Preconditions.checkNotNull(directory);
			
			if (!directory.isDirectory()) {
				return candidateFiles;
			}
			Path trueDir=Paths.get(directory.getAbsolutePath());
			java.nio.file.Files.walkFileTree(trueDir,EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>(){
				@Override
		        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		            throws IOException {
		          if (trueDir.equals(dir)) { // The top directory should always be listed
		            return FileVisitResult.CONTINUE;
		          }
		          String directoryName = dir.getFileName().toString();
		          if (!recursiveFlag ||
		              directoryName.startsWith(".") || 
		              (inChildOutDirName!=null&&dir.toFile().getName().equals(inChildOutDirName))||
		              (includePattern!=null&&!includePattern.matches(dir.getFileName()))||
		              (ignorePattern!=null&&ignorePattern.matches(dir.getFileName()))) {
		            return FileVisitResult.SKIP_SUBTREE;
		          }
		          return FileVisitResult.CONTINUE;
		        }
	
		        @Override
		        public FileVisitResult visitFile(Path candidate, BasicFileAttributes attrs)
		            throws IOException {
		          String fileName = candidate.getFileName().toString();
		          if ((completedSuffix==null||!fileName.endsWith(completedSuffix)) &&
		              !fileName.startsWith(".")&&
		              !fileName.startsWith("#")&&
		              !fileName.startsWith("%")&&
		              !fileName.startsWith("_")&&
		              (
		   	               (includePattern!=null&&includePattern.matches(candidate.getFileName()))||
		   	               (ignorePattern!=null&&!ignorePattern.matches(candidate.getFileName()))||
		   	               (includePattern==null&&ignorePattern==null)
		   	               )
		              ) {
//		        	log.info("file={}",fileName);
		  			candidateFiles.add(candidate.toFile());
		  			
		          }
	
		          return FileVisitResult.CONTINUE;
		        }
	      });
//		for (File file : directory.listFiles(filter)) {
//			if (file.isDirectory()) {
//				candidateFiles.addAll(getCandidateFiles(file));
//			} else {
//				candidateFiles.add(file);
//			}
//		}
		} catch (IOException e) {
		      log.error("I/O exception occurred while listing directories. " +
		                   "Files already matched will be returned. " + d, e);
		    }
		switch(OrderTypeEnum.get(appConfig.getInOrderBy())) {
		case SIZE:
			Collections.sort(candidateFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					long diff = f1.length() - f2.length();
					if (diff > 0)
						return 1;
					else if (diff == 0)
						return 0;
					else
						return -1;
				}
				public boolean equals(Object obj) {
					return true;
				}
			});
			break;
		case TIME:
			Collections.sort(candidateFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					long diff = f1.lastModified() - f2.lastModified();
					if (diff > 0)
					  return 1;
					else if (diff == 0)
					  return 0;
					else
					  return -1;
				}
				public boolean equals(Object obj) {
					return true;
				}
			});
		case NAME:
		default:
			Collections.sort(candidateFiles, new Comparator<File>() {
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && o2.isFile())
				          return -1;
					if (o1.isFile() && o2.isDirectory())
				          return 1;
					return o1.getName().compareTo(o2.getName());
				}
				public boolean equals(Object obj) {
					return true;
				}
			});
			break;
		}
		return candidateFiles;
	}

	@Override
	public void apply(File f) {
		List<File> files;
		if(f.isDirectory()) {
			files=getCandidateFiles(f.getAbsolutePath(),true);
		}else {
			files=Collections.singletonList(f);
		}
		files.forEach(v->{
			if(appConfig.getJobMaxRemainJobs()>0) {
				if(getPrinterQueueRemainSize()>appConfig.getJobMaxRemainJobs()) {
					
					while(!existFlag&&getPrinterQueueRemainSize()>appConfig.getJobMaxRemainJobs()) {
						log.warn("printer queue remain jobs is {}, waiting...",getPrinterQueueRemainSize());
						SleepUtil.Sleep(3000);
					}
				}
			}
			if(appConfig.getJobInterval()>0) {
				SleepUtil.Sleep(appConfig.getJobInterval()*1000);
			}
			toPrinter(v);			
		});
	}
	public void setYourPrinter() {
		if(printService!=null) {
			return;
		}
		for (PrintService ps : PrinterJob.lookupPrintServices()) {
			log.info("printer name: {}",ps.getName());
		}
		if(appConfig.getPrinterName()!=null&&!appConfig.getPrinterName().isEmpty()) {
			// 遍历所有打印机的名称
			for (PrintService ps : PrinterJob.lookupPrintServices()) {
				// 选用指定打印机
			    if (ps.getName().equals(appConfig.getPrinterName())) {
			    	printService=ps;
			        break;
			    }
			}
		}
		if(printService==null) {
			PrinterJob printerJob = PrinterJob.getPrinterJob();
			printService=printerJob.getPrintService();
		}
		log.info("【Selected Printer】 {}",printService.getName());
		for(Attribute attr:printService.getAttributes().toArray()) {
			log.info("printer Attribute【{}】= {}",attr.getName(),attr.toString());
		}
	}
	public int getPrinterQueueRemainSize() {
		for(Attribute attr:printService.getAttributes().toArray()) {
			if(attr.getName().startsWith("queued-job-count")) {
				return Integer.parseInt(attr.toString());
			}
		}
		return 0;
	}
	/**
	 * @param v
	 */
	private void toPrinter(File v) {
		try(PDDocument document = PDDocument.load(v);) {
			
//			DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
//			Doc doc = new SimpleDoc(fis, flavor, null);
			
			//按指定页面拆分文档
			List<SimpleEntry<Integer,Integer>> pageNumList=appConfig.getPageNumList();
			List<PDDocument> pds=new ArrayList<>();
			Splitter splitter = new Splitter();
			for(SimpleEntry<Integer, Integer> entry:pageNumList) {
				splitter.setStartPage(entry.getKey());
		        splitter.setEndPage(entry.getValue());
		        pds.addAll(splitter.split(document));
			}
			if(pageNumList.isEmpty()) {
				pds.add(document);
			}
			if(appConfig.getPrinterScalingValue()!=100) {
				PDPageTree pages = document.getPages();
				pages.forEach(page->{
	//				final PDRectangle mediaBox=vf.getMediaBox();
	//				mediaBox.setUpperRightX((float)(mediaBox.getUpperRightX()*appConfig.getPrinterScalingValue()/100.0));
	//				mediaBox.setUpperRightY((float)(mediaBox.getUpperRightY()*appConfig.getPrinterScalingValue()/100.0));
					try (PDPageContentStream cs = new PDPageContentStream(document, page, AppendMode.PREPEND, true))
			        {
			            cs.saveGraphicsState();
			            cs.transform(Matrix.getScaleInstance((float)(appConfig.getPrinterScalingValue()/100.0),
			            		(float)(appConfig.getPrinterScalingValue()/100.0)));
			            cs.saveGraphicsState();
			        } catch (IOException e) {
						log.error("",e);
					}
			        try (PDPageContentStream cs = new PDPageContentStream(document, page, AppendMode.APPEND, true))
			        {
			            cs.restoreGraphicsState();
			            cs.restoreGraphicsState();
			        } catch (IOException e) {
			        	log.error("",e);
					}
				});
			}
			PrinterJob printerJob = PrinterJob.getPrinterJob();
			printerJob.setJobName(v.getName());
			printerJob.setPrintService(printService);
	//		printerJob.setPageable(new PDFPageable(document));
	        
			/**
			 * 注意: 这边计量单位都是在dpi 72下的尺寸.  如果拿到是mm, 需要转为dpi. 例如n(mm)转换:
						dpi=n * 72 * 10 / 254 (n=10,dpi=28dpi)
			 * */
	//		PaperSize a3 = PaperSize.PAPERSIZE_A3;
			// A3 纸张在72 dpi下的宽高 841 * 1190 （297mm*420mm）
			// A4 Warranty Paper Size 595, 842  （210mm*297mm）
			final int width = (int)(appConfig.getPrinterPageWidth()*72*10/254.0);//unit: 595 dpi; a3.getWidth().toPixI(72);
			final int height = (int)(appConfig.getPrinterPageHeight()*72*10/254.0);//unit: 842 dpi; a3.getHeight().toPixI(72);
			// 10mm边距, 对应 28dpi
			final int marginLeft = (int)(appConfig.getPrinterPageMarginLeft()*72*10/254.0);
			final int marginRight = (int)(appConfig.getPrinterPageMarginRight()*72*10/254.0);
			final int marginTop = (int)(appConfig.getPrinterPageMarginTop()*72*10/254.0);
			final int marginBottom = (int)(appConfig.getPrinterPageMarginBottom()*72*10/254.0);
//			int replicaNum = 1;//副本数
			
			Paper paper = new Paper();
			paper.setSize(width, height);
			// 设置边距
			paper.setImageableArea(marginLeft, marginTop, width - (marginLeft + marginRight), height - (marginTop + marginBottom));
			// 自定义页面设置
			PageFormat pageFormat = new PageFormat();
			// 设置页面横纵向,LANDSCAPE表示横打;PORTRAIT表示竖打;REVERSE_LANDSCAPE表示打印空白
			if(appConfig.getPrinterPageFormat()==0) {
				pageFormat.setOrientation(PageFormat.LANDSCAPE);
			}else if(appConfig.getPrinterPageFormat()==1) {
				pageFormat.setOrientation(PageFormat.PORTRAIT);
			}else if(appConfig.getPrinterPageFormat()==2) {
				pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
			}
			pageFormat.setPaper(paper);
			
			Book book = new Book();
			long total=0;
			for(PDDocument pd:pds) {
				PDFPrintable printable = new PDFPrintable(pd, Scaling.values()[appConfig.getPrinterScaling()]);
				book.append(printable, pageFormat,pd.getNumberOfPages());
				total+=pd.getNumberOfPages();
			}
			printerJob.setPageable(book);
			
			log.info("Start Print(documents total={},pages total={})...",pds.size(),total);
			printerJob.setCopies(appConfig.getPrinterCopies()<=0?1:appConfig.getPrinterCopies());
			
			PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
			if(appConfig.getOutDir()!=null&&!appConfig.getOutDir().isEmpty()) {
				File outFile=new File(appConfig.getOutDir()+File.separator+v.getName());
				if(outFile.exists()) {
					outFile.delete();
				}
				URI uri=URI.create(outFile.toURI().toString());
//				log.info("destionation uri={}",uri.toString());
				attr.add(new Destination(uri));
			}
			
//			attr.add(MediaSizeName.ISO_A4);
			if(appConfig.getPrinterSides()==0) {
				attr.add(Sides.ONE_SIDED);
			}else if(appConfig.getPrinterSides()==1) {
				attr.add(Sides.TWO_SIDED_LONG_EDGE);
			}else if(appConfig.getPrinterSides()==2) {
				attr.add(Sides.TWO_SIDED_SHORT_EDGE);
			}
			for(Attribute attrOne:printerJob.getPrintService().getAttributes().toArray()) {
				log.info("printer Attribute【{}】= {}",attrOne.getName(),attr.toString());
			}
			printerJob.print(attr);
//			printerJob.print();
			//在队列里边无法删除
//			if(!v.renameTo(new File(appConfig.getOutDir()+v.getName()))) {
//				log.error("error rename {} to {}",v.getAbsoluteFile(),appConfig.getOutDir()+v.getName());
//			}
		}catch (PrinterException | IOException e) {
			log.error("",e);
		}
	}
}
