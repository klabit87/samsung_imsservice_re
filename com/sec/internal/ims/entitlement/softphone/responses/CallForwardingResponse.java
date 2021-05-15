package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CallForwardingResponse extends SoftphoneResponse {
    @SerializedName("@active")
    public String mActive;
    @SerializedName("ss:NoReplyTimer")
    public String mNoReplyTimer;
    @SerializedName("cp:ruleset")
    public Ruleset mRuleset;

    public static class Ruleset {
        @SerializedName("cp:rule")
        public List<Rule> mRules;

        public static class Rule {
            @SerializedName("cp:actions")
            public Action mActions;
            @SerializedName("cp:conditions")
            public Condition mConditions;
            @SerializedName("@id")
            public String mId;

            public static class Action {
                @SerializedName("ss:forward-to")
                public ForwardTo mForwardTo;

                public static class ForwardTo {
                    @SerializedName("ss:target")
                    public String mTarget;

                    public String toString() {
                        return "ForwardTo [mTarget = " + this.mTarget + "]";
                    }
                }

                public String toString() {
                    return "Action [mForwardTo = " + this.mForwardTo + "]";
                }
            }

            public static class Condition {
                @SerializedName("ss:busy")
                public String mBusy;
                @SerializedName("ss:no-answer")
                public String mNoAnswer;
                @SerializedName("ss:not-reachable")
                public String mNotReachable;
                @SerializedName("ss:not-registered")
                public String mNotRegistered;
                @SerializedName("ss:rule-deactivated")
                public String mRuleDeactivated;
                @SerializedName("ss:unconditional")
                public String mUnconditional;

                public String toString() {
                    return "Condition [mRuleDeactivated = " + this.mRuleDeactivated + ", mUnconditional = " + this.mUnconditional + ", mBusy = " + this.mBusy + ", mNoAnswer = " + this.mNoAnswer + ", mNotReachable = " + this.mNotReachable + ", mNotRegistered = " + this.mNotRegistered + "]";
                }
            }

            public String toString() {
                return "Rule [mId = " + this.mId + ", mActions = " + this.mActions + ", mConditions = " + this.mConditions + "]";
            }
        }

        public String toString() {
            return "Ruleset [mRules = " + this.mRules + "]";
        }
    }

    public String toString() {
        return "CallForwardingResponse [mActive = " + this.mActive + ", mNoReplyTimer = " + this.mNoReplyTimer + ", mRuleset = " + this.mRuleset + "]";
    }
}
