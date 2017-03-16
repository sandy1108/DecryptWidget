package org.zywx.wbpalmstar.acedes;

public class DESHtmlDecrypt {

    public static String htmlDecrptInPriavte(byte[] bit, String name, String appKey) {
        String result = "";
        if(bit == null || 0 == bit.length){
            return result;
        }
        String zywx = "3G2WIN Safe Guard";
        int actualLen = bit.length;
        int zyLen = zywx.length();
        if(actualLen <= zyLen){
            return new String(bit);
        }

        int start = actualLen - zyLen;
        String endStr = new String(bit, start, zyLen);
        if(!zywx.equals(endStr)){
            return new String(bit);
        }

        int skip = zyLen + 256;
        if(actualLen <= skip){
            return new String(bit);
        }

        int realLen = actualLen - skip;
        appKey = DESUtility.decodeStr(appKey);
        result = DESUtility.nativeHtmlDecode(bit, name, Integer.toString(realLen), appKey);
        return result;
    }
}
