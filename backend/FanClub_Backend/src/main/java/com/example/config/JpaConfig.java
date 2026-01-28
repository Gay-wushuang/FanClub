package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
    * JPA审计配置类
    * 自动管理创建和更新时间
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}