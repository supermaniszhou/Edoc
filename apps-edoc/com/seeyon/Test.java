package com.seeyon;


import com.seeyon.ctp.util.Base64;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;

public class Test {
    public static void main(String[] args) throws Exception{

//        String reverPath="/F:/Seeyon/A8/ApacheJetspeed/webapps/seeyon/pdf/-717066148976769316/";
//
//        System.out.println( reverPath.substring(reverPath.indexOf("/seeyon") ));
//            String s = "woaini";
//            byte[] bytes = s.getBytes();

//            System.out.println("将woaini转为不同进制的字符串：");
//            System.out.println("可以转换的进制范围：" + Character.MIN_RADIX + "-" + Character.MAX_RADIX);
//            System.out.println("2进制："   + binary(bytes, 2));
//            System.out.println("5进制："   + binary(bytes, 5));
//            System.out.println("8进制："   + binary(bytes, 8));
//            System.out.println("16进制："  + binary(bytes, 16));
//            System.out.println("32进制："  + binary(bytes, 32));
//            System.out.println("64进制："  + binary(bytes, 64));// 这个已经超出范围，超出范围后变为10进制显示

//            System.exit(0);

       // F:\Seeyon\A8\ApacheJetspeed\webapps\seeyon\pdf\
        String s="F:\\Seeyon\\A8\\ApacheJetspeed\\webapps\\seeyon\\pdf/";
        System.out.println(s.substring(s.indexOf("pdf")+3));

    }


    public static String binary(byte[] bytes, int radix){
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    public static void test3(){
//        String sBodyPath="/oa/Seeyon/A8/base/upload/2019/04/18/655740050121572878.doc";
        String sBodyPath="";
        String t=sBodyPath.substring(sBodyPath.indexOf("upload")+6).replaceAll("\\\\","/");
        System.out.println(t);
    }

    public static void test2() throws Exception{
        String s = "F:\\Seeyon\\A8\\base\\upload\\2019\\04\\15\\-5954133390304655204\\-5954133390304655204.pdf";
        File file=new File(s);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int len = 0;
        StringBuffer buffer = new StringBuffer();
        while ((len = bis.read(buf)) != -1) {
            buffer.append(new String(buf, 0, len));
        }
        System.out.println(Base64.encodeString(buffer.toString()));
    }
    public static void test(){
        String s="F:/Seeyon/A8/base/upload/2019/04/18/-9025378646825548776/";
        System.out.println(s.substring(s.indexOf("upload")+6).replaceAll("\\\\","/"));
        /**   2019/04/18/-9025378646825548776/     */

        String t="2019/04/18/-9025378646825548776/ ";
        System.out.println(t.replaceAll("\\\\","/"));
    }
}
