package org.example.domain.behavior;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.simulation.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.simulation.matching.pure.PureReactiveSystem;

import java.util.Iterator;

/**
 * The vending machine - a reactive system.
 * It contains the current agent and rules to be evaluated.
 * Further, it defines some rewriting strategies that conform to the analysis.
 *
 * @author Dominik Grzelak
 */
public class VMReactiveSystem extends PureReactiveSystem {

    public PureBigraph execute(int howManyCoins) {

        PureBigraph agent = getAgent();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        PureBigraph agentTmp = getAgent();
        // Coins can be inserted many times
        int cointCounter = howManyCoins;
        while (cointCounter > 0) {
            MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r0"));
            Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
            if (!iterator.hasNext()) {
                break;
            }
            int correct = 1;
            while (iterator.hasNext()) {
                BigraphMatch<PureBigraph> next = iterator.next();
                agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r0"));
                correct--;
            }
            assert correct == 0;
            cointCounter--;
        }

        // Pushing the button shall only be performed once
        MatchIterable<BigraphMatch<PureBigraph>> match2 = matcher.match(agentTmp, getReactionRulesMap().get("r1"));
        Iterator<BigraphMatch<PureBigraph>> iterator2 = match2.iterator();
        if (iterator2.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator2.next();
            agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r1"));
        }
//        try {
//            BigraphGraphvizExporter.toPNG(agentTmp, true, new File("agentTmp.png"));
//            BigraphGraphvizExporter.toPNG(getReactionRulesMap().get("r2").getRedex(), true, new File("redexGiveCoffee.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Produce the resp. item here
        MatchIterable<BigraphMatch<PureBigraph>> match3 = matcher.match(agentTmp, getReactionRulesMap().get("r2"));
        Iterator<BigraphMatch<PureBigraph>> iterator3 = match3.iterator();
        if (iterator3.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator3.next();
            agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r2"));
        }
        if (agent.equals(agentTmp)) return null;
        return agentTmp; // the result
    }

    /**
     * Only executes the first rule added to this reactive system object once.
     * In case the rule match yields multiple occurrences, the first one is also taken as the result.
     *
     * @return the rewritten bigraphical state after the first rule is applied once
     */
    public PureBigraph executeSingleRule() {
        PureBigraph agent = getAgent();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        PureBigraph agentTmp = getAgent();
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r0"));
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();

        if (iterator.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator.next();
            agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r0"));
            if (!agent.equals(agentTmp)) return agentTmp;
        }
        return null;
    }

    public PureBigraph executeConsecutively() {
        PureBigraph agent = getAgent();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        PureBigraph agentTmp = getAgent();
        for (int i = 0; i < getReactionRules().size(); i++) {
            MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r" + i));
            Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
            if (iterator.hasNext()) {
                BigraphMatch<PureBigraph> next = iterator.next();
                agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r" + i));
            } else {
                return null;
            }
        }

        if (!agent.equals(agentTmp)) {
            return agentTmp;
        }
        return null;
    }

    public PureBigraph executeAllRulesUntilOneMatches() {
        PureBigraph agent = getAgent();
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        PureBigraph agentTmp = getAgent();
        for (int i = 0; i < getReactionRules().size(); i++) {
            MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(agentTmp, getReactionRulesMap().get("r" + i));
            Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
            if (iterator.hasNext()) {
                BigraphMatch<PureBigraph> next = iterator.next();
                agentTmp = buildParametricReaction(agentTmp, next, getReactionRulesMap().get("r" + i));
                if (!agent.equals(agentTmp)) return agentTmp;
            }
        }
        return null;
    }
}
