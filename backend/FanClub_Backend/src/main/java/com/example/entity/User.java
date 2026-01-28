package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "nickname", length = 50)
    private String nickname;  // 新增字段，用于显示昵称

    @Column(name = "status")
    private Integer status = 1; // 1正常 0禁用

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @ManyToMany(fetch = FetchType.EAGER)  // 改为EAGER，登录时需要立即加载角色
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // 新增字段：管理的主播ID集合
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_anchor_mapping",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "anchor_id")
    private Set<String> anchorIds = new HashSet<>();

    // 新增字段：管理的房间ID集合
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_room_mapping",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "room_id")
    private Set<String> roomIds = new HashSet<>();
}