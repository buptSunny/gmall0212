package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
public class GmallRedissonTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallRedissonTestApplication.class, args);
	}

}
