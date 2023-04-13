package org.example;

import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.impl.BigraphBaseModelPackageImpl;
import org.example.domain.VMSyntax;
import org.example.domain.data.VendingMachineObject;
import org.example.repository.VMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


/**
 * Main entry point of the application.
 *
 * @author Dominik Grzelak
 */
@SpringBootApplication
@Import(value = {AppConfig.class, CDOServerConfig.class, BigraphBeanConfig.class})
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        BigraphBaseModelPackageImpl.init();
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    VMSyntax vmSyntax;
    @Autowired
    VendingMachineObject vendingMachineObject;
    @Autowired
    VMRepository repository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Creating initial agent and storing the vending machine in Eclipse CDO ...");
        vendingMachineObject.init(VendingMachineObject.createAgent(2, 2, 2, vmSyntax));
        // Wait a little bit for a gracefully start ...
        Thread.sleep(2500);
//        cdoTemplate.remove(ruleSet.getRuleMap().get("giveCoffee").getRedex().getModel(), "/system/rules/giveCoffee/L");
//        cdoTemplate.insert(ruleSet.getRuleMap().get("giveCoffee").getRedex().getModel(), "system/rules/giveCoffee/L");
//        System.out.println("Application has successfully started ...");
    }
}
