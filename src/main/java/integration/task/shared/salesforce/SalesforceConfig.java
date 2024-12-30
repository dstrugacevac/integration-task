package integration.task.shared.salesforce;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SalesforceConfig {

    @Value("${salesforce.username}")
    private String username;

    @Value("${salesforce.password}")
    private String password;

    @Value("${salesforce.instanceUrl}")
    private String instanceUrl;

    @Value("${salesforce.loginUrl}")
    private String loginUrl;

    @Value("${salesforce.ck}")
    private String customerKey;

    @Value("${salesforce.cs}")
    private String customerSecret;

    @Value("${salesforce.securityToken}")
    private String securityToken;
}
