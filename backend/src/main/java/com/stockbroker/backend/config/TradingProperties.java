package com.stockbroker.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable trading/settlement/tax rates (SRS FR7/FR10). These are
 * publicly-known regulatory rate structures approximated for this system,
 * not sourced from anything confidential - they are exposed as properties
 * (application.properties, prefix "trading.") precisely so they can be
 * corrected/tuned without a code change, rather than trusted as exact
 * production figures.
 */
@Component
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

    private double brokerageRate = 0.0003;
    private double gstRate = 0.18;
    private double sttRate = 0.001;
    private double exchangeChargeRate = 0.0000325;
    private double stampDutyRate = 0.00015;

    private double marginCallThreshold = 0.80;
    private double squareOffThreshold = 0.90;

    private double ltcgExemption = 125000.0;
    private double ltcgTaxRate = 0.125;
    private double stcgTaxRate = 0.20;

    private int longTermHoldingDays = 365;

    public double getBrokerageRate() {
        return brokerageRate;
    }

    public void setBrokerageRate(double brokerageRate) {
        this.brokerageRate = brokerageRate;
    }

    public double getGstRate() {
        return gstRate;
    }

    public void setGstRate(double gstRate) {
        this.gstRate = gstRate;
    }

    public double getSttRate() {
        return sttRate;
    }

    public void setSttRate(double sttRate) {
        this.sttRate = sttRate;
    }

    public double getExchangeChargeRate() {
        return exchangeChargeRate;
    }

    public void setExchangeChargeRate(double exchangeChargeRate) {
        this.exchangeChargeRate = exchangeChargeRate;
    }

    public double getStampDutyRate() {
        return stampDutyRate;
    }

    public void setStampDutyRate(double stampDutyRate) {
        this.stampDutyRate = stampDutyRate;
    }

    public double getMarginCallThreshold() {
        return marginCallThreshold;
    }

    public void setMarginCallThreshold(double marginCallThreshold) {
        this.marginCallThreshold = marginCallThreshold;
    }

    public double getSquareOffThreshold() {
        return squareOffThreshold;
    }

    public void setSquareOffThreshold(double squareOffThreshold) {
        this.squareOffThreshold = squareOffThreshold;
    }

    public double getLtcgExemption() {
        return ltcgExemption;
    }

    public void setLtcgExemption(double ltcgExemption) {
        this.ltcgExemption = ltcgExemption;
    }

    public double getLtcgTaxRate() {
        return ltcgTaxRate;
    }

    public void setLtcgTaxRate(double ltcgTaxRate) {
        this.ltcgTaxRate = ltcgTaxRate;
    }

    public double getStcgTaxRate() {
        return stcgTaxRate;
    }

    public void setStcgTaxRate(double stcgTaxRate) {
        this.stcgTaxRate = stcgTaxRate;
    }

    public int getLongTermHoldingDays() {
        return longTermHoldingDays;
    }

    public void setLongTermHoldingDays(int longTermHoldingDays) {
        this.longTermHoldingDays = longTermHoldingDays;
    }
}
