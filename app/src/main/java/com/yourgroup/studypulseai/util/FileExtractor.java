package com.yourgroup.studypulseai.util;

import android.content.Context;
import android.net.Uri;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileExtractor {

    public static String extract(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) return "Could not determine file type.";

        try {
            switch (mimeType) {
                case "application/pdf":
                    return extractPdf(context, uri);
                case "text/plain":
                    return extractTxt(context, uri);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    return extractDocx(context, uri);
                default:
                    return "Unsupported file type: " + mimeType;
            }
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private static String extractPdf(Context context, Uri uri) throws Exception {
        PDDocument doc = PDDocument.load(context.getContentResolver().openInputStream(uri));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    private static String extractTxt(Context context, Uri uri) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        if (is == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        return sb.toString();
    }

    private static String extractDocx(Context context, Uri uri) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        if (is == null) return "";
        XWPFDocument doc = new XWPFDocument(is);
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        extractor.close();
        return text;
    }
}