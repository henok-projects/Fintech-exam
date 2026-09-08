package com.fintech.Exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String message;
    private String status;
    private Object data;
    private String timestamp;

    public static MessageDTO success(String message, Object data) {
        return MessageDTO.builder()
                .message(message)
                .status("SUCCESS")
                .data(data)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static MessageDTO error(String message) {
        return MessageDTO.builder()
                .message(message)
                .status("ERROR")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
