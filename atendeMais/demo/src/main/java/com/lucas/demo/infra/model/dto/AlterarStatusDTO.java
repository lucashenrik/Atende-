package com.lucas.demo.infra.model.dto;

public record AlterarStatusDTO(String pedidoId, String novoStatus, String hora) {
}