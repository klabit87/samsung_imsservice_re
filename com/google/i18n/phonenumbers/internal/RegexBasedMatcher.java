package com.google.i18n.phonenumbers.internal;

import com.google.i18n.phonenumbers.Phonemetadata;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexBasedMatcher implements MatcherApi {
    private final RegexCache regexCache = new RegexCache(100);

    public static MatcherApi create() {
        return new RegexBasedMatcher();
    }

    private RegexBasedMatcher() {
    }

    public boolean matchNationalNumber(CharSequence number, Phonemetadata.PhoneNumberDesc numberDesc, boolean allowPrefixMatch) {
        String nationalNumberPattern = numberDesc.getNationalNumberPattern();
        if (nationalNumberPattern.length() == 0) {
            return false;
        }
        return match(number, this.regexCache.getPatternForRegex(nationalNumberPattern), allowPrefixMatch);
    }

    private static boolean match(CharSequence number, Pattern pattern, boolean allowPrefixMatch) {
        Matcher matcher = pattern.matcher(number);
        if (!matcher.lookingAt()) {
            return false;
        }
        if (matcher.matches()) {
            return true;
        }
        return allowPrefixMatch;
    }
}
