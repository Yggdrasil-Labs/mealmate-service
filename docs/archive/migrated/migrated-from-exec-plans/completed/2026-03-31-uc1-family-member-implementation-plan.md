# UC1 Family Member Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the UC1 family member backend capability with member-centered modeling, covering schema, domain, repositories, application services, HTTP APIs, and tests.

**Architecture:** The implementation keeps `family` as the grouping context and treats `FamilyMember` plus `MemberPreference` as the main business objects. The solution follows COLA layering strictly: adapter for protocol conversion and COLA responses, app for orchestration, domain for rules, and infrastructure for persistence and mapping. Database changes are delivered only through Flyway migrations under `mealmate-start`.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Maven Wrapper, COLA 5.0, MyBatis-Plus, MapStruct, Lombok, Jakarta Validation, JUnit

---

### Task 1: Add Database Migration Scripts

**Files:**
- Create: `mealmate-start/src/main/resources/db/migration/V{next_version}__create_family_member_tables.sql`

**Step 1: Determine the next Flyway version**

Check existing migrations under:

- `mealmate-start/src/main/resources/db/migration/`

Choose the next available version number before creating the script.

**Step 2: Write the Flyway migration**

Add DDL for:

- `family_profile`
- `family_member`
- `member_preference`

Include:

- primary keys
- unique key for `family_code`
- unique key for `member_preference.member_id`
- indexes for family/member lookup
- audit fields and logical delete fields on `family_profile` and `family_member`
- physical-delete table shape for `member_preference`

**Step 3: Review naming and constraints**

Check:

- no `age` column
- `member_preference` remains one-to-one with member
- `BABY` is not constrained to unique
- column names follow lower_snake_case
- index names follow `uk_*` and `idx_*`

**Step 4: Validate migration content manually**

Run:

```bash
ls mealmate-start/src/main/resources/db/migration
sed -n '1,260p' mealmate-start/src/main/resources/db/migration/V{next_version}__create_family_member_tables.sql
```

Expected:

- DDL matches design
- Flyway script is additive and self-contained

### Task 2: Verify Flyway Runtime Integration

**Files:**
- Modify: `mealmate-start/pom.xml` if Flyway dependency is missing
- Modify: `mealmate-start/src/main/resources/application*.yml` if Flyway needs explicit enablement
- Create or modify: startup/integration test files as needed

**Step 1: Check whether Flyway is already enabled**

Inspect:

- `mealmate-start/pom.xml`
- `mealmate-start/src/main/resources/application.yml`
- `mealmate-start/src/main/resources/application-test.yml`

Confirm:

- Flyway dependency or parent-managed support exists
- migration path is the Spring Boot default or explicitly configured

**Step 2: Add a migration smoke test**

Create a minimal integration test in `mealmate-start` that boots against a test database and verifies:

- application startup succeeds
- the new migration is applied
- `family_profile`, `family_member`, and `member_preference` are present

**Step 3: Run the smoke test**

Run:

```bash
./mvnw -q -pl mealmate-start test -Dtest=*Flyway*Test
```

Expected:

- PASS
- startup applies the migration without manual SQL execution

### Task 3: Define Domain Model and Enums

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/FamilyProfile.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/FamilyMember.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/MemberPreference.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/FamilyStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/MemberStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/MemberRoleType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/GenderType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/MemberTargetType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/SpicyLevel.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/SweetLevel.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/OilLevel.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/model/enums/SaltLevel.java`

**Step 1: Write the failing domain test skeleton**

Create test file:

- `mealmate-domain/src/test/java/io/yggdrasil/labs/mealmate/domain/family/service/FamilyDomainServiceTest.java`

Add test cases for:

- baby spicy preference rejection
- baby salty preference rejection
- baby rich oil preference rejection
- list cleanup for duplicates and blanks

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=FamilyDomainServiceTest
```

Expected:

- FAIL because domain classes and service do not exist yet

**Step 3: Write minimal domain objects**

Use Lombok:

- `@Data` or `@Getter/@Builder`
- `@NoArgsConstructor`
- `@AllArgsConstructor`

Model details:

