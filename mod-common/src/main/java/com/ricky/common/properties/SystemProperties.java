package com.ricky.common.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className SystemProperties
 * @desc MPCS系统的设置，由管理员进行配置
 */
@Data
@Validated
public class SystemProperties {

    private boolean httpsEnabled;

    @NotBlank
    private String baseDomainName;

    /**
     * 是否启用流控
     */
    private boolean limitRate = false;

    /**
     * 是否启用鉴权
     */
    private boolean auth = false;

}
