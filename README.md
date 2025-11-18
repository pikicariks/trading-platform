# Trading Platform - Real-Time Trading Simulation

A microservices-based trading platform built with Spring Boot, Angular, and PostgreSQL.

## Architecture

- **Backend**: Java 22, Spring Boot 3.5.7, Spring Cloud
- **Frontend**: Angular
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Containerization**: Docker

## Microservices

1. Config Server (8888) - Centralized configuration
2. Eureka Server (8761) - Service discovery
3. API Gateway (8080) - Single entry point
4. User Service (8081) - Authentication & user management
5. Market Data Service (8082) - Stock prices & data
6. Order Service (8083) - Buy/sell orders
7. Portfolio Service (8084) - Portfolio tracking
8. Notification Service (8085) - Alerts & notifications
9. Wallet Service (8086) - Virtual wallet management

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker Desktop
- IntelliJ IDEA

### Running the Application

1. Start infrastructure:
```bash
   cd docker
   docker-compose up -d
```

2. Start services in order:
    - Config Server
    - Eureka Server
    - API Gateway
    - Other services

### Access Points

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Config Server: http://localhost:8888

## Development

This is a portfolio project demonstrating:
- Microservices architecture
- Event-driven design
- Real-time data streaming
- JWT authentication
- API Gateway pattern
- Service discovery
- Distributed caching