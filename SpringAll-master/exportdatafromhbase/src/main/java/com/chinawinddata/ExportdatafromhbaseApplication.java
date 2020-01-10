package com.chinawinddata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;


@SpringBootApplication(scanBasePackages = "com.chinawinddata", exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class ExportdatafromhbaseApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ExportdatafromhbaseApplication.class, args);
	}


	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ExportdatafromhbaseApplication.class);
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
		sessionLocaleResolver.setDefaultLocale(new Locale("zh", "CN"));
		return sessionLocaleResolver;
	}



}
