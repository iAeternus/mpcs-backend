package com.ricky.revision.domain;

public enum RevisionSource {
    MANUAL_SAVE,
    AUTO_SAVE,
    RESTORE;

    public static RevisionSource from(String value) {
        return RevisionSource.valueOf(value);
    }
}
