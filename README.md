# GrabbingSystem

A high-concurrency **flash-sale (seckill) backend** in Spring Boot, built around one question: how do you sell exactly N items to a flood of concurrent buyers — never N+1, never N-1 — without serialising every request through the database?

**Status:** core feature-complete — atomic Redis + Lua seckill pipeline, MySQL conditional deduction with compensation rollback, order state machine with scheduled timeout recovery, and a reproducible wrk benchmark with measured numbers.

## The problem

Naive seckill implementations fail in one of two ways:

- **Oversell** — two requests both read `stock = 1`, both decrement, two orders are created for one item.
- **Collapse under load** — the fix is a row lock or a distributed lock, and now every request queues on the same row; throughput falls off a cliff exactly when traffic peaks.

This project takes the pre-deduction route: Redis owns the hot path and answers in one atomic step; MySQL is the system of record and is only touched by requests that already won.

## Highlights

- **Atomic Redis + Lua pipeline.** Activity-window check, per-second rate limit, duplicate-request rejection, and stock pre-deduction all execute inside a *single* Lua script, so they are atomic with respect to every other request — no read-modify-write race, no lock contention on the hot path.
- **Conditional MySQL deduction.** Final persistence uses `UPDATE ... SET stock = stock - 1 WHERE id = ? AND stock > 0`, so the database is a second, independent guard against oversell rather than a mere follower of Redis.
- **Compensation rollback.** If persistence fails after Redis already pre-deducted, the reserved stock is returned to Redis — the two stores converge instead of silently drifting apart.
- **Order state machine.** `INIT → PAID / CANCELED / TIMEOUT` with a scheduled job that reclaims stock from orders that were never paid, so abandoned carts do not permanently consume inventory.
- **Layered architecture** (`interfaces / domain.service / infrastructure / common / config`) — the seckill policy lives in the domain layer and is independent of the web and storage layers.

## Performance

Measured with `wrk` on a local machine. The load script (`wrk-seckill.lua`) generates a random `userId` per request so every request is a distinct buyer.

| Metric | Value |
|---|---|
| Throughput | **35.2K req/s** |
| Total requests | 4.22M over 120s |
| p99 latency | **66.7 ms** |
| Test profile | 12 threads / 800 connections / 120s |

**Correctness under load:** with 100,000 units of seckill stock, the run produced exactly **100,000 orders — zero oversell**.

Reproduce:

```bash
# start the app (needs Redis + MySQL running)
./mvnw spring-boot:run

# throughput + latency
wrk -t12 -c800 -d120s -s wrk-seckill.lua http://localhost:8080

# no-oversell validation
wrk -t12 -c800 -d120s -s wrk-proof.lua http://localhost:8080
```

## Seckill request path

```
POST /api/seckill/order/create?userId=&promoId=&skuId=
        │
        ▼
  ┌─────────────────────── Redis (single Lua script, atomic) ───────────────────────┐
  │  activity window valid?  →  rate limit ok?  →  duplicate?  →  stock > 0?        │
  │                                    │                                            │
  │                          DECR stock (pre-deduction)                             │
  └────────────────────────────────────┬───────────────────────────────────────────┘
                                       │ won
                                       ▼
                    MySQL: UPDATE ... WHERE stock > 0   (second guard)
                                       │
                        ┌──────────────┴──────────────┐
                     success                        failure
                        │                              │
                 create order (INIT)         compensate: return stock to Redis
                        │
              scheduled job: unpaid → TIMEOUT, stock reclaimed
```

## Quick start

**Prerequisites:** JDK 17+, Maven (wrapper included), Redis, MySQL.

```bash
# configure datasource / redis in src/main/resources/
./mvnw clean package
./mvnw spring-boot:run
```

## Project layout

```
src/main/java/com/grabbing/grabbingsystem/
  interfaces/       REST controllers (order + seckill endpoints)
  domain/service/   order & seckill business logic, order state transitions
  infrastructure/   MySQL (MyBatis-Plus) + Redis access, Lua script loading
  config/           Redis / MyBatis / scheduling configuration
  common/           shared result types, error codes, utilities
wrk-seckill.lua     load-generation script (random userId per request)
wrk-proof.lua       no-oversell validation script
```

## Tech stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot |
| Persistence | MySQL + MyBatis-Plus |
| Hot path / cache | Redis + Lua |
| Build | Maven |
| Load testing | wrk |
