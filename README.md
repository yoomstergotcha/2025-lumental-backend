# 🐳 Lumental Backend
Spring Boot 기반 RESTful API 서버로, AWS RDS, S3, GitHub Actions, Docker, Nginx, HTTPS를 활용한 클라우드 환경에서 운영됩니다.

- 최종 배포 주소: https://lumental-backend-921348145792.asia-northeast3.run.app
- FastAPI 배포 주소: https://fastapi-analysis-921348145792.asia-northeast3.run.app
- API 명세서: swagger

## 📁 프로젝트 구조
```
📦 com.example.lumental
├─ config # 각종 Configuration 세팅 
├─ global # ApiResponse, GlobalExceptionHandler
├─ controller
├─ service
├─ repository
├─ domain
└─ dto
```

## 🚀 기술 스택
```
Java 21
Spring Boot 3.7
Spring Data JPA (Hibernate)
PostGRESQL 
AWS EC2 (Amazon Linux 2023)
AWS S3
Docker
Nginx + Certbot (HTTPS)
GitHub Actions (CI/CD)
FastAPI 분석 서버
```
