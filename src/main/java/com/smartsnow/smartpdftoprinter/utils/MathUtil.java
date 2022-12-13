package com.smartsnow.smartpdftoprinter.utils;

public class MathUtil {
	private MathUtil() {}
	public static int mod(String key,int total) {
		return HashUtil.selfHash(key)%total;
	}
}
