package org.example;

import org.bigraphs.dsl.bDSL.BDSLDocument;
import org.bigraphs.dsl.interpreter.BdslStatementInterpreterResult;
import org.bigraphs.dsl.interpreter.InterpreterServiceManager;
import org.bigraphs.dsl.interpreter.ParserService;
import org.bigraphs.dsl.interpreter.expressions.main.MainBlockEvalVisitorImpl;
import org.bigraphs.dsl.interpreter.expressions.main.MainStatementEvalVisitorImpl;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.example.domain.VMSyntax;
import org.example.domain.behavior.VMReactiveSystem;
import org.example.domain.data.VendingMachineObject;
import org.example.service.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.exportOpts;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dominik Grzelak
 */
@ExtendWith(InjectionExtension.class)
//@ExtensionMethod({BigraphExpressionVisitableExtension.class, MainBlockVisitableExtension.class})
public class Analysis extends AbstractTestSupport {

    // Bigraph Metamodel of the Vending Machine - required for all agents, rules and predicates later

    @Test
    void createMetaModel() throws IOException {
        BigraphFileModelManagement.Store.exportAsMetaModel(
                PureBigraphBuilder.create(new VMSyntax().sig(), VMSyntax.eMetaModelData).create(),
                Paths.get("src/test/resources/vending-machine/metamodel/"));
    }

    @Test
    void execute_single_insertCoinRule() throws InvalidReactionRuleException, IOException {
        String prefixPath = "vending-machine/rules/";
        String suffixPath = ".xmi";
        List<String> ruleNames = List.of("insertCoin");

        VMSyntax vmSyntax = new VMSyntax();
        DynamicSignature sig = vmSyntax.sig();
        EPackage bMM = BigraphFileModelManagement.Load.bigraphMetaModel(ResourceLoader.getResourceURL("vending-machine/metamodel/vm.ecore").getFile(), false);
//         EPackage bMM = BigraphFactory.createOrGetBigraphMetaModel(sig, VMSyntax.eMetaModelData);

        VMReactiveSystem system = new VMReactiveSystem();
        HashMap<String, ReactionRule<PureBigraph>> ruleMap = new LinkedHashMap<>();

        for (String eachRuleName : ruleNames) {
            URL ruleLeft = ResourceLoader.getResourceURL(prefixPath + eachRuleName + "L" + suffixPath);
            URL ruleRight = ResourceLoader.getResourceURL(prefixPath + eachRuleName + "R" + suffixPath);
//            InputStream ruleLeft = ResourceLoader.getResourceStream(prefixPath + eachRuleName + "L" + suffixPath);
//            InputStream ruleRight = ResourceLoader.getResourceStream(prefixPath + eachRuleName + "R" + suffixPath);

            List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(ruleLeft.getFile());
            List<EObject> eObjects1 = BigraphFileModelManagement.Load.bigraphInstanceModel(ruleRight.getFile());
            PureBigraph left = BigraphUtil.toBigraph(eObjects.get(0).eClass().getEPackage(), eObjects.get(0), sig);
            PureBigraph right = BigraphUtil.toBigraph(eObjects1.get(0).eClass().getEPackage(), eObjects1.get(0), sig);
            ParametricReactionRule<PureBigraph> reactionRule = new ParametricReactionRule<>(left, right);
            ruleMap.put(eachRuleName, reactionRule);
        }

        PureBigraph agent = VendingMachineObject.createAgent(2, 2, 2, vmSyntax);
        BigraphGraphvizExporter.toPNG(agent, true, new File("agent.png"));
        system.setAgent(agent);
        system.addReactionRule(ruleMap.get("insertCoin"));
        PureBigraph updatedModel = system.executeSingleRule();
        // Execute twice
        system.setAgent(updatedModel);
        updatedModel = system.executeSingleRule();
        BigraphGraphvizExporter.toPNG(updatedModel, true, new File("agent-updated.png"));
        BigraphGraphvizExporter.toPNG(system.getReactionRulesMap().get("r0").getRedex(), true, new File("updatedModel_LHS.png"));
        BigraphGraphvizExporter.toPNG(system.getReactionRulesMap().get("r0").getReactum(), true, new File("updatedModel_RHS.png"));
    }

    // Transition Graph : State Space Analysis

