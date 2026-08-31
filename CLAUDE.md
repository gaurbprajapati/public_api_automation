# RecruitCRM API Automation Code Review — Claude Skill

This file governs how Claude reviews and generates code in this REST API (TestNG + REST Assured) automation repository. Follow every rule precisely.

---

## BRANCH CONVENTION

Main branch is: `upstream/test_stability_pipeline`
Use this for all rebase, diff and PR base operations.

---

## FLOW DETECTION — run this automatically before anything else, never ask the user

If user gives a PR url:
  Run: `gh pr view <url> --json author --jq '.author.login'`
  Run: `gh api user --jq '.login'`
  If both match → current user is the PR author → run Flow 1
  If they differ → current user is a reviewer → run Flow 2

If no PR url given:
  Always run Flow 1 (local changes review)
  Use `git diff` to detect what needs reviewing

Never ask the user which flow — detect it automatically every time.

---

## FLOW 1 — PR AUTHOR (current gh user is the same as PR author)

**Triggers:**
- "review my changes"
- "review my PR: \<url>" (auto detected — PR author matches current gh user)
- Any local review request with no PR url

**Step 1 — Detect what to review:**
- `git fetch upstream` first (always, to get latest `upstream/test_stability_pipeline`)
- Run all three diffs and combine any non-empty output:
  - `git diff HEAD` → unstaged changes
  - `git diff --staged` → staged changes
  - `git diff upstream/test_stability_pipeline...HEAD` → committed, pushed or not
- If all three are empty → tell user there is nothing to review and stop

**Always exclude these files — skip them completely, never review, never comment on them:**
- Any `*.properties` files that change at runtime (environment configs, account configs)
- Files under `target/` — build output, never pushed

**Step 2 — Read local repo for context:**
- Scan full project structure
- Read `src/main/java/io/rcrm/api/testbase/TestBase.java` — base class with `@BeforeClass`, `@AccountType`, `getAccounts()`, `ThreadManager` wiring, URL constants
- Read `src/main/java/io/rcrm/api/restclient/RestClient.java` — HTTP client wrapper with `doGet`, `doPost`, `doPut`, `doDelete`
- Read `src/main/java/com/qa/api/util/Owner.java` — `@Owner` annotation definition
- Read `src/main/java/com/qa/api/util/reaper/ThreadManager.java` — `getAccountApiKey()`, `getAlbatrossToken()`, `getOwnerAlbatrossToken()`
- Read the changed test class to understand its package and structure
- Read `src/test/resources/testrunners/runners/testng.xml` header section (first 100 lines) for existing `<test>` name registry

**Step 3 — Review all changes against checklist:**
- Apply every rule in the checklist below
- Suggest all findings in chat with file, line, severity, issue and fix
- NO comments posted to GitHub at all in this flow
- After findings, show summary table
- Do not ask about posting to GitHub

---

## FLOW 2 — REVIEWER (current gh user is different from PR author)

**Triggers:**
- "review PR: \<url>" (auto detected — PR author differs from current gh user)

**Step 1 — Fetch PR data:**
- `gh pr view <url> --json title,author,headRefName,body` → PR metadata
- `gh pr diff <url>` → full diff of every changed file, no truncation
- If diff is large, process all files — do not skip or summarise any file
- **Always exclude from review:** any `*.properties` runtime config files and `target/` files

**Step 2 — Read local repo for context:**
- Same as Flow 1 Step 2

**Step 3 — Review and suggest:**
- Apply every rule in the checklist below
- Show all findings in chat with file, line, severity, issue and fix
- Show summary table at the end
- Then ask exactly this:
  > "Should I post these to the PR? I will post all by default —
  > tell me the numbers you want to skip, or say post all."

**Step 4 — Post to GitHub (only after reviewer explicitly confirms):**
- Default: post every finding as an inline comment
- If reviewer says "post only critical" → filter to CRITICAL only
- If reviewer says "post 1, 3 and 5" → post only those numbers

**Before posting — validate every line number against the diff:**

GitHub rejects inline comments (HTTP 422) on lines that are outside a diff hunk. Always run this check before posting:

```bash
# Step A — get the raw diff and extract all hunk ranges per file
gh pr diff <url> 2>&1 | grep -E "^\+\+\+ |^@@ "

# Step B — for each finding, confirm its line is in an added (+) line in the diff
gh pr diff <url> 2>&1 | grep -n "^+.*<keyword from finding>"

# Step C — if a line is NOT in any diff hunk (pre-existing violation):
#   → Do NOT add it as an inline comment
#   → Include it in the review body text instead
```

**Rules for posting inline comments:**
1. Only comment on lines that fall **within a diff hunk** (`@@` range) of the PR
2. Always include `"side": "RIGHT"` on every comment — GitHub rejects comments without it
3. For violations on lines **outside the diff** (pre-existing lines not touched by this PR) → add to the overall review `body` with a note explaining the line is outside the diff

**Use JSON input file (not --field flags) for reliability:**

```bash
# Write the review to a JSON file first
cat > /tmp/review.json << 'EOF'
{
  "event": "REQUEST_CHANGES",
  "body": "<overall summary — include any pre-existing violations that could not be posted inline>",
  "comments": [
    {
      "path": "src/path/to/File.java",
      "line": <line number in the new file>,
      "side": "RIGHT",
      "body": "🔴 **CRITICAL — short title**\n\nIssue and fix here."
    }
  ]
}
EOF

gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --method POST \
  --input /tmp/review.json
```

