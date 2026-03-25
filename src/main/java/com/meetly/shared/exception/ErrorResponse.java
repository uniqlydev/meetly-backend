package com.meetly.shared.exception;


import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        String path,
        OffsetDateTime timestamp
) {
}