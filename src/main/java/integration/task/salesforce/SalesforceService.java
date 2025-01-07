package integration.task.salesforce;

import com.fasterxml.jackson.databind.ObjectMapper;
import integration.task.accountmanagement.Account;
import integration.task.shared.salesforce.SalesforceConfig;
import integration.task.shared.salesforce.error.SalesforceAuthenticationException;
import integration.task.shared.salesforce.error.SalesforceBatchUpdateException;
import integration.task.shared.salesforce.error.SalesforceJobException;
import integration.task.shared.salesforce.error.SalesforceJsonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceService {
    private static final String API_VERSION = "v60.0";
    private static final String ACCOUNT_OBJECT = "Account";
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private final SalesforceConfig salesforceConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String accessToken;

    public void authenticate() {
        MultiValueMap<String, String> body = buildAuthBody();

        HttpEntity<MultiValueMap<String, String>> request = createAuthRequest(body);

        ResponseEntity<Map> response = restTemplate.exchange(
                salesforceConfig.getLoginUrl(),
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            accessToken = (String) response.getBody().get("access_token");
            log.info("Successfully authenticated with Salesforce");
        } else {
            throw new SalesforceAuthenticationException("Failed to authenticate with Salesforce");
        }
    }

    public List<Account> saveAccounts(List<Account> accounts) {
        if (accounts.isEmpty()) {
            return new ArrayList<>();
        }

        ensureAuthentication();

        String apiUrl = buildApiUrl("/services/data/" + API_VERSION + "/sobjects/" + ACCOUNT_OBJECT);
        HttpHeaders headers = createHeaders();

        for (Account account : accounts) {
            String bodyJson = createJsonBody(account);
            HttpEntity<String> request = new HttpEntity<>(bodyJson, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);
            String id = getIdFromResponse(response);
            account.setSalesforceId(id);
        }

        return accounts;
    }

    public List<Account> updateAccounts(List<Account> accounts) {
        if (accounts.isEmpty()) {
            return new ArrayList<>();
        }

        ensureAuthentication();

        String jobId = startIngestJob();
        putAccountInfoOnJob(accounts, jobId);
        closeJobAndStartProcessing(jobId);

        return accounts;
    }

    public boolean isAccessTokenValid() {
        if (accessToken == null) {
            return false;
        }

        String apiUrl = buildApiUrl("/services/data/" + API_VERSION + "/limits");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return false;
            }
        }
        return false;
    }

    private MultiValueMap<String, String> buildAuthBody() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap();
        params.add("grant_type", "password");
        params.add("username", salesforceConfig.getUsername());
        params.add("password", salesforceConfig.getPassword());
        params.add("client_id", salesforceConfig.getCustomerKey());
        params.add("client_secret", salesforceConfig.getCustomerSecret());
        return params;
    }

    private HttpEntity<MultiValueMap<String, String>> createAuthRequest(MultiValueMap<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(body, headers);
    }

    private String getIdFromResponse(ResponseEntity<Map> response) {
        if (response.getStatusCode() == HttpStatus.CREATED) {
            return (String) response.getBody().get("id");
        } else {
            log.error("Request failed. {}", response);
        }
        return null;
    }

    public String startIngestJob() {
        ensureAuthentication();

        String url = buildApiUrl("/services/data/" + API_VERSION + "/jobs/ingest");

        Map<String, String> body = new HashMap<>();
        body.put("operation", "update");
        body.put("object", ACCOUNT_OBJECT);
        body.put("contentType", "CSV");

        String jsonBody = convertBodyToString(body);
        HttpHeaders headers = createHeaders();

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String jobId = (String) response.getBody().get("id");
            log.info("Ingest job started successfully with ID: {}", jobId);
            return jobId;
        }
        throw new SalesforceJobException("Failed to start ingest job");
    }

    private void putAccountInfoOnJob(List<Account> accounts, String jobId) {
        String url = buildApiUrl("/services/data/" + API_VERSION + "/jobs/ingest/" + jobId + "/batches");
        String jsonBody = createJsonBody(accounts);
        HttpHeaders headers = createCsvHeaders();

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new SalesforceBatchUpdateException("Failed to update batch in Salesforce");
        }
    }

    private void closeJobAndStartProcessing(String jobId) {
        String jobUrl = buildApiUrl("/services/data/" + API_VERSION + "/jobs/ingest/" + jobId);
        Map body = Map.of("state", "UploadComplete");
        String bodyJson = convertBodyToString(body);
        HttpHeaders headers = createHeaders();
        HttpEntity<String> request = new HttpEntity<>(bodyJson, headers);

        restTemplate.exchange(jobUrl, HttpMethod.PATCH, request, Map.class);
        log.info("Job processing started for jobId: {}", jobId);
    }

    private void ensureAuthentication() {
        if (!isAccessTokenValid()) {
            authenticate();
        }
    }

    private String buildApiUrl(String endpoint) {
        return salesforceConfig.getInstanceUrl() + endpoint;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders createCsvHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.parseMediaType(CSV_CONTENT_TYPE));
        return headers;
    }

    private String createJsonBody(Account account) {
        Map<String, String> accountData = new HashMap<>();
        accountData.put("Name", account.getName() + " " + account.getSurname());
        return convertBodyToString(account);
    }

    private String convertBodyToString(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new SalesforceJsonException("Failed to create job close JSON", e);
        }
    }

    private String createJsonBody(List<Account> accounts) {
        StringBuilder json = new StringBuilder("Id,Name\n");
        accounts.forEach(account ->
                json.append(String.format("%s,%s %s\n",
                        account.getSalesforceId(),
                        account.getName(),
                        account.getSurname()
                ))
        );
        return json.toString();
    }
}
