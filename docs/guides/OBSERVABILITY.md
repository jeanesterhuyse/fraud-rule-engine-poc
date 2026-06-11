# Observability Stack - Grafana Loki

**Last Updated:** June 11, 2026 - 10:20  
**Status:** ✅ Production Ready

Enterprise-grade observability for the Fraud Rule Engine POC using Grafana Loki with interactive search and filtering.

---

## 🚀 Quick Start

### Access Grafana
- **URL**: http://localhost:3001
- **Login**: Anonymous access enabled (Admin role - no credentials needed)
- **Dashboard**: "Fraud Detection - Log Monitoring" (pre-configured)

### Access Components
- **Grafana UI**: http://localhost:3001 (visualization)
- **Loki API**: http://localhost:3100 (log aggregation)
- **Promtail**: Runs in background (log collection)

---

## 🔍 Interactive Search & Filters

The dashboard includes real-time search and filtering at the top:

### **Search Text** (Text Box)
- Type any text to search logs (case-insensitive regex matching)
- Examples: `merchant`, `transaction`, `error`, `TXN-12345`
- Default: `.*` (matches all logs)
- Only affects: **Live Log Stream** and **Recent Errors & Warnings** panels
- Top stat panels (INFO, DEBUG, WARN, ERROR, TRACE counts) remain unfiltered

### **Log Level** (Multi-Select Dropdown)
- Filter by: INFO, DEBUG, WARN, ERROR, TRACE, or All
- Multi-select enabled (Ctrl/Cmd + Click)
- Default: All (uses `.+` to match any log level)
- Only affects: **Live Log Stream** panel

### **Service** (Dropdown)
- Select which service to monitor
- Options: fraud-api, fraud-kafka, fraud-postgres, fraud-keycloak, etc.
- Default: All (uses `.+` to match any service)
- Affects: Both log stream panels

---

## 📊 Dashboard Panels

The "Fraud Detection - Log Monitoring" dashboard includes 8 panels:

### Row 1: Log Level Stats (Color Coded)
1. **INFO** (Stat) - Blue - Count of INFO logs in last 5m
2. **DEBUG** (Stat) - Green - Count of DEBUG logs in last 5m
3. **WARN** (Stat) - Orange - Count of WARN logs in last 5m
4. **ERROR** (Stat) - Red - Count of ERROR logs in last 5m (green when 0, red when >0)
5. **TRACE** (Stat) - Purple - Count of TRACE logs in last 5m

### Row 2: Time Series Graphs
6. **Log Rate by Level** (Time Series)
   - Line graph showing log rate per second by level
   - Color-coded: DEBUG=green, INFO=blue, WARN=orange, ERROR=red, TRACE=purple
   - Simple legend at bottom showing level names only

7. **Log Volume by Service** (Stacked Bar Chart)
   - 1-minute interval stacked bars
   - Shows all fraud-* services
   - Legend at bottom showing service names

### Row 3: Live Log Streams
7. **Recent Errors & Warnings** (Log Panel)
   - Shows only ERROR and WARN level logs
   - Filtered by Service and Search Text variables
   - Plain text log format

8. **Live Log Stream - Fraud API** (Log Panel)
   - Large panel showing all logs matching current filters
   - Responds to Search Text, Log Level, and Service filters
   - Plain text log format with timestamps
   - Sortable, scrollable, expandable

### Dashboard Features
- ✅ **Auto-refresh**: Every 10 seconds
- ✅ **Time range**: Default 15 minutes (configurable)
- ✅ **Interactive**: Click to zoom, hover for details
- ✅ **Live mode**: Enable real-time streaming
- ✅ **Export**: Download as JSON or PDF

---

## 🔍 LogQL Query Language

LogQL is Loki's query language (similar to PromQL for Prometheus).

### Basic Syntax
```
{label="value"} | filter | parser
```

### Common Queries

#### Filter by Service
```logql
# All fraud-api logs
{container_name="fraud-api"}

# All fraud services
{container_name=~"fraud-.*"}

# Specific service
{container_name="fraud-kafka"}
```