- Event logic:
  Any CRITICAL or WARNING in selected comments → `REQUEST_CHANGES`
  Only SUGGESTIONS or nothing → `APPROVE`
- After posting, verify with: `gh api repos/{owner}/{repo}/pulls/{number}/reviews/{review_id}/comments --jq '[.[] | {path:.path, line:.line, body:.body[:60]}]'`
- Show the PR url and confirm how many comments were successfully attached

---

## GIT OPERATIONS

Support these when user asks naturally — detect intent from any phrasing:

**"fetch latest" / "get latest" / "sync"**
→ `git fetch upstream`
→ Tell user if their branch is behind `upstream/test_stability_pipeline`

**"rebase" / "rebase from main" / "sync my branch"**
→ `git fetch upstream`
→ `git rebase upstream/test_stability_pipeline`
→ If clean → confirm rebase successful
→ If conflicts → STOP immediately
  - Do not attempt to resolve conflicts automatically
  - Show exactly which files have conflicts
  - Show the conflicting sections in each file
  - Tell the user to resolve them manually
  - Tell the user to run `git add <file>` after resolving each file
  - Tell the user to run `git rebase --continue` once all conflicts are resolved
  - Do not proceed further until user confirms conflicts are resolved

**"push" / "push my changes"**
→ `git push origin <current-branch>`
→ If rejected → suggest `git push --force-with-lease`
  Explain: force-with-lease is safe because it only force pushes
  if no one else has pushed to that branch since your last fetch

**"create PR" / "raise PR" / "open PR"**

→ Step 0: Full code review — MANDATORY before anything else. Never skip or abbreviate.

  ⚠️ INTERNAL PROCESS — do this silently. Never print the checklist, gate table, or grep commands to the user. Only findings and the summary table are shown.

  **What to do (internal):**

  1. Detect changes: run `git fetch upstream`, then all three diffs (`git diff HEAD`, `git diff --staged`, `git diff upstream/test_stability_pipeline...HEAD`). Collect every changed `.java` file (exclude `*.properties` and `target/`). If all diffs are empty → tell user "Nothing to review. Proceeding to create the PR." and jump to Step 1.

  2. Read context: read the key reference files listed in the REVIEW CHECKLIST section, and read the full body of each changed test class.

  3. Run the full MANDATORY PRE-REVIEW GREP COMMANDS checklist (C1–C12):
     - Execute every command — do NOT skip any, do NOT assume any returns nothing
     - Every line of output from any command is a potential finding
     - Also check C13 (unused imports): for every `import` line, count occurrences of the class name in the whole file — a count of 1 means it appears only on the import line → unused import
     - Also verify: class name must NOT start with "Test" — must follow `<FeatureOrAction><Entity>Test` pattern

  4. Silently verify every row in the MANDATORY COMPLETION GATE (CR-1 through CR-14, WR-1 through WR-8, SR-1 through SR-4 = 26 rows). Every row must be ✅ or N/A. Every ❌ becomes a finding. If any row has not been checked — stop and check it before continuing.

  5. Write all findings (CRITICAL, WARNING, SUGGESTION) using the numbered format and summary table, then ask:
  > "These are the issues found in your changes. Do you still want to create the PR, or would you like to fix these first?"
  - "fix first" or similar → STOP. Let the user fix and re-trigger.
  - "yes, create PR" / "proceed" / "create it anyway" → continue to Step 1.
  - Zero findings → tell user "No issues found. Proceeding to create the PR." and continue automatically.

→ Step 1: Check for staged changes
  ```
  git diff --staged --name-only
  git diff HEAD --name-only
  ```
  If there are staged or unstaged changes:
  - Show the list of changed files to the user
  - Ask: "You have the following uncommitted changes: \<list of files>
    Should I commit these before creating the PR?
    Note: I will only commit tracked files — unversioned/untracked files will be ignored.
    Please confirm with a commit message or say skip to ignore."
  - If user confirms: run `git add -u` then `git commit -m "<message>"`
  - If user says skip: proceed without committing
  - Never stash automatically
  - Never touch untracked or unversioned files

→ Step 2: Check if rebase is needed
  ```
  git fetch upstream
  git log upstream/test_stability_pipeline..HEAD --oneline
  ```
  If branch is behind upstream/test_stability_pipeline:
  - Tell user: "Your branch is behind upstream/test_stability_pipeline. Rebasing now before creating the PR."
  - Run: `git rebase upstream/test_stability_pipeline`
  - If conflicts → STOP, report exactly as described in the rebase section above, do not proceed to PR creation
  - If clean → continue to push

→ Step 3: Push
  ```
  git push origin <current-branch>
  ```
  If rejected → suggest `git push --force-with-lease` and explain why

→ Step 4: Create PR
  - Title: use the last commit message only, no auto description added
  - Body: leave empty unless user explicitly provides a description
  ```
  gh pr create \
    --base test_stability_pipeline \
    --title "<last commit message>" \
    --body ""
  ```
  - Show the PR link after creation

All these steps should be automatic.

---

## TOOL PRIORITY

