package org.example;

import jakarta.annotation.PostConstruct;
import org.bigraphs.spring.data.cdo.CdoServerConnectionString;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.bigraphs.spring.data.cdo.SimpleCdoDbFactory;
import org.bigraphs.spring.data.cdo.repository.config.EnableCdoRepositories;
import org.example.repository.VMRepository;
import org.example.service.CDOServerService;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.MappingContextEvent;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.ClassUtils;

//import javax.annotation.PostConstruct;

/**
 * Configuration class for the CDO server and repository.
 * Change here the CDO connection server details if necessary.
 * <p>
 * See also {@code src/main/resources/config/cdo-server.xml}
 *
 * @author Dominik Grzelak
 */
@Configuration
@AutoConfigureOrder(2)
@EnableCdoRepositories(basePackageClasses = VMRepository.class)
//@EnableCdoRepositories(basePackages = "org.example.repository") // alternative configuration by package name
@EnableAsync
public class CDOServerConfig implements ApplicationListener<ApplicationEvent> {
    final CDOServerService serverService;

    public CDOServerConfig(CDOServerService serverService) {
        System.out.println("CDOServerConfig");
        this.serverService = serverService;
    }

    @Bean
    public CdoTemplate cdoTemplate() throws Exception {
        return new CdoTemplate(new SimpleCdoDbFactory(new CdoServerConnectionString("cdo://localhost:2036/repo1")));
    }

    @PostConstruct
    void postConstruct() {
        try {
            serverService.tryStartingCdoServer();
            Thread.sleep(2000);
            System.out.println("Grace period over ... CDO should be running now...");
            System.out.println("Server status: " + serverService.started());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public ExitCodeGenerator exitCodeGenerator() {
        return () -> {
            serverService.stopServer();
            return 0;
        };
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (ClassUtils.isAssignable(applicationEvent.getClass(), MappingContextEvent.class)) {
            serverService.tryStartingCdoServer();
        }
    }
}
