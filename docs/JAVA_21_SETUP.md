# Java 21 Setup Guide

This guide helps you set up Java 21 for the Fraud Rule Engine project, especially if you have multiple Java versions installed.

---

## 🎯 Quick Start (TL;DR)

If you have **Java 21** AND other versions (like Java 25) installed:

```bash
# 1. Check prerequisites (auto-switches to Java 21!)
./check-prerequisites.sh

# 2. Start the project (auto-switches to Java 21!)
./start-dev.sh

# 3. Start the UI (in a new terminal)
cd fraud-rule-engine-ui
npm install  # first time only
npm run dev
```

**That's it!** The scripts automatically switch to Java 21 for you. ✨

---

## ✨ Automatic Java 21 Switching

**Good news!** The project scripts **automatically handle Java 21** for you:

- `./check-prerequisites.sh` - Auto-switches to Java 21 when checking your system
- `./start-dev.sh` - Auto-switches to Java 21 when starting services
- `source ./use-java-21.sh` - Manual helper script if needed

**Just run the scripts normally** and they'll handle the Java version for you!

---

## ❓ Common Issues

### Issue 1: "This project requires Java 21!" error

**Symptoms:**
You run `mvn clean package -DskipTests` and get:
```
[ERROR] Rule 0: org.apache.maven.enforcer.rules.version.RequireJavaVersion failed
[ERROR] This project requires Java 21!
[ERROR] You are currently using: 25 (or other version)
```

**Cause:**
You have multiple Java versions installed. Maven is using a different Java version than expected. This happens because:
- `java -version` might show Java 21
- But `mvn -version` shows Java 25 (or another version)
- Maven uses `JAVA_HOME` environment variable, NOT from PATH

**Solutions (in order of preference):**

#### Option 1: Use our scripts (Recommended)
```bash
# The scripts handle it automatically
./check-prerequisites.sh
./start-dev.sh
```

#### Option 2: Source the helper script
```bash
# Set Java 21 for current terminal session
source ./use-java-21.sh

# Verify it worked
mvn -version | grep "Java version"
# Should show: Java version: 21.x.x

# Now build
cd fraud-rule-engine-api
mvn clean package -DskipTests
```

#### Option 3: One-liner (always works!)
```bash
# macOS
cd fraud-rule-engine-api
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests

# Linux (adjust path to your Java 21 installation)
JAVA_HOME=/usr/lib/jvm/java-21-openjdk mvn clean package -DskipTests
```

This sets `JAVA_HOME` temporarily **only for that one command**, so it always works even if your shell environment is misconfigured.

---

### Issue 2: "Java 21 not found"

**Solution:** Download and install Java 21 from https://adoptium.net/

**macOS (Homebrew):**
```bash
brew install openjdk@21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install openjdk-21-jdk
```

**Linux (RHEL/Fedora):**
```bash
sudo dnf install java-21-openjdk-devel
```

---

### Issue 3: Scripts say Java 21 OK, but Maven still fails

This means Maven is seeing a different Java than your shell.

**Solution:** Use the one-liner command (Option 3 above) which explicitly sets JAVA_HOME for Maven.

---

## 🔧 Making Java 21 Permanent

