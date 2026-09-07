package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.ClientAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {

    Optional<ClientAccount> findByClientId(Long clientId);

}
