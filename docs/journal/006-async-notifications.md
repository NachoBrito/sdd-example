# Feature Journal: Asynchronous Notifications

This document records the development process for the asynchronous notifications feature.
The feature publishes order events to RabbitMQ and delivers email notifications through SMTP.

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow.

First, the agent created the feature specification.
The specification defines asynchronous event publishing, consumer processing, and SMTP delivery.
The user clarified the event triggers, the transport isolation, and the HTML email templates.

Next, the agent created the implementation plan and the task list.
The design followed hexagonal architecture and domain-driven design principles.

Then, the agent implemented the software layers in sequence:
- The agent created the domain models (`OrderLifecycleEvent`, `OrderEventType`, `NotificationChannel`, `NotificationMessage`).
- The agent created the application ports (`OrderEventPublisherPort`, `NotificationTransportPort`, `ProcessNotificationUseCase`).
- The agent created the wire DTO records and RabbitMQ constants in `adapter.messaging`.
- The agent implemented the RabbitMQ channel initializer, client, and event publisher adapter.
- The agent updated `OrderService` to publish events when an order is created, updated, or cancelled.
- The agent implemented the inbound RabbitMQ consumer adapter.
- The agent built the HTML template renderer and the raw socket SMTP client.
- The agent implemented the SMTP notification transport and the application notification service.
- The agent created unit tests for all components.
- The agent created the integration test suite in `NotificationFlowTestCase`.

Finally, the agent executed the quality checks and verified the acceptance criteria.

## 2. Bugs Found

The agent identified four technical bugs during implementation.

### ArchUnit Domain Framework-Free Violation
The initial design declared `@SerdeImport` annotations on the messaging client targeting domain records.
The Micronaut annotation processor generated serde classes inside the domain package.
The generated classes depended on Micronaut framework packages.
This violated the ArchUnit rule that requires a framework-free domain model.
The agent resolved this issue by creating dedicated `@Serdeable` DTO records in `adapter.messaging`.
The adapters convert between wire DTOs and pure domain models at the architectural boundary.

### Inbound Adapter Dependency on Outbound Adapter
The initial consumer class imported queue constants from `RabbitMqChannelInitializer` in `adapter.out.messaging`.
This import violated the ArchUnit rule that prevents inbound adapters from depending on outbound adapters.
The agent extracted shared messaging constants to `RabbitMqConstants` in the parent `adapter.messaging` package.
Both inbound and outbound adapters now depend only on the neutral package.

### Mismatched Order Line Total in Template Test
The test fixture in `SmtpOrderHtmlTemplateTest` created an order with invalid item sums.
The domain validation in the `Order` constructor rejected the invalid aggregate.
The agent corrected the line calculation in the test fixture.
The template tests executed successfully after this correction.

### Missing Parameter in Address DTO Constructor
The initial `AddressPayloadDto` mapped invoice addresses with eight parameters instead of nine.
The compiler rejected the call because `InvoiceAddress` requires an optional VAT number parameter.
The agent added the VAT number field to `AddressPayloadDto`.
The agent passed the VAT number to the domain record constructor.

## 3. Static Code Analysis Issues

The static code analysis tools identified multiple violations across the codebase.

### Checkstyle Violations
Checkstyle reported lines that exceeded 100 characters in DTOs and test classes.
The agent wrapped method calls, annotations, and parameter lists.
Checkstyle flagged acronym capitalization in test method names (`throwsIOException`).
The agent renamed the methods to follow the standard pattern (`throwsIoException`).
Checkstyle also rejected text block formatting in the integration test.
The agent replaced text blocks with structured string concatenation.

### PMD Violations
PMD reported violations across several rules:
- **GuardLogStatement**: Log statements lacked level guards. The agent added `isInfoEnabled` and `isErrorEnabled` guards.
- **AvoidCatchingGenericException**: `NotificationService` caught `RuntimeException`. The agent replaced it with specific exception types.
- **AvoidLiteralsInIfCondition**: Test assertions used literal strings. The agent extracted command constants.
- **AvoidSynchronizedStatement**: The test socket mock used synchronized blocks. The agent replaced them with `ReentrantLock`.
- **AvoidReassigningLoopVariables**: Reader loops reassigned variables inside condition checks. The agent rewrote the loops.
- **AppendCharacterWithChar**: The template builder appended single-character strings. The agent replaced them with character literals.
- **ImplicitFunctionalInterface**: `ProcessNotificationUseCase` lacked the `@FunctionalInterface` annotation. The agent added the annotation.
- **AvoidShortMethodNames**: The factory method name `of` was too short. The agent renamed the method to `from`.

### SpotBugs Violations
SpotBugs reported vulnerabilities and coding risks:
- **CT_CONSTRUCTOR_THROW**: SpotBugs flagged constructors that throw exceptions in non-final classes. The agent removed exception checks from injection constructors.
- **EI_EXPOSE_REP and EI_EXPOSE_REP2**: SpotBugs detected mutable list exposure in DTO classes. The agent wrapped collections with `List.copyOf` and `Collections.unmodifiableList`.
- **NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE**: SpotBugs detected a possible null dereference on optional address fields. The agent added explicit null checks.
- **RV_RETURN_VALUE_IGNORED_BAD_PRACTICE**: The consumer test ignored the return value of `BlockingQueue.offer`. The agent replaced the call with `queue.add`.
- **THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION**: Test methods declared `throws Exception`. The agent replaced declarations with specific exception classes.

## 4. Test Coverage and Mutation Testing

The project requires a minimum of 80 percent line coverage.
The project also requires a minimum of 90 percent mutation score.

The initial mutation testing score was 86 percent.
Surviving mutations existed in `SmtpClient`, `SmtpOrderHtmlTemplate`, and messaging DTO classes.

The agent added focused unit tests to kill surviving mutants:
- The agent added tests for SMTP header formatting (`From`, `To`, `Subject`, `MIME-Version`, `Content-Type`) in `SmtpClientTest`.
- The agent added tests for multiline SMTP response parsing and socket termination in `SmtpClientTest`.
- The agent added tests for formatted amounts and status messages in `SmtpOrderHtmlTemplateTest`.
- The agent added boundary validation tests in `OrderEventDtoTest` and `NotificationMessageTest`.

The final verification achieved:
- **Line coverage**: 98 percent (687 of 699 lines covered).
- **Mutation score**: 91 percent (383 of 420 mutations killed).

All automated checks passed successfully.

