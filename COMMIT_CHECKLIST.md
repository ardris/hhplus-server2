# 커밋 체크리스트

## 📋 커밋 대상 파일 목록

### ✅ 1. Infrastructure Layer - Repository 구현

#### **JPA Repository 인터페이스** (11개)
```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/repository/
├── JpaReservationRepository.java          ✅ 예약 저장소
├── JpaSeatRepository.java                 ✅ 좌석 정보 저장소
├── JpaSeatReservationStatusRepository.java ✅ 좌석 예약 상태 저장소 (중요!)
├── JpaUserRepository.java                 ✅ 사용자/잔액 저장소
├── JpaPaymentRepository.java              ✅ 결제 저장소
├── JpaQueueTokenRepository.java           ✅ 대기열 토큰 저장소
├── JpaTransactionRepository.java          ✅ 거래 내역 저장소
├── JpaConcertRepository.java              ✅ 콘서트 저장소
├── JpaPerformanceRepository.java          ✅ 공연 저장소
├── JpaPerformanceSeatPricingRepository.java ✅ 좌석 가격 저장소
└── JpaVenueRepository.java                ✅ 공연장 저장소
```

#### **Adapter 구현** (7개)
```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/adapter/
├── JpaReservationAdapter.java    ✅ 예약 Port 구현
├── JpaSeatAdapter.java            ✅ 좌석 Port 구현 (임시 배정 로직)
├── JpaUserAdapter.java            ✅ 사용자/잔액 Port 구현
├── JpaPaymentAdapter.java         ✅ 결제 Port 구현
├── JpaQueueTokenAdapter.java      ✅ 대기열 토큰 Port 구현
├── SeatAdapter.java               
└── UserAdapter.java               
```

---

### ✅ 2. 임시 좌석 배정 (Redis 없이 구현)

#### **핵심 파일들**

**좌석 예약 상태 Entity**:
```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/entity/
└── SeatReservationStatusEntity.java  ✅ 상태 컬럼 + 만료 시간 구현
    - seat_status: AVAILABLE/HOLD/SOLD/EXPIRED
    - hold_expires_at: 만료 시간
    - held_by_reservation_id: 예약 ID
```

**자동 만료 스케줄러**:
```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/scheduler/
└── SeatExpirationScheduler.java      ✅ 1분마다 만료된 좌석 자동 해제
```

**좌석 관리 Adapter**:
```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/adapter/
└── JpaSeatAdapter.java               ✅ 좌석 임시 배정/해제 로직
```

---

### ✅ 3. 대기열 토큰 관리 (DB 기반)

```
server-java/src/main/java/kr/hhplus/be/server/infrastructure/
├── entity/
│   └── QueueTokenEntity.java         ✅ 대기열 토큰 Entity
├── repository/
│   └── JpaQueueTokenRepository.java  ✅ DB 기반 토큰 저장소
└── adapter/
    └── JpaQueueTokenAdapter.java     ✅ 토큰 Port 구현
```

**Service Layer** (대기열 관리):
```
server-java/src/main/java/kr/hhplus/be/server/service/
├── QueueService.java                 ✅ 대기열 인터페이스
└── QueueServiceImpl.java             ✅ 대기열 구현체
```

---

### ✅ 4. 통합 테스트

#### **전체 흐름 테스트**
```
server-java/src/test/java/kr/hhplus/be/server/integration/
├── FullScenarioIntegrationTest.java           ✅ 토큰→잔액→예약→결제 전체 흐름
│   - 토큰발급_잔액충전_예약_결제_전체_시나리오_테스트()
│   - 잔액_부족_시_결제_실패_시나리오()
│   - 여러_사용자_동시_토큰_발급_테스트()
│   - 다중_좌석_예약_시나리오_테스트()
│   - 동시_토큰_발급_후_각자_예약_및_결제_테스트()
```