    @Test
    void explore_all_system_states() throws Exception {
        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine.bdsl");
        ParserService parser = InterpreterServiceManager.parser();
        BDSLDocument parse = parser.parse(is);
        parser.validate(parse);

        MainBlockEvalVisitorImpl mainEvalVisitor = new MainBlockEvalVisitorImpl(
                new MainStatementEvalVisitorImpl()
        );

        List<BdslStatementInterpreterResult> output = mainEvalVisitor.beginVisit(parse.getMain());
//        List<BdslStatementInterpreterResult> output = parse.getMain().interpret(mainEvalVisitor);
        Iterator<BdslStatementInterpreterResult> iterator = output.iterator();
        while (iterator.hasNext()) {
            BdslStatementInterpreterResult next = iterator.next();
            Optional<Object> call = next.getBdslExecutableStatement().call();
            if (call.isPresent()) {
                System.out.println(call);
                Object o = call.get();
                assertNotNull(o);
            }
        }
    }

    /**
     * Refill coffee action.
     * Proof by induction.
     *
     * @throws Exception
     */
    @Test
    void check_refill_coffee_rule() throws Exception {
//        final String teaOrCoffee = "Tea";
        final String teaOrCoffee = "Coffee";

        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine_refill" + teaOrCoffee + "_test.bdsl");
        ParserService parser = InterpreterServiceManager.parser();
        BDSLDocument parse = parser.parse(is);

        final int numOfTransitions = 3;

        ModelCheckingOptions opts = ModelCheckingOptions.create().and(
                        transitionOpts().setMaximumTransitions(numOfTransitions).create())
                .and(exportOpts().setReactionGraphFile(new File("transition-graph.png")).create());
        MainBlockEvalVisitorImpl mainEvalVisitor = (MainBlockEvalVisitorImpl) new MainBlockEvalVisitorImpl(
                (MainStatementEvalVisitorImpl) new MainStatementEvalVisitorImpl().withModelCheckingOptions(opts)
        );

        List<BdslStatementInterpreterResult> output = mainEvalVisitor.beginVisit(parse.getMain());
        Iterator<BdslStatementInterpreterResult> iterator = output.iterator();
        while (iterator.hasNext()) {
            BdslStatementInterpreterResult next = iterator.next();
            Optional<Object> call = next.getBdslExecutableStatement().call();
            if (call.isPresent()) {
                Object objResult = call.get();
                assertNotNull(objResult);
                if (objResult instanceof PureBigraphModelChecker) {
                    PureBigraphModelChecker mc = (PureBigraphModelChecker) objResult;
                    ReactionGraph<PureBigraph> reactionGraph = mc.getReactionGraph();
                    assertEquals(numOfTransitions + 1, reactionGraph.getGraph().vertexSet().size());
                    assertEquals(numOfTransitions, reactionGraph.getGraph().edgeSet().size());
                    int i = reactionGraph.getGraph().vertexSet().stream().map(x -> x.getLabel().replaceAll("a[\\:_]", ""))
                            .mapToInt(Integer::parseInt).max().orElseThrow(IllegalStateException::new);
                    String lastStateLabel = "a_" + i;
                    String canonicalForm = reactionGraph.getGraph().vertexSet().stream().filter(x -> x.getLabel().equals(lastStateLabel)).findFirst().get().getCanonicalForm();
                    long teaCount = reactionGraph.getStateMap().get(canonicalForm).getNodes().stream()
                            .map(BigraphEntity::getControl).filter(x -> x.getNamedType().stringValue().equalsIgnoreCase(teaOrCoffee))
                            .count();
                    assertEquals(numOfTransitions + 2, teaCount);
                }
            }
        }
    }

    // Rule Generation

