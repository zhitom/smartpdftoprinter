package com.smartsnow.smartpdftoprinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.yaml.snakeyaml.Yaml;

import com.aivanlink.common.base.sys.OptionUtil;
import com.smartsnow.smartpdftoprinter.collectors.PdfToPrinterCollector;

/**
 * @author Shandy
 *
 */
public class SmartPdfToPrinterSetupApplication {
	public static void main(String[] args) {
		int ret=SmartPdfToPrinterSetupApplication.setup(args);
		if(ret<0) {
			
		}
	}

	public static int setup(String[] args) {
		OptionUtil optionUtil=new OptionUtil();
		optionUtil.addOptWithNoArg("p", "printer", "设置打印机", false);
		optionUtil.addOptWithOneArg("c", "config-file", "yml文件路径", false,"yml-file-path");
		
		if(args.length==0) {
			optionUtil.help();
			System.exit(100);
		}
		optionUtil.parseOpt(SmartPdfToPrinterSetupApplication.class.getSimpleName(), args);
		if(optionUtil.getCommandLine().hasOption("p")) {
			if(optionUtil.getValue("c")==null) {
				System.err.println("缺少指定配置文件");
				return -1;
			}
			String configuredPrinterName=SmartPdfToPrinterSetupApplication.getConfiguredPrinterName(optionUtil.getValue("c"));
			SmartPdfToPrinterSetupApplication.setupToFile(optionUtil.getValue("c"),configuredPrinterName);
		}
		return 0;
	}
	/**
	 * @param value
	 * @return 
	 * @throws FileNotFoundException 
	 */
	private static String getConfiguredPrinterName(String value) {
		Yaml  yaml=new Yaml();
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> confs=(Map<String, Object>) yaml.load(new FileInputStream(value));
			if(confs==null) {
				System.err.println("文件内容错误，无法解析！");
				return null;
			}
			@SuppressWarnings("unchecked")
			Map<String,Object> selfs=(Map<String, Object>) confs.get("smartpdftoprinter");
			if(selfs==null) {
				System.err.println("文件内容错误，无法解析！");
				return null;
			}
			@SuppressWarnings("unchecked")
			Map<String,Object> printer=(Map<String, Object>) selfs.get("printer");
			if(printer==null||printer.get("name")==null) {
				System.err.println("文件内容错误，无法解析！");
				return null;
			}
			String configuredName = printer.get("name").toString();
			if(configuredName.isEmpty()) {
				System.err.println("请先在"+value+"配置好一个默认的打印机！");
				return null;
			}
			return configuredName;
		} catch (FileNotFoundException e) {
			System.err.println("文件找不到:"+value);
			return null;
		}
	}
	private static String getFileFullContent(String f) {
		try(Reader fr=new InputStreamReader(new FileInputStream(f),StandardCharsets.UTF_8);) {
			int len=0;
			char[] buff=new char[100];
	        StringBuilder stringBuilder = new StringBuilder();
	        while ((len=fr.read(buff,0,100))>=0) {
	        	if(len==0) {
	        		continue;
	        	}
	            stringBuilder.append(buff,0,len);
	        }
	        return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void setupToFile(String file,String configuredPrinterName) {
		PdfToPrinterCollector.printPrinterInfos(configuredPrinterName);
		String readIndex=null;
		try(Scanner scanner = new Scanner(System.in);){
			while(readIndex==null) {
				System.out.print("请输入新的打印机编号(退出输入q)：");
				try {
					readIndex=scanner.nextLine().trim();
					if(readIndex.equalsIgnoreCase("q")) {
						break;
					}
					if(readIndex==null||readIndex.isEmpty()) {
						scanner.reset();
						readIndex=null;
						continue;
					}
					String printerName=PdfToPrinterCollector.getPrinterName(Integer.parseInt(readIndex));
					if(printerName==null||printerName.isEmpty()) {
						System.err.println("选择错误，请重新输入！");
						scanner.reset();
						readIndex=null;
						continue;
					}
					replaceContent(file,configuredPrinterName,printerName);
				}catch (NoSuchElementException e) {
					scanner.reset();
					readIndex=null;
					System.err.println("内容错误，请重新输入！");
				}catch (Exception e) {
					System.err.println("错误，请重新输入！"+e.getLocalizedMessage());
					scanner.reset();
					readIndex=null;
				}
			}
		}
	}
	private static void replaceContent(String file,String configuredPrinterName,String newPrinterName) {
		String newContent=getFileFullContent(file).replace(configuredPrinterName, newPrinterName);
		try(FileOutputStream w=new FileOutputStream(file)){
	    	w.write(newContent.getBytes(StandardCharsets.UTF_8));
	    	w.flush();
	    	System.out.println("写入OK，设置成功！");
	    } catch (IOException e) {
	    	System.err.println("写入失败，请核查:"+file);
		}
	}
}
