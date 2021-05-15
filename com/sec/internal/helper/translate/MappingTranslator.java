package com.sec.internal.helper.translate;

import com.sec.internal.helper.Preconditions;
import java.util.HashMap;
import java.util.Map;

public class MappingTranslator<TranslatedType, ResultType> {
    private final Map<TranslatedType, ResultType> mMapping;

    private MappingTranslator(Map<TranslatedType, ResultType> mapping) {
        this.mMapping = mapping;
    }

    public ResultType translate(TranslatedType value) throws TranslationException {
        if (value == null) {
            return null;
        }
        if (this.mMapping.containsKey(value)) {
            return this.mMapping.get(value);
        }
        throw new TranslationException(value.toString());
    }

    public boolean isTranslationDefined(TranslatedType value) {
        return this.mMapping.containsKey(value);
    }

    public static class Builder<TranslatedType, ResultType> {
        private final Map<TranslatedType, ResultType> mMapping = new HashMap();

        public Builder<TranslatedType, ResultType> map(TranslatedType translatedValue, ResultType resultValue) throws IllegalArgumentException, IllegalStateException {
            Preconditions.checkNotNull(translatedValue, "translatedValue can't be NULL");
            Preconditions.checkState(!this.mMapping.containsKey(translatedValue));
            this.mMapping.put(translatedValue, resultValue);
            return this;
        }

        public MappingTranslator<TranslatedType, ResultType> buildTranslator() {
            Preconditions.checkState(!this.mMapping.isEmpty());
            return new MappingTranslator<>(this.mMapping);
        }
    }

    public static class TranslationException extends RuntimeException {
        private static final long serialVersionUID = 1;

        public TranslationException(Object value) {
            super("Could not find translation for: " + value);
        }

        public TranslationException(Object value, Throwable t) {
            super("Could not find translation for: " + value, t);
        }
    }
}
