package org.example;

import org.bigraphs.dsl.bDSL.BDSLDocument;
import org.bigraphs.dsl.interpreter.BdslStatementInterpreterResult;
import org.bigraphs.dsl.interpreter.InterpreterServiceManager;
import org.bigraphs.dsl.interpreter.ParserService;
import org.bigraphs.dsl.interpreter.expressions.main.MainBlockEvalVisitorImpl;
import org.bigraphs.dsl.interpreter.expressions.main.MainStatementEvalVisitorImpl;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.example.domain.VMSyntax;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.exportOpts;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dominik Grzelak
 */
@ExtendWith(InjectionExtension.class)
public class Analysis extends AbstractTestSupport {

    // Bigraph Metamodel of the Vending Machine - required for all agents, rules and predicates later

    @Test
    void createMetaModel() throws IOException {
        BigraphFileModelManagement.Store.exportAsMetaModel(
                PureBigraphBuilder.create(new VMSyntax().sig(), VMSyntax.eMetaModelData).createBigraph(),
                Paths.get("src/test/resources/vending-machine/metamodel/"));
    }

    // Transition Graph : State Space Analysis

    @Test
    void explore_system_states() throws Exception {
        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine.bdsl");
        ParserService parser = InterpreterServiceManager.parser();

        BDSLDocument parse = parser.parse(is);
        MainBlockEvalVisitorImpl mainEvalVisitor = new MainBlockEvalVisitorImpl(
                new MainStatementEvalVisitorImpl()
        );

        List<BdslStatementInterpreterResult> output = mainEvalVisitor.beginVisit(parse.getMain());
        Iterator<BdslStatementInterpreterResult> iterator = output.iterator();
        while (iterator.hasNext()) {
            BdslStatementInterpreterResult next = iterator.next();
            Optional<Object> call = next.getBdslExecutableStatement().call();
            if (call.isPresent()) {
                assertNotNull(call.get());
            }
        }
    }

    // Rule Generation

    @Test
    void createRules_donateCoinToStudent() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("PHD").down().addChild("Wallet").down().addSite();
        reactum.createRoot().addChild("PHD").down().addChild("Wallet").down().addChild("Coin").addSite();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/donateCoinL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/donateCoinR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_insertCoin() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coin").addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2").addChild("Coin");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/insertCoinL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/insertCoinR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_pushButton1() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1")
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1").down().addChild("Pressed");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn1L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn1R.xmi"), schemaLocation);
    }

    @Test
    public void createRules_pushButton2() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2").down().addChild("Pressed")
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/pushBtn2R.xmi"), schemaLocation);
    }

    @Test
    public void createRules_giveCoffee() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Coffee").addSite().up()
                .addChild("Button1").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coffee").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button1")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveCoffeeL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveCoffeeR.xmi"), schemaLocation);
    }

    @Test
    public void createRules_giveTea() throws Exception {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Tea").addSite().up()
                .addChild("Button2").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Tea").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button2")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveTeaL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum,
                new FileOutputStream("src/test/resources/vending-machine/rules/giveTeaR.xmi"), schemaLocation);
    }

    // Predicates

    @Test
    public void teaContainerIsEmpty() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Coffee").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
                new FileOutputStream("src/test/resources/vending-machine/predicates/teaEmpty.xmi"), schemaLocation);
    }

    @Test
    public void coffeeContainerIsEmpty() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Tea").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
                new FileOutputStream("src/test/resources/vending-machine/predicates/coffeeEmpty.xmi"), schemaLocation);
    }


    /////////////////////////////////////////////////////////////////////

    @Test
    void createRules_refillTea() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        // One container already contains tea
        // react refillTea(Sig) = { Container - (Tea | id(1))}, { Container - (Tea | Tea | id(1)) }
        PureBigraphBuilder<DefaultDynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").down().addChild("Tea").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Tea").addChild("Tea").addSite();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTeaL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTeaR.xmi"), schemaLocation);

        // One container is empty, and one contains already coffee
        // react refillTea2(Sig) = { Container | Container - (Coffee | id(1)) }, { Container - (Tea) | Container - (Coffee | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container").down().addChild("Coffee").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Tea").up().addChild("Container").down().addChild("Coffee").addSite();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Tea) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container");
        reactum.createRoot().addChild("Container").down().addChild("Tea").up().addChild("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillTea3R.xmi"), schemaLocation);
    }

    @Test
    void createRules_refillCoffee() throws IOException {
        //        String schemaLocation = "vm.ecore";
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        // One container already contains coffee
        // react refillTea(Sig) = { Container - (Coffee | id(1))}, { Container - (Coffee | Coffee | id(1)) }
        PureBigraphBuilder<DefaultDynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").down().addChild("Coffee").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Coffee").addChild("Coffee").addSite();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffeeL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffeeR.xmi"), schemaLocation);

        // One container is empty, and one contains already tea
        // react refillTea2(Sig) = { Container | Container - (Tea | id(1)) }, { Container - (Coffee) | Container - (Tea | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container").down().addChild("Tea").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Coffee").up().addChild("Container").down().addChild("Tea").addSite();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Coffee) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container");
        reactum.createRoot().addChild("Container").down().addChild("Coffee").up().addChild("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/rules/refillCoffee3R.xmi"), schemaLocation);
    }


    /**
     * Refill tea action.
     * Proof by induction.
     *
     * @throws Exception
     */
    @Test
    void refill_tea_rule() throws Exception {
        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine_refillTea_test.bdsl");
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
                    int i = reactionGraph.getGraph().vertexSet().stream().map(x -> x.getLabel().replaceAll("a\\:", ""))
                            .mapToInt(Integer::parseInt).max().orElseThrow(IllegalStateException::new);
                    String lastStateLabel = "a:" + i;
                    String canonicalForm = reactionGraph.getGraph().vertexSet().stream().filter(x -> x.getLabel().equals(lastStateLabel)).findFirst().get().getCanonicalForm();
                    long teaCount = reactionGraph.getStateMap().get(canonicalForm).getNodes().stream()
                            .map(BigraphEntity::getControl).filter(x -> x.getNamedType().stringValue().equalsIgnoreCase("Tea"))
                            .count();
                    assertEquals(numOfTransitions + 2, teaCount);
                }
            }
        }
    }
}
