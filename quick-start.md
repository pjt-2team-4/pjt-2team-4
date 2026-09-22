# Quick Start Guide

## 1. 데이터베이스 세팅 (Windows CMD)

Windows 명령 프롬프트를 열고 MariaDB에 접속하여 전용 DB와 계정을 생성합니다.

```bash
# 1. 루트 계정 접속 (명령어 입력 후 비밀번호 프롬프트에 입력)
mysql -u root -p

```

```sql
-- 2. 접속 후 아래 쿼리 실행
CREATE DATABASE lab_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lab'@'%' IDENTIFIED BY 'lab';
GRANT ALL PRIVILEGES ON lab_db.* TO 'lab'@'%';
FLUSH PRIVILEGES;
exit;

```

## 2. DB 연결 설정 (`src/main/resources/application.properties`)

애플리케이션(WSL)에서 호스트(Windows)의 DB로 접근하기 위한 네트워크 설정입니다.

```properties
# WSL에서 Windows 호스트를 가리키는 vEthernet IP 주소 사용
spring.datasource.url=jdbc:mariadb://172.17.128.1:3306/lab_db
spring.datasource.username=lab
spring.datasource.password=lab

# 서버 기동 시 JPA 엔티티를 기반으로 DB 테이블 자동 생성
spring.jpa.hibernate.ddl-auto=update

```

*(※ DB 연결 거부 에러 발생 시: Windows CMD에서 `ipconfig`로 WSL IP를 재확인하고, Windows 방화벽에서 인바운드 TCP 3306 포트를 허용해야 합니다.)*

## 3. 애플리케이션 빌드 및 실행 (WSL 터미널)

프로젝트 루트 디렉토리로 이동하여 아래 명령어를 순차적으로 실행합니다.

```bash
cd PJT_2(팀)/Spring-Maven/

# 1. 프로젝트 정리 및 빌드 (의존성 설치 포함)
./mvnw clean package

# 2. 비즈니스 로직 단위 테스트 실행 (선택)
./mvnw test

# 3. 애플리케이션 기동
./mvnw spring-boot:run

```

## 4. 기동 확인

서버 로그에 `Tomcat initialized with port(s): 8080`가 출력되면 브라우저에서 아래 주소로 접속합니다.
```md
http://localhost:8080/
```
---