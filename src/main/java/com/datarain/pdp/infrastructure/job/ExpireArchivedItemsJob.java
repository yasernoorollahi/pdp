package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.item.entity.Item;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class ExpireArchivedItemsJob extends AbstractMonitoredJob {

    private final ItemRepository itemRepository;

    public ExpireArchivedItemsJob(ItemRepository itemRepository, JobMonitoringService jobMonitoringService) {
        super(jobMonitoringService);
        this.itemRepository = itemRepository;
    }

//    @Scheduled(cron = "0 0 3 * * ?")//every night at 3 am
    @Scheduled(cron = "0 */1 * * * ?") //every 1 min
//    @Scheduled(fixedRate = 10000)
    public void purgeOldArchivedItems() {
        executeMonitored("ExpireArchivedItemsJob", () -> {
            log.info("Expire job running...");
            Instant cutoff = Instant.now().minusSeconds(30L * 24 * 60 * 60);

            List<Item> old = itemRepository
                    .findByStatusAndArchivedAtBefore(ItemStatus.ARCHIVED, cutoff);

            itemRepository.deleteAll(old);
            return old.size();
        });
    }
}
