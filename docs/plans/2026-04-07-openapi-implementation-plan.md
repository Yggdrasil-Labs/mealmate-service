# OpenAPI Interface Documentation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a minimal but complete OpenAPI 3.x documentation capability for the current HTTP APIs so AI tools can consume accurate interface definitions from `/v3/api-docs`.

**Architecture:** The implementation keeps COLA boundaries intact by adding `springdoc-openapi` only in `mealmate-start` and placing API semantics only in `mealmate-adapter`. `mealmate-start` handles framework assembly and OpenAPI metadata, while `adapter.web.family` adds operation, request, response, and enum descriptions that improve machine readability without changing business behavior.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Maven Wrapper, COLA 5.0, springdoc-openapi, Jakarta Validation, JUnit

---

### Task 1: Inspect Existing Web and Test Baseline

**Files:**
- Review: `mealmate-start/pom.xml`
- Review: `mealmate-start/src/main/resources/application.yml`
- Review: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberController.java`
- Review: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/`
- Review: `mealmate-start/src/test/java/` if present

**Step 1: Confirm current HTTP API scope**

Inspect:

- `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberController.java`

Verify the currently exposed endpoints:

- `GET /api/families/{familyId}`
- `GET /api/families/{familyId}/members`
- `GET /api/families/{familyId}/members/{memberId}`
- `POST /api/families/{familyId}/members`
- `PUT /api/families/{familyId}/members/{memberId}`
- `PUT /api/families/{familyId}/members/{memberId}/preference`
- `DELETE /api/families/{familyId}/members/{memberId}`

**Step 2: Confirm no existing OpenAPI integration**

Run:

```bash
rg -n "springdoc|openapi|swagger" -S .
```

Expected:

- No existing SpringDoc or Swagger integration is present

**Step 3: Check existing Spring Boot test support**

Inspect the `mealmate-start` test sources and existing testing style before adding a new OpenAPI integration test.

### Task 2: Add SpringDoc Dependency and Minimal Configuration

**Files:**
- Modify: `mealmate-start/pom.xml`
- Create: `mealmate-start/src/main/java/io/yggdrasil/labs/mealmate/start/config/OpenApiConfig.java`
- Modify: `mealmate-start/src/main/resources/application.yml` if an explicit path configuration is needed

**Step 1: Add the SpringDoc starter dependency**

Update `mealmate-start/pom.xml` to include the Spring MVC OpenAPI starter with Swagger UI support.

Target dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

If the parent BOM does not manage this version, add the version in the safest place already used by the repo.

**Step 2: Add the OpenAPI configuration class**

Create `mealmate-start/src/main/java/io/yggdrasil/labs/mealmate/start/config/OpenApiConfig.java`.

Add:

- a Spring `@Configuration`
- an `OpenAPI` bean with title, description, and version
- optionally a `GroupedOpenApi` bean limited to `/api/**` and `adapter.web`

Recommended metadata:

- title: `MealMate Service API`
- description: `HTTP API documentation for MealMate family meal planning workflows.`
- version: use a stable string or a property-backed value

**Step 3: Keep configuration minimal**

Only add explicit properties to `application.yml` if needed to make the endpoint path obvious or stable.

Preferred defaults:

- `/v3/api-docs`
- `/swagger-ui/index.html`

### Task 3: Document the Family Controller Operations

**Files:**
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberController.java`

**Step 1: Add controller-level tagging**

Add a `@Tag` annotation to identify this controller as the `Family` API group.

**Step 2: Add operation-level descriptions**

For each handler method, add:

- `@Operation(summary = ..., description = ...)`

Ensure every summary is short and every description explains the business meaning rather than repeating the method name.

Suggested operation intents:

- get family profile
- list family members
- get member detail
- add member
- update member
- update member preference
- remove member

**Step 3: Add path parameter descriptions where needed**

Use `@Parameter` on `familyId` and `memberId` when the generated output would otherwise be unclear.

Describe:

- `familyId`: unique identifier of the family
- `memberId`: unique identifier of the family member

### Task 4: Document Request and Response DTOs

**Files:**
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/AddFamilyMemberRequest.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/UpdateFamilyMemberRequest.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/UpdateMemberPreferenceRequest.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/FamilyProfileResponse.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/FamilyMemberResponse.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/MemberPreferenceResponse.java`

