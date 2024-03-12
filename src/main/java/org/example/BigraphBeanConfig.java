package org.example;

import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.example.domain.VMSyntax;
import org.example.domain.behavior.VMRuleListener;
import org.example.domain.behavior.VMRuleSet;
import org.example.domain.data.VendingMachineListener;
import org.example.domain.data.VendingMachineObject;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * This configuration class' primary purpose is to be a source of bean definitions.
 * It provides the required beans for the bigraphical vending machine web app.
 * These beans represent bigraphical objects:
 * <ul>
 *     <li>The signature</li>
 *     <li>The vending machine system and a listener</li>
 *     <li>The rules and a listener</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
@Configuration
@AutoConfigureOrder(3)
public class BigraphBeanConfig {
    public BigraphBeanConfig() {
        System.out.println("BigraphBeanConfig");
    }

    @Bean
    @Scope("singleton")
    public VMSyntax vmSyntax(CdoTemplate cdoTemplate) {
        VMSyntax vmSyntax = new VMSyntax();
        vmSyntax.initPackageRepositories(cdoTemplate);
        return vmSyntax;
    }

    @Bean
    @Scope("singleton")
    public VMRuleListener vmRuleListener(VMSyntax vmSyntax, CdoTemplate template) {
        return new VMRuleListener(vmSyntax, template);
    }

    @Bean
    @Scope("singleton")
    public VMRuleSet ruleSet(VMSyntax vmSyntax, CdoTemplate cdoTemplate, VMRuleListener vmRuleListener) {
        VMRuleSet ruleSet = new VMRuleSet(cdoTemplate, vmSyntax, vmRuleListener);
        ruleSet.init();
        return ruleSet;
    }

    @Bean
    @Scope("singleton")
    public VendingMachineListener vendingMachineListener(VMSyntax vmSyntax, CdoTemplate cdoTemplate) { //@Lazy @Qualifier("vmrepository") VMRepository vmrepository
        VendingMachineListener vendingMachineListener = new VendingMachineListener(vmSyntax, cdoTemplate); //vmrepository);
        return vendingMachineListener;
    }

    @Bean
    @Scope("singleton")
    public VendingMachineObject vendingMachine() {
        VendingMachineObject vm = new VendingMachineObject();
        return vm;
    }
}
