package com.example;

import cn.dev33.satoken.SaManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FanClubBackendApplication {

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(FanClubBackendApplication.class, args);
		//System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
	}

}