Always use `gh` CLI and plain `git` commands only.
No GitHub MCP. No tokens. No hardcoded credentials.
Each team member runs `gh auth login` once — that is all the setup needed.
If `gh` is not installed → tell user to install `gh` CLI.
If `gh` is not authed → tell user to run: `gh auth login`

---

## REVIEW CHECKLIST — API AUTOMATION (REST ASSURED + TESTNG)

### HOW GREP AND FILE READING WORK TOGETHER

**Default to grep and targeted commands. Read file content only when you genuinely need to understand logic that a command cannot tell you.**

#### Use grep / commands for:
- `dependsOnMethods`, `Thread.sleep`, debug prints — anything detectable by matching text
- `wc -l` to check if a file was wiped
- `grep '<test name=' | sort | uniq -d` to check XML duplicates
- Counting `@Test` vs `@Owner` — always grep, never read-and-count manually
- Checking schema validation calls (`matchesJsonSchemaInClasspath`)
- Wild import presence vs single-class imports

#### Read file content only when:
- You need to understand **method logic** — e.g. does a data provider actually create isolation data? Does the test assert all relevant fields?
- You need to check a **specific section** flagged by grep — e.g. grep found `dependsOnMethods`; now read that method to understand what data it depends on
- The file is **new and small** (< 300 lines) — reading in full is cheap and catches things grep misses
- You need to verify a **specific line range** around a finding for context

#### Never read in full:
- `testng.xml` or `regression.xml` — always use grep/wc only, never read the full file
- Any file > 500 lines that is not new — read only the diff sections or specific flagged lines
- `TestBase.java`, `RestClient.java` — grep for the specific method you need, do not read all lines

#### The pattern:
1. Run all grep commands → flags potential violations
2. For each flagged line, read only that method/section to confirm
3. For logic checks with no grep signal, read only the new/changed methods — not the whole file

---

### MANDATORY PRE-REVIEW GREP COMMANDS

Run these first. Each command is self-contained — if it returns nothing, that rule passes without any file read needed.

```bash
# 1. dependsOnMethods — must have zero results
grep -rn "dependsOnMethods" <all changed .java files>

# 2. Debug statements — total count
grep -rn "System.out.println\|System.err.println\|e.printStackTrace" <all changed .java files>

# 3. Thread.sleep
grep -rn "Thread.sleep" <all changed .java files>

# 4. @Test / @Owner counts — must be equal
grep -c "@Test("   <TestFile>.java
grep -c "@Owner"   <TestFile>.java

# 5. Test methods without Test suffix
grep -n "public void " <TestFile>.java | grep "@Test" -A1 | grep -v "Test()" | grep -v "_Test("

# 6. Assertions without messages — Assert.assertEquals/assertTrue/assertNotNull with no string literal
grep -n "Assert\." <TestFile>.java | grep -v "Assert\.fail\|\"" 

# 7. Schema validation — new endpoint tests must have matchesJsonSchemaInClasspath
grep -n "matchesJsonSchemaInClasspath" <TestFile>.java

# 8. Duplicate <test> names in testng.xml and regression.xml
grep '<test name=' src/test/resources/testrunners/runners/testng.xml | sort | uniq -d
grep '<test name=' src/test/resources/testrunners/runners/regression.xml | sort | uniq -d

# 9. Wild imports — look for groups of 3+ single-class imports from same package
grep -n "^import" <all changed .java files>

# 10. Commented-out code, TODO/FIXME
grep -n "TODO\|FIXME\|//" <all changed .java files> | grep -v "import\|package"

# 11. Hardcoded credentials, tokens, or API keys
grep -rn "api_key\|apikey\|password\|Bearer\|token" <all changed .java files> | grep -v "getAccountApiKey\|getAlbatrossToken\|ThreadManager\|@AccountType\|comment"

# 12. Missing status code coverage for new endpoint tests
# For each @Test method, confirm at least 200 and one of 401/422/404 are asserted
grep -n "getStatusCode\|statusCode" <TestFile>.java
```

If any command returns output, each line is a potential finding — do not skip it.

---

### Key reference files to read before applying rules (read these, then run the greps above):

| File | Purpose |
|---|---|
| `src/main/java/io/rcrm/api/testbase/TestBase.java` | Base class — `@BeforeClass`, `@AccountType`, `getAccounts()`, all base URL constants |
| `src/main/java/io/rcrm/api/restclient/RestClient.java` | HTTP client — `doGet`, `doPost`, `doPost1`, `doPut`, `doDelete` |
| `src/main/java/com/qa/api/util/Owner.java` | `@Owner` annotation definition |
| `src/main/java/com/qa/api/util/reaper/ThreadManager.java` | `getAccountApiKey()`, `getAlbatrossToken()`, `getOwnerAlbatrossToken()` |
| `src/main/java/io/rcrm/api/commanfunctions/commanFunction.java` | Shared factory helpers — `createNewCandidateWithMandatoryFields`, etc. |
| `src/test/resources/publicApi/` | JSON schema files — verify schema exists before flagging missing validation |
| `src/test/resources/testrunners/runners/testng.xml` | Existing `<test>` name registry — check for duplicate names |
| `src/test/resources/testrunners/runners/regression.xml` | Regression suite — check for duplicate `<test>` names |

Apply rules based on what actually exists in this specific repo.

---

### CRITICAL — block merge

#### `dependsOnMethods` usage

