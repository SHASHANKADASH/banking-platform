*Note: These notes I have generated with the help of AI while I was building this project for my learning.*
*I have created this so that I can have a quick read before interviews.*
*If you want more detailed notes written by me [checkout](https://k8s-notes.pages.dev) *

# Kubernetes Migration — Phase 1 Notes

## 1. Objective
I will be migrating all the services which I was deploying using docker to kubernetes.

**Current Architecture**
```text
                    Kubernetes Cluster
                    banking-platform namespace
                           │
             ┌─────────────┴─────────────┐
             │                           │
      account-service                PostgreSQL
         Service                      Service
             │                           │
             ▼                           ▼
      account-service                PostgreSQL
           Pod                           Pod
                                         │
                                         ▼
                                  postgres-pvc
```

---

## 2. Kubernetes Namespace

**File:** `infrastructure/kubernetes/namespace.yaml`
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: banking-platform
```

**Apply:**
```bash
kubectl apply -f infrastructure/kubernetes/namespace.yaml
```

**Why Namespace?**
* Logical isolation for the application
* Avoids cluttering the `default` namespace
* Better organization as the project grows

**Before:**
```text
default
├── account-service
├── postgres
├── kafka
└── ...
```

**After:**
```text
banking-platform
├── account-service
├── postgres
└── ...
```

**Common Commands:**
```bash
kubectl get pods -n banking-platform
kubectl get all -n banking-platform
```

---

## 3. Kubernetes Folder Structure
```text
infrastructure/
└── kubernetes/
    ├── namespace.yaml
    │
    ├── account-service/
    │   ├── deployment.yaml
    │   └── service.yaml
    │
    └── postgres/
        ├── deployment.yaml
        ├── service.yaml
        └── pvc.yaml
```
> **Key Principle:** Kubernetes configuration belongs in `infrastructure/`, not inside application modules.

---

## 4. Docker Image → Kubernetes

**Local Build Process**
```bash
# Build the Docker image
docker build   -t account-service:1.0   -f account-service/Dockerfile .

# Load into Kind cluster
kind load docker-image account-service:1.0
```

**Why `kind load docker-image`?**
* Kind runs Kubernetes nodes as Docker containers.
* Local images aren't automatically available in the cluster.
* We must explicitly load images into the cluster.

**Image Flow**
```text
Source Code
    ↓
Docker Build
    ↓
account-service:1.0
    ↓
kind load docker-image
    ↓
Kind Cluster
```

**Image Pull Policy**
```yaml
imagePullPolicy: IfNotPresent
```
Since the image is already loaded into the cluster, we use `IfNotPresent` to avoid pulling from a registry.

---

## 5. Deployment

**File:** `infrastructure/kubernetes/account-service/deployment.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: account-service
  namespace: banking-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: account-service
  template:
    metadata:
      labels:
        app: account-service
    spec:
      containers:
        - name: account-service
          image: account-service:1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
```

**What is a Deployment?**
A Deployment manages the desired state of our application.

**Hierarchy:**
```text
Deployment
    ↓
ReplicaSet
    ↓
Pod
    ↓
Container
```

**Why Use a Deployment?**
✅ **Replica management** - maintains desired number of Pods
✅ **Self-healing** - recreates failed Pods
✅ **Rolling updates** - zero-downtime deployments
✅ **Desired-state management** - declarative configuration

**Self-Healing Example:**
```text
account-service Pod
      ✕ (dies)
ReplicaSet notices
      ↓
Creates new Pod
```

---

## 6. Why No pod.yaml?
We don't create standalone Pods for production applications.

Instead of:
```yaml
kind: Pod
```
We use:
```yaml
kind: Deployment
```

Standalone Pods are for:
* Testing/debugging
* One-off tasks
* *Not for production workloads*

---

## 7. Labels and Selectors
*Critical Kubernetes Concept*

**Labels** are attached to Pods:
```yaml
labels:
  app: account-service
```

**Selectors** allow Services to find Pods:
```yaml
selector:
  app: account-service
```

**Relationship**
```text
Service (selector: app=account-service)
             │
             │ Finds Pods with matching label
             ▼
Pod (labels: app=account-service)
```
> **Important:** The Deployment and Service are independent objects. They communicate through labels, not direct references.

---

## 8. Service

**File:** `infrastructure/kubernetes/account-service/service.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: account-service
  namespace: banking-platform
spec:
  type: ClusterIP
  selector:
    app: account-service
  ports:
    - port: 8080
      targetPort: 8080
```

**Why Services?**
Pods are ephemeral:
* Pod names change: `account-service-59566ff84d-vz6lp` → `account-service-7f8d9c6b7-xk92p`
* Pod IP addresses change
* Pods can be recreated

Service provides:
* Stable network endpoint
* Load balancing
* Service discovery

**Communication Flow**
```text
Other Service
     │
     ▼
account-service:8080  (stable DNS name)
     │
     ▼
Service (stable IP)
     │
     ▼
Account Pod (ephemeral IP/name)
```

---

## 9. ClusterIP
Type: `ClusterIP` (default)

**Characteristics**
✅ Accessible only from inside the Kubernetes cluster
✅ Internal communication between services
❌ Not exposed to the outside world

**Future External Access Methods**
* **NodePort** - expose on node's IP
* **LoadBalancer** - cloud provider load balancer
* **Ingress** - HTTP/HTTPS routing

---

## 10. PostgreSQL Deployment

**File:** `infrastructure/kubernetes/postgres/deployment.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: banking-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: payments
            - name: POSTGRES_USER
              value: postgres
            - name: POSTGRES_HOST_AUTH_METHOD
              value: trust
```

**PostgreSQL Environment Variables**

| Variable | Value | Purpose |
| :--- | :--- | :--- |
| `POSTGRES_DB` | `payments` | Database name |
| `POSTGRES_USER` | `postgres` | Database user |
| `POSTGRES_HOST_AUTH_METHOD` | `trust` | Authentication (trust for local dev) |

> ⚠️ **Security Note:** `trust` is acceptable for local learning but should be replaced with Kubernetes Secrets in production.

---

## 11. PostgreSQL Service

**File:** `infrastructure/kubernetes/postgres/service.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: banking-platform
spec:
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
```

**DNS Discovery**
* **Service Name:** `postgres`
* **Full DNS Name:** `postgres.banking-platform.svc.cluster.local`

**Application Configuration**
```properties
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/payments
    username: postgres
```
> **Key:** `postgres` is the Kubernetes Service name, resolved by Kubernetes DNS.

---

## 12. PersistentVolumeClaim

**File:** `infrastructure/kubernetes/postgres/pvc.yaml`
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: banking-platform
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

**Why PVC?**
Containers are ephemeral:
```text
PostgreSQL Pod
    ↓
Container filesystem
    ↓
Pod deleted
    ↓
Data lost ❌
```

**Solution: Persistent Storage**
```text
PostgreSQL
    ↓
/var/lib/postgresql/data
    ↓
PersistentVolumeClaim
    ↓
PersistentVolume
    ↓
Persistent storage ✅
```

---

## 13. PVC vs PV

**PersistentVolumeClaim (PVC)**
"I need 1 GiB of persistent storage."
*Role:* Request for storage

**PersistentVolume (PV)**
"Here is actual storage available to Kubernetes."
*Role:* Actual storage resource

**Relationship Flow**
```text
Application
    ↓
PVC (Request)
    ↓
PV (Resource)
    ↓
Storage
```

---

## 14. PVC Status: Pending

**Initial State:**
```bash
kubectl get pvc -n banking-platform
NAME            STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
postgres-pvc    Pending                                      standard       5s
```

**Why Pending?**
Our StorageClass uses:
```yaml
volumeBindingMode: WaitForFirstConsumer
```

**Lifecycle**
```text
PVC created
    ↓
Pending (waiting for consumer)
    ↓
PostgreSQL Pod consumes PVC
    ↓
Storage provisioned
    ↓
PVC becomes Bound
```

**Final State:**
```bash
kubectl get pvc -n banking-platform
NAME            STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
postgres-pvc    Bound    pvc-8a7b9c6d-1234-5678-9abc-def012345678   1Gi        RWO            standard       2m
```

---

## 15. PostgreSQL Volume Mount

**Deployment Configuration:**
```yaml
spec:
  containers:
    - name: postgres
      volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
  volumes:
    - name: postgres-storage
      persistentVolumeClaim:
        claimName: postgres-pvc
```

**Flow**
```text
postgres-pvc (PVC)
    ↓
postgres-storage (Volume)
    ↓
/var/lib/postgresql/data (Mount Path)
    ↓
PostgreSQL (Database files)
```

---

## 16. Final Architecture
```text
                 banking-platform namespace
                 ──────────────────────────

                    account-service
                          Service
                            │
                            ▼
                    ┌───────────────┐
                    │ Account Pod   │
                    │               │
                    │ Spring Boot   │
                    │ :8080         │
                    └───────┬───────┘
                            │
                            │ postgres:5432
                            ▼
                    ┌───────────────┐
                    │ PostgreSQL    │
                    │ Service       │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ PostgreSQL    │
                    │ Pod           │
                    └───────┬───────┘
                            │
                            ▼
                       postgres-pvc
                            │
                            ▼
                    Persistent Storage
```

---

## 17. Useful Commands

**View All Resources**
```bash
kubectl get all -n banking-platform
```

**Pod Management**
```bash
# List pods
kubectl get pods -n banking-platform

# Pod logs
kubectl logs <pod-name> -n banking-platform

# Follow logs
kubectl logs -f <pod-name> -n banking-platform

# Describe pod
kubectl describe pod <pod-name> -n banking-platform
```

**Resource Management**
```bash
# Deployments
kubectl get deployments -n banking-platform

# Services
kubectl get svc -n banking-platform

# PersistentVolumeClaims
kubectl get pvc -n banking-platform

# Describe PVC
kubectl describe pvc postgres-pvc -n banking-platform
```

---

## 18. Key Concepts Summary

| Concept | Responsibility |
| :--- | :--- |
| **Namespace** | Logical isolation/grouping |
| **Deployment** | Manages application replicas and desired state |
| **ReplicaSet** | Ensures desired number of Pods exist |
| **Pod** | Smallest deployable Kubernetes unit |
| **Service** | Stable network endpoint for Pods |
| **ClusterIP** | Internal cluster-only Service |
| **Label** | Metadata attached to resources |
| **Selector** | Finds resources based on labels |
| **PVC** | Request for persistent storage |
| **PV** | Actual persistent storage resource |
| **StorageClass** | Defines/provisions storage dynamically |

---

## 19. Critical Mental Model
**Four Key Relationships:**
```text
1. Deployment → manages → Pods
2. Service → discovers → Pods (using labels/selectors)
3. Pod → consumes → PVC
4. PVC → gets storage from → PV/StorageClass
```
*This foundation will be used for the entire banking platform.*