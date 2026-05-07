package com.hyejin.portfolio.error;

public class PortfolioException extends RuntimeException {
    private final ErrorCode code;

    public PortfolioException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
