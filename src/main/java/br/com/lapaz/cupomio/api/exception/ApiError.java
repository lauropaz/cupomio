package br.com.lapaz.cupomio.api.exception;

import java.util.List;

public record ApiError(String code, String message, List<String> details) {
}
