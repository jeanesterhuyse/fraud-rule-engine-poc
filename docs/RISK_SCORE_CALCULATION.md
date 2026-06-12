# Risk Score Calculation

**Last Updated:** June 11, 2026

Complete documentation of how risk scores are calculated for triggered fraud transactions.

---

## Overview

Each fraud rule calculates a **risk score** (0-100) when triggered. The score reflects the severity of the suspicious behavior detected. Higher scores indicate more serious fraud indicators.

The system currently implements **12 distinct rule types**, each with its own risk scoring algorithm optimized for the specific fraud pattern it detects.

---

## Rule-Specific Calculations

### 1. Customer Blocklist (Fixed: 100)

**Triggers when:** Transaction involves a customer on the blocklist

**Score:** Always **100** (Maximum severity)

**Example:**
- Customer ID: CUST-BLOCKED-001
- Status: Blocked for previous fraud
- Action: Instant block

**Logic:** Blocklisted customers have confirmed fraud history. Immediate intervention required. Highest possible risk score.

**Code:** `CustomerBlocklistRuleEvaluator.java`

---

### 2. Merchant Blocklist (Fixed: 95)

**Triggers when:** Transaction involves a merchant on the blocklist

**Score:** Always **95** (Near-maximum severity)

**Example:**
- Merchant: "Suspicious Electronics Ltd"
- Status: Known fraudulent merchant
- Action: Instant block

**Logic:** Blocklisted merchants have confirmed fraud activity. Slightly lower than customer blocklist as merchant may have legitimate customers mixed in.

**Code:** `MerchantBlocklistRuleEvaluator.java`

---

### 3. Cross-Border High Risk (Fixed: 90)

**Triggers when:** Customer makes transaction in high-risk foreign country

**Score:** Always **90**

**Example:**
- Customer home: South Africa (ZAF)
- Transaction country: Russia (RUS)
- Action: High-priority review

**Logic:** Cross-border transactions to sanctioned or high-fraud countries represent significant risk, especially combined with geographic anomalies.

**Code:** `CrossBorderHighRiskRuleEvaluator.java`

---

### 4. Amount Threshold (50-100)

**Triggers when:** Transaction amount exceeds configured threshold

**Formula:**
```
Base Score: 50
Additional: min(50, (excess / threshold) × 50)
Final Score: min(100, Base + Additional)
```

**Example:**
- Threshold: R10,000
- Transaction: R15,000
- Excess: R5,000 (50% over threshold)
- Calculation: 50 + (0.5 × 50) = 75

**Logic:** The more the threshold is exceeded, the higher the risk. Score reaches 100 when amount is 2x threshold or more.

**Code:** `AmountThresholdRuleEvaluator.java`

---

### 5. Geographic Anomaly (Fixed: 75)

**Triggers when:** Transaction originates from high-risk country

**Score:** Always **80**

**Example Countries:**
- Russia (RUS)
- North Korea (PRK)
- High-risk regions configured by compliance

**Logic:** Transactions from sanctioned or high-fraud countries are inherently high-risk. Fixed score ensures consistent treatment.

**Code:** `GeographicAnomalyRuleEvaluator.java`

---

### 4. Merchant Risk (Category-Dependent)

**Triggers when:** Transaction involves high-risk merchant category

**Scores by Category:**
- Gambling: **90**
- Cryptocurrency Exchanges: **85**
- Money Transfer Services: **75**
- Adult Content: **70**
- Other high-risk: **65**

**Logic:** Certain merchant categories are frequently associated with fraud, money laundering, or require enhanced monitoring.

**Code:** `MerchantRiskRuleEvaluator.java`

---

### 5. Rapid Fire (70-100)

**Triggers when:** Multiple transactions in rapid succession (shorter window than velocity)

**Formula:**
```
Base Score: 70
Additional: min(30, excess_count × 8)
Final Score: min(100, Base + Additional)
```

**Example:**
- Threshold: 3 transactions in 30 seconds
- Actual: 6 transactions
- Excess: 3
- Calculation: 70 + (3 × 8) = 94

**Logic:** Extremely rapid transactions often indicate automated bot attacks or card testing. Higher base score than velocity.

**Code:** `RapidFireRuleEvaluator.java`

---

### 9. Amount Range (Fixed: 70)

