package com.smartsnow.smartpdftoprinter.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Hash算法大全 推荐使用FNV1算法
 */
public class HashUtil {

	protected HashUtil() {
	}
	/**返回一个正整数的hash值*/
	public  static final <T> int selfHash(T key) {
		if(key==null) {
			return 0;
		}
		return key.hashCode() & 0x7FFFFFFF;
	}

	/**
	 * 加法hash
	 * 
	 * @param key
	 *            字符串
	 * @param prime
	 *            一个质数
	 * @return hash结果
	 */
	public static final int additiveHash(String key, int prime) {
		if(key==null) {
			return 0;
		}
		int hash, i;
		for (hash = key.length(), i = 0; i < key.length(); i++)
			hash += key.charAt(i);
		return (hash % prime);
	}

	/**
	 * 旋转hash
	 * 
	 * @param key
	 *            输入字符串
	 * @param prime
	 *            质数
	 * @return hash值
	 */
	public static final int rotatingHash(String key, int prime) {
		if(key==null) {
			return 0;
		}
		int hash, i;
		for (hash = key.length(), i = 0; i < key.length(); ++i)
			hash = (hash << 4) ^ (hash >> 28) ^ key.charAt(i);
		return (hash % prime);
		// return (hash ^ (hash>>10) ^ (hash>>20));
	}

	// 替代：
	// 使用：hash = (hash ^ (hash>>10) ^ (hash>>20)) & mask;
	// 替代：hash %= prime;

	/**
	 * MASK值，随便找一个值，最好是质数
	 */
	static int M_MASK = 0x8765fed1;

	/**
	 * 一次一个hash
	 * 
	 * @param key
	 *            输入字符串
	 * @return 输出hash值
	 */
	public static final int oneByOneHash(String key) {
		if(key==null) {
			return 0;
		}
		int hash, i;
		for (hash = 0, i = 0; i < key.length(); ++i) {
			hash += key.charAt(i);
			hash += (hash << 10);
			hash ^= (hash >> 6);
		}
		hash += (hash << 3);
		hash ^= (hash >> 11);
		hash += (hash << 15);
		// return (hash & M_MASK);
		return hash;
	}

	/**
	 * Bernstein's hash
	 * 
	 * @param key
	 *            输入字节数组
	 * @return 结果hash
	 */
	public static final int bernstein(String key) {
		if(key==null) {
			return 0;
		}
		int hash = 0;
		int i;
		for (i = 0; i < key.length(); ++i)
			hash = 33 * hash + key.charAt(i);
		return hash;
	}

	//
	// // Pearson's Hash
	// char pearson(char[]key, ub4 len, char tab[256])
	// {
	// char hash;
	// ub4 i;
	// for (hash=len, i=0; i<len; ++i)
	// hash=tab[hash^key[i]];
	// return (hash);
	// }

	// // CRC Hashing，计算crc,具体代码见其他
	// ub4 crc(char *key, ub4 len, ub4 mask, ub4 tab[256])
	// {
	// ub4 hash, i;
	// for (hash=len, i=0; i<len; ++i)
	// hash = (hash >> 8) ^ tab[(hash & 0xff) ^ key[i]];
	// return (hash & mask);
	// }

	/**
	 * Universal Hashing
	 */
	public static final int universal(char[] key, int mask, int[] tab) {
		if(key==null) {
			return 0;
		}
		int hash = key.length, i, len = key.length;
		for (i = 0; i < (len << 3); i += 8) {
			char k = key[i >> 3];
			if ((k & 0x01) == 0)
				hash ^= tab[i + 0];
			if ((k & 0x02) == 0)
				hash ^= tab[i + 1];
			if ((k & 0x04) == 0)
				hash ^= tab[i + 2];
			if ((k & 0x08) == 0)
				hash ^= tab[i + 3];
			if ((k & 0x10) == 0)
				hash ^= tab[i + 4];
			if ((k & 0x20) == 0)
				hash ^= tab[i + 5];
			if ((k & 0x40) == 0)
				hash ^= tab[i + 6];
			if ((k & 0x80) == 0)
				hash ^= tab[i + 7];
		}
		return (hash & mask);
	}