If you want Java 21 as your default (so you don't need the scripts):

### macOS

1. **Find where Java 21 is installed:**
   ```bash
   /usr/libexec/java_home -V
   ```
   
   Output shows all Java versions, e.g.:
   ```
   21.0.10 (x86_64) "Amazon.com Inc." ...
   25.0.2 (x86_64) "Oracle Corporation" ...
   ```

2. **Add to your shell profile** (`~/.zshrc` for Zsh or `~/.bash_profile` for Bash):
   ```bash
   # Force Java 21 by default
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export PATH=$JAVA_HOME/bin:$PATH
   ```

3. **Reload your shell:**
   ```bash
   source ~/.zshrc  # or source ~/.bash_profile
   ```

4. **Verify:**
   ```bash
   java -version    # Should show: openjdk version "21.x.x"
   mvn -version     # Should show: Java version: 21.x.x
   ```

### Linux

1. **Find Java 21 installation:**
   ```bash
   ls /usr/lib/jvm/
   # Look for: java-21-openjdk or similar
   ```

2. **Set JAVA_HOME in `~/.bashrc` or `~/.zshrc`:**
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
   export PATH=$JAVA_HOME/bin:$PATH
   ```

3. **Or use update-alternatives (Ubuntu/Debian):**
   ```bash
   sudo update-alternatives --config java
   # Select Java 21 from the list
   ```

4. **Reload and verify:**
   ```bash
   source ~/.bashrc
   java -version
   mvn -version
   ```

### Windows

1. **Open System Properties:**
   - Search for "Environment Variables" in Start Menu
   - Click "Environment Variables"

2. **Set JAVA_HOME:**
   - Click "New" under System Variables
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-21` (or wherever Java 21 is installed)

3. **Update PATH:**
   - Find `Path` in System Variables
   - Add: `%JAVA_HOME%\bin` to the **top** of the list

4. **Restart Command Prompt and verify:**
   ```cmd
   java -version
   mvn -version
   ```

---

## 📋 Diagnostic Commands

Run these to understand what's happening:

```bash
# What Java does the shell see?
java -version

# What's JAVA_HOME set to?
echo $JAVA_HOME

# What Java does Maven see?
mvn -version | grep "Java version"

# List all Java installations (macOS)
/usr/libexec/java_home -V

# Run our diagnostic tool
./check-maven-java.sh
```

**Key insight:** If `java -version` shows Java 21 but `mvn -version` shows Java 25, that's your problem!

---

## 🎓 First Time Setup (Step-by-Step)

### Prerequisites
- Docker Desktop installed and running
- Node.js 18+ installed
- Java 21 installed (see installation section above)

### Setup Steps

1. **Check prerequisites:**
   ```bash
   ./check-prerequisites.sh
   ```
   This will:
   - ✅ Check Docker, Java, Maven, Node.js, npm
   - ✅ Auto-switch to Java 21 if needed
   - ✅ Show you what to do next

2. **Start the backend:**
   ```bash
   ./start-dev.sh
   ```
   This will:
   - ✅ Auto-switch to Java 21
   - ✅ Build the backend JAR if not found
   - ✅ Start all Docker services (PostgreSQL, Kafka, Keycloak, API)
   - ✅ Configure Keycloak with test users

3. **Start the frontend** (in a NEW terminal):
   ```bash
   cd fraud-rule-engine-ui
   npm install
   npm run dev
   ```

4. **Open your browser:**
   - Go to: http://localhost:3000/login-keycloak
   - Login: `john.smith` / `FraudDetect123!`

---

## 🆘 Advanced Troubleshooting

### Maven ignores JAVA_HOME

If you've set `JAVA_HOME` but Maven still uses the wrong version:

1. **Check if Maven has a custom Java path:**
   ```bash
   # macOS/Linux
   echo $M2_HOME
   cat $M2_HOME/bin/mvn  # Look for JAVA_HOME override
   ```

2. **Use Maven Wrapper (if available):**
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. **Specify Java explicitly:**
   ```bash
   JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests
   ```

### Using IntelliJ IDEA or Eclipse

IDEs have their own Java SDK configuration separate from terminal:

**IntelliJ IDEA:**
- File → Project Structure → Project SDK → Select Java 21

**Eclipse:**
- Window → Preferences → Java → Installed JREs → Select Java 21

**VS Code:**
- Set `java.configuration.runtimes` in settings.json

---

## ❓ Why Java 21 Specifically?

This project uses:
- **Spring Boot 3.2.5** - requires Java 17+
- **Java 21 features** - Records, Pattern Matching, Virtual Threads
- **Maven Enforcer Plugin** - configured to require Java 21 for consistency

**Java 22+ won't work** because:
- Potential breaking changes
- Dependency incompatibilities
- Not tested/supported

**Java 17-20 won't work** because:
- We use Java 21-specific features
- Spring Boot 3.2.5 is optimized for Java 21

---

## ✅ Verification Checklist

After setup, verify these all work:

- [ ] `java -version` shows Java 21.x.x
- [ ] `mvn -version` shows Java 21.x.x
- [ ] `./check-prerequisites.sh` reports all green
- [ ] `docker ps` shows fraud-postgres, fraud-kafka, fraud-api running
- [ ] http://localhost:8080/actuator/health returns "UP"
- [ ] http://localhost:3000 shows the UI
- [ ] You can login with john.smith / FraudDetect123!

---

## 📚 Additional Resources

- **Main README:** [../README.md](../README.md)
- **Getting Started Guide:** [GETTING_STARTED.md](GETTING_STARTED.md)
- **Adoptium (Java downloads):** https://adoptium.net/
- **Maven documentation:** https://maven.apache.org/

---

## 🔄 Summary

| Method | Scope | Use When |
|--------|-------|----------|
| `./check-prerequisites.sh` | Auto-detects and switches | First time checking system |
| `./start-dev.sh` | Auto-detects and switches | Starting the application |
| `source ./use-java-21.sh` | Current terminal session | Manual control needed |
| `JAVA_HOME=$(...) mvn ...` | Single command | Failsafe / scripting |
| Edit `~/.zshrc` | Permanent (all sessions) | You want Java 21 as default |

**Recommended:** Just use the scripts! They handle everything automatically. 🚀

---

**Need more help?** Check the main [README.md](../README.md) or open an issue.
