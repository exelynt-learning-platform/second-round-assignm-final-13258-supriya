package com.ecommerce.backend.dto.response;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AuthResponse {

    private String token;
    private String name;
    private String email;    
}
