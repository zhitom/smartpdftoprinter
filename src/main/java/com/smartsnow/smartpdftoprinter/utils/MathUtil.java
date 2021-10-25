package com.smartsnow.smartpdftoprinter.utils;

import com.aivanlink.common.base.algorithm.HashUtil;

public class MathUtil {
	private MathUtil() {}
	public static int mod(String key,int total) {
		return HashUtil.selfHash(key)%total;
	}
}
