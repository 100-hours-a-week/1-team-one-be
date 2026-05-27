#!/bin/bash
echo "=== Start Deployment ==="

DEPLOY_DIR="/home/ubuntu/app/backend" # CodeDeploy가 파일을 푼 곳
INFRA_DIR="/home/ubuntu/app"           # docker-compose.yml이 있는 곳

cd $DEPLOY_DIR || exit 1

# 서버로 같이 넘어온 .env 파일을 읽어서 환경변수로 적용
if [ -f .env ]; then
  export $(cat .env | xargs)
  echo "Deploying Version: $BACKEND_IMAGE_URI"
else
  echo ".env file not found!"
  exit 1
fi

echo "1. ECR Login & Pull New Image"
# (ECR 로그인은 EC2의 IAM Role(Instance Profile) 권한을 사용해 수행)
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ECR_REGISTRY

cd $INFRA_DIR || exit 1
docker compose pull backend

echo "2. Restart Container"
docker compose up -d backend

echo "Reloading Nginx to recognize new container IP..."
docker compose exec nginx nginx -s reload || echo "Nginx reload failed or Nginx is not running"

echo "3. Health Check & Verification"
IS_HEALTHY=false
FOR_LIMIT=24 # 최대 120초 대기 (5초 * 24회)

for i in $(seq 1 $FOR_LIMIT); do
  if [ -z "$(docker compose ps -q backend)" ]; then
    echo "Container is not running!"
    exit 1 # exit 1을 반환하면 CodeDeploy가 배포 실패로 간주함
  fi
  
  if curl -s -f --max-time 5 http://localhost:8080/api/actuator/health > /dev/null; then
    echo "Health Check Passed! Deployment Complete."
    IS_HEALTHY="true"
    break
  fi
  
  echo "App is starting... ($i/$FOR_LIMIT)"
  sleep 5
done

if [ "$IS_HEALTHY" = "true" ]; then
  docker image prune -f
  exit 0
else
  echo "Health Check Failed!"
  docker compose logs backend --tail 50
  exit 1 # 여기서 실패하면 CodeDeploy가 이전 버전 앱스펙을 기반으로 자동 롤백 진행
fi