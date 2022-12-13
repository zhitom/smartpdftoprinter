package com.smartsnow.smartpdftoprinter.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * 日期工具类
 * 
 * @author xue
 *
 */
public class DateUtil {
	private static String ZONE_ID_STRING="Asia/Shanghai";
	public static final ZoneId ZONE_ID=ZoneId.of(ZONE_ID_STRING);
	
	public static final String DD = "dd";
	public static final String MM = "MM";
	public static final String YYYYMM = "yyyyMM";
	public static final String YYYYMMDD = "yyyyMMdd";
	public static final String YYYYMMDDHH = "yyyyMMddHH";
	public static final String YYYYMMDD_HHMMSS = "yyyyMMddHHmmss";
	public static final String YYYYMMDD_HHMMSS_SSS = "yyyyMMddHHmmssSSS";
	public static final String SEG_YYYYMMDD = "yyyy-MM-dd";
	public static final String SEG_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String SEG_YYYYMMDD_HHMMSS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String SEG_YYYYMMDD_HHMMSS_ZONE = "yyyy-MM-ddTHH:mm:ss+HH:MM";
	
	public static final DateTimeFormatter DateTimeFormatter_DD = DateTimeFormatter.ofPattern(DD).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_MM = DateTimeFormatter.ofPattern(MM).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_YYYYMM = DateTimeFormatter.ofPattern(YYYYMM).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_YYYYMMDD = DateTimeFormatter.ofPattern(YYYYMMDD).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_YYYYMMDDHH = DateTimeFormatter.ofPattern(YYYYMMDDHH).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_YYYYMMDD_HHMMSS = DateTimeFormatter.ofPattern(YYYYMMDD_HHMMSS).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_YYYYMMDD_HHMMSS_SSS = DateTimeFormatter.ofPattern(YYYYMMDD_HHMMSS_SSS).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_SEG_YYYYMMDD = DateTimeFormatter.ofPattern(SEG_YYYYMMDD).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_SEG_YYYYMMDD_HHMMSS = DateTimeFormatter.ofPattern(SEG_YYYYMMDD_HHMMSS).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_SEG_YYYYMMDD_HHMMSS_SSS = DateTimeFormatter.ofPattern(SEG_YYYYMMDD_HHMMSS_SSS).withZone(ZONE_ID);
	public static final DateTimeFormatter DateTimeFormatter_SEG_YYYYMMDD_HHMMSS_ZONE = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	static {
		if(System.getProperty("user.timezone")!=null) {
			ZONE_ID_STRING=System.getProperty("user.timezone");
		}
	}
	
