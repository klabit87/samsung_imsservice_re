package com.sec.internal.ims.servicemodules.ss;

import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import java.util.ArrayList;
import java.util.List;

public class CallBarringData extends SsRuleData {

    static class Rule extends SsRuleData.SsRule {
        List<ActionElm> actions = new ArrayList();
        boolean allow;
        List<String> target = new ArrayList();

        Rule() {
        }
    }

    /* access modifiers changed from: package-private */
    public Rule getRule(int condition, MEDIA media) {
        Rule rule = (Rule) findRule(condition, media);
        if (rule != null) {
            return rule;
        }
        return makeRule(condition, media);
    }

    static Rule makeRule(int condition, MEDIA media) {
        Rule temp = new Rule();
        makeInternalRule(temp, condition, media);
        temp.allow = false;
        return temp;
    }

    /* access modifiers changed from: package-private */
    public void copyRule(SsRuleData.SsRule ssrule) {
        Rule rule = (Rule) ssrule;
        Rule temp = new Rule();
        temp.allow = rule.allow;
        temp.target.addAll(rule.target);
        temp.actions.addAll(rule.actions);
        super.copySsRule(rule, temp);
    }

    public final CallBarringData clone() {
        CallBarringData clone = new CallBarringData();
        cloneSsDataInternal(clone);
        return clone;
    }
}
