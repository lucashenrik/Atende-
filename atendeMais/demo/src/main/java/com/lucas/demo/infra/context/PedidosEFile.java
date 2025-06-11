package com.lucas.demo.infra.context;

import java.io.File;
import java.util.List;
import java.util.Map;

public record PedidosEFile(File file, List<Map<String, Object>> pedidos) {
}