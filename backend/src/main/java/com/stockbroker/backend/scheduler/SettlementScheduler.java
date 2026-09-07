package com.stockbroker.backend.scheduler;

import com.stockbroker.backend.entity.TradeSettlement;
import com.stockbroker.backend.repository.TradeSettlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * SRS FR7: T+1 settlement. Ledger balance is already updated immediately
 * at execution for usability in this demo system (see LedgerServiceImpl);
 * this daily job only flips the `settled` marker once settlementDate has
 * passed, so a real overnight batch settlement process can be substituted
 * later without changing the ledger-update path.
 */
@Component
public class SettlementScheduler {

    private static final Logger log = LoggerFactory.getLogger(SettlementScheduler.class);

    private final TradeSettlementRepository tradeSettlementRepository;

    public SettlementScheduler(TradeSettlementRepository tradeSettlementRepository) {
        this.tradeSettlementRepository = tradeSettlementRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void run() {

        try {
            List<TradeSettlement> due = tradeSettlementRepository
                    .findBySettledFalseAndSettlementDateLessThanEqual(LocalDate.now());

            for (TradeSettlement settlement : due) {
                settlement.setSettled(true);
            }

            tradeSettlementRepository.saveAll(due);

        } catch (Exception e) {
            log.error("Settlement scheduler failed", e);
        }
    }
}
