package com.ricky.common.properties;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class CommonProperties {

    private boolean httpsEnabled;

    @NotBlank
    private String baseDomainName;

    private boolean limitRate;

//    @NotBlank
//    private String webhookUserName;
//
//    @NotBlank
//    private String webhookPassword;

//    private boolean webhookAllowLocalhost;

}
