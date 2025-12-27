package org.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {

    private Long userId;
    
    private String displayName;
    
    private String email;
    
    private String role;
    
    private LocalDateTime createdAt;
}