#### **동시성 테스트**
```
server-java/src/test/java/kr/hhplus/be/server/integration/
└── ConcurrencyTest.java                       ✅ 동시성 제어 테스트
    - 동일_좌석_동시_예약_시도_테스트()          ✅ 한 명만 성공
    - 여러_좌석_동시_예약_테스트()
    - 잔액_충전_동시성_테스트()
    - 좌석_예약_만료_자동_해제_테스트()          ✅ 만료 후 재예약
    - 대량_트래픽_동시_처리_성능_테스트()
```

#### **예약-결제 통합 테스트**
```
server-java/src/test/java/kr/hhplus/be/server/integration/
└── ReservationPaymentIntegrationTest.java     ✅ 예약-결제 통합
    - 좌석_불가_시_예약_실패하고_결제_안됨()
    - 잔액_부족_시_결제_실패()
    - VIP좌석_예약_및_결제_테스트()
    - 일반석_여러_좌석_예약_결제_테스트()
    - 예약_여러_좌석_동시_예약_결제_테스트()
```

---

## 📦 커밋 그룹별 정리

### **Commit 1: Infrastructure Layer - Repository**
```bash
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/repository/
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/adapter/
git commit -m "feat: Infrastructure Layer - JPA Repository 및 Adapter 구현

- 11개 JPA Repository 구현 (MySQL 기반)
- 7개 Adapter 구현 (Domain Port 연결)
- ReservationRepositoryPort, SeatPort, UserPort, PaymentPort 구현
"
```

### **Commit 2: 임시 좌석 배정 (Redis 없이)**
```bash
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/entity/SeatReservationStatusEntity.java
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/scheduler/SeatExpirationScheduler.java
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/adapter/JpaSeatAdapter.java
git commit -m "feat: 임시 좌석 배정 - Redis 없이 DB 기반 구현

- SeatReservationStatusEntity: 상태 컬럼 + 만료 시간 관리
- SeatExpirationScheduler: 1분마다 만료된 좌석 자동 해제
- JpaSeatAdapter: 좌석 임시 배정/해제 로직 구현
- Redis 없이 DB만으로 5분 임시 배정 구현 완료
"
```

### **Commit 3: 대기열 토큰 관리 (DB 기반)**
```bash
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/entity/QueueTokenEntity.java
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/repository/JpaQueueTokenRepository.java
git add server-java/src/main/java/kr/hhplus/be/server/infrastructure/adapter/JpaQueueTokenAdapter.java
git add server-java/src/main/java/kr/hhplus/be/server/service/QueueService*.java
git commit -m "feat: 대기열 토큰 관리 - DB 기반 구현

- QueueTokenEntity: 대기열 토큰 정보 관리
- JpaQueueTokenRepository: DB 기반 토큰 저장소
- JpaQueueTokenAdapter: QueueTokenPort 구현
- QueueService: 대기열 순번 관리 및 활성화
"
```

### **Commit 4: 통합 테스트 - 전체 흐름**
```bash
git add server-java/src/test/java/kr/hhplus/be/server/integration/FullScenarioIntegrationTest.java
git commit -m "test: 전체 시나리오 통합 테스트 작성

- 토큰 발급 → 잔액 충전 → 좌석 예약 → 결제 완료 흐름 테스트
- 잔액 부족 시 결제 실패 시나리오 테스트
- 여러 사용자 동시 토큰 발급 테스트
- 다중 좌석 예약 시나리오 테스트
- 동시 예약 및 결제 테스트 (5명)
"
```

### **Commit 5: 통합 테스트 - 동시성**
```bash
git add server-java/src/test/java/kr/hhplus/be/server/integration/ConcurrencyTest.java
git commit -m "test: 동시성 제어 통합 테스트 작성

- 동일 좌석 동시 예약 시 한 명만 성공하도록 테스트
- 여러 좌석 동시 예약 병렬 처리 테스트
- 잔액 충전/차감 동시성 테스트
- 좌석 예약 만료 자동 해제 테스트 (만료 후 재예약 가능)
- 대량 트래픽 성능 테스트 (100명 동시 접속)
"
```

