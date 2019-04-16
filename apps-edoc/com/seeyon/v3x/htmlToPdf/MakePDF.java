package com.seeyon.v3x.htmlToPdf;

/**
 * @author YangZheng 328170112@qq.com
 * @desc 使用wkhtmltopdf 把html转为pdf
 * @date 2019-02-21 19:49
 */
public class MakePDF {
    public static String pdfPath = Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1);

    public static void htmlToPdf(String htmlPath, String newPdfPath) {
        String opts = "--outline --outline-depth 2" +//生成目录,深度2
                " --margin-top 5mm --margin-right 1mm --margin-bottom 5mm --margin-left 1mm";//边距
        String command = "\"" + pdfPath + "wkhtmltopdf.exe\" " + opts + " \"" + htmlPath + "\" \"" + newPdfPath + "\"";

        CommandHelper.run(command, null, null);
    }
}