**Exhaustive check — zero tolerance.**
- Run: `grep -rn "dependsOnMethods" <all changed files>` — flag every single occurrence
- `dependsOnMethods` creates hard ordering dependencies that break parallel execution, cause cascade failures, and are impossible to run in isolation
- Fix: every test that needs pre-existing data must create that data in its own `@DataProvider` or in a `@BeforeClass` block; never share state via class-level variables populated by a prior test
- Show the correct `@DataProvider` pattern for the specific case:

```java
// WRONG — depends on another test method
@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
public void editCandidateTest() {
    // uses slug populated by createNewCandidate
}

// CORRECT — self-contained via @DataProvider
@DataProvider
public Object[][] createCandidateAndGetSlug() {
    Response r = RestClient.doPost("JSON", baseURL, "candidates",
        ThreadManager.getAccountApiKey(), null, true, buildCandidatePayload());
    Assert.assertEquals(r.getStatusCode(), 200, "Prerequisite: candidate creation failed");
    return new Object[][] {{ r.jsonPath().getString("slug") }};
}

@Owner("Full Name")
@Test(dataProvider = "createCandidateAndGetSlug", groups = "nightly-build")
public void editCandidateTest(String slug) {
    // self-contained — slug is provided by @DataProvider
}
```

#### Missing `@Owner` tag

- Every `@Test` method must have `@Owner("Full Name")` annotation (`com.qa.api.util.Owner`)
- Run C4: `grep -c "@Test(" <file>` and `grep -c "@Owner" <file>` — counts must match
- Flag every test method missing it with the exact line number

#### Test method name does not end with `Test`

- Every `@Test` method name must end with `Test` (e.g., `createCandidateTest`, `verifyUnauthorizedAccess_Test`)
- Both `verifyFooTest` and `verifyFoo_Test` are acceptable
- Flag only if the method does not end with `Test` at all (e.g., `createNewCandidate`, `editBySlug_POST`)
- Run: `grep -n "public void" <TestFile>.java` — inspect every method annotated with `@Test`

#### Missing status code coverage for new endpoint tests

- When a new endpoint is being automated, the test class **must** cover all applicable HTTP status codes
- Required coverage for a standard CRUD endpoint:
  - **200** — happy path (valid request, valid auth)
  - **401** — unauthorized (missing or invalid token)
  - **404** — not found (invalid slug/ID)
  - **422** — validation error (invalid or missing required fields)
  - **405** — method not allowed (wrong HTTP verb) — include when the endpoint documentation lists it
- Run C12: `grep -n "getStatusCode\|statusCode" <TestFile>.java` — verify each status code has at least one test
- `@DataProvider` tests that vary the data for the same status code (e.g., multiple 422 validation cases) count as a single coverage point — only one test per status code is required unless the data provider is driven by business logic, not endpoint variations
- Flag: list exactly which status codes are missing with a suggested method name and approach

#### No assertions or insufficient assertions

- Every `@Test` method must have meaningful assertions — not just a status code check
- Minimum required for a 200 response test:
  1. `Assert.assertEquals(response.getStatusCode(), 200, "...")` — status code
  2. Schema validation with `matchesJsonSchemaInClasspath(...)` — response structure
  3. At least one business-field assertion (e.g., `.body("first_name", Matchers.equalTo(expectedName), "...")`) — data correctness
- For error response tests (401, 404, 422):
  1. `Assert.assertEquals(response.getStatusCode(), <code>, "...")` — status code
  2. At least one assertion on the error message or error structure
- Flag any test method with zero assertions or only a single `statusCode` check

#### Assertions without meaningful message

- Every assertion must have a descriptive failure message
- `Assert.assertEquals(response.getStatusCode(), 200)` with no message is a violation
- REST Assured `.statusCode(200)` without a surrounding `Assert.assertEquals` with a message is a violation — prefer `Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for valid create candidate request but got " + response.getStatusCode())`
- Hamcrest `.body("field", Matchers.equalTo("value"))` chains must be preceded by or include a message
- Message must say what was expected, in what context, and what went wrong
- Run C6: `grep -n "Assert\." <TestFile>.java | grep -v "Assert\.fail\|\"" ` — flag every result

#### Missing JSON schema validation for new endpoints

- Every test covering a **200 response on a new endpoint** must include schema validation:
  ```java
  response.then()
      .assertThat()
      .body(matchesJsonSchemaInClasspath("publicApi/<entity>/<endpointAction>.json"));
  ```
- If the schema file does not exist yet → it must be created at `src/test/resources/publicApi/<entity>/<endpointAction>.json`
- Run C7: `grep -n "matchesJsonSchemaInClasspath" <TestFile>.java` — if the test is for a new endpoint and returns zero results → CRITICAL
- Schema must use at least `"required": [...]` for mandatory fields; do not use an empty schema `{}` 

#### Thread.sleep usage

**Exhaustive check — zero tolerance.**
- Run: `grep -rn "Thread.sleep" <all changed files>` — flag every single occurrence with file and line number
- Fix: API tests should not need waits — if a wait is needed, it is a symptom of a test design problem; use retry logic via `RestClient` or polling patterns instead

#### Debug statements

**Exhaustive check — scan every changed file with a grep, not a manual read.**
- Run: `grep -rn "System.out.println\|System.err.println\|e.printStackTrace" <all changed files>` — flag every occurrence with file and line number
- These are never acceptable in committed test code
- Acceptable logging: use `log.info(...)` / `log.error(...)` from a proper logger if the framework provides one

