package org.example.domain.behavior;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.bigraphs.spring.data.cdo.core.listener.DefaultCdoSessionListener;
import org.bigraphs.spring.data.cdo.core.listener.filter.CdoListenerFilter;
import org.bigraphs.spring.data.cdo.core.listener.filter.FilterCriteria;
import org.eclipse.emf.cdo.session.CDOSessionInvalidationEvent;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.net4j.util.event.IListener;
import org.example.domain.VMSyntax;
import org.example.service.ResourceLoader;
import org.springframework.core.io.DefaultResourceLoader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rules describe the possible behavior of the system.
 *
 * @author Dominik Grzelak
 */
public class VMRuleSet implements PropertyChangeListener {

    CdoTemplate template;
    VMSyntax vmSyntax;
    VMRuleListener ruleActionListener;
    HashMap<String, ReactionRule<PureBigraph>> ruleMap = new LinkedHashMap<>();

    String[] ruleNames = new String[]{
            "giveCoffee", "giveTea", "insertCoin", "pushBtn1", "pushBtn2",
            "refillTea", "refillTea2", "refillTea3", "refillCoffee", "refillCoffee2", "refillCoffee3",
            "donateCoin"
    };

    public VMRuleSet(CdoTemplate template, VMSyntax vmSyntax, VMRuleListener listener) {
        this.template = template;
        this.vmSyntax = vmSyntax;
        this.ruleActionListener = listener;
    }

