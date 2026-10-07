# 🚀 memora-api Deployment Guide

## 1. Create Cloud SQL Instance (virtual database server)

> 💡 Tip: `db-f1-micro` is the smallest and most cost-effective tier

```bash
gcloud sql instances create memora-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=$REGION

2. Create database

gcloud sql databases create memora \
--instance=memora-db

$PROJECT_ID = gcloud config get-value project
$REGION = "europe-central2"
$REPO_NAME = "memora-repo"

gcloud artifacts repositories create $REPO_NAME `
    --repository-format=docker `
--location=$REGION

Final steps in project root:

./mvnw clean package -DskipTests

docker build --no-cache -t "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/backend:v1" .

docker push "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/backend:v1"


gcloud run deploy memora-backend `
    --image="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/backend:v1" `
--add-cloudsql-instances "${PROJECT_ID}:${REGION}:memora-db" `
    --set-env-vars "SPRING_CLOUD_GCP_SQL_INSTANCE_CONNECTION_NAME=${PROJECT_ID}:${REGION}:memora-db,SPRING_CLOUD_GCP_SQL_DATABASE_NAME=memora,DB_URL=jdbc:postgresql://localhost:5432/memora,DB_USER=postgres,DB_PASS=DatabasePW,BUCKET_NAME=bucket-name,JWT_SECRET=longsecretkey" `
--allow-unauthenticated `
--region=$REGION

# Why HttpOnly Cookies Are Safer Than LocalStorage

### 1. Immunity to XSS Token Theft
* **`localStorage`:** Accessible by any JavaScript code running on your page. If an attacker injects a malicious script via Cross-Site Scripting (XSS) or a compromised npm package, they can instantly read `localStorage.getItem()` and exfiltrate your tokens.
* **`HttpOnly` Cookie:** Completely hidden from JavaScript (`document.cookie` cannot see or read it). Even if an XSS vulnerability exists on your frontend, the attacker cannot steal the token value from the browser.

### 2. Automatic & Isolated Network Handling
* **`localStorage`:** Requires manual JavaScript code to extract and attach the token to every outgoing request, increasing surface area for client-side bugs or leaks.
* **`HttpOnly` Cookie:** Automatically managed by the browser's native network engine. When `withCredentials: true` is configured, the browser securely handles attaching the cookie to authorized requests without exposing the raw string to application code.

### 3. Native CSRF Protection
* By adding the `SameSite=Lax` or `SameSite=Strict` directive to the cookie header, modern browsers automatically prevent the cookie from being sent on unauthorized cross-site requests, mitigating Cross-Site Request Forgery (CSRF) attacks natively.

### 4. Granular Scope Control
* Cookies allow server-defined constraints via headers that `localStorage` cannot offer:
  * **`Path`:** Scopes the credential to specific endpoints (e.g., restricting the Refresh Token strictly to `/api/auth/refresh`).
  * **`Secure`:** Enforces that the browser will only transmit the token over encrypted HTTPS connections.
  * **`Max-Age` / `Expires`:** Ensures automated expiration and cleanup at the browser level.

### 5. Multi-Tab Session Reliability
* `localStorage` uses a shared key namespace per domain, making it vulnerable to race conditions or accidental key overwrites when multiple browser tabs interact with the session simultaneously. Cookie storage is managed isolatedly by the browser's storage engine.