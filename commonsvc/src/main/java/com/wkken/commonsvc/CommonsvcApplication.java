package com.wkken.commonsvc;

import com.wkken.commonsvc.com.wkken.base.DynamicDataSourceContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommonsvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommonsvcApplication.class, args);
		System.out.println(DynamicDataSourceContextHolder.dataSourceIds);
	}

}
