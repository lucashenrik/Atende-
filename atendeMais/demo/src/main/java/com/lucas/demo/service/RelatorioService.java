package com.lucas.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.exceptions.ErroRelatorioException;
import com.lucas.demo.model.CaminhoInfo;
import com.lucas.demo.model.PedidosContext;

@Service
public class RelatorioService {

	private static final Logger logger = LoggerFactory.getLogger(ApagarPedidos.class);
	
	@Autowired
	PedidoServico pedidoServico;

	@Autowired
	ApagarPedidos apagarPedidos;
	
	Font fontTitulo = new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Font.ITALIC));
	Font fontSubTitulo = new Font(FontFactory.getFont(FontFactory.HELVETICA, 18, Font.ITALIC));
	Font fontTexto = new Font(FontFactory.getFont(FontFactory.HELVETICA, 13));
	Font fontTextoBold = new Font(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
	Font fontTextoVermelha = new Font(FontFactory.getFont(FontFactory.HELVETICA, 13, BaseColor.RED));

	public List<LocalDate> obterDiasDaSemana(LocalDate data) {
		List<LocalDate> diasDaSemana = new ArrayList<>();
		LocalDate inicioSemana = data.with(DayOfWeek.MONDAY);
		if (inicioSemana.isBefore(data.with(data.withDayOfMonth(1)))) {
			inicioSemana = data.withDayOfMonth(1);
		}
		for (LocalDate dia = inicioSemana; !dia.isAfter(data); dia = dia.plusDays(1)) {
			diasDaSemana.add(dia);
		}
		return diasDaSemana;
	}

	public List<LocalDate> obterDiasDaSemanaNull(LocalDate hoje) {
		List<LocalDate> diasDaSemana = new ArrayList<>();
		LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);
		LocalDate fimSemana = hoje.with(DayOfWeek.SUNDAY);

		for (LocalDate data = inicioSemana; !data.isAfter(fimSemana); data = data.plusDays(1)) {
			if (data.getMonthValue() == hoje.getMonthValue()) {
				diasDaSemana.add(data);
			} else {
				diasDaSemana.add(null); // Adiciona nulo para representar dias fora do mês
			}
		}
		return diasDaSemana;
	}

	// Ja nao tem utilidade
	public void validarPdfDiariosExist(File relatorioDiario, PedidosContext pedidoContext, String destino,
			LocalDate data) {
		if (relatorioDiario.exists()) {
			logger.info("Relatorio diario ja existe.");
			this.gerarPDFDiario(pedidoContext, destino, data);
		} else {
			this.gerarPDFDiario(pedidoContext, destino, data);
		}
	}

	public void criarTodosRelatoriosDiarios(LocalDate data, String idCliente) {
		List<LocalDate> diasSemanas = obterDiasDaSemana(data);
		for (int i = 0; i < diasSemanas.size(); i++) {
			LocalDate dia = diasSemanas.get(i);
			if (i == diasSemanas.size() - 1) {
				continue;
			}
			gerarPdf(dia, idCliente);
		}
	}
	
	public void verificarPossivelPdfSemanalECriar(LocalDate data, CaminhoInfo caminhoInfo, String destino,
			String idCliente) {
		if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
			
			@SuppressWarnings("deprecation")
			String nomeMes = data.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")).toLowerCase();
			WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);

			int semanaDoMes = data.get(weekFields.weekOfMonth()); // Obtém a semana do mês (1, 2, 3, 4, etc.)

			String caminhoArq = caminhoInfo.getCaminhoArquivo();
			String destinoSemanal = caminhoArq + nomeMes + "_semana_" + semanaDoMes + ".pdf";

			this.gerarPDFSemanal(destino, destinoSemanal, data, idCliente, semanaDoMes, nomeMes);
			
			this.criarTodosRelatoriosDiarios(data, idCliente);
			
			apagarPedidos.verificarEApagarPedidosSemana(obterDiasDaSemanaNull(data), MudancaSO.separatorParaPedidos(idCliente));
		}
	}

	public File gerarPdf(LocalDate data, String idCliente) {
		String destino = caminhoRelatorio(data, idCliente);
		File relatorioDiario = new File(destino);

		CaminhoInfo caminhoInfo = MudancaSO.separatorParaRelatorio(idCliente);

		this.validarDiretorio(caminhoInfo.getDiretorio());
		PedidosContext pedidoContext = this.lerPedidosArquivo(data, idCliente);

		this.validarPdfDiariosExist(relatorioDiario, pedidoContext, destino, data);

		this.verificarPossivelPdfSemanalECriar(data, caminhoInfo, destino, idCliente);
		
		return relatorioDiario;
	}

	public void gerarPDFDiario(PedidosContext pedidoContext, String destino, LocalDate data) {
		Document document = new Document();
		try {
			List<String> entregues = this.pedidoServico.contar(pedidoContext.getPedidosEntregues(), "entregue");
			List<String> cancelados = this.pedidoServico.contar(pedidoContext.getPedidosCancelados(), "cancelar");
			List<String> naoEntregeus = this.pedidoServico.contar(pedidoContext.getPedidosAll(), "pronto");
			
			PdfWriter.getInstance(document, new FileOutputStream(destino));
			document.open();

			// Usando DateTimeFormatter para formatar o LocalDate
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String dataTitulo = data.format(formatter);

			// Titulo
			this.titulo(document, dataTitulo);
			this.paragrafroPedidosEntregues(document, getQuantidadePedidos(entregues));

			// Paragrafo - Pedido Mais Vendido
			String maisVendido = this.itemMaisVendido(entregues);
			this.paragrafroMaisVendido(document, maisVendido);

			// 1 Tabela - Produtos e Quantidades
			this.primeiraTabela(document, entregues);

			// 2 Tabela - Produtos e Status
			List<String> listaStatus = new ArrayList<>();
			listaStatus.addAll(entregues);
			listaStatus.addAll(cancelados);
			listaStatus.addAll(naoEntregeus);
			this.segundaTabela(document, listaStatus);

			document.add(new Paragraph("        *Os pedidos cancelados não são incluidos na quantidade total*",
					fontTextoVermelha));

			logger.info("PDF gerado com sucesso em: " + destino);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar relatorio diário.");
		} finally {
			document.close();
		}
	}

	public void gerarPDFSemanal(String destino, String destinoSemanal, LocalDate data, String idCliente,
			int semanaDoMes, String mes) {
		Document document = new Document();
		try {
			List<LocalDate> diasDaSemana = this.obterDiasDaSemanaNull(data);

			List<PedidosContext> relatoriosDiarios = this.lerOsRelatoriosDiarios(diasDaSemana, idCliente);

			this.consolidarRelatoriosDiarios(document, relatoriosDiarios, destinoSemanal, semanaDoMes, mes);
			
			
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar relatorio semanal.");
		} finally {
			document.close();
		}
	}

	public List<PedidosContext> lerOsRelatoriosDiarios(List<LocalDate> diasDaSemana, String idCliente) {
		List<PedidosContext> relatoriosDiarios = new ArrayList<>();

		for (LocalDate dia : diasDaSemana) {
			if (dia != null) {
				try {
					PedidosContext pedidosContext = this.lerPedidosArquivo(dia, idCliente);
					relatoriosDiarios.add(pedidosContext);
				} catch (ErroArquivoException e) {
					relatoriosDiarios.add(new PedidosContext());
					throw new ErroRelatorioException("Arquivo de pedidos para o dia " + dia + " não encontrado: ");
				}
			} else {
				relatoriosDiarios.add(null);
			}
		}
		return relatoriosDiarios;
	}

	public void consolidarRelatoriosDiarios(Document document, List<PedidosContext> relatoriosDiarios,
			String destinoSemanal, int semanaDoMes, String mes) {

		try {
			PdfWriter.getInstance(document, new FileOutputStream(destinoSemanal));
			document.open();

			String dataTitulo = semanaDoMes + " Semana de " + mes;
			this.titulo(document, dataTitulo);

			List<String> allEntregues = new ArrayList<>();
			List<String> allCancelados = new ArrayList<>();
			List<String> allNaoEntregues = new ArrayList<>();

			for (PedidosContext pedidosContext : relatoriosDiarios) {
				if (pedidosContext == null || pedidosContext.isEmpty()) {
					// Cria uma entrada vazia para evitar que a tabela quebre
					allEntregues.add(" ");
					allCancelados.add(" ");
					allNaoEntregues.add(" ");
					continue;
				}

				List<String> entregues = this.pedidoServico.contar(pedidosContext.getPedidosEntregues(), "entregue");
				List<String> cancelados = this.pedidoServico.contar(pedidosContext.getPedidosCancelados(), "cancelar");
				List<String> naoEntregues = this.pedidoServico.contar(pedidosContext.getPedidosAll(), "pronto");

				allEntregues.addAll(entregues);
				allCancelados.addAll(cancelados);
				allNaoEntregues.addAll(naoEntregues);
			}

			List<String> listaConsolidadaEntregues = this.getListaConsolidadaEntregues(allEntregues);

			// Paragrafo - Pedidos Entregues
			this.paragrafroPedidosEntregues(document, getQuantidadePedidos(allEntregues));

			// Paragrafo - Mais Vendido
			String maisVendido = this.itemMaisVendido(listaConsolidadaEntregues);
			this.paragrafroMaisVendido(document, maisVendido);

			// 1 Tabela - Todos os pedidos e suas quantidades
			this.primeiraTabela(document, listaConsolidadaEntregues);

			// 2 Tabela - Pedidos por Status
			List<String> listaStatus = new ArrayList<>();
			listaStatus.add(getQuantidadePedidos(allEntregues));
			listaStatus.add(getQuantidadePedidos(allCancelados));
			listaStatus.add(getQuantidadePedidos(allNaoEntregues));
			this.segundaTabela(document, listaStatus);

			// 3 Tabela - Pedidos Diarios
			criarTerceiraTabelaSemanal(document, relatoriosDiarios);

			document.add(new Paragraph("        *Os pedidos cancelados não são incluidos na quantidade total*",
					fontTextoVermelha));

			document.close();

			logger.info("PDF gerado com sucesso em: " + destinoSemanal);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao consolidar relatorios diarios.");
		}
	}

	public void criarTerceiraTabelaSemanal(Document document, List<PedidosContext> relatoriosDiarios) {
		try {
			PdfPTable table3 = new PdfPTable(8);
			table3.setSpacingBefore(16f);
			table3.setSpacingAfter(20f);
			table3.setWidthPercentage(100);

			String[] diasSemana = { "", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo" };
			for (String dia : diasSemana) {
				PdfPCell cell = new PdfPCell(new Phrase(dia, fontTextoBold));
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setPadding(8f);
				table3.addCell(cell);
			}

			addLinhaTerceiraTabela(table3, "Entregues", relatoriosDiarios, pedidos -> {
				if (pedidos.getPedidosEntregues() != null) {
					List<String> pedidosContado = pedidoServico.contar(pedidos.getPedidosEntregues(), "entregue");
					return getQuantidadePedidos(pedidosContado);
				} else {
					return " ";
				}
			});

			addLinhaTerceiraTabela(table3, "Mais Vendidos", relatoriosDiarios, pedidos -> {
				if (pedidos.getPedidosAll() != null) {
					List<String> pedidosContado = pedidoServico.contar(pedidos.getPedidosAll(), "entregue");
					return itemMaisVendidoAux(pedidosContado);
				} else {
					return " ";
				}
			});

			addLinhaTerceiraTabela(table3, "Cancelados", relatoriosDiarios, pedidos -> {
				if (pedidos.getPedidosCancelados() != null) {
					List<String> pedidosContado = pedidoServico.contar(pedidos.getPedidosCancelados(), "cancelar");
					return getQuantidadePedidos(pedidosContado);
				} else {
					return " ";
				}
			});

			document.add(table3);
		} catch (DocumentException e) {
			throw new ErroRelatorioException("Erro ao criar a terceira tabela semanal.");
		}
	}

	private void addLinhaTerceiraTabela(PdfPTable table, String titulo, List<PedidosContext> pedidosSemanais,
			Function<PedidosContext, String> funcaoDados) {
		table.addCell(new PdfPCell(new Phrase(titulo, fontTextoBold)));

		for (PedidosContext pedidos : pedidosSemanais) {
			if (pedidos == null) {
				table.addCell(" ");
			} else {
				String dado = funcaoDados.apply(pedidos);
				table.addCell(dado != null ? dado : " ");
			}
		}
	}

	public void titulo(Document document, String data) {
		try {
			Paragraph titulo = new Paragraph("Relatorio " + data, fontTitulo);
			titulo.setSpacingBefore(100);
			titulo.setAlignment(Element.ALIGN_CENTER);
			document.add(titulo);
			document.add(Chunk.NEWLINE);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar titulo.");
		}
	}

	public void paragrafroPedidosEntregues(Document document, String quantidade) {
		try {
			document.add(new Paragraph("     Pedidos Entregues", fontSubTitulo));
			document.add(new Paragraph("       " + quantidade + " pedidos", fontTextoVermelha));

			document.add(Chunk.NEWLINE);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar pedidos entregues.");
		}
	}

	public void paragrafroMaisVendido(Document document, String maisVendido) {
		try {
			Paragraph textMaisVendido = new Paragraph("    Item Mais Vendido", fontSubTitulo);
			document.add(textMaisVendido);
			Paragraph itemMaisVendido = new Paragraph("       " + maisVendido, fontTextoVermelha);
			document.add(itemMaisVendido);

			document.add(Chunk.NEWLINE);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar paragrafo mais vendidos.");
		}
	}

	public void primeiraTabela(Document document, List<String> lista) {
		try {
			document.add(new Paragraph("    Todas as vendas", fontSubTitulo));

			PdfPTable table = new PdfPTable(2);
			table.setSpacingBefore(16f);
			table.setSpacingAfter(20f);
			table.setWidthPercentage(90);

			for (int i = 0; i < 2; i++) {
				table.getDefaultCell().setPadding(8f);
			}

			PdfPCell headerCell1 = new PdfPCell(new Phrase("Produto", fontTextoBold));
			headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headerCell1.setPadding(8f);
			table.addCell(headerCell1);

			PdfPCell headerCell2 = new PdfPCell(new Phrase("Quantidade", fontTextoBold));
			headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headerCell2.setPadding(8f);
			table.addCell(headerCell2);

			for (String item : lista) {
				String[] partes = item.split(":");

				if (partes.length == 2) {
					String produto = partes[0].trim();
					String quantidade = partes[1].trim();

					table.addCell(produto);
					table.addCell(quantidade);
				} else {
					logger.error("Formato invalido.");
				}
			}

			document.add(table);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar a primeira tabela.");
		}
	}

	public void segundaTabela(Document document, List<String> listaStatus) {
		try {
			PdfPTable table = new PdfPTable(3);
			table.setSpacingBefore(16f);
			table.setSpacingAfter(20f);
			table.setWidthPercentage(90);

			for (int i = 0; i < 3; i++) {
				table.getDefaultCell().setPadding(8f); // Aumenta o padding de todas as células da tabela
			}

			PdfPCell segHeaderCell1 = new PdfPCell(new Phrase("Entregues", fontTextoBold));
			segHeaderCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
			segHeaderCell1.setPadding(8f);
			table.addCell(segHeaderCell1);

			PdfPCell segHeaderCell2 = new PdfPCell(new Phrase("Cancelados", fontTextoBold));
			segHeaderCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
			segHeaderCell2.setPadding(8f);
			table.addCell(segHeaderCell2);

			PdfPCell segHeaderCell3 = new PdfPCell(new Phrase("Não Entregues", fontTextoBold));
			segHeaderCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
			segHeaderCell3.setPadding(8f);
			table.addCell(segHeaderCell3);

			for (String item : listaStatus) {

				PdfPCell dataCell = new PdfPCell(new Phrase(item));
				dataCell.setPadding(8f); // Padding
				table.addCell(dataCell);
			}

			document.add(table);
		} catch (Exception e) {
			throw new ErroRelatorioException("Erro ao gerar segunda tabela.");
		}
	}

	public String itemMaisVendido(List<String> listAll) {
		String itemMaisVendido = null;
		int maiorQuantidade = Integer.MIN_VALUE;

		for (String item : listAll) {
			String[] partes = item.split(":");

			if (partes.length == 2) {
				String nomeItem = partes[0].trim();
				int quantidade;

				try {
					quantidade = Integer.parseInt(partes[1].trim());
				} catch (NumberFormatException e) {
					continue; // Ignorar itens com formato inválido
				}

				if (quantidade > maiorQuantidade) {
					maiorQuantidade = quantidade;
					itemMaisVendido = nomeItem + ": " + quantidade;
				}
			}
		}
		return itemMaisVendido;
	}

	private String itemMaisVendidoAux(List<String> pedidosContado) {
		if (pedidosContado == null || pedidosContado.isEmpty()) {
			return " ";
		}
		return this.itemMaisVendido(pedidosContado);
	}

	public List<String> getListaConsolidadaEntregues(List<String> allEntregues) {
		Map<String, Integer> entreguesConsolidado = new HashMap<>();

		for (String item : allEntregues) {
			String[] partes = item.split(":");
			if (partes.length == 2) {
				String produto = partes[0].trim();
				int quantidade = Integer.parseInt(partes[1].trim());

				// Soma todos os produtos e seus status
				entreguesConsolidado.put(produto, entreguesConsolidado.getOrDefault(produto, 0) + quantidade);
			}
		}
		List<String> listaConsolidadaEntregues = new ArrayList<>();
		entreguesConsolidado.forEach((produto, quantidade) -> {
			listaConsolidadaEntregues.add(produto + ": " + quantidade);
		});
		return listaConsolidadaEntregues;
	}

	public String getQuantidadePedidos(List<String> lista) {
		int totalPedidos = 0;
		for (String item : lista) {
			String[] partes = item.split(":");
			if (partes.length == 2) {
				int quantidade = Integer.parseInt(partes[1].trim());
				totalPedidos += quantidade;
			}
		}
		String resultado = "" + totalPedidos;
		return resultado;
	}

	protected String caminhoRelatorio(LocalDate data, String idCliente) {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaRelatorio(idCliente);
		String caminhoArq = caminhoInfo.getCaminhoArquivo();
		return caminhoArq + data + ".pdf";
	}

	protected String caminhoArquivoPedido(LocalDate data, String idCliente) {
		CaminhoInfo caminhoInfo = MudancaSO.separatorParaPedidos(idCliente);
		String caminhoArq = caminhoInfo.getCaminhoArquivo();
		return caminhoArq + data + ".json";
	}

	public PedidosContext lerPedidosArquivo(LocalDate data, String idCliente) {
		String caminhoArq = this.caminhoArquivoPedido(data, idCliente);
		PedidosContext pedidoContext = this.pedidoServico.carregarPedidosDeArquivo(caminhoArq);
		return pedidoContext;
	}

	protected void validarDiretorio(String diretorio) {
		File pasta = new File(diretorio);
		if (!pasta.exists() && !pasta.mkdirs()) {
			throw new ErroArquivoException("Não foi possível criar o diretório: " + diretorio);
		}
	}
}