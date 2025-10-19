package br.com.centralar.dtos;

// Devolve opções de frete já “limpas”
public record ShippingOptionDTO(
    String id,
    String friendlyName,
    long priceInCents,
    String estimate // ex.: "7bd" (7 business days)
    ) {}