	/**
	 * Zobrist Hashing
	 */
	public static final int zobrist(char[] key, int mask, int[][] tab) {
		if(key==null) {
			return 0;
		}
		int hash, i;
		for (hash = key.length, i = 0; i < key.length; ++i)
			hash ^= tab[i][key[i]];
		return (hash & mask);
	}

	// LOOKUP3
	// 见Bob Jenkins(3).c文件

	// 32位FNV算法
	static int M_SHIFT = 0;

	/**
	 * 32位的FNV算法
	 * 
	 * @param data
	 *            数组
	 * @return int值
	 */
	public static final int FNVHash(byte[] data) {
		if(data==null) {
			return 0;
		}
		int hash = (int) 2166136261L;
		for (byte b : data)
			hash = (hash * 16777619) ^ b;
		if (M_SHIFT == 0)
			return hash;
		return (hash ^ (hash >> M_SHIFT)) & M_MASK;
	}

	/**
	 * 改进的32位FNV算法1
	 * 
	 * @param data
	 *            数组
	 * @return int值
	 */
	public static final int FNVHash1(byte[] data) {
		if(data==null) {
			return 0;
		}
		final int p = 16777619;
		int hash = (int) 2166136261L;
		for (byte b : data)
			hash = (hash ^ b) * p;
		hash += hash << 13;
		hash ^= hash >> 7;
		hash += hash << 3;
		hash ^= hash >> 17;
		hash += hash << 5;
		return hash;
	}

	/**
	 * 改进的32位FNV算法1
	 * 
	 * @param data
	 *            字符串
	 * @return int值
	 */
	public static final int FNVHash1(String data) {
		if(data==null) {
			return 0;
		}
		final int p = 16777619;
		int hash = (int) 2166136261L;
		for (int i = 0; i < data.length(); i++)
			hash = (hash ^ data.charAt(i)) * p;
		hash += hash << 13;
		hash ^= hash >> 7;
		hash += hash << 3;
		hash ^= hash >> 17;
		hash += hash << 5;
		return hash;
	}

	/**
	 * Thomas Wang的算法，整数hash
	 */
	public static final int intHash(int key) {
		key += ~(key << 15);
		key ^= (key >>> 10);
		key += (key << 3);
		key ^= (key >>> 6);
		key += ~(key << 11);
		key ^= (key >>> 16);
		return key;
	}

	/**
	 * RS算法hash
	 */
	public static final int RSHash(String str) {
		if(str==null) {
			return 0;
		}
		int b = 378551;
		int a = 63689;
		int hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = hash * a + str.charAt(i);
			a = a * b;
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of RS Hash Function */

	/**
	 * JS算法
	 */
	public static final int JSHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 1315423911;

		for (int i = 0; i < str.length(); i++) {
			hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of JS Hash Function */

	/**
	 * PJW算法
	 */
	public static final int PJWHash(String str) {
		if(str==null) {
			return 0;
		}
		int BitsInUnsignedInt = 32;
		int ThreeQuarters = (BitsInUnsignedInt * 3) / 4;
		int OneEighth = BitsInUnsignedInt / 8;
		int HighBits = 0xFFFFFFFF << (BitsInUnsignedInt - OneEighth);
		int hash = 0;
		int test = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash << OneEighth) + str.charAt(i);

			if ((test = hash & HighBits) != 0) {
				hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
			}
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of P. J. Weinberger Hash Function */

	/**
	 * ELF算法
	 */
	public static final int ELFHashMod(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 0;
		int x = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash << 4) + str.charAt(i);
			if ((x = (int) (hash & 0xF0000000L)) != 0) {
				hash ^= (x >> 24);
				hash &= ~x;
			}
		}

		return (hash & 0x7FFFFFFF) % 100;
	}

