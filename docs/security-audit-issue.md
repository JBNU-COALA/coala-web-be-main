# 보안 점검 이슈 초안

아래 내용은 `.github/ISSUE_TEMPLATE/custom.md` 형식에 맞춘 보안 점검 이슈/PR 본문 초안이다.

### 🔥PURPOSE
> 백엔드 API의 인증/인가, 토큰, CORS, 입력 검증, 배포 스크립트 관점에서 운영 전 보안 위험을 점검하고 우선순위에 따라 조치한다.

### 📒TODO
- [ ] `SecurityConfig` CORS 정책 제한
  - 위치: `src/main/java/com/example/coalawebbackend/common/config/SecurityConfig.java`
  - 현재 `allowedOriginPatterns("*")`와 `allowCredentials(true)`를 같이 사용한다.
  - 운영에서는 `https://<frontend-domain>`처럼 허용 origin을 환경변수 기반 allowlist로 제한한다.
- [ ] 운영 환경 Swagger 공개 범위 제한
  - 위치: `src/main/java/com/example/coalawebbackend/common/config/SecurityConfig.java`
  - 현재 `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`이 인증 없이 공개된다.
  - 운영 profile에서는 비활성화하거나 관리자 권한으로 제한한다.
- [ ] Refresh Token 재발급 검증 보강
  - 위치: `src/main/java/com/example/coalawebbackend/api/auth/facade/AuthFacade.java`
  - 현재 refresh 요청에서 Redis에 저장된 refresh token hash와 대조하지 않는다.
  - `refreshTokenStore.validate(userId, request.refreshToken())` 검증 후 재발급하도록 수정한다.
- [ ] 로그인 사용자 존재 여부 노출 줄이기
  - 위치: `src/main/java/com/example/coalawebbackend/api/auth/facade/AuthFacade.java`
  - 현재 이메일 미존재와 비밀번호 불일치 경로가 코드상 분리되어 있다.
  - 응답 메시지와 타이밍 차이를 줄여 계정 열거 위험을 낮춘다.
- [ ] 운영 기본 secret과 SQL 로그 설정 분리
  - 위치: `src/main/resources/application.yaml`
  - 현재 `JWT_SECRET` 기본값이 `change-me-dev-only`이고 `show-sql: true`가 기본이다.
  - 운영 profile에서는 secret 미설정 시 기동 실패, SQL 로그 비활성화를 적용한다.
- [ ] 댓글 본문 길이 제한 추가
  - 위치: `src/main/java/com/example/coalawebbackend/api/comment/dto/CreateCommentRequest.java`
  - 위치: `src/main/java/com/example/coalawebbackend/api/comment/dto/UpdateCommentRequest.java`
  - 현재 `@NotBlank`만 있어 긴 입력으로 저장소/응답 부하를 만들 수 있다.
  - `@Size(max = ...)` 제한을 추가한다.
- [ ] 리소스 URL/타입 검증 강화
  - 위치: `src/main/java/com/example/coalawebbackend/api/resource/dto/CreateResourceRequest.java`
  - 현재 `fileUrl`, `fileType` 길이와 URL 형식 제한이 부족하다.
  - URL 형식, 허용 scheme, 길이, MIME/type allowlist를 적용한다.
- [ ] Validation 예외 응답 표준화
  - 위치: `src/main/java/com/example/coalawebbackend/common/exception/GlobalExceptionHandler.java`
  - 현재 `CustomException`만 처리한다.
  - `MethodArgumentNotValidException`, `ConstraintViolationException`을 공통 응답으로 처리한다.
- [ ] 배포 스크립트 실패 즉시 중단
  - 위치: `.github/workflows/deploy.yml`
  - 현재 서버에서 `git pull`이 실패해도 Gradle/Docker 단계가 이어질 수 있다.
  - `set -e` 또는 `script_stop: true`를 적용한다.
- [ ] Docker 컨테이너 이름 충돌 정리
  - 위치: `docker-compose.yml`
  - 서버에 기존 `coala-web-be-main` 컨테이너가 남아 있으면 compose up이 실패한다.
  - compose service/container name을 현재 운영 컨테이너와 일치시키거나 배포 전에 기존 컨테이너를 compose project 기준으로 정리한다.

### 우선순위
- P1: Refresh Token 검증, 운영 secret/profile 분리, 배포 스크립트 실패 중단
- P2: CORS 제한, Swagger 운영 공개 제한, Validation 예외 표준화
- P3: 댓글 길이 제한, 리소스 URL/type 검증, Docker 이름 충돌 정리
