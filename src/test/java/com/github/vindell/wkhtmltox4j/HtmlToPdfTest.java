package com.github.vindell.wkhtmltox4j;

/**
 * 测试
 */
public class HtmlToPdfTest {

    public static void main(String[] args) {
        String htmlPath = "www.baidu.com";
        String pdfPath = "/root/pdfFile/testpdf.pdf";
        HtmlToPdf.convert(htmlPath, pdfPath );
    }
}