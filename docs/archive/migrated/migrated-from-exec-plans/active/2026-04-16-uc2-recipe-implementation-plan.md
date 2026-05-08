# UC2 Recipe Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the UC2 recipe backend capability with recipe-centered modeling, covering schema, domain rules, persistence, application services, HTTP APIs, and tests.

**Architecture:** The implementation keeps `Recipe` as the aggregate root and models ingredients, steps, and nutrition as internal child objects/value objects. The solution follows the existing COLA layering already used by UC1: adapter for protocol conversion, app for orchestration and transaction boundaries, domain for invariants, and infrastructure for persistence assembly across four tables.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Maven Wrapper, COLA 5.0, MyBatis-Plus, MapStruct, Lombok, Jakarta Validation, JUnit

---

### Task 1: Add Flyway Migration for Recipe Tables

**Files:**
- Create: `mealmate-start/src/main/resources/db/migration/V4__create_recipe_tables.sql`
- Modify: `mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/FlywayMigrationSmokeTest.java`

**Step 1: Write the migration script**

Add DDL for:

- `recipe`
- `recipe_ingredient`
- `recipe_step`
- `recipe_nutrition`

Include:

- primary keys
- unique key for `recipe_nutrition.recipe_id`
- unique key for `recipe_step(recipe_id, step_no)`
- logical delete field on `recipe`
- audit fields on all tables
- indexes for name, type, season + crowd, status, recipe ID lookups

**Step 2: Write the failing migration smoke assertions**

Extend `FlywayMigrationSmokeTest` to verify:

- the four UC2 tables exist
- `recipe.id`, `recipe_ingredient.id`, `recipe_step.id`, `recipe_nutrition.id` are identity columns

**Step 3: Run the smoke test to verify migration wiring**

Run:

```bash
./mvnw -q -pl mealmate-start -am test -Dtest=FlywayMigrationSmokeTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS
- H2 test profile applies the new migration automatically

### Task 2: Add Domain Model, Enums, and Domain Service Tests

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/Recipe.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/RecipeIngredient.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/RecipeStep.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/NutritionFact.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/RecipeType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/RecipeSourceType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/SeasonTag.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/CrowdTag.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/DifficultyLevel.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/IngredientType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/model/enums/RecipeStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/service/RecipeDomainService.java`
- Test: `mealmate-domain/src/test/java/io/yggdrasil/labs/mealmate/domain/recipe/service/RecipeDomainServiceTest.java`

**Step 1: Write the failing domain tests**

Cover:

- normalize taste tags by trimming blanks and removing duplicates
- reject empty ingredient list
- normalize ingredient sort order starting at 1
- normalize step numbering starting at 1
- auto-set `isBabyFriendly` when `crowdTag = BABY`
- reject negative nutrition values
- reject editing or deleting system recipes

**Step 2: Run the domain test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=RecipeDomainServiceTest
```

Expected:

- FAIL because the UC2 domain types and service do not exist yet

**Step 3: Implement the minimal domain model and service**

Use Lombok for the model classes and keep `RecipeDomainService` framework-free, following `FamilyDomainService`.

**Step 4: Run the domain test again**

Run:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=RecipeDomainServiceTest
```

Expected:

- PASS

### Task 3: Add Repository Interface and App Wiring Bean

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/repo/RecipeRepository.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/application/RecipeAppConfiguration.java`

**Step 1: Define the repository contract**

Add methods for:

- `findById`
- `findByName`
- `page`
- `searchByKeyword`
- `save`
- `update`
- `updateIngredients`
- `updateSteps`
- `updateNutrition`
- `updateStatus`
- `logicalDeleteById`

**Step 2: Add the recipe domain service bean**

Mirror the UC1 pattern in `FamilyAppConfiguration` and expose `RecipeDomainService` as a Spring bean from `RecipeAppConfiguration`.

**Step 3: Run module compilation**

Run:

```bash
./mvnw -q -pl mealmate-app -am test -DskipTests -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- compilation succeeds for the new interfaces and configuration

### Task 4: Add Persistence DOs and Infra Convertors

**Files:**
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/dataobject/RecipeDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/dataobject/RecipeIngredientDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/dataobject/RecipeStepDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/dataobject/RecipeNutritionDO.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/convertor/RecipeInfraConvertor.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/convertor/RecipeIngredientInfraConvertor.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/convertor/RecipeStepInfraConvertor.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/convertor/RecipeNutritionInfraConvertor.java`
- Test: `mealmate-infrastructure/src/test/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/convertor/RecipeInfraConvertorTest.java`

**Step 1: Write the failing convertor tests**

Cover:

- enum-to-string conversion and back
- `tasteTag` comma-separated storage mapping
- `nutritionJson` map conversion
- ingredient list item mapping

**Step 2: Run the convertor test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeInfraConvertorTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- FAIL because UC2 DOs and convertors do not exist yet

**Step 3: Implement the DOs and MapStruct convertors**

Follow the existing `family` convertor style and use named methods for enum/string conversion.

**Step 4: Run the convertor test again**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeInfraConvertorTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

### Task 5: Add Repository Implementation and Persistence Tests

**Files:**
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/RecipeRepositoryImpl.java`
- Create: `mealmate-infrastructure/src/test/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/RecipeRepositoryImplTest.java`

