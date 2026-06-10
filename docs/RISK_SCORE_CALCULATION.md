# Risk Score Calculation

**Last Updated:** June 9, 2026

Complete documentation of how risk scores are calculated for triggered fraud transactions.

---

## Overview

Each fraud rule calculates a **risk score** (0-100) when triggered. The score reflects the severity of the suspicious behavior detected. Higher scores indicate more serious fraud indicators.

---

## Rule-Specific Calculations

### 1. Amount Threshold (50-100)

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

### 2. Velocity (60-100)

**Triggers when:** Customer makes multiple transactions within a time window

**Formula:**
```
Base Score: 60
Additional: min(40, excess_count × 10)
Final Score: min(100, Base + Additional)
```

**Example:**
- Threshold: 5 transactions in 10 minutes
- Actual: 8 transactions
- Excess: 3 transactions
- Calculation: 60 + (3 × 10) = 90

**Logic:** Exceeding velocity thresholds indicates rapid, potentially automated fraud behavior. Each extra transaction adds 10 points.

**Code:** `VelocityRuleEvaluator.java`

---

### 3. Geographic Anomaly (Fixed: 80)

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

### 6. Amount Range (Fixed: 70)

**Triggers when:** Transaction amount falls within suspicious range

**Score:** Always **70**

**Example Patterns:**
- R9,000 - R9,999 (just below R10,000 reporting threshold)
- R4,500 - R4,999 (structuring pattern)

**Logic:** Amounts consistently just below regulatory reporting thresholds indicate deliberate structuring to evade detection.

**Code:** `AmountRangeRuleEvaluator.java`

---

### 7. Dormant Account (40-100)

**Triggers when:** Previously inactive account suddenly becomes active

**Formula:**
```
Base Score: 40
Additional: Scaled based on dormancy period
```

**Example:**
- Account dormant for 30 days: Score ~50
- Account dormant for 90 days: Score ~65
- Account dormant for 180+ days: Score ~80-90

**Logic:** Longer dormancy periods followed by sudden activity often indicate compromised accounts.

**Code:** `DormantAccountRuleEvaluator.java`

---

## Multiple Rule Triggers

### When Transaction Triggers Multiple Rules

If a single transaction triggers multiple rules, **each rule generates its own triggered_transaction record** with its own risk score.

**Example:**

Transaction: R25,000 from Russia, 8th transaction in 10 minutes

| Rule | Type | Score | Reason |
|------|------|-------|--------|
| Large Transaction Alert | Amount Threshold | 85 | Exceeded R20,000 threshold |
| High-Risk Country | Geographic Anomaly | 80 | Transaction from Russia |
| Rapid Velocity | Velocity | 90 | 8 transactions in 10 minutes (threshold: 5) |

**Database Records:** 3 separate entries in `triggered_transactions` table

---

## Dashboard Aggregations

### Average Risk Score

The dashboard displays the **average of all risk scores** across all triggered transactions:

```sql
SELECT AVG(risk_score) 
FROM triggered_transactions 
WHERE risk_score IS NOT NULL
```

**Current System:** ~83 average (high-risk system)

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
| **41-60** | Medium | Review within 24h | Moderate velocity, medium amounts |
| **61-80** | High | Investigate within 4h | High-risk countries, suspicious patterns |
| **81-100** | Critical | Immediate action | Multiple rule triggers, extreme patterns |

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

**Files:**
- `AmountThresholdRuleEvaluator.java` - Lines 62-69
- `VelocityRuleEvaluator.java` - Lines 82-86
- `GeographicAnomalyRuleEvaluator.java` - Line 52 (fixed score)
- `MerchantRiskRuleEvaluator.java` - Category mapping
- `RapidFireRuleEvaluator.java` - Similar to velocity
- `AmountRangeRuleEvaluator.java` - Fixed score
- `DormantAccountRuleEvaluator.java` - Dormancy scaling

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

**Last Reviewed:** June 9, 2026  
**Reviewed By:** Development Team