**Triggers when:** Transaction amount falls within suspicious range

**Score:** Always **70**

**Example Patterns:**
- R9,000 - R9,999 (just below R10,000 reporting threshold)
- R4,500 - R4,999 (structuring pattern)

**Logic:** Amounts consistently just below regulatory reporting thresholds indicate deliberate structuring to evade detection.

**Code:** `AmountRangeRuleEvaluator.java`

---

### 10. Merchant Risk (Fixed: 65)

**Triggers when:** Transaction involves high-risk merchant category

**Score:** Always **65**

**Example Categories:**
- Gambling
- Cryptocurrency Exchanges
- Money Transfer Services
- Adult Content

**Logic:** Certain merchant categories are frequently associated with fraud, money laundering, or require enhanced monitoring.

**Code:** `MerchantRiskRuleEvaluator.java`

---

### 11. Time of Day Anomaly (Fixed: 60)

**Triggers when:** Transaction occurs during unusual hours (2-5 AM)

**Score:** Always **60**

**Example:**
- Transaction time: 03:45 AM
- Configured hours: 2:00 AM - 5:00 AM
- Action: Medium-priority review

**Logic:** Transactions during unusual hours (late night/early morning) may indicate unauthorized access or fraud, especially if inconsistent with customer's normal patterns.

**Code:** `TimeOfDayAnomalyRuleEvaluator.java`

---

### 12. Round Amount (55-65)

**Triggers when:** Large transaction with suspiciously round amount

**Score:** 55-65 (based on roundness and amount)

**Example:**
- R10,000.00 (perfectly round)
- R50,000.00 (perfectly round, large)
- Pattern: Card testing or structuring

**Logic:** Criminals often test stolen cards with round amounts. The larger and rounder the amount, the higher the score.

**Code:** `RoundAmountRuleEvaluator.java`

---

### 13. Currency Mismatch (Fixed: 55)

**Triggers when:** Transaction uses foreign currency in foreign country

**Score:** Always **55**

**Example:**
- Customer home country: ZAF
- Customer home currency: ZAR
- Transaction country: USA
- Transaction currency: EUR (not USD or ZAR)

**Logic:** Using a third currency in a foreign country is unusual and may indicate money laundering or card cloning.

**Code:** `CurrencyMismatchRuleEvaluator.java`

---

### 14. Large Withdrawal (50-80)

**Triggers when:** Large ATM or cash withdrawal

**Formula:**
```
Base Score: 50
Additional: Scaled based on amount over threshold
Final Score: min(80, Base + Additional)
```

**Example:**
- Threshold: R5,000
- Withdrawal: R15,000
- Excess: R10,000 (200% over)
- Score: ~75

**Logic:** Large cash withdrawals, especially at ATMs, may indicate compromised cards or money laundering. Score scales with withdrawal size.

**Code:** `LargeWithdrawalRuleEvaluator.java`

---

## Multiple Rule Triggers

### When Transaction Triggers Multiple Rules

If a single transaction triggers multiple rules, **each rule generates its own triggered_transaction record** with its own risk score.

**Example:**

Transaction: R25,000 from Russia by blocklisted customer

| Rule | Type | Score | Reason |
|------|------|-------|--------|
| Blocked Customer Alert | Customer Blocklist | 100 | Customer on blocklist |
| Cross-Border Alert | Cross-Border High Risk | 90 | ZAF customer in Russia |
| Large Transaction Alert | Amount Threshold | 85 | Exceeded R20,000 threshold |
| High-Risk Country | Geographic Anomaly | 75 | Transaction from Russia |

**Database Records:** 4 separate entries in `triggered_transactions` table

**Combined Risk Interpretation:** Multiple high-scoring rules indicate severe fraud risk requiring immediate intervention.

---

## Dashboard Aggregations

### Average Risk Score

The dashboard displays the **average of all risk scores** across all triggered transactions:

```sql
SELECT AVG(risk_score) 
FROM triggered_transactions 
WHERE risk_score IS NOT NULL
```

**Current System:** Average varies based on active rules and transaction patterns

### Highest Risk Score

Maximum risk score seen:

```sql
SELECT MAX(risk_score) 
FROM triggered_transactions
```

Typically: **100** (critical alerts exist)

### Risk Score by Rule

