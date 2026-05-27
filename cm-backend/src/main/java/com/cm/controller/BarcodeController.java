package com.cm.controller;

import com.cm.common.exception.BusinessException;
import com.cm.common.result.Result;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/barcode")
public class BarcodeController {

    @PostMapping("/recognize")
    public Result<Map<String, String>> recognize(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "请上传图片文件");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException(400, "无法读取图片文件");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.CODABAR,
                    BarcodeFormat.ITF
            ));

            com.google.zxing.Result zxingResult = new MultiFormatReader().decode(bitmap, hints);
            String barcode = zxingResult.getText();
            String format = zxingResult.getBarcodeFormat().name();

            log.info("条码识别成功：{}，格式：{}", barcode, format);

            Map<String, String> data = new HashMap<>();
            data.put("barcode", barcode);
            data.put("format", format);
            return Result.ok(data);

        } catch (NotFoundException e) {
            log.warn("图片中未识别到条码");
            throw new BusinessException(400, "未能识别到条码，请确保图片清晰且包含完整的条码");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("条码识别异常", e);
            throw new BusinessException(500, "条码识别失败");
        }
    }
}