- `FamilyMember` contains member identity and profile fields
- `MemberPreference` uses `List<String>` and `Map<String, Object>`
- enums contain canonical storage codes

**Step 4: Run domain test again**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=FamilyDomainServiceTest
```

Expected:

- still FAIL until service logic is implemented

### Task 4: Add Repository Interfaces and Domain Service

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/repo/FamilyProfileRepository.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/repo/FamilyMemberRepository.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/repo/MemberPreferenceRepository.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/family/service/FamilyDomainService.java`

**Step 1: Write the failing domain behavior tests**

In `FamilyDomainServiceTest`, add expectations for:

- reject invalid baby preference
- normalize tag collections
- reject member access when `familyId` mismatches

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=FamilyDomainServiceTest
```

Expected:

- FAIL with missing service methods or assertions failing

**Step 3: Implement the minimal service**

Service methods should cover:

- `assertFamilyExists(...)`
- `assertMemberBelongsToFamily(...)`
- `validatePreferenceForMember(...)`
- `normalizePreference(...)`

Keep Spring annotations out of the domain service unless the project already uses componentized domain services as a convention.

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=FamilyDomainServiceTest
```

Expected:

- PASS

### Task 5: Add Persistence DOs and Infra Convertors

**Files:**
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/dataobject/FamilyProfileDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/dataobject/FamilyMemberDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/dataobject/MemberPreferenceDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/convertor/FamilyProfileInfraConvertor.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/convertor/FamilyMemberInfraConvertor.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/convertor/MemberPreferenceInfraConvertor.java`

**Step 1: Write the failing mapping test**

Create:

- `mealmate-infrastructure/src/test/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/convertor/MemberPreferenceInfraConvertorTest.java`

Cover:

- list fields to comma-separated string
- comma-separated string back to list
- JSON map conversion

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure test -Dtest=MemberPreferenceInfraConvertorTest
```

Expected:

- FAIL because DOs and convertors do not exist yet

**Step 3: Implement DOs and MapStruct convertors**

Use:

- Lombok on DOs
- `@Mapper(componentModel = "spring")` for MapStruct convertors
- helper methods for string-list and JSON-map conversion

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure test -Dtest=MemberPreferenceInfraConvertorTest
```

Expected:

- PASS

### Task 6: Implement Repository Infrastructure

**Files:**
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/FamilyProfileRepositoryImpl.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/FamilyMemberRepositoryImpl.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/MemberPreferenceRepositoryImpl.java`
- Create or generate mapper files under `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/mapper/`

**Step 1: Write the failing repository tests**

Create:

- `mealmate-infrastructure/src/test/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/family/FamilyMemberRepositoryImplTest.java`

Cover:

- find family by id
- find members by family id
- find member by id and family id
- save new member
- update preference
- logical delete member

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure test -Dtest=FamilyMemberRepositoryImplTest
```

Expected:

- FAIL because repository implementations are incomplete

**Step 3: Implement repositories**

Repository responsibilities:

- `FamilyProfileRepository`: family existence and basic lookup
- `FamilyMemberRepository`: member queries, save, update, logical delete
- `MemberPreferenceRepository`: find by member id, save, update, delete by member id

Honor project conventions for `@AutoMybatis` if present.

Repository methods should always account for logical delete on `family_profile` and `family_member`.
Delete flow must remove `member_preference` in the same transaction when a member is logically deleted.

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure test -Dtest=FamilyMemberRepositoryImplTest
```

Expected:

- PASS

### Task 7: Add App DTOs, COs, and Convertors

**Files:**
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/cmd/AddFamilyMemberCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/cmd/UpdateFamilyMemberCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/cmd/UpdateMemberPreferenceCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/cmd/RemoveFamilyMemberCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/qry/GetFamilyProfileQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/qry/GetFamilyMemberListQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/qry/GetFamilyMemberDetailQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/co/FamilyProfileCO.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/co/FamilyMemberCO.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/dto/co/MemberPreferenceCO.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/convertor/FamilyMemberConvertor.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/assembler/FamilyMemberAssembler.java`

**Step 1: Write the failing app mapping test**

Create:

- `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/family/assembler/FamilyMemberAssemblerTest.java`

Cover:

- domain member + preference mapped to detail CO
- family mapped to profile CO

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-app test -Dtest=FamilyMemberAssemblerTest
```

