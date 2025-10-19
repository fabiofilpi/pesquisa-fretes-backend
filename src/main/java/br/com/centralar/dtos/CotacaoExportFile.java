package br.com.centralar.dtos;

import jakarta.ws.rs.core.StreamingOutput;

public record CotacaoExportFile(StreamingOutput stream, String fileName) {}
