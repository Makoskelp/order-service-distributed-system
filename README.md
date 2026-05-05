HayBale - Distributed Food Delivery Platform

Requirements

Docker Desktop (with Docker Compose)
Ports 3000, 8080, 8081, 8082, 8083, 8761, 8161, 61616 must be free

Steps

1. Clone the repository and navigate to the project folder

2. Build and start all services
   docker-compose up --build -d

3. Scale the Order Service to two instances
   docker-compose up --scale order-service=2 -d

4. Wait around 30 seconds for all services to finish starting

5. Open the frontend at http://localhost:3000

6. Eureka dashboard (service registry) is at http://localhost:8761

7. ActiveMQ management console is at http://localhost:8161
   Username: artemis, Password: artemis

To stop everything
   docker-compose down
