package com.ricky.common.sensitive.domain;

import com.ricky.common.sensitive.domain.filter.SensitiveWordFilter;
import com.ricky.common.sensitive.domain.filter.impl.ACFilter;
import com.ricky.common.sensitive.domain.filter.impl.ACProFilter;
import com.ricky.common.sensitive.domain.filter.impl.DFAFilter;
import lombok.Getter;

@Getter
public enum AlgorithmStrategy {

    AC(ACFilter.getInstance()),
    AC_PRO(ACProFilter.getInstance()),
    DFA(DFAFilter.getInstance()),
    ;

    final SensitiveWordFilter filter;

    AlgorithmStrategy(SensitiveWordFilter filter) {
        this.filter = filter;
    }
}
