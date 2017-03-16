/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.acedes;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DESUtility {
	public final static String contentSuffix = "3G2WIN Safe Guard";

	public static boolean isEncrypted(InputStream inStream) {
		boolean isV = false;
		if (inStream == null) {
			return isV;
		}
		try {
			String text = getStringFromInputStream(inStream);
			String lastStr = text.substring(text.length() - 17, text.length());
			if (lastStr.equals(contentSuffix)) {
				isV = true;
			}
		} catch (Exception e) {
		}
		return isV;
	}

	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static String decodeStr(String key) {
		char map[] = { 'd', 'b', 'e', 'a', 'f', 'c' };
		char nmap[] = { '2', '4', '0', '9', '7', '1', '5', '8', '3', '6' };
		String dest = "";
		String swapstr = "";
		String output = "";
		for (int j = 0; j < key.length(); j++) {
			if (key.charAt(j) == '-')
				continue;
			swapstr = swapstr + key.charAt(j);
		}
		for (int j = 0; j < swapstr.length(); j++) {
			if (j == 8 || j == 12 || j == 16 || j == 20)
				dest = dest + "-";
			dest = dest + swapstr.charAt(swapstr.length() - j - 1);
		}
		for (int i = 0; i < dest.length(); i++) {
			char t = dest.charAt(i);
			if (t >= 'a' && t <= 'f') {
				t = map[t - 'a'];
			} else if (t >= '0' && t <= '9') {
				t = nmap[t - '0'];
			}
			output = output + t;
		}
		return output;
	}

	public static byte[] transStreamToBytes(InputStream is, int buffSize) {
		if (is == null) {
			return null;
		}
		if (buffSize <= 0) {
			throw new IllegalArgumentException(
					"buffSize can not less than zero.....");
		}
		byte[] data = null;
		byte[] buffer = new byte[buffSize];
		int actualSize = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ((actualSize = is.read(buffer)) != -1) {
				baos.write(buffer, 0, actualSize);
			}
			data = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static String htmlDecode(byte[] bit, String name, String appKey){
		return DESHtmlDecrypt.htmlDecrptInPriavte(bit, name, appKey);
	}

	static {
		System.loadLibrary("appcan");
	}

	public native static String nativeHtmlDecode(byte[] bit, String name, String lenStr, String akey);

	public static String getFileNameWithNoSuffix(String path) {
		String name = null;
		int index = path.lastIndexOf('/');
		if (index > 0) {
			name = path.substring(index + 1, path.length());
		}
		int index1 = name.lastIndexOf('.');
		if (index1 > 0) {
			name = name.substring(0, index1);
		}
		return name;
	}
}
