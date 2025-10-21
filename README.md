# MSA: member service
### 자기 계발 및 취미 매칭 웹 애플리케이션, GROW 🌳

**GROW Member Service**는 플랫폼 전체의 **회원 도메인** 역할을 수행하는 핵심 마이크로서비스입니다.  
회원의 **인증·인가(OAuth2 기반 소셜 로그인)**부터 **프로필 관리, 휴대폰 인증, 탈퇴 및 개인정보 보호**,  
그리고 **업적·포인트·퀴즈 결과·멤버 온도(평판)** 등 성장 지표를 통합 관리합니다.

서비스는 **DDD(Domain-Driven Design)** 원칙을 중심으로 **Hexagonal Architecture (Ports & Adapters)** 구조로 구현되어,  
도메인 로직과 인프라 의존성을 명확히 분리하고 높은 응집도와 낮은 결합도를 유지합니다.  
비즈니스 규칙은 `domain` 계층에서 관리되고, 외부 연동(예: Kafka, Redis, JPA, 외부 API)은 `infra` 어댑터를 통해 주입됩니다.  
`application` 계층에서는 트랜잭션 단위의 유스케이스(예: 회원 탈퇴, 포인트 적립, 업적 트리거)를 조합하며,  
`presentation` 계층에서는 REST API를 통해 게이트웨이와 통신합니다.

---

## 👥 팀

