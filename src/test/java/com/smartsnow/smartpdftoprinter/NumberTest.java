package com.smartsnow.smartpdftoprinter;

import java.io.File;

/**
 * @author Shandy
 *
 */
public class NumberTest {
//	@Test
	public static void numberTest() {
//		BigDecimal bd=BigDecimal.valueOf(Long.parseLong("50"));
//		String json=JSON.toJSONString(bd);
//		System.out.println(json);
//		
//		bd=BigDecimal.valueOf(Double.parseDouble("50.55"));
//		json=JSON.toJSONString(bd);
//		System.out.println(json);
//		
//		bd=BigDecimal.valueOf(Double.parseDouble(""));
//		json=JSON.toJSONString(bd);
//		System.out.println(json);
//		
//		bd=BigDecimal.valueOf(Double.parseDouble(null));
//		json=JSON.toJSONString(bd);
//		System.out.println(json);
		
		File f=new File("./pdfs");
		System.out.println(f.toURI().getPath());
		System.out.println(f.toURI().toASCIIString());
	}
	public static void main(String[] args) {
		numberTest();
	}
}