**Step 1: Add model-level descriptions**

Add `@Schema(description = "...")` to each request and response DTO.

**Step 2: Add field-level descriptions**

For each core field, add:

- `description`
- `example` where useful

Focus on fields that AI consumers must understand correctly:

- member identity fields
- role and target fields
- birthday and status fields
- preference list fields
- taste level fields

**Step 3: Preserve validation semantics**

Do not alter existing Jakarta Validation behavior while adding documentation annotations.

### Task 5: Document Enum Semantics

**Files:**
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/FamilyStatus.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/MemberStatus.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/MemberRoleType.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/GenderType.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/MemberTargetType.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/SpicyLevel.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/SweetLevel.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/OilLevel.java`
- Modify: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums/SaltLevel.java`

**Step 1: Add enum-level descriptions**

Use `@Schema(description = "...")` to explain the business meaning of each enum.

**Step 2: Verify serialized values stay unchanged**

Do not rename enum constants or alter Jackson serialization behavior.

The goal is documentation clarity only.

### Task 6: Add an Integration Test for `/v3/api-docs`

**Files:**
- Create: `mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/openapi/OpenApiDocumentationIntegrationTest.java`
- Modify: `mealmate-start/src/test/resources/` only if extra test configuration is required

**Step 1: Write the failing integration test**

Create a Spring Boot test that starts the web context and requests `/v3/api-docs`.

Assertions should include:

- response status is `200`
- response body contains `/api/families/{familyId}`
- response body contains `/api/families/{familyId}/members`
- response body contains `Family`

**Step 2: Run the test to verify it fails**

Run:

```bash
./mvnw -q test -pl mealmate-start -am -Dtest=OpenApiDocumentationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- FAIL because SpringDoc is not wired yet or OpenAPI endpoints are unavailable

**Step 3: Implement the minimal changes needed**

Wire the dependency and config from Tasks 2 to make the test pass.

**Step 4: Run the test again**

Run:

```bash
./mvnw -q test -pl mealmate-start -am -Dtest=OpenApiDocumentationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS with valid OpenAPI JSON returned

### Task 7: Review Generated Schema Quality

**Files:**
- Review generated `/v3/api-docs` output from local test or runtime

**Step 1: Check response wrappers**

Verify that `SingleResponse`, `MultiResponse`, and `Response` render clearly enough for:

- success flag or equivalent status fields
- `data` payload
- list payloads

**Step 2: Check DTO and enum readability**

Verify generated docs include:

- DTO descriptions
- field descriptions
- enum values

**Step 3: Apply only minimal fixes**

If schema readability is weak, add the smallest possible annotation-based improvements.

Do not add custom converters or broad schema rewriting in this iteration.

### Task 8: Run Final Verification

**Files:**
- Verify the modified files from Tasks 2 through 6

**Step 1: Run module-scoped verification**

Run:

```bash
./mvnw -q test -pl mealmate-start -am -Dtest=OpenApiDocumentationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

**Step 2: Run a broader compile check if the DTO annotations touched shared code**

Run:

```bash
./mvnw -q -pl mealmate-start -am test -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS or only known unrelated failures

**Step 3: Manually inspect the changed files**

Review:

- dependency placement is only in `mealmate-start`
- OpenAPI config stays in `start`
- API annotations stay in `adapter`
- no business logic was added

### Task 9: Prepare a Focused Commit

**Files:**
- Stage only the OpenAPI-related files

**Step 1: Stage the changes**

Run:

```bash
git add mealmate-start/pom.xml \
  mealmate-start/src/main/java/io/yggdrasil/labs/mealmate/start/config/OpenApiConfig.java \
  mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/openapi/OpenApiDocumentationIntegrationTest.java \
  mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberController.java \
  mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto \
  mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/enums
```

**Step 2: Commit with Conventional Commits**

Run:

```bash
git commit -m "feat(start): add openapi interface documentation"
```

Expected:

- one focused commit describing the new API documentation capability
