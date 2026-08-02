package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByClientIdOrderByTransactionDateDesc(Long clientId);

}