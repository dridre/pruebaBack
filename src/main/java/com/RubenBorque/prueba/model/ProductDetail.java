package com.RubenBorque.prueba.model;

import java.math.BigDecimal;

public record ProductDetail(String id, String name, BigDecimal price, boolean availability) {}