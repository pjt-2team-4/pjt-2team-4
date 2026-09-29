package com.rookies6.myspringboot4project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // 추가

@EnableJpaAuditing // 이 줄을 반드시 추가하세요!
@SpringBootApplication
@ConfigurationPropertiesScan
public class MySpringBoot4ProjectApplication {

    public static void main(String[] args) {

        SpringApplication application =
                new SpringApplication(MySpringBoot4ProjectApplication.class);

        application.setWebApplicationType(WebApplicationType.SERVLET);
        application.run(args);
    }
}