|                                           장무영                                           |                                           최지선                                           |
|:---------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/136911104?v=4" alt="장무영" width="200"> | <img src="https://avatars.githubusercontent.com/u/192316487?v=4" alt="최지선" width="200"> | 
|                           [GitHub](https://github.com/wkdan)                            |                        [GitHub](https://github.com/wesawth3sun)                         |

---

## 🏗️ 아키텍처 개요

<img width="1541" height="1241" alt="image" src="https://github.com/user-attachments/assets/8a40b1c6-0bdb-4414-86b4-eee9f298d6ca" />

---

## 🧩 운영 구조

서비스 간 연결은 **Gateway, Kafka, Redis, Kubernetes**를 중심으로 구성되어 있습니다.

| 구성 요소 | 역할 |
|------------|------|
| 🧭 **Gateway (Spring Cloud Gateway)** | JWT 기반 인증 검증 및 요청 라우팅. 로그인 성공 후 게이트웨이 리다이렉트 처리 |
| 🧵 **Kafka (Event Bus)** | 각종 이벤트를 발행하고, 퀴즈/결제 결과 이벤트를 구독하여 데이터 동기화 |
| 💾 **Redis (Cache & Lock)** | 분산 캐시 및 멱등 처리(Idempotency Key) 관리 — `SETNX` 기반 요청 중복 방지 |
| 🗃️ **MySQL (Primary DB)** | 회원 프로필, 활동 이력, 포인트/업적 로그의 영속 데이터 저장소 |
| ☸️ **Kubernetes** | 서비스 배포·스케일링·롤링 업데이트를 자동화하여 고가용성(HA) 확보 |
| 📊 **Prometheus + Grafana** | 회원 이벤트 처리율, 로그인 성공률, 탈퇴 처리 지연 등 메트릭 기반 실시간 모니터링 |

---

## 🧩 주요 기능 요약

| 구분 | 기능 설명 |
|------|------------|
| 🔐 **OAuth2 소셜 로그인** | Google, Kakao, Naver 등 외부 플랫폼 로그인 연동 및 신규 회원 자동 등록 |
| 👤 **회원 정보 관리** | 프로필, 추가 정보(휴대폰, 주소 등) 수정 및 상태 관리 |
| 📱 **휴대폰 인증** | 인증 토큰 기반으로 휴대폰 번호 검증 및 검증 상태 유지 |
| 🧾 **회원 탈퇴 처리** | UUID 기반 개인정보 마스킹, 탈퇴 로그 저장|
| 🌡️ **멤버 온도 시스템** | 리뷰 평균 점수 기반으로 온도(기본값 36.5℃) 변동 — 신뢰도 지표 반영 |
| 🏆 **업적(Achievement) 관리** | 로그인, 인증, 결제, 퀴즈, 학습 등 이벤트 기반 업적 달성 처리 |
| 💰 **포인트(Point) 관리** | 업적·퀴즈·결제 등 플랫폼 내 다양한 활동에 따른 포인트 적립 및 차감 |
| 🧠 **퀴즈 결과 기록 관리** | 퀴즈 서비스와의 Kafka 이벤트 연동을 통해 퀴즈 참여 및 정답률 추적 |
| 📨 **이벤트 기반 연동** | Notification/Payment/Challenge 등 타 서비스와 Kafka 이벤트로 통신 |
| 🧱 **캐시 및 데이터 보호** | Redis 기반 캐시 관리, 민감 정보 마스킹 및 로그 저장으로 데이터 무결성 보장 |

---

## 🛠️ 기술 스택

### FrontEnd
<div> 
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white"/>
  <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white"/>
</div>

### BackEnd
<div> 
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"/>
</div>

### Database
<div> 
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white"/>
</div>

### IDLE&Tool
<div> 
  <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/>
</div>

### OPEN API

### Event Bus / Messaging
<div>
  <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white"/>
</div>

### Infra
<div>
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black"/>
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white"/>
</div>

### Container & Orchestration
<div>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white"/>
</div>

### CI/CD
<div>
  <img src="https://img.shields.io/badge/ArgoCD-EF7B4D?style=for-the-badge&logo=argo&logoColor=white"/>
</div>

---

## Conventional Commits 규칙

| 이모지 | 타입      | 설명                                               | 예시 커밋 메시지                                   |
|--------|-----------|--------------------------------------------------|--------------------------------------------------|
| ✨     | feat      | 새로운 기능 추가                                    | feat: 로그인 기능 추가                             |
| 🐛     | fix       | 버그 수정                                          | fix: 회원가입 시 이메일 중복 체크 오류 수정         |
| 📝     | docs      | 문서 수정                                          | docs: README 오타 수정                            |
| 💄     | style     | 코드 포맷, 세미콜론 누락 등 스타일 변경 (기능 변경 없음) | style: 코드 정렬 및 세미콜론 추가                  |
| ♻️     | refactor  | 코드 리팩토링 (기능 변경 없음)                     | refactor: 중복 코드 함수로 분리                    |
| ⚡     | perf      | 성능 개선                                          | perf: 이미지 로딩 속도 개선                       |
| ✅     | test      | 테스트 코드 추가/수정                              | test: 유저 API 테스트 코드 추가                    |
| 🛠️     | build     | 빌드 시스템 관련 변경                              | build: 배포 스크립트 수정                         |
| 🔧     | ci        | CI 설정 변경                                      | ci: GitHub Actions 워크플로우 수정                |

```text
타입(범위): 간결한 설명 (50자 이내, 한글 작성)

(필요시) 변경 이유/상세 내용
```
- 하나의 커밋에는 하나의 목적만 담기

    → 여러 변경 사항을 한 커밋에 몰아넣지 않기
- 제목 끝에 마침표(.)를 붙이지 않기
- 본문(Body)은 선택 사항이지만, 변경 이유나 상세 설명이 필요할 때 작성

    → 72자 단위로 줄바꿈, 제목과 본문 사이에 한 줄 띄우기
- 작업 중간 저장은 WIP(Work In Progress)로 표시할 것

  → 예) WIP: 회원가입 로직 구현 중

---
## 🕒 협업 시간 안내

팀원들이 주로 활동하는 시간대입니다.  
이 시간에 맞춰 커뮤니케이션과 코드 리뷰, 회의 등을 진행합니다.

| 요일     | 활동 시간                  |
|----------|----------------------------|
| 📅 평일  | 14:00 ~ 18:00, 20:00 ~ 23:00 |
| 📅 주말  | 14:00 ~ 18:00               |

---

## 🧐 코드 리뷰 규칙

- PR 제목과 설명을 명확하게 작성 (변경 내용, 목적, 참고 이슈 등 포함)
- Conventional Commits 규칙을 준수하여 커밋 메시지 작성
- 하나의 PR에는 하나의 기능/이슈만 포함
- 코드 스타일, 네이밍, 로직, 성능, 보안, 예외 처리 등 꼼꼼히 확인
- 리뷰 코멘트에는 반드시 답변, 필요시 추가 커밋으로 반영
- 모든 리뷰 코멘트 resolve 후 머지
- 스쿼시 머지 방식 권장, 충돌 발생 시 머지 전 해결
- 리뷰는 24시간 이내 진행, 모르는 부분은 적극적으로 질문
- 리뷰 과정에서 배운 점은 팀 문서에 공유 (트러블 슈팅 등)
