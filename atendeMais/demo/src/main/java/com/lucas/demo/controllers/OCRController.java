package com.lucas.demo.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.demo.model.ImageData;

import net.sourceforge.tess4j.Tesseract;

@RestController
@RequestMapping("/api")
public class OCRController {

	@PostMapping("/process-image")
	public String processImage(@RequestBody ImageData imageData) {

		String caminhoArq = "usr\share\tesseract-ocr\4.00\tessdata";
		// String caminhoArq = "/usr/share/tesseract-ocr/4.00/tessdata";
		try {
			// Decodifica a imagem Base64
			byte[] imageBytes = Base64.getDecoder().decode(imageData.getImage().split(",")[1]);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

			// Configura o Tesseract
			Tesseract tesseract = new Tesseract();
			tesseract.setDatapath(caminhoArq); // Defina o caminho correto para tessdata

			// Processa a imagem e extrai o texto
			return tesseract.doOCR(img);
		} catch (Exception e) {
			e.printStackTrace();
			return "Erro ao processar imagem";
		}
	}

	@PostMapping("/ocr")
	public String processImage() {
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath("  usr\share\tesseract-ocr\4.00\tessdata\";");
		// tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Ajuste
		// conforme necessário

		try {
			// Caminho da imagem
			File imageFile = new File("home\\atendeMais\\imagem.png");
			// File imageFile = new File("/home/atendeMais/imagem.png");
			// Processa a imagem e retorna o texto
			String result = tesseract.doOCR(imageFile);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "Erro ao processar a imagem: " + e.getMessage();
		}
	}
}