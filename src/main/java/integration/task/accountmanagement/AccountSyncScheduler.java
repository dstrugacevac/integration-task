package integration.task.accountmanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AccountSyncScheduler {

    private final AccountService accountService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void syncAccounts() {
        accountService.syncAccounts();
    }
}
