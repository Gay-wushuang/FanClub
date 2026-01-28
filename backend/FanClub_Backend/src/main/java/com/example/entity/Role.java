package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_role")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    /*
    角色ID
    主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    角色代码
     */
    @Column(name = "role_code", unique = true, length = 50)
    private String roleCode;

    /*
    角色名称
     */
    @Column(name = "role_name", length = 50)
    private String roleName;

    /*
    创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // ✅ 这里引用了上面定义的 Permission 实体类
    // 多对多关系：角色拥有多个权限
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_role_permission",  // 中间表
            joinColumns = @JoinColumn(name = "role_id"),  // 当前表的外键
            inverseJoinColumns = @JoinColumn(name = "permission_id")  // 对方表的外键
    )
    private Set<Permission> permissions = new HashSet<>();

    // 反向引用：这个角色被哪些用户拥有
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /*
    在实体类中自动设置创建时间
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
     */
}