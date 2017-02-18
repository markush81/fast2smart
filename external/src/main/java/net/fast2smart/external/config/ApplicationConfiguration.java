package net.fast2smart.external.config;

import net.fast2smart.Application;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaRepositories(basePackages = {"net.fast2smart.legacy.repository"})
@EntityScan(basePackageClasses = {Application.class, Jsr310JpaConverters.class}, basePackages = {"net.fast2smart.legacy.model"})
public class ApplicationConfiguration {


}