Expected:

- FAIL because DTOs and assemblers do not exist

**Step 3: Implement minimal DTOs and MapStruct mapping**

Use Lombok and MapStruct to keep DTO code small and consistent.

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-app test -Dtest=FamilyMemberAssemblerTest
```

Expected:

- PASS

### Task 8: Implement App Executors and Service

**Files:**
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/application/FamilyMemberAppService.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/AddFamilyMemberCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/UpdateFamilyMemberCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/UpdateMemberPreferenceCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/RemoveFamilyMemberCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/GetFamilyProfileQryExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/GetFamilyMemberListQryExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/family/executor/GetFamilyMemberDetailQryExe.java`

**Step 1: Write the failing app integration tests**

Create:

- `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/family/executor/AddFamilyMemberCmdExeTest.java`
- `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/family/executor/UpdateMemberPreferenceCmdExeTest.java`
- `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/family/executor/GetFamilyMemberDetailQryExeTest.java`

Cover:

- add member under valid family
- reject add when family missing
- update baby preference with invalid spicy level
- query member detail returns combined result
- remove member performs logical delete
- remove member also physically deletes `member_preference`

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-app test -Dtest=AddFamilyMemberCmdExeTest,UpdateMemberPreferenceCmdExeTest,GetFamilyMemberDetailQryExeTest
```

Expected:

- FAIL because executors and service are missing

**Step 3: Implement executors and service**

Rules:

- transactions live in app layer
- queries assemble member + preference
- commands call domain service before persistence
- remove-member use case performs logical delete on member and physical delete on preference in one app-layer transaction

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-app test -Dtest=AddFamilyMemberCmdExeTest,UpdateMemberPreferenceCmdExeTest,GetFamilyMemberDetailQryExeTest
```

Expected:

- PASS

### Task 9: Add Adapter Requests, Controller, and Web Convertor

**Files:**
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberController.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/AddFamilyMemberRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/UpdateFamilyMemberRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/UpdateMemberPreferenceRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/FamilyProfileResponse.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/FamilyMemberResponse.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/dto/MemberPreferenceResponse.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/family/convertor/FamilyMemberWebConvertor.java`

**Step 1: Write the failing controller test**

Create:

- `mealmate-adapter/src/test/java/io/yggdrasil/labs/mealmate/adapter/web/family/FamilyMemberControllerTest.java`

Cover:

- get family profile
- get family members
- get member detail
- add member
- update member
- update preference
- delete member

**Step 2: Run test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-adapter test -Dtest=FamilyMemberControllerTest
```

Expected:

- FAIL because controller layer is missing

**Step 3: Implement controller and request conversion**

Requirements:

- validate request with Jakarta Validation
- path uses plural kebab-case resources, centered on `/api/families/{familyId}/members`
- controller only delegates to app service
- no domain logic in controller
- responses use `SingleResponse` and `MultiResponse` instead of raw DTOs

**Step 4: Run test to verify it passes**

Run:

```bash
./mvnw -q -pl mealmate-adapter test -Dtest=FamilyMemberControllerTest
```

Expected:

- PASS

### Task 10: Run Cross-Module Verification

**Files:**
- Modify: any files needed based on test failures

**Step 1: Run targeted tests**

Run:

```bash
./mvnw -q -pl mealmate-domain test
./mvnw -q -pl mealmate-infrastructure test
./mvnw -q -pl mealmate-app test
./mvnw -q -pl mealmate-adapter test
./mvnw -q -pl mealmate-start test
```

Expected:

- PASS in touched modules

**Step 2: Run full compile verification**

Run:

```bash
./mvnw clean verify
```

Expected:

- BUILD SUCCESS

**Step 3: Fix any breakages minimally**

Only adjust:

- imports
- bean wiring
- MapStruct generation issues
- test fixtures

---

Plan complete and archived at `docs/exec-plans/completed/2026-03-31-uc1-family-member-implementation-plan.md`. Two execution options:

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