#### Duplicate `<test>` names in XML runner files

**Exhaustive check.**
- Run: `grep '<test name=' src/test/resources/testrunners/runners/testng.xml | sort | uniq -d`
- Run: `grep '<test name=' src/test/resources/testrunners/runners/regression.xml | sort | uniq -d`
- Any output means duplicate `<test name=` values exist **within the same file** → flag every duplicate
- Duplicates cause silent test skip: TestNG executes only the last definition when two `<test>` blocks share the same name **within a single suite file**
- Two different XML suite files (e.g., `contractStaffingTest.xml` and `expenseAndReimbursement.xml`) may legitimately contain the same `<test name=` value — this is NOT a violation; only intra-file duplicates are flagged

#### Wild imports — missing where three or more classes are imported from the same package

- Where 3 or more classes are imported from the same package, a wildcard import must be used instead
- Run C9: `grep -n "^import" <changed files>` — look for import blocks with 3+ lines from the same package
- Example violation:
  ```java
  import org.testng.Assert;
  import org.testng.annotations.DataProvider;
  import org.testng.annotations.Test;
  import org.testng.annotations.BeforeClass;
  ```
  Fix:
  ```java
  import org.testng.Assert;
  import org.testng.annotations.*;
  ```
- Exception: `static` imports must remain explicit (e.g., `import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath`)

#### Duplicate code

- Do not re-implement entity creation inline in tests when a factory method already exists in `commanFunction.java`
- Run: `grep -n "doPost\|doGet" <TestFile>.java` — for each setup call in a `@DataProvider`, check if `commanFunction` already provides a method for it
- Example: `function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())` already exists — do not inline a raw `RestClient.doPost` for candidate creation in a data provider

---

### WARNING — should fix

#### Test not registered in any XML file

- Every `@Test` method in a changed or new test class must appear in at least one XML runner file
- Run: `grep -rn "<ClassName>" src/test/resources/testrunners/` for each changed test class
- If no XML entry exists → WARNING, provide the XML snippet to add

#### Wrong package for new test class

- Test classes covering public API endpoints: `io.rcrm.api.<entity>.*`
  - Example: `io.rcrm.api.candidate.CreateCandidateTest`
- Test classes covering internal services (albatross, neptune, ostrich): `io.recruitcrm.albatross.*`, `io.recruitcrm.ostrich.*`
- Test classes covering filter/search: `io.recruitcrm.Filters.*`
- Test classes covering contract staffing: `io.recruitcrm.contractStaffing.*`
- Misplaced test classes → flag with the correct package

#### Wrong class or method naming

**Class names:**
- Test classes: `<FeatureOrAction><Entity>Test` or `<FeatureOrAction><Entity>_Test` (e.g., `CreateCandidateTest`, `EditContactTest`, `GetReimbursementList_Test`)
- All-endpoint classes: `AllEndPointsOf<Entity>Test` (e.g., `AllEndPointsOfCandidatesTest`)
- Class names must end with `Test` or `_Test` — both suffixes are acceptable
- Flag only if the class name does not end with `Test` or `_Test` at all

**Method names:**
- Must be descriptive and end with `Test` or `_Test`
- Method name must reflect the scenario: `verifyUnauthorizedUserCannotCreateCandidate_Test`, `createCandidateWithInvalidEmail_Test`
- Never name methods after the HTTP method alone: `post_Test`, `get_Test` are not acceptable
- Flag any method that does not clearly describe the test scenario

#### `@AccountType` missing or wrong at class level

- Every test class must have `@AccountType("...")` at the class level
- Valid types: `"Business"`, `"Business|Email1"`, `"Business|AlbatrossTkn"`, `"RBAC|Business|automationForRevamp"`, `"CrossAccount|Email"`, `"Business|ContractStaffing"`
- If the test hits an endpoint that requires a specific account type and the class uses the wrong one → WARNING
- Run: `grep -n "@AccountType" <TestFile>.java` — zero results = flag

#### `@DataProvider` without test isolation

- Each `@DataProvider` must create fresh test data via API or Faker — never read from class-level fields that were set by another method
- If a data provider returns a hard-coded slug or ID string → WARNING, it will break when that entity is deleted
- Each data provider name must be unique within the class

#### Unused or dead-code `@DataProvider`

- A `@DataProvider` method not referenced by any `@Test(dataProvider = "...")` in the same file → flag it

#### Commented-out test methods or XML includes

- Commented-out `<include>` entries in XML files that have no open GitHub issue or ticket reference → WARNING
- Commented-out `@Test` methods → WARNING
- Run C10: `grep -n "//\|/\*" <changed files>` — inspect each result

#### Unwanted comments

- Do not add comments that simply restate what the code does
- `// Create candidate` before a `createNewCandidate()` call is noise — remove it
- `TODO` or `FIXME` without a linked ticket number → flag it
- Run C10: `grep -n "TODO\|FIXME\|//" <all changed files> | grep -v "import\|package"` — inspect each result

---

### SUGGESTION — nice to have

- Data provider that builds request payload inline → suggest extracting payload construction to a POJO builder or factory method
- Test class over 400 lines → suggest splitting by operation (Create, Edit, Delete, Get, Search) into separate classes
- Magic strings for endpoint paths → suggest extracting to a constant or static final field in the class
- Missing `groups = "nightly-build"` on new `@Test` methods — all tests should declare at least one group for filtered execution

