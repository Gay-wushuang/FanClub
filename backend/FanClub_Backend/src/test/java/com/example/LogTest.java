package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {
    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);

    public static void main(String[] args) {
        logger.info("这是一条测试日志");
        logger.error("这是一条错误日志");
    }
}