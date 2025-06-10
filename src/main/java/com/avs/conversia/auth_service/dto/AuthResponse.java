package com.avs.conversia.auth_service.dto;


import java.util.Set;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Set<Long> tenantIds;
}