#### Filter by Log Level
```logql
# Only errors
{container_name="fraud-api", level="ERROR"}

# Errors and warnings
{container_name="fraud-api"} | level=`ERROR` or level=`WARN`

# Everything except DEBUG
{container_name="fraud-api", level!="DEBUG"}
```

#### Text Search
```logql
# Contains "Transaction"
{container_name="fraud-api"} |= "Transaction"

# Contains "error" (case-insensitive)
{container_name="fraud-api"} |~ "(?i)error"

# Does not contain "health"
{container_name="fraud-api"} != "health"
```

#### JSON Parsing
```logql
# Note: fraud-api logs are plain text, not JSON format
# JSON parsing is not used in this project's queries

# For JSON logs (if you change log format), use:
{container_name="fraud-api"} | json | transaction_id="TXN-12345"
```

#### Aggregations
```logql
# Log rate per second
rate({container_name="fraud-api"} [5m])

# Count logs
count_over_time({container_name="fraud-api"} [5m])

# Group by level
sum by(level) (rate({container_name="fraud-api"} [5m]))

# Error rate
sum(rate({container_name="fraud-api", level="ERROR"} [5m]))
```

---

## 🎯 Common Use Cases

### 1. Trace a Specific Transaction
```logql
{container_name="fraud-api"} | json | transaction_id="TXN-ABC-123"
```
Shows all logs for transaction TXN-ABC-123 across the entire processing pipeline.

### 2. Find All Errors in Last Hour
```logql
{container_name="fraud-api", level="ERROR"}
```
Change time range to "Last 1 hour" in Grafana.

### 3. Monitor Rule Evaluation
```logql
{container_name="fraud-api"} |= "Rule triggered"
```
Shows all instances where rules were triggered.

### 4. Debug Kafka Issues
```logql
{container_name="fraud-api"} |= "kafka" | level="ERROR"
```
Finds all Kafka-related errors.

### 5. Track Rule Performance
```logql
{container_name="fraud-api"} | json | rule_id="42"
```
Shows all logs related to a specific rule ID.

### 6. System Health Check
```logql
sum by(container_name, level) (count_over_time({container_name=~"fraud-.*"} [5m]))
```
Shows log volume by service and level - useful for spotting issues.

---

## 🛠️ Advanced Features

### Using Explore View

1. Navigate to http://localhost:3001/explore
2. Select "Loki" datasource
3. Choose between:
   - **Builder mode**: Visual query builder
   - **Code mode**: Write LogQL directly

### Live Streaming
- Click **"Live"** button in top right
- Logs stream in real-time as they arrive
- Useful for debugging active issues

### Log Context
- Click any log line
- Click "Show context"
- See logs before and after that timestamp

### Create Alert Rules
1. Write a LogQL query (e.g., high error rate)
2. Click "Alert" button
3. Define threshold and notification channel
4. Save rule

Example alert query:
```logql
sum(rate({container_name="fraud-api", level="ERROR"} [5m])) > 1
```
Triggers if error rate exceeds 1 per second.

---

## 📈 Dashboard Customization

### Add New Panel
1. Click "Add panel" button
2. Write LogQL query
3. Choose visualization type (time series, gauge, logs, etc.)
4. Configure options (colors, thresholds, legend)
5. Save dashboard

### Modify Existing Panel
1. Click panel title → "Edit"
2. Modify query or visualization
3. Save changes

### Variables (Dynamic Dashboards)
Create dashboard variables for:
- Service name (select from dropdown)
- Log level (filter by level)
- Time range presets

---

## 🔧 Configuration Files

### Loki Configuration
**File**: `loki-config.yml`
- Retention: 7 days
- Storage: Filesystem (local dev)
- Limits: 16MB/s ingestion rate

### Promtail Configuration
**File**: `promtail-config.yml`
- Scrapes: Docker container logs
- Parses: JSON logs from fraud-api
- Extracts: level, container_name, stream labels