**Step 1: Write the failing repository tests**

Cover:

- find recipe by ID with children assembled
- page query returns ordered recipe summaries
- page query applies `recipeType`、`seasonTag`、`crowdTag`、`isBabyFriendly`、`isWeightLossFriendly`、`difficultyLevel`、`maxCookingTime` filters correctly
- search query returns keyword matches
- save recipe writes main record and child records
- update ingredients replaces old rows
- update steps replaces old rows
- update nutrition upserts one-to-one row
- logical delete marks only the main table row as deleted and physically removes ingredient/step/nutrition child rows

**Step 2: Run the repository test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- FAIL because the repository implementation does not exist yet

**Step 3: Implement the repository**

Follow the UC1 repository style:

- use MyBatis-Plus generated services for CRUD
- assemble aggregate detail inside `RecipeRepositoryImpl`
- keep child table replacement logic in infrastructure, not in adapter/app

**Step 4: Run the repository test again**

Run:

```bash
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

### Task 6: Add App DTOs, Convertors, Assemblers, and Executors

**Files:**
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/application/RecipeAppService.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/assembler/RecipeAssembler.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/convertor/RecipeConvertor.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/CreateRecipeCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/UpdateRecipeCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/UpdateRecipeIngredientsCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/UpdateRecipeStepsCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/UpdateRecipeNutritionCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/UpdateRecipeStatusCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/cmd/DeleteRecipeCmd.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/qry/PageRecipeQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/qry/GetRecipeDetailQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/qry/SearchRecipeQry.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/co/RecipeCO.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/dto/co/RecipeDetailCO.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/CreateRecipeCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeIngredientsCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeStepsCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeNutritionCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeStatusCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/DeleteRecipeCmdExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/PageRecipeQryExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/GetRecipeDetailQryExe.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/executor/SearchRecipeQryExe.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/CreateRecipeCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeIngredientsCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeStepsCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeNutritionCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/UpdateRecipeStatusCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/DeleteRecipeCmdExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/GetRecipeDetailQryExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/PageRecipeQryExeTest.java`
- Test: `mealmate-app/src/test/java/io/yggdrasil/labs/mealmate/app/recipe/executor/SearchRecipeQryExeTest.java`

**Step 1: Write the failing executor tests**

Cover:

- create recipe rejects duplicate names
- create recipe saves normalized ingredients, steps, and nutrition
- page query applies all agreed filters
- search query returns lightweight keyword matches
- update basics rejects system recipes
- update ingredients rejects system recipes
- update steps rejects system recipes
- update nutrition rejects system recipes
- update status rejects system recipes
- delete rejects system recipes
- get detail returns assembled detail object

**Step 2: Run the app tests to verify they fail**

Run:

```bash
./mvnw -q -pl mealmate-app -am test -Dtest=CreateRecipeCmdExeTest,UpdateRecipeCmdExeTest,UpdateRecipeIngredientsCmdExeTest,UpdateRecipeStepsCmdExeTest,UpdateRecipeNutritionCmdExeTest,UpdateRecipeStatusCmdExeTest,DeleteRecipeCmdExeTest,PageRecipeQryExeTest,SearchRecipeQryExeTest,GetRecipeDetailQryExeTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- FAIL because the app-layer UC2 types and executors do not exist yet

**Step 3: Implement the app layer**

Follow the UC1 patterns:

- transactions on command executors
- queries stay side-effect free
- app service only delegates to executors
- convertor handles cmd-to-domain transformations
- assembler handles domain-to-CO transformations

**Step 4: Run the app tests again**

Run:

```bash
./mvnw -q -pl mealmate-app -am test -Dtest=CreateRecipeCmdExeTest,UpdateRecipeCmdExeTest,UpdateRecipeIngredientsCmdExeTest,UpdateRecipeStepsCmdExeTest,UpdateRecipeNutritionCmdExeTest,UpdateRecipeStatusCmdExeTest,DeleteRecipeCmdExeTest,PageRecipeQryExeTest,SearchRecipeQryExeTest,GetRecipeDetailQryExeTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

### Task 7: Add Adapter HTTP API, Web DTOs, and Controller Tests

**Files:**
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/RecipeController.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/convertor/RecipeWebConvertor.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/RecipePageRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/RecipeSearchRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/CreateRecipeRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/UpdateRecipeRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/UpdateRecipeIngredientsRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/UpdateRecipeStepsRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/UpdateRecipeNutritionRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/UpdateRecipeStatusRequest.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/RecipeResponse.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/dto/RecipeDetailResponse.java`
- Test: `mealmate-adapter/src/test/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/RecipeControllerTest.java`

**Step 1: Write the failing controller tests**

Cover:

- get page
- get page with filter query params
- get detail
- search
- search with keyword and limit
- create
- update basics
- update ingredients
- update steps
- update nutrition
- update status
- delete
- request validation failure for empty name or empty ingredient list

**Step 2: Run the controller test to verify it fails**

Run:

```bash
./mvnw -q -pl mealmate-adapter -am test -Dtest=RecipeControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- FAIL because the controller and request DTOs do not exist yet

