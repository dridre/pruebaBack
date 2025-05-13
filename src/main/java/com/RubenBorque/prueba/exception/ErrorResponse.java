package com.RubenBorque.prueba.exception;

public record ErrorResponse(int status, String error, String message) {}