	private DateUtil(){}
	/**返回当前时间*/
	public static ZonedDateTime getZonedDateTime() {
		return ZonedDateTime.now(ZONE_ID); 
	}
	/**
	 * 等同getZonedDateTime()
	 * */
	public static ZonedDateTime getCurrentZonedDateTime() {
		return getZonedDateTime(); 
	}
	/**按SEG_YYYYMMDD_HHMMSS返回当前时间*/
	public static String getCurrentZonedDateTimeStr() {
		return toDateTimeStr(getZonedDateTime()); 
	}
	public static ZonedDateTime getZonedDateTime(long utcMilliSecs) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(utcMilliSecs),ZONE_ID); 
	}
	///long,14位时间 -> zonedatetime
	public static ZonedDateTime getZonedDateTime(long time_YYYYMMDDHHMISS,boolean isStuff) {
		return ZonedDateTime.of(
				(int)(time_YYYYMMDDHHMISS/10000000000L), (int)(time_YYYYMMDDHHMISS/100000000L%100), (int)(time_YYYYMMDDHHMISS/1000000L%100), 
				(int)(time_YYYYMMDDHHMISS/10000L%100), (int)(time_YYYYMMDDHHMISS/100L%100), (int)(time_YYYYMMDDHHMISS%100), 
				0, ZONE_ID); 
	}
	/**
	 * @param formattedTimeString SEG_YYYYMMDD_HHMMSS:yyyy-MM-dd HH:mm:ss
	 * */
	public static ZonedDateTime getZonedDateTime(String segFormattedTimeString) {
		if(segFormattedTimeString.length()>SEG_YYYYMMDD_HHMMSS.length()&&
				segFormattedTimeString.charAt(SEG_YYYYMMDD_HHMMSS.length())=='.') {
			//"yyyy-MM-dd HH:mm:ss.SSS"
			return getZonedDateTime(segFormattedTimeString,DateTimeFormatter_SEG_YYYYMMDD_HHMMSS_SSS);
		}
		return getZonedDateTime(segFormattedTimeString, DateTimeFormatter_SEG_YYYYMMDD_HHMMSS); 
	}
	/**
	 * @param formattedTimeString 由dateTimeFormatter决定了其格式
	 * @param dateTimeFormatter 一般是上面预定义的格式对象,也可以是自定义
	 * */
	public static ZonedDateTime getZonedDateTime(String formattedTimeString,DateTimeFormatter dateTimeFormatter) {
		return ZonedDateTime.parse(formattedTimeString, dateTimeFormatter.withZone(ZONE_ID)); 
	}
	public static ZonedDateTime getZonedDateTime(String formattedTimeString,String dateTimeFormatter) {
		return ZonedDateTime.parse(formattedTimeString, DateTimeFormatter.ofPattern(dateTimeFormatter).withZone(ZONE_ID)); 
	}
	/**
	 * @return 返回SEG_YYYYMMDD_HHMMSS:yyyy-MM-dd HH:mm:ss
	 * */
	public static String toDateTimeStr(ZonedDateTime zonedDateTime) {
		return toDateTimeStr(zonedDateTime,DateTimeFormatter_SEG_YYYYMMDD_HHMMSS);
	}
	/**
	 * @param dateTimeFormatter 一般是上面预定义的格式对象,也可以是自定义
	 * @return 返回dateTimeFormatter定义的格式
	 * */
	public static String toDateTimeStr(ZonedDateTime zonedDateTime,DateTimeFormatter dateTimeFormatter) {
		return zonedDateTime.format(dateTimeFormatter.withZone(ZONE_ID));
	}
	public static long getMilliSecs(ZonedDateTime zonedDateTime) {
		return zonedDateTime.toInstant().toEpochMilli();
	}
	public static long diffSeconds(ZonedDateTime end,ZonedDateTime start) {
		return end.toEpochSecond()-start.toEpochSecond();
	}
	public static ZonedDateTime addSeconds(ZonedDateTime zonedDateTime,long secsIncre) {
		return zonedDateTime.plusSeconds(secsIncre);
	}
	public static ZonedDateTime subSeconds(ZonedDateTime zonedDateTime,long secsIncre) {
		return zonedDateTime.minusSeconds(secsIncre);
	}
	public static ZonedDateTime addMonth(ZonedDateTime zonedDateTime,int months) {
		return zonedDateTime.plusMonths(months);
	}
	
	/// 秒差
	public static long diffSeconds(Date endDate, Date beginDate) {
		return (endDate.getTime() - beginDate.getTime()) / 1000;
	}

	/// 毫秒差
	public static long diffMilliSeconds(Date endDate, Date beginDate) {
		return (endDate.getTime() - beginDate.getTime());
	}

	/// 当前绝对毫秒数
	public static long nowAbsMilliSeconds() {
		return System.currentTimeMillis();
	}

	/// 当前绝对秒数
	public static long nowAbsSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	public static String addSeconds(String inYYYYMMDD_HHMMSS, long addsecs) {
		return toDateTimeStr(addSeconds(getZonedDateTime(inYYYYMMDD_HHMMSS, DateTimeFormatter_YYYYMMDD_HHMMSS), addsecs),
				DateTimeFormatter_YYYYMMDD_HHMMSS);
	}
	
	public static Date addMonths(Date time,int months){
		return new Date(addMonth(getZonedDateTime(time.getTime()), months).toInstant().toEpochMilli());
	}
	
	/**
	 * 判断是否为闲时
	 * 
	 * @param time HHMMSS
	 * @return
	 */
	public static String isTimeIdle(String time_HHMMSS) {
		long valTime = Long.valueOf(time_HHMMSS);
		if (valTime >= 230000 && valTime <= 240000 || valTime >= 0 && valTime <= 70000) {
			return "1";
		} else {
			return "0";
		}
	}
	public static final String REG_EXP_DATE = "^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";

    /**
     * 自动解析多种格式的时间字符串为时间对象<br>
     * 支持格式为：yyyy-MM-dd HH:mm:ss 支持多种分隔符，以及多种日期精度。 如yyyy年MM月。 HH时mm分ss秒
     *
     * @param dateString 时间字符串 <br>
     * @return 格式正确则返回对应的java.util.Date对象 格式错误返回null
     */
    public static ZonedDateTime formatUnknownString2Date(String dateString) {
        try {
            if (dateString==null||dateString.isEmpty()) {
                return null;
            }
            dateString = dateString.replace("T", " ");
            String hms = "00:00:00";
            dateString = dateString.trim();
            if (dateString.contains(" ")) {
                // 截取时分秒
                hms = dateString.substring(dateString.indexOf(" ") + 1, dateString.length());
                // 重置日期
                dateString = dateString.substring(0, dateString.indexOf(" "));
                // 多中分隔符的支持
                hms = hms.replace("：", ":");
                hms = hms.replace("时", ":");
                hms = hms.replace("分", ":");
                hms = hms.replace("秒", ":");
                hms = hms.replace("-", ":");
                hms = hms.replace("－", ":");
                // 时间不同精确度的支持
                if (hms.endsWith(":")) {
                    hms = hms.substring(0, hms.length() - 1);
                }
                if (hms.split(":").length == 1) {
                    hms += ":00:00";
                }
                if (hms.split(":").length == 2) {
                    hms += ":00";
                }
            }
            String[] hmsarr = hms.split(":");
            // 不同日期分隔符的支持
            dateString = dateString.replace(".", "-");
            dateString = dateString.replace("/", "-");
            dateString = dateString.replace("－", "-");
            dateString = dateString.replace("年", "-");
            dateString = dateString.replace("月", "-");
            dateString = dateString.replace("日", "");
            // 切割年月日
            String yearStr, monthStr, dateStr;
            // 截取日期
            String[] ymd = dateString.split("-");
            // 判断日期精确度
            yearStr = ymd[0];
            monthStr = ymd.length > 1 ? ymd[1] : "";
            dateStr = ymd.length > 2 ? ymd[2] : "";
            monthStr = monthStr == "" ? Integer.toString(1) : monthStr;
            dateStr = dateStr == "" ? Integer.toString(1) : dateStr;
            String dtr = (yearStr + "-" + monthStr + "-" + dateStr + " " + hms);
            if (!dtr.matches(REG_EXP_DATE))
                return null;
            // 返回日期
            return ZonedDateTime.of(Integer.parseInt(yearStr.trim()), 
            		Integer.parseInt(monthStr.trim()), 
            		Integer.parseInt(dateStr.trim()), 
            		Integer.parseInt(hmsarr[0].trim()), 
            		Integer.parseInt(hmsarr[1].trim()), 
            		Integer.parseInt(hmsarr[2].trim()), 
            		0, 
            		ZONE_ID);
        } catch (Exception e) {
            return null;
        }
    }
}