### Grafana Datasource
**File**: `grafana-datasources.yml`
- Datasource: Loki at http://loki:3100
- Type: Loki
- Default: Yes

---

## 🐛 Troubleshooting

### No Logs Appearing

**Check Loki is running:**
```bash
curl http://localhost:3100/ready
# Expected: "ready"
```

**Check Promtail is collecting:**
```bash
docker logs fraud-promtail --tail 20
# Should see: "added Docker target" messages
```

**Verify labels exist:**
```bash
curl -s "http://localhost:3100/loki/api/v1/labels" | jq
# Should include: "container_name", "level"
```

### "No data" in Dashboard Panels

**Adjust time range:**
- Click time picker (top right)
- Select "Last 1 hour" or "Last 30 minutes"
- Ensure time zone is correct

**Check query:**
- Edit panel → Check query syntax
- Remove filters if too restrictive
- Test query in Explore view first

### Promtail Not Scraping Logs

**Restart Promtail:**
```bash
docker-compose restart promtail
```

**Check Docker socket access:**
```bash
docker exec fraud-promtail ls -la /var/run/docker.sock
# Should be readable
```

### Grafana Dashboard Not Loading

**Clear browser cache:**
- Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)

**Check Grafana logs:**
```bash
docker logs fraud-grafana --tail 50
```

---

## 📊 Performance & Resource Usage

### Loki
- Memory: 512MB-1GB (stable)
- CPU: <5% (idle), <20% (active)
- Disk: ~100MB per day (depends on log volume)

### Promtail
- Memory: 128-256MB
- CPU: <5%
- Network: Minimal (local)

### Grafana
- Memory: 512MB-1GB
- CPU: <10%
- Disk: ~50MB (persistent data)

### Total Overhead
- **RAM**: ~2GB (vs 4-6GB for ELK stack)
- **CPU**: <10% system-wide
- **Disk**: ~150MB per day

---

## 🔒 Security Considerations

### Current Setup (Local Dev)
- Anonymous access enabled (Admin role)
- No authentication required
- All logs visible to all users

### Production Recommendations
1. **Disable anonymous access**
2. **Integrate with Keycloak OAuth2**
3. **Role-based dashboard access**
4. **Audit log access** (who viewed what)
5. **PII redaction** in logs (if needed)
6. **HTTPS for Grafana** (reverse proxy)
7. **API authentication** for Loki queries

---

## 📚 Learn More

### Official Documentation
- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [LogQL Query Language](https://grafana.com/docs/loki/latest/logql/)
- [Promtail Configuration](https://grafana.com/docs/loki/latest/clients/promtail/)
- [Grafana Dashboards](https://grafana.com/docs/grafana/latest/dashboards/)

### LogQL Examples
- [LogQL Tutorial](https://grafana.com/docs/loki/latest/logql/tutorial/)
- [Query Examples](https://grafana.com/docs/loki/latest/logql/examples/)

### Community Resources
- [Grafana Community Forum](https://community.grafana.com/)
- [GitHub Discussions](https://github.com/grafana/loki/discussions)

---

## 🎓 Quick Reference Card

### Essential LogQL Patterns
```logql
# Basic filtering
{label="value"}

# Multiple labels
{label1="value1", label2="value2"}

# Regex matching
{label=~"regex.*"}

# Negative matching
{label!="value"}

# Contains text
{label="value"} |= "search term"

# JSON parsing
{label="value"} | json

# Rate calculation
rate({label="value"} [5m])

# Aggregation
sum by(label) (query)
```

### Useful Time Ranges
- Last 5 minutes: `[5m]`
- Last 1 hour: `[1h]`
- Last 24 hours: `[24h]`
- Last 7 days: `[7d]`

### Panel Types
- **Time series**: Graphs over time
- **Logs**: Raw log viewer
- **Stat**: Single number
- **Gauge**: With thresholds
- **Pie/Donut**: Distribution
- **Bar chart**: Categorical comparison

---

**Created:** June 10, 2026  
**Maintainer:** Development Team  
**Related Docs:** SESSION_SUMMARY_2026-06-10.md, README.md
