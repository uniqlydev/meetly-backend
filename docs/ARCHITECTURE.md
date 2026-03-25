src/main/java/com/meetly/backend
├── MeetlyApplication.java
├── shared
│   ├── domain
│   │   ├── BaseEntity.java
│   │   ├── DomainException.java
│   │   └── AggregateRoot.java
│   ├── application
│   │   ├── CurrentUser.java
│   │   └── ApiError.java
│   ├── infrastructure
│   │   ├── config
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JpaConfig.java
│   │   │   └── JacksonConfig.java
│   │   └── web
│   │       └── GlobalExceptionHandler.java
│   └── security
│       ├── AuthenticatedUser.java
│       ├── CurrentUserResolver.java
│       └── JwtAuthenticationFilter.java
├── modules
│   ├── identity
│   │   ├── domain
│   │   │   ├── User.java
│   │   │   ├── UserProfile.java
│   │   │   └── UserRepository.java
│   │   ├── application
│   │   │   ├── command
│   │   │   │   └── UpsertUserCommand.java
│   │   │   ├── query
│   │   │   │   └── GetMyProfileQuery.java
│   │   │   ├── IdentityCommandService.java
│   │   │   └── IdentityQueryService.java
│   │   ├── infrastructure
│   │   │   └── persistence
│   │   │       └── JpaUserRepository.java
│   │   └── presentation
│   │       ├── IdentityController.java
│   │       ├── request
│   │       └── response
│   ├── events
│   │   ├── domain
│   │   │   ├── Event.java
│   │   │   ├── EventType.java
│   │   │   ├── EventStatus.java
│   │   │   └── EventRepository.java
│   │   ├── application
│   │   │   ├── command
│   │   │   │   ├── CreateEventCommand.java
│   │   │   │   └── CancelEventCommand.java
│   │   │   ├── query
│   │   │   │   ├── GetEventDetailsQuery.java
│   │   │   │   └── ListEventsQuery.java
│   │   │   ├── EventCommandService.java
│   │   │   └── EventQueryService.java
│   │   ├── infrastructure
│   │   │   └── persistence
│   │   │       └── JpaEventRepository.java
│   │   └── presentation
│   │       ├── EventController.java
│   │       ├── request
│   │       │   └── CreateEventRequest.java
│   │       └── response
│   │           ├── EventDetailsResponse.java
│   │           └── EventSummaryResponse.java
│   ├── participation
│   │   ├── domain
│   │   │   ├── EventParticipant.java
│   │   │   ├── ParticipationStatus.java
│   │   │   └── EventParticipantRepository.java
│   │   ├── application
│   │   │   ├── command
│   │   │   │   ├── JoinEventCommand.java
│   │   │   │   └── ReviewJoinRequestCommand.java
│   │   │   ├── query
│   │   │   │   └── ListEventParticipantsQuery.java
│   │   │   ├── ParticipationCommandService.java
│   │   │   └── ParticipationQueryService.java
│   │   ├── infrastructure
│   │   │   └── persistence
│   │   │       └── JpaEventParticipantRepository.java
│   │   └── presentation
│   │       ├── ParticipationController.java
│   │       ├── request
│   │       │   └── ReviewJoinRequestRequest.java
│   │       └── response
│   │           └── EventParticipantResponse.java
│   └── trust
│       ├── domain
│       │   ├── VerificationStatus.java
│       │   └── SafetyReport.java
│       ├── application
│       ├── infrastructure
│       └── presentation