---

## MANDATORY COMPLETION GATE — internal use only, never shown to user

**RULE: Run this gate silently in your internal reasoning before writing any finding. Do NOT print this table or any part of it in the response. The user sees only the numbered findings and summary table.**

**You must not write a single finding until every row below is confirmed ✅ or N/A. If you cannot confirm a row, go back and run the corresponding grep command before continuing.**

### CRITICAL rules — all must be verified

| # | Rule | Grep / Verification | Status |
|---|---|---|---|
| CR-1 | No `dependsOnMethods` anywhere | C1 output — zero results = pass | ✅ / ❌ |
| CR-2 | Every `@Test` method name ends with `Test` or `_Test` (class names may also use either suffix — both are valid) | Read all `@Test` method signatures — inspect each | ✅ / ❌ |
| CR-3 | Every `@Test` method has `@Owner` | C4: counts match | ✅ / ❌ |
| CR-4 | New endpoint tests cover 200, 401, 404, 422 (and 405 if applicable) | C12 output + read test class method list | ✅ / ❌ |
| CR-5 | Every 200-response test has ≥ 3 assertions (status + schema + field) | Read each 200-path test body | ✅ / ❌ |
| CR-6 | Every assertion has a descriptive failure message | C6 output — zero results = pass | ✅ / ❌ |
| CR-7 | Every 200-response test for a new endpoint has schema validation | C7 output — at least one result per new-endpoint test file | ✅ / ❌ |
| CR-8 | JSON schema file exists at `src/test/resources/publicApi/...` | `find src/test/resources/publicApi -name "<schema>.json"` | ✅ / ❌ |
| CR-9 | No `Thread.sleep` anywhere | C3 output — zero results = pass | ✅ / ❌ |
| CR-10 | No `System.out.println`, `System.err.println`, `e.printStackTrace` | C2 output — zero results = pass | ✅ / ❌ |
| CR-11 | No duplicate `<test>` names **within** a single XML file (cross-file duplicates are allowed) | C8 output — zero results = pass | ✅ / ❌ |
| CR-12 | Wild imports used where 3+ classes imported from same package | C9 output — inspect each import block | ✅ / ❌ |
| CR-13 | No hardcoded credentials or tokens | C11 output — inspect every match | ✅ / ❌ |
| CR-14 | No inline re-implementation of `commanFunction` factory helpers | Grep `commanFunction` methods + compare with test data providers | ✅ / ❌ |

### WARNING rules — all must be verified

| # | Rule | Grep / Verification | Status |
|---|---|---|---|
| WR-1 | Every new test method registered in at least one XML file | `grep -rn "<ClassName>" src/test/resources/testrunners/` | ✅ / ❌ |
| WR-2 | Class and method names follow naming convention | Read file header and all method signatures | ✅ / ❌ |
| WR-3 | `@AccountType` present at class level with correct value | `grep -n "@AccountType" <TestFile>.java` | ✅ / ❌ |
| WR-4 | Each `@DataProvider` creates fresh isolated data | Read each `@DataProvider` body — no class-level field reads | ✅ / ❌ |
| WR-5 | No unused `@DataProvider` methods | Check every `@DataProvider` name matches a `dataProvider = "..."` reference | ✅ / ❌ |
| WR-6 | No commented-out test methods or XML includes without ticket refs | C10 output — inspect each match | ✅ / ❌ |
| WR-7 | No unwanted inline comments | C10 output — inspect each match | ✅ / ❌ |
| WR-8 | Test class in correct package | Read `package` statement, compare to package layout table | ✅ / ❌ |

### SUGGESTION rules — all must be verified

| # | Rule | Verification | Status |
|---|---|---|---|
| SR-1 | Request payload construction extracted from test methods | Read `@DataProvider` and test method bodies for inline JSON/POJO building | ✅ / ❌ |
| SR-2 | Test class not over 400 lines | `wc -l <TestFile>.java` | ✅ / ❌ |
| SR-3 | Endpoint path strings extracted to constants | Read test methods for repeated string literals | ✅ / ❌ |
| SR-4 | `groups = "nightly-build"` declared on all `@Test` methods | `grep -c 'groups' <TestFile>.java` vs `grep -c '@Test' <TestFile>.java` | ✅ / ❌ |

---

**Internal rule: Only after every row above is ✅ or N/A, write the user-facing findings.**
**Every ❌ row becomes a numbered finding with the correct severity.**
**Never print the gate table itself — it is internal scaffolding only.**

---

## WHAT THE USER SEES (output format)

Write only this, nothing else:

1. Numbered findings using the format below
2. Summary table at the end

```
**[🔴 CRITICAL / 🟡 WARNING / 🔵 SUGGESTION] #<number> — <short title>**
File: path/to/File.java — Line <N>
Issue: what is wrong and why it matters
Current code:
  // snippet
Fix:
  // corrected version
Why: impact on reliability, maintainability or team conventions
```

Summary table:

| # | Severity | File | Line | Issue |
|---|---|---|---|---|

For Flow 1 — stop here. No GitHub posting, no gate table, no internal notes.
For Flow 2 — after the summary table ask exactly: "Should I post these to the PR? I will post all by default — tell me the numbers you want to skip, or say post all."

