SeatSure
A concurrency-safe event ticket booking platform — built to solve the double-booking race condition properly, not paper over it.
![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)
![Backend](https://img.shields.io/badge/api-online-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen)
![React](https://img.shields.io/badge/React-Vite-61DAFB)
🔗 Live Demo — no signup required, demo accounts below
---
The problem
Ticket-booking systems have one genuinely hard problem to get right: what happens when two people click "book" on the same seat at the same moment?
Most naive implementations get this wrong — a "check availability, then write" flow is a textbook race condition, and it's how real ticketing platforms end up selling the same seat twice. SeatSure solves this at the database level with pessimistic locking, and — rather than just asserting that — ships with a live, one-click demo that fires two real, simultaneous booking requests and shows you, in real time, that exactly one succeeds.
> Open any event on the live demo and click **⚡ Run Demo** on its seat map to see it for yourself.
---
Demo accounts
Role	Email	Password	Can do
Customer	`demo@seatsure.com`	`demo1234`	Browse events, hold & book seats
Organizer	`organizer@seatsure.com`	`organizer1234`	Everything above, plus create events & define seat maps
---
Features
Booking & concurrency
Pessimistic and optimistic locking strategies, both implemented and directly comparable
Realistic hold → confirm → auto-expire lifecycle, with a scheduled background job releasing abandoned holds
Zero double-bookings, proven by an automated 20-thread concurrency test — not just claimed
Security
Stateless JWT authentication with a custom Spring Security filter chain
BCrypt password hashing
Role-based authorization (customer vs. organizer)
Rate limiting on booking endpoints (token bucket algorithm)
Identity derived exclusively from the authenticated token — never trusts a client-supplied user ID
Performance & architecture
Redis-backed caching for high-traffic reads, with explicit invalidation on writes
Kafka producer/consumer architecture decoupling booking confirmation from downstream side effects
Layered backend (Controller → Service → Repository) with DTOs isolating the API from persistence
Frontend
React + Vite, with client-side routing (React Router)
Live-polling seat maps that reflect real-time seat status
Role-aware UI (organizer-only event creation)
Built-in live concurrency demo, streaming real request outcomes
Quality
Unit tests (JUnit 5, Mockito) covering core business rules
Integration tests against a real, disposable PostgreSQL instance (Testcontainers)
A dedicated concurrency load test simulating 20 simultaneous users racing for one seat
Fully containerized (multi-stage Docker build + Docker Compose) for one-command local orchestration
---
Tech stack
Layer	Technologies
Backend	Java 21, Spring Boot 4, Spring Security, Spring Data JPA (Hibernate)
Database	PostgreSQL
Caching	Redis
Messaging	Apache Kafka
Frontend	React, Vite, React Router, Tailwind CSS
Testing	JUnit 5, Mockito, Testcontainers
Infrastructure	Docker, Docker Compose, Railway, Vercel
---
Architecture
```
┌─────────────┐        HTTPS/JSON        ┌──────────────────┐
│   React     │ ───────────────────────▶ │   Spring Boot     │
│  (Vercel)   │ ◀─────────────────────── │   REST API         │
└─────────────┘          JWT             └────────┬──────────┘
                                                   │
                       ┌───────────────────────────┼───────────────────────────┐
                       ▼                           ▼                           ▼
                ┌─────────────┐            ┌─────────────┐            ┌─────────────┐
                │ PostgreSQL  │            │    Redis    │            │    Kafka    │
                │  bookings,  │            │   event &   │            │  booking    │
                │  seats,     │            │   seat       │            │  confirmed  │
                │  users      │            │   caching    │            │  events     │
                └─────────────┘            └─────────────┘            └─────────────┘
```
---
Getting started locally
Prerequisites
Java 21+, Maven
Node.js 18+
Docker & Docker Compose
Backend
```bash
git clone <this-repo-url>
cd seatsure
docker-compose up --build
```
Starts the API alongside PostgreSQL, Redis, and Kafka. Available at `http://localhost:8081`.
Frontend
```bash
git clone <frontend-repo-url>
cd seatsure-frontend
npm install
npm run dev
```
Runs at `http://localhost:5173`.
Running the test suite
```bash
./mvnw test
```
Includes the concurrency load test (`BookingConcurrencyTest`) — the test that actually proves the core claim of this project.
---
API overview
Method	Endpoint	Description	Auth
POST	`/api/auth/register`	Create an account	—
POST	`/api/auth/login`	Authenticate, receive a JWT	—
GET	`/api/events`	List events	—
GET	`/api/events/{id}/seats`	View seat map	—
POST	`/api/events`	Create an event	Organizer/Admin
POST	`/api/events/{id}/seats`	Define seats	Organizer/Admin
POST	`/api/bookings/hold`	Hold a seat	Authenticated
POST	`/api/bookings/{id}/confirm`	Confirm a held booking	Authenticated
---
License
MIT
Author
Built by Lokesh — LinkedIn
