package com.seeyon;


import com.seeyon.ctp.util.Base64;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class Test {
    public static void main(String[] args) throws Exception{
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
}
