.PHONY: help start stop restart build clean logs health test db-shell kafka-topics kafka-ui backend-logs

# Default target
help:
	@echo "Fraud Rule Engine POC - Make Commands"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  start              Start all services (docker-compose up -d)"
	@echo "  stop               Stop all services"
	@echo "  restart            Restart all services"
	@echo "  build              Build and start all services"
	@echo "  clean              Stop and remove all containers, volumes, and images"
	@echo "  logs               Show logs from all services"
	@echo "  backend-logs       Show logs from backend only"
	@echo "  health             Check health of all services"
	@echo "  test               Run backend tests"
	@echo "  db-shell           Open PostgreSQL shell"
	@echo "  kafka-topics       List Kafka topics"
	@echo "  kafka-ui           Open Kafka UI in browser"
	@echo "  login              Get JWT token (test/test)"
	@echo ""

# Start all services
start:
	docker-compose up -d
	@echo "Services starting... waiting for health checks..."
	@sleep 10
	@make health

# Stop all services
stop:
	docker-compose stop

# Restart all services
restart:
	docker-compose restart

# Build and start
build:
	docker-compose up -d --build
	@echo "Services building and starting... waiting for health checks..."
	@sleep 30
	@make health

# Clean everything
clean:
	docker-compose down -v --rmi all
	@echo "All services, volumes, and images removed"

# Show logs
logs:
	docker-compose logs -f

# Show backend logs only
backend-logs:
	docker-compose logs -f fraud-api

# Check health
health:
	@echo "Checking service health..."
	@docker-compose ps
	@echo ""
	@echo "Backend API health:"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "API not ready yet"

# Run backend tests
test:
	cd fraud-rule-engine-api && mvn test

# PostgreSQL shell
db-shell:
	docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine

# List Kafka topics
kafka-topics:
	docker exec -it fraud-kafka kafka-topics --list --bootstrap-server localhost:9092

# Open Kafka UI
kafka-ui:
	@echo "Opening Kafka UI..."
	@open http://localhost:8090 || xdg-open http://localhost:8090

# Login and get token
login:
	@echo "Logging in as test/test..."
	@curl -s -X POST http://localhost:8080/api/v1/auth/login \
		-H "Content-Type: application/json" \
		-d '{"username":"test","password":"test"}' | jq -r '.token' > .token
	@echo "Token saved to .token file"
	@echo "Use: export TOKEN=\$$(cat .token)"

# Infrastructure only (for local backend development)
infra:
	docker-compose up -d postgres kafka zookeeper kafka-ui
	@echo "Infrastructure started. Run backend locally with:"
	@echo "  cd fraud-rule-engine-api"
	@echo "  mvn spring-boot:run -Dspring-boot.run.profiles=docker"
