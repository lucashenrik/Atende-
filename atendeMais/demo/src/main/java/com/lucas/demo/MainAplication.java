package com.lucas.demo;

import java.io.FileOutputStream;
import java.util.concurrent.Phaser;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class MainAplication {
	public static void main(String[] args) {
		System.out.println("Iniciando geração de PDF com iText...");

		String destino = "C:\\Users\\Lucas\\Documents\\TestePdf\\documento.pdf";

		Font fontTitulo = new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Font.ITALIC));
		Font fontSubTitulo = new Font(FontFactory.getFont(FontFactory.HELVETICA, 18, Font.ITALIC));
		Font fontTexto = new Font(FontFactory.getFont(FontFactory.HELVETICA, 13));
		Font fontTextoBold = new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
		Font fontTextoVermelha = new Font(FontFactory.getFont(FontFactory.HELVETICA, 13, BaseColor.RED));

		// Criar um documento
		Document document = new Document();

		try {
			// Criar um escritor para o arquivo PDF
			PdfWriter.getInstance(document, new FileOutputStream(destino));

			// Abrir o documento para edição
			document.open();

			// Adicionar um parágrafo simples
			Paragraph titulo = new Paragraph("Relatorio do dia 13/01", fontTitulo);
			titulo.setSpacingBefore(100);
			titulo.setAlignment(Element.ALIGN_CENTER);
			document.add(titulo);

			document.add(Chunk.NEWLINE);

			document.add(new Paragraph("    Pedidos Entregues", fontSubTitulo));
			document.add(new Paragraph("       120 pedidos", fontTextoVermelha));

			document.add(Chunk.NEWLINE);
			Paragraph maisVendido = new Paragraph("    Mais Vendido", fontSubTitulo);
			// maisVendido.setAlignment(Element.ALIGN_RIGHT);
			document.add(maisVendido);

			Paragraph itemMaisVendido = new Paragraph("       Batata: 34", fontTextoVermelha);
			// itemMaisVendido.setAlignment(Element.ALIGN_RIGHT);
			document.add(itemMaisVendido);

			document.add(Chunk.NEWLINE);
			document.add(new Paragraph("    Vendas", fontSubTitulo));

			PdfPTable table = new PdfPTable(2);
			table.setSpacingBefore(16f); // Adiciona espaçamento antes da tabela
			table.setSpacingAfter(20f); // Adiciona espaçamento após a tabela

			// Ajustar o padding de cada célula
			for (int i = 0; i < 2; i++) {
				table.getDefaultCell().setPadding(8f); // Aumenta o padding de todas as células da tabela
			}

			table.setWidthPercentage(90);

			PdfPCell headerCell1 = new PdfPCell(new Phrase("Produto", fontTextoBold));
			headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			headerCell1.setPadding(8f);
			table.addCell(headerCell1);

			PdfPCell headerCell2 = new PdfPCell(new Phrase("Quantidade", fontTextoBold));
			headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headerCell2.setPadding(8f);
			table.addCell(headerCell2);

			table.addCell("Jantinha");
			table.addCell("33");

			table.addCell("Batata");
			table.addCell("azul");

			document.add(table);

			PdfPTable table2 = new PdfPTable(3);
			table2.setSpacingBefore(16f);
			table2.setSpacingAfter(20f);

			// Ajustar o padding de cada célula
			for (int i = 0; i < 3; i++) {
				table.getDefaultCell().setPadding(8f); // Aumenta o padding de todas as células da tabela
			}

			table2.setWidthPercentage(90);

			PdfPCell segHeaderCell1 = new PdfPCell(new Phrase("Entregues", fontTextoBold));
			segHeaderCell1.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			segHeaderCell1.setPadding(8f);
			table2.addCell(segHeaderCell1);

			PdfPCell segHeaderCell2 = new PdfPCell(new Phrase("Cancelados", fontTextoBold));
			segHeaderCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
			segHeaderCell2.setPadding(8f);
			table2.addCell(segHeaderCell2);

			PdfPCell segHeaderCell3 = new PdfPCell(new Phrase("Não Entregues", fontTextoBold));
			segHeaderCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
			segHeaderCell3.setPadding(8f);
			table2.addCell(segHeaderCell3);

			// Adicionando dados às células
			PdfPCell dataCell1 = new PdfPCell(new Phrase("13"));
			dataCell1.setPadding(8f); // Padding
			table2.addCell(dataCell1);

			PdfPCell dataCell2 = new PdfPCell(new Phrase("2"));
			dataCell2.setPadding(8f); // Padding
			table2.addCell(dataCell2);

			PdfPCell dataCell3 = new PdfPCell(new Phrase("13"));
			dataCell3.setPadding(8f); // Padding
			table2.addCell(dataCell3);

			document.add(table2);

			PdfPTable table3 = new PdfPTable(8);
			table3.setSpacingBefore(16f);
			table3.setSpacingAfter(20f);

			// Ajustar o padding de cada célula
			for (int i = 0; i < 3; i++) {
				table3.getDefaultCell().setPadding(8f); // Aumenta o padding de todas as células da tabela
			}

			table3.setWidthPercentage(100);
			
			PdfPCell terceiraHeaderCell1 = new PdfPCell(new Phrase("    ", fontTextoBold));
			terceiraHeaderCell1.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell1.setPadding(18f);
			terceiraHeaderCell1.setPaddingRight(18f);
			table3.addCell(terceiraHeaderCell1);
			
			PdfPCell terceiraHeaderCell = new PdfPCell(new Phrase("Segunda", fontTextoBold));
			terceiraHeaderCell.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell.setPadding(8f);
			table3.addCell(terceiraHeaderCell);
			
			PdfPCell terceiraHeaderCell2 = new PdfPCell(new Phrase("Terça", fontTextoBold));
			terceiraHeaderCell2.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell2.setPadding(8f);
			table3.addCell(terceiraHeaderCell2);
			
			PdfPCell terceiraHeaderCell4 = new PdfPCell(new Phrase("Quarta", fontTextoBold));
			terceiraHeaderCell4.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell4.setPadding(8f);
			table3.addCell(terceiraHeaderCell4);
			
			PdfPCell terceiraHeaderCell5 = new PdfPCell(new Phrase("Quinta", fontTextoBold));
			terceiraHeaderCell5.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell5.setPadding(8f);
			table3.addCell(terceiraHeaderCell5);
			
			PdfPCell terceiraHeaderCell6 = new PdfPCell(new Phrase("Sexta", fontTextoBold));
			terceiraHeaderCell6.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell6.setPadding(8f);
			table3.addCell(terceiraHeaderCell6);
			
			PdfPCell terceiraHeaderCell7 = new PdfPCell(new Phrase("Sábado", fontTextoBold));
			terceiraHeaderCell7.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell7.setPadding(8f);
			table3.addCell(terceiraHeaderCell7);
			
			PdfPCell terceiraHeaderCell8 = new PdfPCell(new Phrase("Domingo", fontTextoBold));
			terceiraHeaderCell8.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			terceiraHeaderCell8.setPadding(8f);
			table3.addCell(terceiraHeaderCell8);
			
			PdfPCell entregues = new PdfPCell(new Phrase("Entregues", fontTextoBold));
			entregues.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			table3.addCell(entregues);
			table3.addCell("12");
			table3.addCell("32");
			table3.addCell("45");
			table3.addCell("44");
			table3.addCell("42");
			table3.addCell("143");
			table3.addCell("46");
			
			PdfPCell maisVendidos = new PdfPCell(new Phrase("Mais Vendidos", fontTextoBold));
			maisVendidos.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			table3.addCell(maisVendidos);
			table3.addCell("Batata: 24");
			table3.addCell("Jantinha: 33");
			table3.addCell("Jantinha: 34");
			table3.addCell("Jantinha: 64");
			table3.addCell("Batata: 52");
			table3.addCell("Jantinha: 91");
			table3.addCell("Jantinha: 36");
			
			PdfPCell cancelados = new PdfPCell(new Phrase("Cancelados", fontTextoBold));
			cancelados.setBackgroundColor(BaseColor.LIGHT_GRAY); // Cor de fundo
			table3.addCell(cancelados);
			table3.addCell("2");
			table3.addCell("3");
			table3.addCell("4");
			table3.addCell("4");
			table3.addCell("2");
			table3.addCell("1");
			table3.addCell("6");

			document.add(table3);

			document.add(new Paragraph("        *Os pedidos cancelados não são incluidos na quantidade total*",
					fontTextoVermelha));

			// Fechar o documento
			document.close();

			System.out.println("PDF gerado com sucesso em: " + destino);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
