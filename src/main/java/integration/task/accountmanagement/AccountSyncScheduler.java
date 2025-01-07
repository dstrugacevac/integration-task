package integration.task.accountmanagement;

import integration.task.salesforce.SalesforceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AccountSyncScheduler {

    private final AccountRepository accountRepository;
    private final SalesforceService salesforceClient;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void syncAccounts() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Account> accounts = accountRepository.findAllModifiedAfter(oneHourAgo);
        List<Account> accountsForProcess = accounts.stream().filter(Account::hasValidEmail).toList();

        Map<Boolean, List<Account>> partitionedAccounts = accountsForProcess.stream()
                .collect(Collectors.partitioningBy(account -> account.getSalesforceId() == null));

        List<Account> newAccounts = partitionedAccounts.get(true);
        List<Account> existingAccounts = partitionedAccounts.get(false);

        List<Account> createdAccounts = salesforceClient.saveAccounts(newAccounts);
        List<Account> updatedAccounts = salesforceClient.updateAccounts(existingAccounts);

        accountRepository.saveAll(createdAccounts);
    }
}
