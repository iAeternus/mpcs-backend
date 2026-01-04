package com.ricky.common.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className SystemProperties
 * @desc 全文档系统的设置，由管理员进行配置
 */
@Data
@Validated
public class SystemProperties {

    /**
     * 是否启用流控
     */
    private Boolean enableLimitRate = false;

    @NotBlank
    private String baseDomainName;

}