---

## CODEBASE QUICK REFERENCE

### Package layout

```
src/
├── main/java/
│   ├── io/rcrm/api/
│   │   ├── testbase/TestBase.java              ← Base class for all tests
│   │   ├── restclient/RestClient.java           ← HTTP client wrapper
│   │   ├── commanfunctions/commanFunction.java  ← Entity creation helpers
│   │   ├── listeners/
│   │   │   ├── ExtentReporterNG.java            ← Reporting listener
│   │   │   └── TestTrackingListener.java        ← Ownership tracking
│   │   ├── pojo/                                ← Request/response POJOs
│   │   └── javafaker/                           ← Faker data generators
│   └── com/qa/api/util/
│       ├── Owner.java                           ← @Owner annotation
│       └── reaper/
│           └── ThreadManager.java               ← Thread-local account/token access
│
└── test/java/
    ├── io/rcrm/api/
    │   ├── candidate/                           ← Candidate endpoint tests
    │   ├── contact/                             ← Contact endpoint tests
    │   ├── job/                                 ← Job endpoint tests
    │   ├── company/                             ← Company endpoint tests
    │   ├── deals/                               ← Deal endpoint tests
    │   ├── notes/                               ← Note endpoint tests
    │   ├── tasks/                               ← Task endpoint tests
    │   ├── meeting/                             ← Meeting endpoint tests
    │   ├── calllogs/                            ← Call log endpoint tests
    │   ├── hotlists/                            ← Hotlist endpoint tests
    │   ├── files/                               ← File upload endpoint tests
    │   ├── list/                                ← Lookup list endpoint tests
    │   └── [entity]/                            ← Other public API entities
    ├── io/rcrm/nyma/                            ← Nylas email integration tests
    └── io/recruitcrm/
        ├── Filters/                             ← Filter search tests
        ├── BooleanSearch/                       ← Boolean search tests
        ├── CandidateService/                    ← Candidate microservice tests
        ├── JobService/                          ← Job microservice tests
        ├── contractStaffing/                    ← Contract staffing tests
        ├── albatross/                           ← Albatross internal service tests
        │   ├── neptune/                         ← Neptune AI service tests
        │   └── dashboard/                       ← Dashboard tests
        ├── ostrich/                             ← Ostrich service tests
        ├── report/                              ← Reporting tests
        └── adminSettings/                       ← Admin settings tests

src/test/resources/
├── publicApi/                                   ← JSON schema files
│   ├── candidate/createCandidate.json
│   ├── contact/
│   ├── job/
│   └── [entity]/
└── testrunners/
    ├── runners/
    │   ├── testng.xml                           ← Default full-suite runner
    │   └── regression.xml                       ← Regression suite
    ├── modules/
    │   ├── candidate.xml
    │   ├── company.xml
    │   ├── job.xml
    │   └── [entity].xml
    ├── services/
    │   ├── nyma.xml
    │   ├── albatross.xml
    │   ├── stripeAPI.xml
    │   └── [service].xml
    └── contractStaffing/
        ├── contractStaffingTest.xml
        └── [rule-engine-variant].xml
```

### Test class anatomy

```java
package io.rcrm.api.candidate;               // public API tests go here

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;

@AccountType("Business")
public class CreateCandidateTest extends TestBase {

    commanFunction function = new commanFunction();
    JavaFakerCandidate faker = new JavaFakerCandidate();

    // ── Happy path ────────────────────────────────────────────────────────

    @Owner("Full Name")
    @Test(groups = "nightly-build")
    public void createCandidateWithMandatoryFieldsTest() {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(faker.getFirstName());
        candidate.setLast_name(faker.getLastName());
        candidate.setEmail(faker.getEmailID());

        Response response = RestClient.doPost("JSON", baseURL, "candidates",
            ThreadManager.getAccountApiKey(), null, true, candidate);

        Assert.assertEquals(response.getStatusCode(), 200,
            "Expected HTTP 200 for create candidate with mandatory fields but got "
                + response.getStatusCode());
        response.then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath("publicApi//candidate//createCandidate.json"));
        Assert.assertNotNull(response.jsonPath().getString("slug"),
            "slug must be present in create candidate 200 response");
        Assert.assertEquals(response.jsonPath().getString("first_name"), candidate.getFirst_name(),
            "first_name in response must match the value sent in request");
    }

    // ── 422 Validation errors ─────────────────────────────────────────────

    @DataProvider
    public Object[][] missingMandatoryFieldCases() {
        JavaFakerCandidate f = new JavaFakerCandidate();
        return new Object[][] {
            { buildCandidate(null, f.getLastName(), f.getEmailID()),  "first_name is required" },
            { buildCandidate(f.getFirstName(), null, f.getEmailID()), "last_name is required"  },
            { buildCandidate(f.getFirstName(), f.getLastName(), null), "email is required"     },
        };
    }

    @Owner("Full Name")
    @Test(dataProvider = "missingMandatoryFieldCases", groups = "nightly-build")
    public void createCandidateWithMissingMandatoryField_Test(Candidate candidate, String expectedMessage) {
        Response response = RestClient.doPost("JSON", baseURL, "candidates",
            ThreadManager.getAccountApiKey(), null, true, candidate);

        Assert.assertEquals(response.getStatusCode(), 422,
            "Expected HTTP 422 for missing mandatory field but got " + response.getStatusCode());
        Assert.assertTrue(
            response.getBody().asString().contains(expectedMessage),
            "Response body must contain validation message '" + expectedMessage + "'");
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────────

    @Owner("Full Name")
    @Test(groups = "nightly-build")
    public void createCandidateWithInvalidToken_Test() {
        Candidate candidate = new Candidate();
        candidate.setFirst_name(faker.getFirstName());

        Response response = RestClient.doPost("JSON", baseURL, "candidates",
            "invalid_token", null, true, candidate);

        Assert.assertEquals(response.getStatusCode(), 401,
            "Expected HTTP 401 for invalid auth token but got " + response.getStatusCode());
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────

    @Owner("Full Name")
    @Test(groups = "nightly-build")
    public void getCandidateByInvalidSlug_Test() {
        Response response = RestClient.doGet("JSON", baseURL, "candidates/invalid-slug-xyz",
            ThreadManager.getAccountApiKey(), null, null, true);

        Assert.assertEquals(response.getStatusCode(), 404,
            "Expected HTTP 404 for invalid candidate slug but got " + response.getStatusCode());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Candidate buildCandidate(String firstName, String lastName, String email) {
        Candidate c = new Candidate();
        c.setFirst_name(firstName);
        c.setLast_name(lastName);
        c.setEmail(email);
        return c;
    }
}
```

