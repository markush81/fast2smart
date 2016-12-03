package net.fast2smart.external;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Created by markus on 22/10/2016.
 */
@SpringBootApplication(scanBasePackages = {"net.fast2smart"})
@SuppressWarnings({"squid:S1118"})
public class Application {

    @Bean
    @ConditionalOnMissingClass(value = "org.springframework.boot.test.context.SpringBootTest")
    public static BeanFactoryPostProcessor initializeDispatcherServlet() {
        return beanFactory -> {
            BeanDefinition bean = beanFactory.getBeanDefinition(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
            bean.getPropertyValues().add("loadOnStartup", 1);
        };
    }

    @SuppressWarnings({"squid:S2095"})
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
