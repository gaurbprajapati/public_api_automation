# contractStaffingSkill (pointer)

This is a pointer, not the canonical copy. The real, auto-invocable Claude Code skill lives at:

```
.claude/skills/contractStaffingSkill/SKILL.md                                    <- router
.claude/skills/contractStaffingSkill/references/shared-conventions.md            <- cross-cutting: JSON schema, assertions, taxonomy, utilities, caveats
.claude/skills/contractStaffingSkill/references/RuleEngine.md
.claude/skills/contractStaffingSkill/references/Filters.md
.claude/skills/contractStaffingSkill/references/timesheetsListPage.md
.claude/skills/contractStaffingSkill/references/timesheetInvoice.md
.claude/skills/contractStaffingSkill/references/expenseAndReimbursement.md
.claude/skills/contractStaffingSkill/references/contractorPortal.md
.claude/skills/contractStaffingSkill/references/publicApi.md
.claude/skills/contractStaffingSkill/references/shiftBasedTimesheets.md
.claude/skills/contractStaffingSkill/references/hourBasedTimeSheets.md
.claude/skills/contractStaffingSkill/references/shiftBasedRuleEngineCalculation.md
.claude/skills/contractStaffingSkill/references/hoursBasedRuleEngineCalculation.md
```

Each reference file is named identically to the `contractStaffing` subpackage it documents.

## What it does

Takes an HLD/LLD design or endpoint spec for a Contract Staffing feature and turns it into API automation test code consistent with the conventions already used across this package. The main `SKILL.md` decides whether new endpoints belong in an existing subpackage or need a new feature package, then routes to that package's own reference file for the concrete structure/base-class/DataProvider/assertion pattern to follow, covering:

1. Package placement (existing subpackage vs. new feature package)
2. Base-class / helper-method architecture (one helper per endpoint)
3. DataProvider / test-data design — including the JSON-fixture pattern used by the rule-engine calculation suites (see `shiftBasedRuleEngineCalculation/`)
4. Assertion strategy (schema validation, tolerance-based numeric assertions, error-envelope patterns)
5. The full positive/negative/valid/invalid test-case taxonomy actually implemented across this codebase — shared categories in `shared-conventions.md`, package-specific ones in each package's own reference file

## Relationship to other docs in this folder

- `qa-test-scenario-generator.md` — generates a **CSV scenario matrix** from a PRD/wireframes for manual/QA review. Use it upstream when the input is a PRD rather than a concrete endpoint spec.
- `api-automaton-rule.md` — the older, generic `DataEnrichmentTest`-based convention doc. `contractStaffingSkill` supersedes it with conventions verified against the actual current state of every `contractStaffing` subpackage (including the JSON DataProvider pattern, the `ContractStaffingBaseTest` helper library, and the full negative-test taxonomy that doc doesn't cover).
- `Multiple_Time_entry/` — rule-engine calculation domain docs (`RULE_ENGINE_COMPREHENSIVE_DOCUMENTATION.md`, `rule-engine-test-data-guide.mdc`, `JSON_DATAPROVIDER_DESIGN.md`). Read these when the HLD/LLD involves pay/bill calculation logic — `contractStaffingSkill` references them for that case rather than duplicating the domain rules.

To invoke the real skill in an interactive session, ask Claude to write automation for a contractStaffing endpoint/feature, or run `/contractStaffingSkill` directly.
