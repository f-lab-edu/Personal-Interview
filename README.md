# Personal Interview - Spring Boot Application

Spring Boot 기반의 개인 면접 연습 서비스 백엔드 애플리케이션입니다.

## 기술 스택

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 25
- **ORM**: JPA (Hibernate) + MyBatis
- **Database**: MySQL 8.0
- **Container**: Docker Compose
- **Build Tool**: Gradle

## 개발 환경 설정

### 필수 요구사항

- Java 25 이상
- Docker Desktop (Docker Compose 포함)
- IDE (IntelliJ IDEA / Eclipse 등)

### 환경별 설정

프로젝트는 **local**과 **dev** 두 가지 프로필을 지원합니다.

#### Local 환경 (기본)
- Docker Compose를 통한 MySQL 자동 실행
- JPA DDL Auto: `update` (자동 스키마 업데이트)
- 상세한 SQL 로깅 활성화
- 개발 편의를 위한 디버그 로깅

#### Dev 환경
- 외부 MySQL 서버 사용
- JPA DDL Auto: `validate` (스키마 검증만)
- Docker Compose 비활성화
- 프로덕션 수준의 로깅 레벨

## 실행 방법

### 1. Local 환경에서 실행 (Docker Compose 자동 실행)

애플리케이션을 실행하면 Docker Compose가 자동으로 MySQL 컨테이너를 시작합니다.

```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew build
java -jar build/libs/interview-0.0.1-SNAPSHOT.jar
```

**중요**: Docker Desktop이 실행 중이어야 합니다!

### 2. Dev 프로필로 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

또는 환경변수 설정:
```bash
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### 3. 특정 프로필 지정

IDE에서 실행 시 VM options 또는 Program arguments에 다음을 추가:
```
-Dspring.profiles.active=local
```

## Docker Compose 구성

### MySQL 컨테이너 정보

- **컨테이너 이름**: `personal-interview-mysql`
- **포트**: `3306`
- **데이터베이스**: `interview_db`
- **사용자**: `interview_user`
- **비밀번호**: `interview_password`
- **Root 비밀번호**: `password`

### 수동 Docker 명령어

```bash
# 컨테이너 시작
docker compose up -d

# 컨테이너 중지
docker compose down

# 로그 확인
docker compose logs -f mysql

# MySQL 접속
docker exec -it personal-interview-mysql mysql -uroot -ppassword
```

## 데이터베이스 설정

### JPA vs MyBatis 사용 기준

- **JPA (Hibernate)**: 
  - 기본적인 CRUD 작업
  - 엔티티 간 관계 매핑
  - 간단한 쿼리

- **MyBatis**:
  - 복잡한 조인 쿼리
  - 동적 쿼리
  - 성능 최적화가 필요한 대용량 쿼리

### 초기 스키마

데이터베이스 초기화 스크립트는 `docker/init.sql`에 정의되어 있습니다.
컨테이너가 처음 생성될 때 자동으로 실행됩니다.

## 설정 파일 상세

### application-local.properties

로컬 개발 환경에서 사용되는 설정:
- MySQL 로컬 연결 (localhost:3306)
- JPA DDL Auto: `update`
- SQL 로깅 활성화
- Docker Compose 자동 실행

### application-dev.properties

개발 서버 환경에서 사용되는 설정:
- 외부 MySQL 서버 연결
- JPA DDL Auto: `validate`
- SQL 로깅 비활성화
- Docker Compose 비활성화

**주의**: `application-dev.properties`의 데이터베이스 연결 정보는 실제 개발 서버 정보로 수정해야 합니다!


### 빌드 실패

1. Gradle 캐시 정리
   ```bash
   ./gradlew clean build --refresh-dependencies
   ```
2. Docker 이미지 재빌드
   ```bash
   docker compose down -v
   docker compose up -d --build
   ```

## 추가 정보

- Spring Boot 버전: 4.0.0
- MySQL 버전: 8.0
- 문자 인코딩: UTF8MB4
- 타임존: Asia/Seoul

## 라이센스

This project is licensed under the MIT License.
