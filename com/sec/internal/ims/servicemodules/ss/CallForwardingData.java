package com.sec.internal.ims.servicemodules.ss;

import com.sec.internal.ims.servicemodules.ss.SsRuleData;

public class CallForwardingData extends SsRuleData {
    int replyTimer;

    static class Rule extends SsRuleData.SsRule {
        ForwardTo fwdElm = new ForwardTo();

        Rule() {
        }

        public void clear() {
            this.conditions = new Condition();
            this.fwdElm = new ForwardTo();
        }
    }

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
        temp.fwdElm = new ForwardTo();
        return temp;
    }

    /* access modifiers changed from: package-private */
    public void copyRule(SsRuleData.SsRule ssrule) {
        Rule rule = (Rule) ssrule;
        Rule temp = new Rule();
        temp.fwdElm = new ForwardTo();
        temp.fwdElm.target = rule.fwdElm.target;
        temp.fwdElm.fwdElm.addAll(rule.fwdElm.fwdElm);
        super.copySsRule(rule, temp);
    }

    public final CallForwardingData clone() {
        CallForwardingData clone = new CallForwardingData();
        cloneSsDataInternal(clone);
        clone.replyTimer = this.replyTimer;
        return clone;
    }
}
