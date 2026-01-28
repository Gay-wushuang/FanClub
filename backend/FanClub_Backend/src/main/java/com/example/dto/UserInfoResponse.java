package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserInfoResponse {
    private String userId;
    private String nickname;
    private Set<String> roles;
    private Set<String> anchorIds;
    private Set<String> roomIds;
}