**Step 3: Implement the controller and web convertor**

Follow the UC1 controller style:

- `SingleResponse` for detail/create
- `PageResponse` for page query
- `MultiResponse` for search
- `Response` for void writes
- OpenAPI annotations on every endpoint

**Step 4: Run the controller test again**

Run:

```bash
./mvnw -q -pl mealmate-adapter -am test -Dtest=RecipeControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

### Task 8: Add Start-Layer Integration Tests and OpenAPI Coverage

**Files:**
- Create: `mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/CreateRecipeApiIntegrationTest.java`
- Modify: `mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/openapi/OpenApiDocumentationIntegrationTest.java`

**Step 1: Write the failing integration test**

Cover:

- create recipe via HTTP API
- assert one row inserted into `recipe`
- assert related rows inserted into `recipe_ingredient`, `recipe_step`, and `recipe_nutrition`
- query recipe page with at least one filter condition and assert only matching rows return

**Step 2: Extend OpenAPI assertions**

Verify the generated `/v3/api-docs` contains:

- `/api/recipes`
- `/api/recipes/{recipeId}`
- `/api/recipes/search`
- `/api/recipes/{recipeId}/ingredients`
- `/api/recipes/{recipeId}/steps`
- `/api/recipes/{recipeId}/nutrition`
- page query parameters such as `keyword`、`recipeType`、`seasonTag`、`crowdTag`、`isBabyFriendly`、`isWeightLossFriendly`、`difficultyLevel`、`maxCookingTime`
- search query parameters `keyword` and `limit`

**Step 3: Run the start-layer tests**

Run:

```bash
./mvnw -q -pl mealmate-start -am test -Dtest=CreateRecipeApiIntegrationTest,OpenApiDocumentationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS

### Task 9: Run Focused Regression Verification

**Files:**
- No code changes expected

**Step 1: Run UC2 module tests**

Run:

```bash
./mvnw -q -pl mealmate-domain,mealmate-infrastructure,mealmate-app,mealmate-adapter,mealmate-start -am test -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:

- PASS for the touched modules

**Step 2: Run full repository verification if time permits**

Run:

```bash
./mvnw -q test
```

Expected:

- PASS
- if unrelated failures appear, capture them separately and do not conflate them with UC2

**Step 3: Update plan status**

Before finishing, update:

- `docs/exec-plans/active/2026-04-16-uc2-recipe-design.md`
- `docs/exec-plans/active/2026-04-16-uc2-recipe-implementation-plan.md`

Record:

- completed tasks
- verification commands actually run
- any follow-up debt discovered during implementation

---

## Execution Status

### 2026-04-18

- Task 1 completed: added `V4__create_recipe_tables.sql` and extended Flyway smoke assertions for UC2 tables and identity columns
- Task 2 completed: added recipe aggregate, child objects, enums, and `RecipeDomainService`
- Task 3 completed: added `RecipeRepository` contract and `RecipeAppConfiguration`
- Task 4 completed: added recipe persistence DOs and infra convertors
- Task 5 completed: added `RecipeRepositoryImpl` and persistence integration tests
- Task 6 completed: added app DTOs, convertors, assemblers, executors, and app service
- Task 7 completed: added recipe controller, web DTOs, web convertor, and controller tests
- Task 8 completed: added HTTP integration test for recipe create/page flows and extended OpenAPI coverage
- Task 9 completed: passed touched-module regression and full repository regression

## Verification Record

Actual commands run during implementation:

```bash
./mvnw -q -pl mealmate-domain test -Dtest=RecipeDomainServiceTest
./mvnw -q -pl mealmate-app -am -Dmaven.test.skip=true compile
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeInfraConvertorTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q -pl mealmate-infrastructure -am test -Dtest=RecipeRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q -pl mealmate-app -am test -Dtest=CreateRecipeCmdExeTest,UpdateRecipeCmdExeTest,UpdateRecipeIngredientsCmdExeTest,UpdateRecipeStepsCmdExeTest,UpdateRecipeNutritionCmdExeTest,UpdateRecipeStatusCmdExeTest,DeleteRecipeCmdExeTest,PageRecipeQryExeTest,SearchRecipeQryExeTest,GetRecipeDetailQryExeTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q -pl mealmate-adapter -am test -Dtest=RecipeControllerTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q -pl mealmate-start -am test -Dtest=CreateRecipeApiIntegrationTest,OpenApiDocumentationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q -pl mealmate-domain,mealmate-infrastructure,mealmate-app,mealmate-adapter,mealmate-start -am test -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.test.skip=false
./mvnw -q test
```

Result:

- all commands above passed on 2026-04-18

## Follow-up Debt

- start-layer recipe integration tests use a recipe-focused minimal test application to avoid unrelated `family` context loading and classpath noise in this repository state
- OpenAPI integration test manually wires controller beans instead of using direct `@Import(SomeController.class)` because the latter triggered classpath loading issues in the custom test application