### JSON schema file anatomy

Location: `src/test/resources/publicApi/<entity>/<action>.json`

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "id":         { "type": "integer" },
    "slug":       { "type": "string"  },
    "first_name": { "type": "string"  },
    "last_name":  { "type": "string"  },
    "email":      { "type": "string"  }
  },
  "required": ["id", "slug", "first_name", "email"]
}
```

Minimum requirements for a valid schema:
- Must declare `"type": "object"` at the root
- Must list mandatory response fields in `"required": [...]`
- Must declare the type of each field in `"properties"`
- Do not use an empty schema `{}` — it validates nothing

### XML suite file anatomy

Every runner XML must include both standard listeners:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd" >
<suite name="Descriptive Suite Name" thread-count="30" parallel="tests">

    <listeners>
        <listener class-name="io.rcrm.api.listeners.ExtentReporterNG" />
        <listener class-name="io.rcrm.api.listeners.TestTrackingListener" />
    </listeners>

    <test name="Create Candidate — 200 Mandatory Fields">
        <classes>
            <class name="io.rcrm.api.candidate.CreateCandidateTest">
                <methods>
                    <include name="createCandidateWithMandatoryFieldsTest" />
                </methods>
            </class>
        </classes>
    </test>

    <test name="Create Candidate — 422 Missing Fields">
        <classes>
            <class name="io.rcrm.api.candidate.CreateCandidateTest">
                <methods>
                    <include name="createCandidateWithMissingMandatoryField_Test" />
                </methods>
            </class>
        </classes>
    </test>

    <test name="Create Candidate — 401 Unauthorized">
        <classes>
            <class name="io.rcrm.api.candidate.CreateCandidateTest">
                <methods>
                    <include name="createCandidateWithInvalidToken_Test" />
                </methods>
            </class>
        </classes>
    </test>

</suite>
```

Rules for the XML file:
- Every `<test name=` must be unique within the file
- `<test>` names must be descriptive: entity, action, and the scenario / status code (e.g., `"Create Candidate — 422 Missing Fields"`)
- Do not use generic names like `"Test 1"` or `"Candidate Tests"`
- When the same class appears in multiple `<test>` blocks, each block must include only the methods relevant to that scenario — do not repeat the full method list

### `@AccountType` values and when to use them

```java
@AccountType("Business")                              // Standard business account — use for most public API tests
@AccountType("Business|Email1")                       // With Nylas email connection — use for email-related tests
@AccountType("Business|AlbatrossTkn")                 // With Albatross service token — use for internal service tests
@AccountType("Business|AlbatrossTkn|Email1")          // With both — albatross + email tests
@AccountType("RBAC|Business|automationForRevamp")     // RBAC-enabled — use for permission/role tests
@AccountType("CrossAccount|Email")                    // Cross-account — use for security boundary tests
@AccountType("Business|ContractStaffing")             // Contract staffing — use for timesheet/shift tests
```

### RestClient methods

```java
// GET
RestClient.doGet(contentType, baseURI, basePath, token, queryParams, pathParams, log)

// POST (no path params)
RestClient.doPost(contentType, baseURI, basePath, token, queryParams, log, body)

// POST (with both query and path params)
RestClient.doPost1(contentType, baseURI, basePath, token, queryParams, pathParams, log, body)

// DELETE
RestClient.doDelete(contentType, baseURI, basePath, token, queryParams, pathParams, log)

// PUT
RestClient.doPut(contentType, baseURI, basePath, token, queryParams, pathParams, log, body)
```

Pass `null` for unused param maps. Pass `true` for `log` in tests to enable request/response logging.

### ThreadManager access

```java
ThreadManager.getAccountApiKey()         // Current account's public API key
ThreadManager.getOwnerAlbatrossToken()   // Owner role Albatross token
ThreadManager.getAlbatrossToken("Admin") // Admin role Albatross token
ThreadManager.getAlbatrossToken("TeamMember") // Team member role token
ThreadManager.getAccount()               // Full Account object
```