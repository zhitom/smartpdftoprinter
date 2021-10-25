package com.smartsnow.smartpdftoprinter.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Shandy
 *
 */
@UtilityClass
@Slf4j
public class SleepUtil {
	public static void Sleep(long l) {
		if(l==0) {
			return;
		}
		try {
			Thread.sleep(l);
		} catch (InterruptedException e) {
			log.error("",e);
			Thread.currentThread().interrupt();
		}
	}
}