### **Commit 6: 통합 테스트 - 예약/결제**
```bash
git add server-java/src/test/java/kr/hhplus/be/server/integration/ReservationPaymentIntegrationTest.java
git commit -m "test: 예약-결제 통합 테스트 작성

- 좌석 불가 시 예약 실패 및 결제 차단 테스트
- 잔액 부족 시 결제 실패 테스트
- VIP 좌석 예약 및 결제 테스트
- 여러 좌석 동시 예약 및 결제 테스트
"
```

---

## 📁 전체 파일 트리

```
server-java/src/main/java/kr/hhplus/be/server/
├── infrastructure/
│   ├── repository/              ✅ 11개 JPA Repository
│   │   ├── JpaReservationRepository.java
│   │   ├── JpaSeatRepository.java
│   │   ├── JpaSeatReservationStatusRepository.java  ⭐ 핵심
│   │   ├── JpaUserRepository.java
│   │   ├── JpaPaymentRepository.java
│   │   ├── JpaQueueTokenRepository.java
│   │   ├── JpaTransactionRepository.java
│   │   ├── JpaConcertRepository.java
│   │   ├── JpaPerformanceRepository.java
│   │   ├── JpaPerformanceSeatPricingRepository.java
│   │   └── JpaVenueRepository.java
│   │
│   ├── adapter/                 ✅ 7개 Adapter
│   │   ├── JpaReservationAdapter.java
│   │   ├── JpaSeatAdapter.java                      ⭐ 임시 배정 로직
│   │   ├── JpaUserAdapter.java
│   │   ├── JpaPaymentAdapter.java
│   │   ├── JpaQueueTokenAdapter.java
│   │   ├── SeatAdapter.java
│   │   └── UserAdapter.java
│   │
│   ├── entity/                  ✅ 20개 JPA Entity
│   │   ├── SeatReservationStatusEntity.java         ⭐ 상태+만료시간
│   │   ├── QueueTokenEntity.java                    ⭐ 대기열 토큰
│   │   ├── ReservationEntity.java
│   │   ├── PaymentEntity.java
│   │   ├── UserEntity.java
│   │   └── ... (나머지 Entity)
│   │
│   └── scheduler/               ✅ 스케줄러
│       └── SeatExpirationScheduler.java             ⭐ 자동 만료
│
└── service/                     ✅ Service Layer
    ├── QueueService.java
    └── QueueServiceImpl.java

server-java/src/test/java/kr/hhplus/be/server/integration/
├── FullScenarioIntegrationTest.java         ⭐ 전체 흐름 (5개 테스트)
├── ConcurrencyTest.java                     ⭐ 동시성 (5개 테스트)
└── ReservationPaymentIntegrationTest.java   ⭐ 예약-결제 (6개 테스트)
```

---

## 🎯 핵심 파일 (반드시 포함)

### **⭐ 임시 좌석 배정 관련** (Redis 없이)
1. `SeatReservationStatusEntity.java` - 상태 + 만료 시간
2. `SeatExpirationScheduler.java` - 자동 만료 처리
3. `JpaSeatAdapter.java` - 임시 배정 로직

### **⭐ 대기열 토큰 관련** (DB 기반)
1. `QueueTokenEntity.java` - 토큰 정보
2. `JpaQueueTokenRepository.java` - DB 저장소
3. `JpaQueueTokenAdapter.java` - Port 구현

### **⭐ 통합 테스트**
1. `FullScenarioIntegrationTest.java` - 전체 흐름
2. `ConcurrencyTest.java` - 동시성 (한 명만 성공)
3. `ReservationPaymentIntegrationTest.java` - 예약-결제

---

## ✅ 커밋 전 체크리스트

- [ ] 모든 Repository가 MySQL(JPA) 기반인가?
- [ ] Redis 의존성이 없는가?
- [ ] 임시 좌석 배정이 상태 컬럼 + 만료 시간으로 구현되었나?
- [ ] 자동 만료 스케줄러가 동작하는가?
- [ ] 전체 흐름 테스트가 있는가?
- [ ] 동시성 테스트에서 한 명만 성공하는가?
- [ ] 만료 후 재예약 테스트가 있는가?

---

**작성일**: 2025-10-27
**대상**: Infrastructure Layer + 통합 테스트
**총 파일 수**: 약 40개

