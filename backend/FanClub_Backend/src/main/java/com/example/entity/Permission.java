package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_code", length = 100)
    private String permissionCode;

    @Column(name = "permission_name", length = 100)
    private String permissionName;

    @Column(name = "api_path", length = 200)
    private String apiPath;

    @Column(name = "method", length = 10)
    private String method;

    // 反向引用：这个权限被哪些角色拥有
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}