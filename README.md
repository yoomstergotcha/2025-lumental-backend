# 🐳 Lumental 

Lumental은 웨어러블 디바이스에서 수집된 생체지표(Physiological signals) 와 사용자와의 AI 대화(Cognitive interaction) 를 함께 분석하여 심리적 상태를 객관적으로 이해하고 맞춤형 피드백을 제공하는 AI 멘탈 코치 서비스입니다.

사용자의 심박수·HRV·수면·활동 데이터와 일정·대화 맥락을 통합 분석해, “오늘의 나”의 상태를 데이터 기반으로 해석하고 솔루션을 제시합니다.

### [PART 1] Physiological Intelligence Engine
웨어러블 데이터로부터 사용자의 스트레스 상태 및 생리적 안정도 분석

- Data Collector: HealthKit export.xml 파일 parse
- Preprocessing Module: 결측치 보정, Z-score / Baseline 편차 계산
- Stress Index Analyzer: HRV, 수면, 활동량 기반 스트레스 지수 산출
- Confidence Scorer: 착용률 및 데이터 커버율 기반 신뢰도 산출
### [PART 2] Cognitive Interaction Engine
생체지표 + 대화 컨텍스트를 결합해 GPT-4o 기반 맞춤 피드백 생성

- Context Builder: 캘린더 + 생체데이터 병합된 정보 기반 프롬프트 생성
- GPT-4o API 호출 모듈: OpenAI GPT-4o 호출 및 응답 처리
- Response Formatter: 분석 결과를 자연스러운 문장(위로/솔루션/루틴 제안)으로 재구성

## Lumental Backend
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