    @Test
    void createRules_donateCoinToStudent() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("PHD").down().child("Wallet").down().site();
        reactum.root().child("PHD").down().child("Wallet").down().child("Coin").site();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/donateCoinL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/donateCoinR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_insertCoin() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.root()
                .child("PHD").down().child("Wallet").down().child("Coin").site()
                .top()
                .child("VM").down().site().child("Button1").child("Button2");
        ;
        builder2.root()
                .child("PHD").down().child("Wallet").down().site()
                .top()
                .child("VM").down().site().child("Button1").child("Button2").child("Coin")
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/insertCoinL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/insertCoinR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_pushButton1() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.root()
                .child("PHD").down().site()
                .top()
                .child("VM").down().child("Coin").site()
                .child("Button2")
                .child("Button1")
        ;
        builder2.root()
                .child("PHD").down().site()
                .top()
                .child("VM").down().child("Coin").site()
                .child("Button2")
                .child("Button1").down().child("Pressed");
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn1L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn1R.xmi"), schemaLocation);
    }

    @Test
    public void createRules_pushButton2() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.root()
                .child("PHD").down().site()
                .top()
                .child("VM").down().child("Coin").site()
                .child("Button1")
                .child("Button2");
        ;
        builder2.root()
                .child("PHD").down().site()
                .top()
                .child("VM").down().child("Coin").site()
                .child("Button1")
                .child("Button2").down().child("Pressed")
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn2R.xmi"), schemaLocation);
    }

    @Test
    public void createRules_giveCoffee() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.root()
                .child("PHD").down().child("Wallet").down().site()
                .top()
                .child("VM").down()
                .child("Coin").site()
                .child("Container").down().child("Coffee").site().up()
                .child("Button1").down().child("Pressed").up()
                .child("Tresor").down().site();
        ;
        builder2.root()
                .child("PHD").down().child("Wallet").down().child("Coffee").site()
                .top()
                .child("VM").down()
                .site()
                .child("Container").down().site().up()
                .child("Button1")
                .child("Tresor").down().child("Coin").site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveCoffeeL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveCoffeeR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_giveTea() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.root()
                .child("PHD").down().child("Wallet").down().site()
                .top()
                .child("VM").down()
                .child("Coin").site()
                .child("Container").down().child("Tea").site().up()
                .child("Button2").down().child("Pressed").up()
                .child("Tresor").down().site();
        ;
        builder2.root()
                .child("PHD").down().child("Wallet").down().child("Tea").site()
                .top()
                .child("VM").down()
                .site()
                .child("Container").down().site().up()
                .child("Button2")
                .child("Tresor").down().child("Coin").site()
        ;
        PureBigraph redex = builder.create();
        PureBigraph reactum = builder2.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveTeaL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveTeaR.xmi"), schemaLocation);
    }

    @Test
    void createRules_refillTea() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        // One container already contains tea
        // react refillTea(Sig) = { Container - (Tea | id(1))}, { Container - (Tea | Tea | id(1)) }
        PureBigraphBuilder<DynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").down().child("Tea").site();
        reactum.root().child("Container").down().child("Tea").child("Tea").site();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTeaL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTeaR.xmi"), schemaLocation);

        // One container is empty, and one contains already coffee
        // react refillTea2(Sig) = { Container | Container - (Coffee | id(1)) }, { Container - (Tea) | Container - (Coffee | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").child("Container").down().child("Coffee").site();
        reactum.root().child("Container").down().child("Tea").up().child("Container").down().child("Coffee").site();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Tea) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").child("Container");
        reactum.root().child("Container").down().child("Tea").up().child("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea3R.xmi"), schemaLocation);
    }

    @Test
    void createRules_refillCoffee() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        // One container already contains coffee
        // react refillTea(Sig) = { Container - (Coffee | id(1))}, { Container - (Coffee | Coffee | id(1)) }
        PureBigraphBuilder<DynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").down().child("Coffee").site();
        reactum.root().child("Container").down().child("Coffee").child("Coffee").site();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffeeL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffeeR.xmi"), schemaLocation);

        // One container is empty, and one contains already tea
        // react refillTea2(Sig) = { Container | Container - (Tea | id(1)) }, { Container - (Coffee) | Container - (Tea | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").child("Container").down().child("Tea").site();
        reactum.root().child("Container").down().child("Coffee").up().child("Container").down().child("Tea").site();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Coffee) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.root().child("Container").child("Container");
        reactum.root().child("Container").down().child("Coffee").up().child("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.create(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee3R.xmi"), schemaLocation);
    }

    // Predicates

    @Test
    public void createPred_teaContainerIsEmpty() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        builder.root()
                .child("VM").down()
                .site()
                .child("Container")
                .child("Container").down()
                .child("Coffee").site()
        ;
        PureBigraph bigraph = builder.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
                new FileOutputStream("src/test/resources/vending-machine/predicates/teaEmpty.xmi"), schemaLocation);
    }

    @Test
    public void createPred_coffeeContainerIsEmpty() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        builder.root()
                .child("VM").down()
                .site()
                .child("Container")
                .child("Container").down()
                .child("Tea").site()
        ;
        PureBigraph bigraph = builder.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
                new FileOutputStream("src/test/resources/vending-machine/predicates/coffeeEmpty.xmi"), schemaLocation);
    }
}
