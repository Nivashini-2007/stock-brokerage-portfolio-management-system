package com.stockbroker.backend.entity;

import com.stockbroker.backend.crypto.EncryptedStringConverter;
import com.stockbroker.backend.enums.KycStatus;
import com.stockbroker.backend.enums.RiskProfile;
import com.stockbroker.backend.enums.TradingStatus;
import jakarta.persistence.*;
import lombok.Data;

/**
 * KYC / trading-eligibility profile for a client, separate from User
 * (identity/auth) and from Ledger/Margin (balances) - mirrors SRS Appendix B
 * ClientAccounts table, minus the balance fields already owned by Ledger/Margin.
 */
@Entity
@Table(name = "client_accounts")
@Data
public class ClientAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private User client;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pan_number", length = 512)
    private String panNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "demat_id", length = 512)
    private String demateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradingStatus tradingStatus = TradingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account_number", length = 512)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc")
    private String bankIfsc;
}
