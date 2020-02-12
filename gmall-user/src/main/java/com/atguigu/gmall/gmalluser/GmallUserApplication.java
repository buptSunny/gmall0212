package com.atguigu.gmall.gmalluser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.gmalluser.mapper")
//MapperScan需用使用tk下的，否则会使用原始org的Scan
//tk下的mapper为通用mapper，自带了基本的增删改查方法；
public class GmallUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUserApplication.class, args);
    }

}
