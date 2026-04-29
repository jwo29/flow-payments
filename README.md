# FlowPayments

FlowPayments는 원장 기반 금융 거래 시스템을 확장하여, 외부 PG 연동 환경에서 발생하는 불확실 상태를 처리하는 결제까지 포함한 통합 금융 백엔드 시스템입니다.

이 프로젝트는 단순 계좌 이체를 넘어
결제 승인, 취소, 환불, 외부 PG 연동, 정합성 보장까지 포함한
실무형 결제 시스템 아키텍처를 구현하는 것을 목표로 합니다.

UI 없이 **API 기반 결제 시스템**에 집중하여 핵심적으로 다음 문제를 해결합니다:

- 금융 거래 데이터 정합성
- 동시성 문제
- 멱등성(Idempotency)
- 외부 PG 연동 시 장애 대응 (timeout, 불확실 상태)

## ⭐ 프로젝트 특징 (핵심 차별점)

- 불확실 상태 모델링
  - PG timeout ≠ 실패 → UNKNOWN 상태 도입 후 inquiry로 정합성 보정
- PaymentTransaction 기반 상태 이력 관리
  - 단일 결제가 아닌 상태 변화 흐름을 별도 테이블로 관리
  - 상태 변화 이력 추적 가능
  - audit / 복구 / 부분 환불 기능으로 확장 가능성 확보
- Idempotency + Compensation 설계
  - 중복 결제 방지
  - 외부 성공 / 내부 실패 상황에서 보장 트랜잭션 처리
- PG 확장 가능한 구조
  - Strategy + Factory 패턴 기반 PaymentProcessor 설계

## 🧠 해결한 문제

### 1. 외부 PG 연동 시 정합성 문제

  요청 → timeout → 실제 성공 여부 불명확

해결:

- 상태를 UNKNOWN으로 저장
- transactionId 기반 inquiry로 상태 보정

### 2. 중복 결제 문제

사용자 재시도 / 네트워크 재전송에 의한 중복 결제 요청

해결:

- Idempotency-Key 기반 요청 식별
- 동일 요청은 1회만 처리

### 3. 결제 상태 관리 문제

하나의 결제에 여러 번의 거래 요청(approve / cancel / refund) 가능

해결:

- Payment vs PaymentTransaction 분리
- 상태 흐름 기반 설계

## ⚙️ 기술 스택

### Backend

-   Java 17
-   Spring Boot 3
-   Spring Transaction
-   Spring Validation

### Database

-   PostgreSQL

### Messaging

-   RabbitMQ

### Build Tool

-   Gradle

## 🚀 핵심 기능

### 1. Account & Ledger System

계좌 생성 및 잔액 관리

-   계좌 생성
-   계좌 조회
-   잔액 조회

잔액은 컬럼 값이 아닌 **Ledger 기반 계산**으로 관리됩니다.

#### Double Ledger

하나의 거래는 반드시 두개의 Ledger Entry를 생성합니다.

예시

    Account A -10000 (DEBIT)
    Account B +10000 (CREDIT)

이를 통해 다음을 보장합니다.

-   데이터 정합성
-   거래 추적 가능성
-   감사 로그(Audit)

### 2. Transfer System

계좌 간 이체 처리

-   계좌 간 송금
-   잔액 검증
-   거래 기록 생성

모든 거래는 **Atomic Transaction**으로 처리됩니다.

### 3. Payment System (핵심 확장)

외부 PG 연동 기반 결제 처리

- 결제 승인 (Approve)
- 결제 취소 (Cancel)
- 환불 (Refund)
- 결제 상태 조회 (Inquiry)

#### PaymentTransaction 구조

하나의 결제는 여러 상태 변화를 거칠 수 있습니다:

    APPROVED → CANCELLED → REFUNDED

이를 별도 테이블로 관리하여:
- 상태 이력 추적
- 장애 복구
- 부분 환불 대응
을 가능하게 설계했습니다.

### 4. PG 연동 및 장애 대응

외부 PG 통신 시 발생하는 문제 대응(Mock PG)

#### Timeout 처리 전략

- timeout를 실패로 간주하지 않음
- 상태를 UNKNOWN으로 저장
- 이후 inquiry로 상태 보정

    요청 → timeout → UNKNOWN → 재조회(inquiry) → 상태 확정

이를 통해 다음을 보장합니다.

- 중복 결제 방지
- 데이터 정합성 유지

### 5. Idempotent API

금융 API에서 필수적인 **멱등성 처리**를 지원합니다.

IdempotencyKey로 네트워크 재시도, 사용자 중복 요청을 방지하도록 설계했습니다.

예시

    POST /transfers
    Idempotency-Key: abc123

동일 요청은 한 번만 처리하고,
재요청 시 기존 결과를 반환합니다.

### 6. Compensation (보상 트랜잭션)

외부 PG 성공 + 내부 DB 실패 상황 대응

    PG 성공
    → DB 실패
    → 결제 취소 필요

→ 보상 트랜잭션으로 정합성 유지

## 🏗 시스템 아키텍처

    Client
    ↓
    Payment / Transfer API
    ↓
    Service
    ├─ Payment Service
    └─ Transaction Service
    ↓
    Database
    ↓
    Outbox Event
    ↓
    Message Queue (RabbitMQ)

## 🗂 데이터 모델

주요 테이블

- accounts
- transactions
- account_ledgers
- payments
- payment_transactions
- outbox_events

## 🔄 결제 처리 흐름

결제 요청

    POST /payments

처리 과정

1. 요청 검증
2. Idempotency 체크
3. Payment 생성
4. PG 승인 요청
5. PaymentTransaction 기록
6. 상태 확정 (CAPTURED / REFUNDED / UNKNOWN)
7. 이벤트 발행

## 🎯 프로젝트 목표

- 결제 시스템의 상태 모델링 이해
- 외부 시스템 연동 시 정합성 유지 전략 구현
- Idempotency / Compensation / Event Driven 구조 설계

## 🧩 프로젝트 구조

    flowpayments
    ├─ user
    ├─ account
    ├─ transaction 
    ├─ payment
    │    ├─ controller
    │    ├─ domain
    │    ├─ dto
    │    ├─ infrastructure
    │    ├─ service
    │    ├─ processor
    │    └─ repository
    ├─ outbox
    ├─ messaing
    ├─ pg
    └─ common

## ▶ 실행 방법

    ./gradlew bootRun

## 🔧 개선 계획

- PG Mock → 실제 API 스펙 기반 고도화
- Retry / Backoff 정책 정교화
- Payment 상태 머신(State Machine) 도입
- 대량 거래 처리 성능 테스트
- 배치 기반 정산 시스템 확장
