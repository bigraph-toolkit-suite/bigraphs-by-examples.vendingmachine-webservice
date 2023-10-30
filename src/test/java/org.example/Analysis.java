package org.example;


import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ReactionGraph;
//import de.tudresden.inf.st.bigraphs.dsl.bDSL.BDSLDocument;
//import de.tudresden.inf.st.bigraphs.dsl.interpreter.BdslStatementInterpreterResult;
//import de.tudresden.inf.st.bigraphs.dsl.interpreter.InterpreterServiceManager;
//import de.tudresden.inf.st.bigraphs.dsl.interpreter.ParserService;
//import de.tudresden.inf.st.bigraphs.dsl.interpreter.expressions.main.MainBlockEvalVisitorImpl;
//import de.tudresden.inf.st.bigraphs.dsl.interpreter.expressions.main.MainStatementEvalVisitorImpl;
import org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions;
import org.bigraphs.framework.simulation.modelchecking.PureBigraphModelChecker;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.example.domain.VMSyntax;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.exportOpts;
import static org.bigraphs.framework.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dominik Grzelak
 */
@ExtendWith(InjectionExtension.class)
public class Analysis extends AbstractTestSupport {

    @Test
    void explore_system_states() throws Exception {
//        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine.bdsl");
//        ParserService parser = InterpreterServiceManager.parser();
//
//        BDSLDocument parse = parser.parse(is);
//        MainBlockEvalVisitorImpl mainEvalVisitor = new MainBlockEvalVisitorImpl(
//                new MainStatementEvalVisitorImpl()
//        );
//
//        List<BdslStatementInterpreterResult> output = mainEvalVisitor.beginVisit(parse.getMain());
//        Iterator<BdslStatementInterpreterResult> iterator = output.iterator();
//        while (iterator.hasNext()) {
//            BdslStatementInterpreterResult next = iterator.next();
//            Optional<Object> call = next.getBdslExecutableStatement().call();
//            if (call.isPresent()) {
//                assertNotNull(call.get());
//            }
//        }
    }

    @Test
    void createRules_donateCoinToStudent() throws IOException {
        String schemaLocation = "../metamodel/vm.ecore";
        DefaultDynamicSignature sig = new VMSyntax().sig();
        PureBigraphBuilder<DefaultDynamicSignature> redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        PureBigraphBuilder<DefaultDynamicSignature> reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("PHD").down().addChild("Wallet").down().addSite();
        reactum.createRoot().addChild("PHD").down().addChild("Wallet").down().addChild("Coin").addSite();
        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/donateCoinL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/donateCoinR.xmi"), schemaLocation);
    }

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
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTeaL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTeaR.xmi"), schemaLocation);

        // One container is empty, and one contains already coffee
        // react refillTea2(Sig) = { Container | Container - (Coffee | id(1)) }, { Container - (Tea) | Container - (Coffee | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container").down().addChild("Coffee").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Tea").up().addChild("Container").down().addChild("Coffee").addSite();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTea2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTea2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Tea) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container");
        reactum.createRoot().addChild("Container").down().addChild("Tea").up().addChild("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTea3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillTea3R.xmi"), schemaLocation);
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
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffeeL.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffeeR.xmi"), schemaLocation);

        // One container is empty, and one contains already tea
        // react refillTea2(Sig) = { Container | Container - (Tea | id(1)) }, { Container - (Coffee) | Container - (Tea | id(1)) }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container").down().addChild("Tea").addSite();
        reactum.createRoot().addChild("Container").down().addChild("Coffee").up().addChild("Container").down().addChild("Tea").addSite();

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffee2L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffee2R.xmi"), schemaLocation);

        // Both containers are empty -> choose any
        // react refillTea3(Sig) = { Container | Container }, { Container - (Coffee) | Container }
        redex = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        reactum = PureBigraphBuilder.create(sig, VMSyntax.eMetaModelData);
        redex.createRoot().addChild("Container").addChild("Container");
        reactum.createRoot().addChild("Container").down().addChild("Coffee").up().addChild("Container");

        BigraphFileModelManagement.Store.exportAsInstanceModel(redex.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffee3L.xmi"), schemaLocation);
        BigraphFileModelManagement.Store.exportAsInstanceModel(reactum.createBigraph(),
                new FileOutputStream("src/test/resources/vending-machine/bigraphs/refillCoffee3R.xmi"), schemaLocation);
    }


    /**
     * Refill tea action.
     * Proof by induction.
     *
     * @throws Exception
     */
    @Test
    void refill_tea_rule() throws Exception {
//        InputStream is = getResourceAsStream("vending-machine/script/vendingmachine_refillTea_test.bdsl");
//        ParserService parser = InterpreterServiceManager.parser();
//        BDSLDocument parse = parser.parse(is);
//
//        final int numOfTransitions = 3;
//
//        ModelCheckingOptions opts = ModelCheckingOptions.create().and(
//                        transitionOpts().setMaximumTransitions(numOfTransitions).create())
//                .and(exportOpts().setReactionGraphFile(new File("transition-graph.png")).create());
//        MainBlockEvalVisitorImpl mainEvalVisitor = (MainBlockEvalVisitorImpl) new MainBlockEvalVisitorImpl(
//                (MainStatementEvalVisitorImpl) new MainStatementEvalVisitorImpl().withModelCheckingOptions(opts)
//        );
//
//        List<BdslStatementInterpreterResult> output = mainEvalVisitor.beginVisit(parse.getMain());
//        Iterator<BdslStatementInterpreterResult> iterator = output.iterator();
//        while (iterator.hasNext()) {
//            BdslStatementInterpreterResult next = iterator.next();
//            Optional<Object> call = next.getBdslExecutableStatement().call();
//            if (call.isPresent()) {
//                Object objResult = call.get();
//                assertNotNull(objResult);
//                if (objResult instanceof PureBigraphModelChecker) {
//                    PureBigraphModelChecker mc = (PureBigraphModelChecker) objResult;
//                    ReactionGraph<PureBigraph> reactionGraph = mc.getReactionGraph();
//                    assertEquals(numOfTransitions + 1, reactionGraph.getGraph().vertexSet().size());
//                    assertEquals(numOfTransitions, reactionGraph.getGraph().edgeSet().size());
//                    int i = reactionGraph.getGraph().vertexSet().stream().map(x -> x.getLabel().replaceAll("a\\:", ""))
//                            .mapToInt(Integer::parseInt).max().orElseThrow(IllegalStateException::new);
//                    String lastStateLabel = "a:" + i;
//                    String canonicalForm = reactionGraph.getGraph().vertexSet().stream().filter(x -> x.getLabel().equals(lastStateLabel)).findFirst().get().getCanonicalForm();
//                    long teaCount = reactionGraph.getStateMap().get(canonicalForm).getNodes().stream()
//                            .map(BigraphEntity::getControl).filter(x -> x.getNamedType().stringValue().equalsIgnoreCase("Tea"))
//                            .count();
//                    assertEquals(numOfTransitions + 2, teaCount);
//                }
//            }
//        }
    }
}