    public void init() {
        try {
            loadRulesFromFileSystem();
            storeRulesInCdo();
            setupCdoListener(ruleActionListener);
        } catch (IOException | InvalidReactionRuleException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ReactionRule<PureBigraph>> getRuleMap() {
        return ruleMap;
    }

    private void setupCdoListener(final VMRuleListener listener) {
        // add property listener
        listener.addChangeListener(this);
        // add CDO listener
        CdoListenerFilter filter = new CdoListenerFilter()
                .addCriteria(new FilterCriteria("rp1").byRepositoryPath("system/rules/pushBtn1/L"))
                .addCriteria(new FilterCriteria("rp2").byRepositoryPath("system/rules/pushBtn1/R"))
                .addCriteria(new FilterCriteria("rp3").byRepositoryPath("system/rules/pushBtn2/L"))
                .addCriteria(new FilterCriteria("rp4").byRepositoryPath("system/rules/pushBtn2/R"))
                .addCriteria(new FilterCriteria("rp5").byRepositoryPath("system/rules/insertCoin/L"))
                .addCriteria(new FilterCriteria("rp6").byRepositoryPath("system/rules/insertCoin/R"))
                .addCriteria(new FilterCriteria("rp7").byRepositoryPath("system/rules/giveTea/L"))
                .addCriteria(new FilterCriteria("rp8").byRepositoryPath("system/rules/giveTea/R"))
                .addCriteria(new FilterCriteria("rp9").byRepositoryPath("system/rules/giveCoffee/L"))
                .addCriteria(new FilterCriteria("rp10").byRepositoryPath("system/rules/giveCoffee/R"))
                // refills
                .addCriteria(new FilterCriteria("rp11").byRepositoryPath("system/rules/refillTea/L"))
                .addCriteria(new FilterCriteria("rp12").byRepositoryPath("system/rules/refillTea/R"))
                .addCriteria(new FilterCriteria("rp13").byRepositoryPath("system/rules/refillTea2/L"))
                .addCriteria(new FilterCriteria("rp14").byRepositoryPath("system/rules/refillTea2/R"))
                .addCriteria(new FilterCriteria("rp15").byRepositoryPath("system/rules/refillTea3/L"))
                .addCriteria(new FilterCriteria("rp16").byRepositoryPath("system/rules/refillTea3/R"))
                //
                .addCriteria(new FilterCriteria("rp17").byRepositoryPath("system/rules/refillCoffee/L"))
                .addCriteria(new FilterCriteria("rp18").byRepositoryPath("system/rules/refillCoffee/R"))
                .addCriteria(new FilterCriteria("rp19").byRepositoryPath("system/rules/refillCoffee2/L"))
                .addCriteria(new FilterCriteria("rp20").byRepositoryPath("system/rules/refillCoffee2/R"))
                .addCriteria(new FilterCriteria("rp21").byRepositoryPath("system/rules/refillCoffee3/L"))
                .addCriteria(new FilterCriteria("rp22").byRepositoryPath("system/rules/refillCoffee3/R"))
                .restrict(CDOSessionInvalidationEvent.class);
        template.addListener(filter, listener);
    }

    private void loadRulesFromFileSystem() throws IOException, InvalidReactionRuleException {
        String prefixPath = "models/rules/";
        String suffixPath = ".xmi";
        DefaultDynamicSignature sig = vmSyntax.sig();
        EPackage mm = BigraphFactory.createOrGetBigraphMetaModel(sig);
        for (String eachRuleName : ruleNames) {
//            URL ruleLeft = ResourceLoader.getResourceURL(prefixPath + eachRuleName + "L" + suffixPath);
//            URL ruleRight = ResourceLoader.getResourceURL(prefixPath + eachRuleName + "R" + suffixPath);
            InputStream rsLeft = ResourceLoader.getResourceStream(prefixPath + eachRuleName + "L" + suffixPath);
            InputStream rsRight = ResourceLoader.getResourceStream(prefixPath + eachRuleName + "R" + suffixPath);

            List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(mm, rsLeft);
            List<EObject> eObjects1 = BigraphFileModelManagement.Load.bigraphInstanceModel(mm, rsRight);

//            List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(ruleLeft.getPath());
//            List<EObject> eObjects1 = BigraphFileModelManagement.Load.bigraphInstanceModel(ruleRight.getPath());
            PureBigraph left = BigraphUtil.toBigraph(eObjects.get(0).eClass().getEPackage(), eObjects.get(0), sig);
            PureBigraph right = BigraphUtil.toBigraph(eObjects1.get(0).eClass().getEPackage(), eObjects1.get(0), sig);
            ParametricReactionRule<PureBigraph> reactionRule = new ParametricReactionRule<>(left, right);
            ruleMap.put(eachRuleName, reactionRule);
        }
    }

    private void storeRulesInCdo() {
        for (Map.Entry<String, ReactionRule<PureBigraph>> eachEntry : ruleMap.entrySet()) {
            String ruleName = eachEntry.getKey();
            ReactionRule<PureBigraph> rule = eachEntry.getValue();
            template.insert(rule.getRedex().getInstanceModel(), "/system/rules/" + ruleName + "/L");
            template.insert(rule.getReactum().getInstanceModel(), "/system/rules/" + ruleName + "/R");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        System.out.println("Changed property: " + event.getPropertyName() + " [old -> "
                + event.getOldValue() + "] | [new -> " + event.getNewValue() + "]");
        String propertyName = event.getPropertyName();
        for (String ruleName : ruleNames) {
            if (propertyName.contains(ruleName)) {
                ReactionRule<PureBigraph> reactionRuleOld = ruleMap.get(ruleName);
                try {
                    // Update either the redex (L) or reactum (R)
                    if (propertyName.endsWith("L")) {
                        ParametricReactionRule<PureBigraph> reactionRuleNew = new ParametricReactionRule<>(
                                (PureBigraph) event.getNewValue(),
                                (PureBigraph) reactionRuleOld.getReactum(),
                                reactionRuleOld.getInstantationMap()
                        );

                        ruleMap.put(ruleName, reactionRuleNew);
                    } else if (propertyName.endsWith("R")) {
                        ParametricReactionRule<PureBigraph> reactionRuleNew = new ParametricReactionRule<>(
                                (PureBigraph) reactionRuleOld.getRedex(),
                                (PureBigraph) event.getNewValue(),
                                reactionRuleOld.getInstantationMap()
                        );
                        ruleMap.put(ruleName, reactionRuleNew);
                    }
                } catch (InvalidReactionRuleException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
