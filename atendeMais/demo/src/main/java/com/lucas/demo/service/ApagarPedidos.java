package com.lucas.demo.service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lucas.demo.exceptions.ErroArquivoException;
import com.lucas.demo.model.CaminhoInfo;

@Service
public class ApagarPedidos {

	private static final Logger logger = LoggerFactory.getLogger(ApagarPedidos.class);
	
	public void verificarEApagarPedidosSemana(List<LocalDate> diasDaSemana, CaminhoInfo caminhoInfo) {
		LocalTime horaLimite = LocalTime.of(23, 0);
		LocalTime agora = LocalTime.now();
		
		String caminho = caminhoInfo.getDiretorio();
		if (agora.isBefore(horaLimite)) {
			apagarPedidosDaSemana(diasDaSemana, caminho);
		}
	}
	
	public void apagarPedidosDaSemana(List<LocalDate> diasDaSemana, String caminhoPedidos) {
		File dir = new File(caminhoPedidos);
		if (!dir.exists() || !dir.isDirectory()) {
			logger.error("Diretório de pedidos não encontrado: " + caminhoPedidos);
			throw new ErroArquivoException("Diretório de pedidos não encontrado");
		}
		
		LocalDate inicioSemana = diasDaSemana.get(0);
		LocalDate finalDaSemana = diasDaSemana.get(diasDaSemana.size() - 1);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		File[] files = dir.listFiles();
		if (files == null) return;
		
		for(File file : files) {
			String fileName = file.getName();
			if (fileName.startsWith("pedidos_") && fileName.endsWith(".json")) {
				// Extrai a perta da data
				String dataStr = fileName.substring("pedidos_".length(), fileName.length() - ".json".length());
				try {
					LocalDate fileDate = LocalDate.parse(dataStr, formatter);
					
					  // Verifica se a data do arquivo está entre o início e o fim da semana
					if (!fileDate.isBefore(inicioSemana) && !fileDate.isAfter(finalDaSemana)) {
						boolean deletado = file.delete();
						if (deletado) {
							logger.info("Arquivo deletado: " + fileDate);
						} else {
							logger.error("Falha ao deletar arquivo: " + file.getAbsolutePath());
							throw new ErroArquivoException("Falha ao deletar arquivo.");
						}
					}
				} catch (ErroArquivoException e) {
					logger.error("Formato de data inválido no arquivo: " + fileName);
					throw new ErroArquivoException("Formato de data inválido no arquivo.");
				}
				
			}
		}
	}
}