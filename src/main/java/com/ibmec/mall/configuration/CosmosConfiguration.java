package com.ibmec.mall.configuration;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCosmosRepositories(basePackages = "com.ibmec.mall.repository")
public class CosmosConfiguration extends AbstractCosmosConfiguration {

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        return new CosmosClientBuilder()
                .endpoint("https://rodriguez-teste-ibmec-cloud.documents.azure.com:443/")
                .key("z72onIkSRxSiQ54fVgEqTGXkPYfYTNaTXhmn7o9gcscgFGtLdu7H1Bh45lJhKW94lobvo8J9jkOyACDbcWo0hA==")
                .directMode(DirectConnectionConfig.getDefaultConfig());
    }

    @Bean
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder().build();
    }

    @Override
    protected String getDatabaseName() {
        return "ibmec-cloud-products";
    }
}
