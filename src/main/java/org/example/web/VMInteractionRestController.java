package org.example.web;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.eclipse.emf.cdo.common.revision.CDORevisionUtil;
import org.eclipse.emf.ecore.EPackage;
import org.example.domain.behavior.VMReactiveSystem;
import org.example.domain.behavior.VMRuleSet;
import org.example.domain.data.VendingMachineObject;
import org.example.repository.VMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Spring REST controller that helps to interact with the vending machine system via the web.
 * Therefore, it provides several REST endpoints.
 * <p>
 * To execute behaviour a BRS of type {@link VMReactiveSystem} is created with suitable rules that resemble the
 * valid traces from the analysis (cf. {@code src/test/java/org.example/Analysis.java}).
 * Specifically, executing the core logic represents multiple coordinated transformations of the bigraph model.
 * <p>
 * The result is stored in the database after.
 * The vending machine bigraph of type {@link VendingMachineObject} is automatically refreshed since database listeners react to this change.
 * Thus, it is just injected here as a class member.
 *
 * @author Dominik Grzelak
 */
@RestController
public class VMInteractionRestController {

    private static final String template = "You tried to input %s coins in the VM and got an item!";
    private static final String template_fail = "You tried to input %s coins in the VM but no item for you!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    VendingMachineObject vendingMachineObject;
    @Autowired
    VMRuleSet ruleSet;
    @Autowired
    VMRepository repository;

    public VMInteractionRestController() {
        System.out.println("VMInteractionRestController");
    }

    //TODO tea action

    @GetMapping("/insertCoin")
    public ResponseEntity<ResponseData> insertCoin() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("insertCoin"));
            PureBigraph updatedModel = system.executeSingleRule();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "insertCoin"));
            }
        } catch (InvalidReactionRuleException e) {
            return ResponseEntity.badRequest().body(ResponseData.create(e.getMessage()));
        }
    }

    @GetMapping("/donateCoin")
    public ResponseEntity<ResponseData> donateCoin() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("donateCoin"));
            PureBigraph updatedModel = system.executeSingleRule();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "donateCoin"));
            }
        } catch (InvalidReactionRuleException e) {
            return ResponseEntity.badRequest().body(ResponseData.create(e.getMessage()));
        }
    }

    @GetMapping("/refillTea")
    public ResponseEntity<ResponseData> refillTea() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("refillTea"));
            system.addReactionRule(ruleSet.getRuleMap().get("refillTea2"));
            system.addReactionRule(ruleSet.getRuleMap().get("refillTea3"));
            PureBigraph updatedModel = system.executeAllRulesUntilOneMatches();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "refillTea"));
            }
        } catch (InvalidReactionRuleException e) {
            return ResponseEntity.badRequest().body(ResponseData.create(e.getMessage()));
        }
    }

    @GetMapping("/refillCoffee")
    public ResponseEntity<ResponseData> refillCoffee() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("refillCoffee"));
            system.addReactionRule(ruleSet.getRuleMap().get("refillCoffee2"));
            system.addReactionRule(ruleSet.getRuleMap().get("refillCoffee3"));
            PureBigraph updatedModel = system.executeAllRulesUntilOneMatches();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "refillCoffee"));
            }
        } catch (InvalidReactionRuleException e) {
            return ResponseEntity.badRequest().body(ResponseData.create(e.getMessage()));
        }
    }

    @GetMapping("/pushBtnCoffee")
    public ResponseEntity<ResponseData> pushButtonForCoffee() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("pushBtn1"));
            system.addReactionRule(ruleSet.getRuleMap().get("giveCoffee"));
            PureBigraph updatedModel = system.executeConsecutively();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "pushBtn1+giveCoffee"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ResponseData.create("FAIL!"));
    }

    @GetMapping("/pushBtnTea")
    public ResponseEntity<ResponseData> pushButtonForTea() {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            system.addReactionRule(ruleSet.getRuleMap().get("pushBtn2"));
            system.addReactionRule(ruleSet.getRuleMap().get("giveTea"));
            PureBigraph updatedModel = system.executeConsecutively();
            if (updatedModel != null) {
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return ResponseEntity.ok().body(ResponseData.create("OK!"));
            } else {
                return ResponseEntity.badRequest().body(ResponseData.create("Could not apply rule: " + "pushBtn2+giveTea"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ResponseData.create("FAIL!"));
    }


    //TODO return a response object with result image base64 + message
    @GetMapping("/coffeeAction")
    public String coffeeAction(@RequestParam(value = "coins", defaultValue = "1") String numOfCoins) {
        try {
            VMReactiveSystem system = new VMReactiveSystem();
            system.setAgent(vendingMachineObject.bigraph);
            ReactionRule<PureBigraph> insertCoin = ruleSet.getRuleMap().get("insertCoin");
            ReactionRule<PureBigraph> pushBtn1 = ruleSet.getRuleMap().get("pushBtn1");
            ReactionRule<PureBigraph> giveCoffee = ruleSet.getRuleMap().get("giveCoffee");
            system.addReactionRule(insertCoin);
            system.addReactionRule(pushBtn1);
            system.addReactionRule(giveCoffee);
            PureBigraph updatedModel = system.execute(Integer.parseInt(numOfCoins));
            if (updatedModel != null) {
                BigraphGraphvizExporter.toPNG(updatedModel, true, new File("agent.png"));
                vendingMachineObject.bigraph = updatedModel;
                vendingMachineObject.eModel = updatedModel.getModel();
                vendingMachineObject = repository.save(vendingMachineObject);
                return String.format(template, numOfCoins);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format(template_fail, numOfCoins);
    }

    @GetMapping("/getCurrentStateAsImage")
    public ResponseEntity<ResponseData> currentBigraphState() {
        try {
            File png = File.createTempFile("vm-current-agent", "");
            BigraphGraphvizExporter.toPNG(vendingMachineObject.bigraph, true, png);
            byte[] content = Files.readAllBytes(Paths.get(png.toPath().toString() + ".png"));
            String encodedString = Base64.getEncoder().encodeToString(content);
            return ResponseEntity.ok().body(ResponseData.create("OK!", encodedString));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ResponseData.create(e.getMessage()));
        }
    }
}