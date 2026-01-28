package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresInSec;
    private Set<String> roles;
    private String userId;
    private String nickname;
    private Set<String> anchorIds;
    private Set<String> roomIds;
}