Average risk score per rule type helps identify which rules catch the most serious fraud:

```sql
SELECT rule_type, AVG(risk_score)
FROM triggered_transactions
GROUP BY rule_type
```

---

## Risk Score Interpretation

| Score Range | Risk Level | Recommended Action | Example Scenarios |
|-------------|------------|-------------------|-------------------|
| **0-40** | Low | Monitor only | Small excess over threshold, minor patterns |
| **41-60** | Medium | Review within 24h | Time of day anomaly, currency mismatch, round amounts |
| **61-80** | High | Investigate within 4h | High-risk countries, CNP fraud, amount range structuring, large withdrawals |
| **81-100** | Critical | Immediate action | Blocklists, cross-border high risk, multiple rule triggers |

**Note:** Scores 95-100 are reserved for the most severe cases (blocklists) requiring instant intervention.

---

## Customizing Risk Scores

Risk score formulas can be adjusted in the rule evaluator classes:

### To Increase Sensitivity (Higher Scores)

1. Increase base score
2. Increase multiplication factor for excess
3. Lower the max cap on additional points

**Example (AmountThresholdRuleEvaluator):**
```java
// Current: 50 + min(50, ratio × 50)
// More sensitive: 60 + min(40, ratio × 60)
```

### To Decrease Sensitivity (Lower Scores)

1. Decrease base score
2. Decrease multiplication factor
3. Raise the minimum threshold before triggering

---

## Risk Score Flow

```
Transaction Received
    ↓
Evaluate Against All Active Rules
    ↓
For Each Triggered Rule:
    ↓
    Calculate Risk Score
    (Based on rule type formula)
    ↓
    Create TriggeredTransaction Record
    (Store: transaction data + risk score)
    ↓
Dashboard/Reports
    ↓
Display: Individual scores, averages, trends
```

---

## Code References

All risk score calculations are in:
```
fraud-rule-engine-api/src/main/java/com/fraud/ruleengine/service/rule/strategy/
```

**Current Evaluators (12 Types):**
1. `CustomerBlocklistRuleEvaluator.java` - Fixed: 100
2. `MerchantBlocklistRuleEvaluator.java` - Fixed: 95
3. `CrossBorderHighRiskRuleEvaluator.java` - Fixed: 90
4. `AmountThresholdRuleEvaluator.java` - Dynamic: 50-100
5. `GeographicAnomalyRuleEvaluator.java` - Fixed: 75
6. `CnpHighRiskRuleEvaluator.java` - Category-based: 60-75
7. `AmountRangeRuleEvaluator.java` - Fixed: 70
8. `MerchantRiskRuleEvaluator.java` - Fixed: 65
9. `TimeOfDayAnomalyRuleEvaluator.java` - Fixed: 60
10. `RoundAmountRuleEvaluator.java` - Dynamic: 55-65
11. `CurrencyMismatchRuleEvaluator.java` - Fixed: 55
12. `LargeWithdrawalRuleEvaluator.java` - Dynamic: 50-80

---

## Historical Context

**Design Decision:** Each rule calculates its own score rather than having a global scoring function. This allows:

1. **Rule-specific logic** - Each fraud pattern has appropriate severity
2. **Independent tuning** - Adjust one rule without affecting others
3. **Audit trail** - Each triggered_transaction shows which rule and score
4. **Flexibility** - Easy to add new rules with custom scoring

**Trade-off:** Multiple records per transaction if multiple rules trigger, but provides complete audit trail.

---

## Future Enhancements

Potential improvements to risk scoring:

1. **Machine Learning Scores** - Augment rule scores with ML model predictions
2. **Dynamic Scoring** - Adjust scores based on time of day, customer history
3. **Composite Scores** - When multiple rules trigger, calculate combined risk
4. **Score Decay** - Reduce risk score over time if no follow-up suspicious activity
5. **Customer Risk Profiles** - Baseline score based on customer behavior patterns

---

**See Also:**
- [Architecture Overview](ARCHITECTURE.md)
- [Rule Types Documentation](../README.md#rule-types)
- [Database Schema](database/SCHEMA.md)

---

**Last Reviewed:** June 11, 2026  
**Reviewed By:** Development Team  
**Rule Count:** 12 distinct rule types with individual risk scoring algorithms
