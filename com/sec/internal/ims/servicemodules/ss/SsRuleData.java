package com.sec.internal.ims.servicemodules.ss;

import java.util.ArrayList;
import java.util.List;

abstract class SsRuleData implements Cloneable {
    protected boolean active;
    protected List<SsRule> rules = new ArrayList();

    /* access modifiers changed from: protected */
    public abstract SsRuleData clone();

    /* access modifiers changed from: package-private */
    public abstract void copyRule(SsRule ssRule);

    /* access modifiers changed from: package-private */
    public abstract SsRule getRule(int i, MEDIA media);

    static class SsRule {
        Condition conditions = new Condition();
        String ruleId;

        SsRule() {
        }
    }

    SsRuleData() {
    }

    /* access modifiers changed from: package-private */
    public SsRule findRule(int condition, MEDIA media) {
        for (SsRule r : this.rules) {
            if (r.conditions.condition == condition && r.conditions.media.contains(media)) {
                return r;
            }
        }
        return null;
    }

    static void makeInternalRule(SsRule temp, int condition, MEDIA media) {
        temp.conditions.condition = condition;
        temp.conditions.state = false;
        temp.conditions.action = 0;
        temp.conditions.media = new ArrayList();
        temp.conditions.media.add(media);
    }

    /* access modifiers changed from: package-private */
    public void setRule(SsRule remote) {
        for (SsRule r : this.rules) {
            if (r.conditions.condition == remote.conditions.condition && r.conditions.media.equals(remote.conditions.media)) {
                this.rules.remove(r);
                this.rules.add(remote);
                return;
            }
        }
        this.rules.add(remote);
    }

    /* access modifiers changed from: package-private */
    public boolean isExist(int condition, MEDIA media) {
        for (SsRule r : this.rules) {
            if (r.conditions.condition == condition && r.conditions.media.contains(media)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isExist(int condition) {
        for (SsRule r : this.rules) {
            if (r.conditions.condition == condition) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void copySsRule(SsRule original, SsRule copy) {
        copy.ruleId = original.ruleId;
        copy.conditions = new Condition();
        copy.conditions.condition = original.conditions.condition;
        copy.conditions.state = original.conditions.state;
        copy.conditions.action = original.conditions.action;
        copy.conditions.media = new ArrayList();
        copy.conditions.media.addAll(original.conditions.media);
        setRule(copy);
    }

    /* access modifiers changed from: package-private */
    public void cloneSsDataInternal(SsRuleData clone) {
        clone.active = this.active;
        for (SsRule r : this.rules) {
            clone.copyRule(r);
        }
    }
}
