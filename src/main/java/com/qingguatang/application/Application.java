package com.qingguatang.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan({"com.qingguatang.*.dao"})
@ComponentScan(basePackages = {"com.qingguatang.*.control",
    "com.qingguatang.*.service.impl"})
public class Application {

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);

  }
}
