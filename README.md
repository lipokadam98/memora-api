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

