package com.example;

import com.example.entitiesTest.lombokTest;

public class Test {

    public static void main(String[] args) {
        lombokTest test = new lombokTest();
        test.setName("Hello");
        test.setPassword("1234");

        System.out.println("Field1: " + test.getName());
        System.out.println("Field2: " + test.getPassword());
    }
}
