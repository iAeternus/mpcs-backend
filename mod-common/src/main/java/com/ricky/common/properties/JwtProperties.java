package com.ricky.common.properties;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class JwtProperties {

    @NotBlank
    private String issuer;

    @NotBlank
    private String secret;

    @Min(value = 60)
    @Max(value = 43200) // 30天
    private int expire; // 分钟

    @Min(value = 10)
    @Max(value = 2880) // 2天
    private int aheadAutoRefresh; // 分钟

}
