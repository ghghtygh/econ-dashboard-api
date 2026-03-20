# Econ Dashboard API

경제 지표 대시보드 서비스의 백엔드 API 서버입니다.
Kotlin + Spring Boot 기반으로, 경제 지표 데이터 수집/조회/캐싱 및 대시보드 위젯 관리 기능을 제공합니다.

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Kotlin | 1.9.25 |
| Framework | Spring Boot | 3.3.4 |
| JDK | Java | 21 |
| ORM | Spring Data JPA | Boot 관리 |
| Cache | Spring Data Redis | Boot 관리 |
| Database | PostgreSQL (prod) / H2 (dev) | 16 |
| HTTP Client | Spring WebFlux (WebClient) | Boot 관리 |
| Messaging | Spring Kafka (추후) | Boot 관리 |
| Build | Gradle (Kotlin DSL) | - |

## 프로젝트 구조

```
api/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── kotlin/com/econdashboard/
    │   ├── EconDashboardApplication.kt   # 앱 진입점 (@EnableScheduling)
    │   └── config/
    │       ├── RedisConfig.kt            # Redis 캐시 설정
    │       └── WebConfig.kt              # CORS 설정
    └── resources/
        ├── application.yml               # 공통 설정
        ├── application-dev.yml           # 개발 환경 (H2)
        └── application-prod.yml          # 운영 환경 (PostgreSQL)
```

### 계획된 멀티 모듈 구조

```
backend/
├── core/        # 공통 도메인, DTO, 예외, 유틸
├── infra/       # 인프라 (DB, Cache, 외부 API)
├── api/         # REST API 서버
├── batch/       # 배치/스케줄러 (데이터 수집)
└── gateway/     # API Gateway (추후)
```

## 시작하기

### 사전 준비

- JDK 21+
- Docker & Docker Compose (Redis, PostgreSQL)

### 인프라 실행

프로젝트 루트에서 Docker Compose로 Redis와 PostgreSQL을 실행합니다.

```bash
# 프로젝트 루트 디렉토리에서
docker-compose up -d
```

| 서비스 | 포트 | 용도 |
|--------|------|------|
| PostgreSQL | 5432 | 메인 DB |
| Redis | 6379 | 캐시 / 실시간 데이터 |

### 개발 서버 실행

```bash
cd api
./gradlew bootRun
```

개발 환경에서는 H2 인메모리 DB를 사용하므로 PostgreSQL 없이도 실행 가능합니다.

- API 서버: `http://localhost:8080`
- H2 콘솔: `http://localhost:8080/h2-console`
- Actuator: `http://localhost:8080/actuator/health`

### 빌드

```bash
./gradlew build
```

## 환경별 설정

| 항목 | dev | prod |
|------|-----|------|
| DB | H2 (in-memory) | PostgreSQL 16 |
| DDL 전략 | create-drop | validate |
| Redis | localhost:6379 | 환경변수 |
| 서버 포트 | 8080 | 8080 |
| 로그 레벨 | DEBUG + SQL | INFO |

### 환경변수 (prod)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `REDIS_HOST` | Redis 호스트 | localhost |
| `REDIS_PORT` | Redis 포트 | 6379 |
| `REDIS_PASSWORD` | Redis 비밀번호 | (없음) |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 브로커 주소 | localhost:9092 |

## API 엔드포인트

### 경제 지표

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/indicators` | 전체 지표 목록 조회 |
| GET | `/api/indicators/{id}` | 특정 지표 상세 조회 |
| GET | `/api/indicators/categories` | 카테고리 목록 조회 |
| GET | `/api/indicators/{id}/data?from=&to=` | 지표 시계열 데이터 조회 |
| POST | `/api/indicators/series` | 복수 지표 시계열 데이터 일괄 조회 |

### 대시보드

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/dashboard/widgets` | 위젯 레이아웃 조회 |
| POST | `/api/dashboard/widgets` | 위젯 레이아웃 저장 |
| PUT | `/api/dashboard/widgets/{id}` | 위젯 개별 수정 |
| DELETE | `/api/dashboard/widgets/{id}` | 위젯 삭제 |

### 구독/알림 (계획)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/subscriptions` | 구독 목록 조회 |
| POST | `/api/subscriptions` | 지표 구독 |
| DELETE | `/api/subscriptions/{id}` | 구독 해제 |
| GET | `/api/alerts` | 알림 조회 |
| POST | `/api/alerts/rules` | 알림 규칙 생성 |

### 뉴스 (계획)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/news?category=&page=&size=` | 경제 뉴스 조회 |
| GET | `/api/news/{id}` | 뉴스 상세 |

## 데이터 모델

주요 엔티티:

- **indicator** - 경제 지표 (KOSPI, S&P 500, Bitcoin 등)
- **indicator_data** - 시계열 데이터 (가격, 변동률 등)
- **dashboard_widget** - 대시보드 위젯 설정
- **subscription** - 지표 구독 (계획)
- **alert_rule** - 알림 규칙 (계획)
- **news_article** - 경제 뉴스 (계획)

자세한 ERD는 [ARCHITECTURE.md](../docs/ARCHITECTURE.md#5-erd-데이터-모델)를 참조하세요.

## 현재 구현 상태

| 기능 | 상태 |
|------|------|
| 앱 진입점 + CORS 설정 | 완료 |
| Redis 캐시 설정 | 완료 |
| Scheduling 활성화 | 완료 |
| Entity / Repository | 미구현 |
| Service / Controller | 미구현 |
| 배치 데이터 수집기 | 미구현 |
