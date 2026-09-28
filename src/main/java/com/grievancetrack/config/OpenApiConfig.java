package com.grievancetrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GrievanceTrack REST API")
                        .version("1.0.0")
                        .description("Citizen Complaint Tracking System with SLA Automation, Automatic Escalation, and Satisfaction Ratings.")
                        .contact(new Contact()
                                .name("Municipal Civic Support Team")
                                .email("civic-support@grievancetrack.gov")
                                .url("https://grievancetrack.gov"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
