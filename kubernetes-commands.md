# Commands run during moving the infra from docker to k8s

## Account Service (same for other services)
1. kind load docker-image infrastructure-account-service:latest --name payment-cluster
2. kubectl create namespace banking-platform
3. kubectl apply -f namespace.yml 
4. kubectl apply -f deployment.yml
5. kubectl get deployments -n banking-platform
6. kubectl apply -f service.yml
7. kubectl get service -n banking-platform

## Postgres
1. kubectl apply -f pvc.yml
2. kubectl get pvc -n banking-platform
3. kubectl apply -f deployment.yml
4. kubectl apply -f service.yml

## Getting inside the service
1. kubectl -n banking-platform exec -it pods/payment-service-7d867b6c78-9kqtz -- sh
2. wget -qO- http://fraud-service:8082/actuator/health (checking connectivity with fraud service)