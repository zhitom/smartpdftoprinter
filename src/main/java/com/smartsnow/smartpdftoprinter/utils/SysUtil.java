package com.smartsnow.smartpdftoprinter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

/**
 * Simple to Introduction
 *
 * @ProjectName: [${project_name}]
 * @Package: [${package_name}.${file_name}]
 * @ClassName: [${type_name}]
 * @Description: [系统常用函数类]
 * @Author: [${user}]
 * @CreateDate: [${date} ${time}]
 * @Version: [v1.0]
 */
public class SysUtil {
	private final static Logger logger = LoggerFactory.getLogger(SysUtil.class);
	private final static ClassLoaderUtil classLoaderUtil=new ClassLoaderUtil();
	private static InetAddress ia;
	private static ThreadLocal<Random> rand=new ThreadLocal<>();

	private static String processName = ManagementFactory.getRuntimeMXBean().getName();

	static {
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			logger.error("UnknownHostException:", e);
			ia = null;
		}
	}

	/**
	 * @return 本机主机名
	 */
	public static final String getHostName() {
		String hostName = "localhost";
		if (ia == null) {
			logger.error("Error getHostName()");
		} else {
			hostName = ia.getHostName();
		}
		return hostName;
	}

	/**
	 * @return 本机IP
	 */
	public static final String getHostAddress() {
		String ip = "127.0.0.1";
		if (ia == null) {
			logger.error("Error getHostAddress(!)");

		} else {
			ip = ia.getHostAddress();
		}
		return ip;
	}

	/**
	 * 因为主机ip比较多，所以采用主机名的方式 进程实例标识：hostname+'+'+procId
	 */
	public static final String getProcessInstanceId() {
		return getHostName() + "+" + processName.substring(0, processName.indexOf('@'));
	}

	public static final long getProcessId() {
		return Long.parseLong(processName.substring(0, processName.indexOf('@')));
	}

	/**
	 * 因为主机ip比较多，所以采用主机名的方式 线程实例标识：hostname+'+'+procId+'+'+threadId
	 */
	public static final String getThreadInstanceId() {
		return getProcessInstanceId() + "+" + String.valueOf(getThreadId());
	}
	public static final long getThreadId() {
		return Thread.currentThread().getId();
	}

	/**
	 * 功能：加密字符串（先简单base64加密，后期修改）。
	 *
	 * @param str
	 *            要加密的字符串
	 * @return 加密后的字符串
	 * @author 许陆荣
	 * @date 2017年06月30日
	 */
	public static final String encode(String str) {
		return new String(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 功能：解密字符串（先简单base64加密，后期修改）。
	 *
	 * @param str
	 *            要解密的字符串
	 * @return 解密后的字符串
	 * @author 许陆荣
	 * @date 2017年06月30日
	 */
	public static final String decode(String str) {
		return new String(Base64.getDecoder().decode(str));
	}

	/**
	 * 功能：将属性中的配置设置到对象字段中。
	 *
	 * @param obj
	 *            要设置的目标对象
	 * @param props
	 *            来源属性
	 * @param decodeKeys
	 *            要解密的字段
	 * @author 许陆荣
	 * @date 2017年06月30日
	 */
	public static final void setObjProperties(Object obj, Properties props, String... decodeKeys) {
		if (obj != null && props != null && props.size() > 0) {
			// 对加密的属性值解密
			if (decodeKeys != null) {
				for (int i = 0; i < decodeKeys.length; i++) {
					if (props.containsKey(decodeKeys[i])) {
						props.setProperty(decodeKeys[i], SysUtil.decode(props.getProperty(decodeKeys[i])));
					}
				}
			}

			// 设置各项属性值
			for (Entry<Object, Object> prop : props.entrySet()) {
				try {
					Field f = obj.getClass().getDeclaredField(prop.getKey().toString());

					if (f != null) {
						f.setAccessible(true);
						String type = f.getType().toString();
						String val = prop.getValue().toString();
						// System.out.println(f.getName()+" type is "+f.getGenericType());
						if (type.endsWith("String")) {
							f.set(obj, val); // 给属性设值
						} else if (type.endsWith("boolean")) {
							f.set(obj, Boolean.parseBoolean(val));
						} else if (type.endsWith("int") || type.endsWith("Integer")) {
							f.set(obj, Integer.parseInt(val));
						} else if (type.endsWith("long") || type.endsWith("Long")) {
							f.set(obj, Long.parseLong(val));
						}
					}
				} catch (Exception e) {
					// e.printStackTrace();
					logger.debug("Object getDeclaredField NoSuchFieldException:" + prop.getKey());
				}
			}
		}
	}

	public static final String getStringValue(Object obj, String defaultValue) {
		if (obj == null) {
			return defaultValue;
		} else {
			return obj.toString();
		}
	}

	public static final int getIntValue(String str, int defaultValue) {
		if (str==null||str.isEmpty()) {
			return defaultValue;
		} else {
			return Integer.parseInt(str);
		}
	}

	public static final Boolean getBoolValue(String str, Boolean defaultValue) {
		if (str==null||str.isEmpty()) {
			return defaultValue;
		} else {
			return Boolean.parseBoolean(str);
		}
	}

	// /fileName不带路径,先从$IOT_HOME/conf下面查找文件,再到resource下面查找文件,路径需配置到CLASSPATH环境变量里边
	public static final URL getResource(String fileName) {
		URL url;
		// String homeEnv=System.getenv(Const.SYSTEM_HOME);
		// if(CheckNull.isNull(homeEnv)) {
		// url=SysUtil.class.getClassLoader().getResource(fileName);
		// logger.info("url=",url.getRef());
		// return url;
		// }
		// url=SysUtil.class.getClassLoader().getResource(homeEnv+File.separator+Const.SYSTEM_CONF+File.separator+fileName);
		// logger.info("url=",url.getRef());
		// return
		// SysUtil.class.getClassLoader().getResource(homeEnv+File.separator+Const.SYSTEM_CONF+File.separator+fileName);
		url = classLoaderUtil.getResourceAsURL(fileName,SysUtil.class.getClassLoader());
		if(url==null) {
			logger.warn("url=null");
		}else {
			logger.warn("url={}", url.getFile());
		}
		return url;
	}

	public static final InputStream getResourceAsStream(String fileName) {
		URL url;
		// String homeEnv=System.getenv(Const.SYSTEM_HOME);
		// if(CheckNull.isNull(homeEnv)) {
		// url=SysUtil.class.getClassLoader().getResource(fileName);
		// logger.info("url=",url.getRef());
		// return SysUtil.class.getClassLoader().getResourceAsStream(fileName);
		// }
		// url=SysUtil.class.getClassLoader().getResource(homeEnv+File.separator+Const.SYSTEM_CONF+File.separator+fileName);
		// logger.info("url=",url.getRef());
		// return
		// SysUtil.class.getClassLoader().getResourceAsStream(homeEnv+File.separator+Const.SYSTEM_CONF+File.separator+fileName);
		url = classLoaderUtil.getResourceAsURL(fileName,SysUtil.class.getClassLoader());
		if(url==null) {
			logger.warn("url=null");
		}else {
			logger.warn("url={}", url.getFile());
		}
		return classLoaderUtil.getResourceAsStream(fileName,SysUtil.class.getClassLoader());
	}

	/**
	 * 根据起始资源或队列编号和应用并发总数,再去除已经分配使用的资源或队列列表,产生当前应用需要读写的资源或队列编号
	 *
	 * @param appNum
	 *            应用总并发个数
	 * @param usedNumList
	 *            已经分配使用的资源列表
	 */
	public static final List<Integer> getBalanceNumList(List<Integer> resNumList, int appNum, Set<Integer> usedNumList) {
		List<Integer> resultNumList = new ArrayList<Integer>();
		int avg = (resNumList.size() + appNum) / appNum;// 为保证均衡,直接进位
		if (avg == 0)
			avg = 1;
		for (Integer i : resNumList) {
			if (usedNumList.contains(i)) {
				continue;
			}
			resultNumList.add(i);
			if (resultNumList.size() == avg) {// 达到均值了
				return resultNumList;
			}
		}
		return resultNumList;
	}

	public static final List<Integer> getBalanceNumList(int resBegin, int resEnd, int appNum, Set<Integer> usedNumList) {
		List<Integer> numList = new ArrayList<Integer>();
		for (int i = 0; i <= resEnd; i++) {
			numList.add(i);
		}
		return getBalanceNumList(numList, appNum, usedNumList);
	}

	public static final List<String> getBalanceStringList(List<String> resStringList, int appNum,
			Set<String> usedStringList) {
		List<String> resultStringList = new ArrayList<String>();
		int avg = (resStringList.size() + appNum) / appNum;// 为保证均衡,直接进位
		if (avg == 0)
			avg = 1;
		for (String res : resStringList) {
			if (usedStringList.contains(res)) {
				continue;
			}
			resultStringList.add(res);
			if (resultStringList.size() == avg) {// 达到均值了
				return resultStringList;
			}
		}
		return resultStringList;
	}
	public static final String getStringByEnvVarNameWithYours(String path,Map<String, String> yourKvMap) {
		return getStringByEnvVarName(path,yourKvMap);
	}
	public static final String getStringByEnvVarName(String path) {
		Map<String, String> envMap = System.getenv();
		return getStringByEnvVarName(path,envMap);
	}
	private static final String getStringByEnvVarName(String path,Map<String, String> envMap) {
		if(path==null) {
			return null;
		}
		//Map<String, String> envMap = System.getenv();
		StringBuilder newPath=new StringBuilder();
		StringBuilder varName=new StringBuilder();
		char[] pathChars=path.toCharArray();
		char c;
		String vStr;
		for(int i=0;i<pathChars.length;i++) {
			c=pathChars[i];
			if(c!='$') {
				newPath.append(c);
				continue;
			}
			//处理$
			i++;
			if(i>=pathChars.length) {
				newPath.append(c);
				break;
			}
			//处理{
			c=pathChars[i];
			i++;
			if(i>=pathChars.length) {
				newPath.append('$');
				newPath.append(c);
				break;
			}
			if(c=='{') {//含{}
				varName.setLength(0);
				for(;i<pathChars.length;i++) {
					if(pathChars[i]=='}') {
						break;
					}
					varName.append(pathChars[i]);
				}
				if(i>=pathChars.length) {//不存在},原样输出
					newPath.append(c);
					newPath.append(varName.toString());
					continue;
				}
				if(varName.length()==0) {//是个${},原样输出
					newPath.append(c);
					newPath.append(pathChars[i]);
					continue;
				}
				//System.out.println("varName1="+varName);
				vStr=envMap.get(varName.toString());
				if(vStr!=null) {
					newPath.append(vStr);
				}else {
					newPath.append("${"+varName+"}");
				}    
				continue;
			}
			//处理不带{}的变量
			if(!(Character.isLetterOrDigit(c)||c=='_')){//是不是变量名结束
				newPath.append('$');
				newPath.append(c);
				i--;
				continue;
			}
			varName.setLength(0);
			varName.append(c);
			for(;i<pathChars.length;i++) {
				if(!(Character.isLetterOrDigit(pathChars[i])||pathChars[i]=='_')){//是不是变量名结束
					i--;
					break;
				}
				varName.append(pathChars[i]);
			}
			//System.out.println("varName2="+varName);
			vStr=envMap.get(varName.toString());
			if(vStr!=null) {
				newPath.append(vStr);
			}else {
				newPath.append("$"+varName+"");
			}    
		}
		String newPathStr=newPath.toString();
		if(!newPathStr.equals(path)) {
			logger.info("path={},newPath={}",path,newPathStr);
		}
		return newPathStr;
	}
	//【min，max】闭区间
    public static class MinMax{
        private int min= Integer.MAX_VALUE;
        private int max=-1;
        public int getMin() {
            return min;
        }
        public void setMin(int min) {
            this.min = min;
        }
        public int getMax() {
            return max;
        }
        public void setMax(int max) {
            this.max = max;
        }
        public int getTotal() {
            return max-min+1;
        }
    }
    
    //返回【min，max】闭区间的数字
    public static final int getRandomValue(int min,int max) {
        if(rand.get()==null) {
            rand.set(new Random());
        }
        //return rand.get().nextInt(Integer.MAX_VALUE)%(max-min+1) + min;
        //return (int) (System.nanoTime()%(max-min+1) + min);
        return (int) ((System.currentTimeMillis()+System.nanoTime())%(max-min+1) + min);
    }
    
    //alreadyGetValue 为已经分配的结果集，返回的结果会返回未分配的结果
    public static final int getRandomValue(int min,int max,Set<Integer> alreadyGetValue) {
        if(max-min+1<=alreadyGetValue.size()) {
            return 0;
        }
        int r;
        while(true) {
            r = getRandomValue(min,max);
            if(alreadyGetValue.contains(r))
                continue;
            else
                return r;
        }
    }
    
    //alreadyGetValue 为已经分配的结果集，mapAll为所有记录集，返回的结果会返回未分配且记录集中存在的结果
    public static final int getRandomValue(int min,int max,Set<Integer> alreadyGetValue,Map<Integer,?> mapAll) {
        if(max-min+1<=alreadyGetValue.size()) {
            return 0;
        }
        int r;
        while(true) {
            r = getRandomValue(min,max);
            if(alreadyGetValue.contains(r))
                continue;
            else {
                if(mapAll.containsKey(r))
                    return r;
                continue;
            }                
        }
    }
}