	/**
	 * ELF算法
	 */
	public static final int ELFHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 0;
		int x = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash << 4) + str.charAt(i);
			if ((x = (int) (hash & 0xF0000000L)) != 0) {
				hash ^= (x >> 24);
				hash &= ~x;
			}
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of ELF Hash Function */

	/**
	 * BKDR算法
	 */
	public static final int BKDRHash(String str) {
		if(str==null) {
			return 0;
		}
		int seed = 131; // 31 131 1313 13131 131313 etc..
		int hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of BKDR Hash Function */

	/**
	 * SDBM算法
	 */
	public static final int SDBMHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of SDBM Hash Function */

	/**
	 * DJB算法
	 */
	public static final int DJBHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 5381;

		for (int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) + hash) + str.charAt(i);
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of DJB Hash Function */

	/**
	 * DEK算法
	 */
	public static final int DEKHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = str.length();

		for (int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
		}

		return (hash & 0x7FFFFFFF);
	}

	/* End Of DEK Hash Function */

	/**
	 * AP算法
	 */
	public static final int APHash(String str) {
		if(str==null) {
			return 0;
		}
		int hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash ^= ((i & 1) == 0) ? ((hash << 7) ^ str.charAt(i) ^ (hash >> 3))
					: (~((hash << 11) ^ str.charAt(i) ^ (hash >> 5)));
		}

		// return (hash & 0x7FFFFFFF);
		return hash;
	}

	/* End Of AP Hash Function */

	/**
	 * JAVA自己带的算法
	 */
	public static final int java(String str) {
		if(str==null) {
			return 0;
		}
		int h = 0;
		int off = 0;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			h = 31 * h + str.charAt(off++);
		}
		return h;
	}

	/**
	 * 混合hash算法，输出64位的值
	 */
	public static final long mixHash(String str) {
		if(str==null) {
			return 0;
		}
		long hash = str.hashCode();
		hash <<= 32;
		hash |= FNVHash1(str);
		return hash;
	}

	/**
	 * 计算sha1
	 * 
	 * @param text
	 *            文本
	 * @return 字节数组
	 * @throws Exception
	 */
	public static final byte[] sha1(String text) throws Exception {
		if(text==null) {
			text="";
		}
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		byte[] input = text.getBytes(StandardCharsets.UTF_8);
		md.update(input, 0, input.length);
		sha1hash = md.digest();
		return sha1hash;
	}

	// 4位值对应16进制字符
	static char[] m_byteToHexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 计算sha1
	 * 
	 * @param text
	 *            文本
	 * @return 16进制表示的hash值
	 * @throws Exception
	 */
	public static final String sha1_text(String text) throws Exception {
		byte[] hash = sha1(text);
		StringBuilder ret = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			int d = (b & 0xff);
			ret.append(m_byteToHexChar[(d & 0xf)]);
			d >>= 4;
			ret.append(m_byteToHexChar[(d & 0xf)]);
		}
		return ret.toString();
	}

	/**
	 * MD5方法
	 * 
	 * @param text
	 *            明文
	 * @param key
	 *            密钥
	 * @return 密文
	 * @throws Exception
	 */
	public static final String md5(String text, String key){
		if(text==null) {
			text="";
		}
		if(key==null) {
			key="";
		}
		// 加密后的字符串
		// String encodeStr=DigestUtils.md5Hex(text + key);
		// System.out.println("MD5加密后的字符串为:encodeStr="+encodeStr);
		return DigestUtils.md5Hex(text + key);
	}
	public static final String md5(String text){
		if(text==null) {
			text="";
		}
		return DigestUtils.md5Hex(text);
	}

	/**
	 * MD5验证方法
	 * 
	 * @param text
	 *            明文
	 * @param key
	 *            密钥
	 * @param md5
	 *            密文
	 * @return true/false
	 * @throws Exception
	 */
	public static final boolean verifyMd5(String text, String key, String md5) throws Exception {
		return md5(text, key).equalsIgnoreCase(md5);
	}
}
