package integration.task.accountmanagement;

import integration.task.salesforce.SalesforceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final SalesforceService salesforceClient;


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
