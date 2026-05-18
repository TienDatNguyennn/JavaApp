/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

/**
 *
 * @author Tien Dat
 */

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

public class QRUtils {

    /**
     * Hàm chuyển đổi một chuỗi văn bản thành hình ảnh mã QR (BufferedImage).
     * * @param content Nội dung cần mã hóa (VD: ID lớp học, Token)
     * @param width   Chiều rộng của ảnh QR
     * @param height  Chiều cao của ảnh QR
     * @return BufferedImage Hình ảnh QR Code để gắn lên UI
     * @throws WriterException Nếu có lỗi trong quá trình mã hóa
     */
    public static BufferedImage generateQRImage(String content, int width, int height) throws WriterException {
        // Khởi tạo đối tượng ghi mã QR của ZXing
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        // Mã hóa nội dung thành ma trận điểm ảnh (BitMatrix)
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        
        // Chuyển ma trận điểm ảnh thành đối tượng BufferedImage của Java
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}