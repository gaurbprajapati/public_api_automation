---
name: qa-test-scenario-generator
description: Generate comprehensive QA test scenarios in structured CSV format from PRDs, wireframe designs, and feature specifications. Use this skill whenever the user asks to write test cases, test scenarios, test plans, QA scenarios, or wants to review a feature for testing — especially for Recruit CRM features involving jobs, timesheets, margin/markup, rule engine, client portal, contractor portal, or any CRUD-based SaaS feature. Also trigger when the user uploads PRD documents, Figma designs, wireframe screenshots, or feature specs and asks to generate test coverage. This skill covers functional testing, backend API validation, edge cases, cross-portal sync, rule engine integration, filter/export/audit scenarios, and integration testing (Zapier, Workato, mobile, public API).
---
 
# QA Test Scenario Generator
 
Generate comprehensive, structured QA test scenarios from PRDs, wireframe designs, and feature specifications. Output is a CSV file with `Subcategory,Scenarios` columns — ready for import into test management tools.
 
## Table of Contents
 
1. [Workflow](#1-workflow)
2. [Output Format](#2-output-format)
3. [Scenario Writing Rules](#3-scenario-writing-rules)
4. [Subcategory Taxonomy](#4-subcategory-taxonomy)
5. [Analysis Checklist — What to Extract](#5-analysis-checklist--what-to-extract)
6. [Scenario Generation Patterns](#6-scenario-generation-patterns)
7. [Backend Validation Pattern](#7-backend-validation-pattern)
8. [Rule Engine Integration Pattern](#8-rule-engine-integration-pattern)
9. [Cross-Portal Sync Pattern](#9-cross-portal-sync-pattern)
10. [Edge Case Thinking Framework](#10-edge-case-thinking-framework)
11. [Domain Knowledge Reference](#11-domain-knowledge-reference)
---
 
## 1. Workflow
 
Follow these steps in order:
 
### Step 1 — Gather Inputs
 
Collect all available inputs from the user:
 
| Input Type | What to Extract |
|-----------|----------------|
| **PRD document** | Requirements, field names, validation rules, formulas, edge cases, admin settings, API specs, analytics events, audit log behavior |
| **Wireframe / Figma screenshots** | Field layout, enabled/disabled states, dropdown options, error messages (exact text), info banners, column names in tables, filter UI, export modals |
| **Existing test scenarios** | Format reference, naming conventions, subcategory style |
| **Rule engine docs** | Pay/bill calculation formulas, rule types, multiplier behavior, break handling, threshold logic |
| **User callouts** | Specific behaviors the user explicitly asks to cover |
 
### Step 2 — Analyze and Map
 
Before writing any scenarios:
 
1. **List every field** mentioned in the PRD with its type, validation range, and where it appears
2. **List every page/screen** where the feature touches (add form, edit form, details page, list page, filters, export, admin settings, timesheet form, contractor page, reports, mobile, API)
3. **List every formula** with input → output mapping
4. **List every edge case** explicitly called out in the PRD
5. **List every integration** (API, Zapier, Workato, mobile, portals)
6. **Identify the "change propagation boundary"** — when settings change, what gets the new values vs what retains old values
### Step 3 — Generate Scenarios by Subcategory
 
Walk through the [Subcategory Taxonomy](#4-subcategory-taxonomy) and generate scenarios for every applicable subcategory. Skip subcategories that don't apply to the feature.
 
### Step 4 — Cross-Reference with Wireframes
 
For every wireframe screenshot provided:
- Extract exact field values shown and create scenarios that reference them
- Extract exact error message text and use it in validation scenarios
- Extract exact info banner text for edge case scenarios
- Extract column names from list views and table headers
- Extract filter options from filter dropdowns
- Note which fields appear enabled vs disabled (greyed out)
### Step 5 — Output CSV
 
Generate the final CSV file at `/mnt/user-data/outputs/` and present it to the user.
 
---
 
## 2. Output Format
 
### CSV Structure
 
```csv
Subcategory,Scenarios
,
[Subcategory Name],[First scenario in this subcategory]
,[Second scenario — subcategory cell is empty for continuation rows]
,[Third scenario]
,
[Next Subcategory Name],[First scenario in next subcategory]
,[Second scenario]
```
 
**Rules:**
- Column headers: `Subcategory,Scenarios` (exactly)
- First scenario in a group has the subcategory name in column A
- Subsequent scenarios in the same group have column A empty (just a comma prefix)
- Blank row (just a comma) separates subcategory groups
- Wrap scenario text in double quotes if it contains commas, quotes, or special characters
- Escape internal double quotes by doubling them: `""like this""`
### File Naming
 
`[feature_name]_test_scenarios.csv`
 
Example: `margin_markup_test_scenarios.csv`
 
---
 
## 3. Scenario Writing Rules
 
### Format of Each Scenario
 
Every scenario MUST start with `Verify that` followed by a clear, testable assertion.
 
**Good:**
```
Verify that when "Margin" is selected and Pay Rate is $40, entering Margin = 30% auto-calculates Bill Rate as $57.14 and Markup as 42.85%.
```
 
**Bad:**
```
Check margin calculation works.
Test the margin field.
```
 
### Writing Guidelines
 
| Guideline | Example |
|-----------|---------|
| Be specific — include field names, values, expected results | `Verify that with Pay Rate = $10 and Markup = 50%, Bill Rate is calculated as $15.` |
| Reference exact UI text from wireframes when available | `Verify that the error message reads "Value must be between 0 and 99"` |
| State the precondition before the assertion | `Verify that when "Margin" is selected and no Pay Rate is entered, an error message is shown.` |
| One assertion per scenario | Don't combine unrelated checks in one scenario |
| Include the mode/context | `Verify that in Fixed Rate mode, when Pay Rate = $0...` |
| For API scenarios, mention the method | `Verify that the Edit Job API endpoint rejects negative Margin values.` |
| For cross-portal scenarios, name all portals checked | `Verify that updated values are reflected in RCRM app, Client Portal, and Contractor Portal.` |
 
### Scenario Density Targets
 
| Subcategory Type | Target Scenarios |
|-----------------|-----------------|
| Core field behavior (per mode) | 8–12 scenarios |
| Validation rules | 4–6 scenarios |
| Admin settings | 6–10 scenarios |
| Each page/screen where fields appear | 3–6 scenarios |
| Filters (per page) | 4–8 scenarios |
| Export | 4–6 scenarios |
| Backend validation (per API endpoint) | 6–10 scenarios |
| Rule engine integration | 10–20 scenarios |
| Edge cases | 8–12 scenarios |
| Cross-portal sync | 3–6 scenarios |
 
---
 
## 4. Subcategory Taxonomy
 
Use these subcategory names. Skip any that don't apply to the feature.
 
### Core Feature Behavior
 
| Subcategory | When to Use |
|-------------|-------------|
| `[Feature] - [Mode/State] (Default)` | Default mode behavior, field enable/disable states |
| `[Feature] - [Mode/State]` | Each additional mode/state of the feature |
| `Calculation Accuracy & Formulas` | When the feature involves mathematical calculations |
 
### Form & Field Behavior
 
| Subcategory | When to Use |
|-------------|-------------|
| `Add [Entity] Form` | New entity creation form behavior |
| `Edit [Entity] Form` | Edit form behavior, pre-population, mode switching |
| `Enable [Feature] Form` | Enablement/activation forms (e.g. Enable Timesheet) |
| `Admin Settings - Field Visibility` | Admin toggle for field visibility, required marking |
 
### Display & Navigation
 
| Subcategory | When to Use |
|-------------|-------------|
| `[Entity] Details Page` | Entity detail view display |
| `[Entity] List Page` | List view columns, hide/show, sorting |
| `[Entity] List Page - Filters` | Filter types, parameters, combinations |
| `Contractor Details Page` | Contractor-specific detail view |
| `Reports Pages` | Report-specific display and filters |
| `All Timesheets Page - Columns & Filters` | Timesheet list specific scenarios |
 
### Data Flow & Propagation
 
| Subcategory | When to Use |
|-------------|-------------|
| `[Form] - Auto-Population & Editability` | Values flowing from one form to another |
| `[Form] - Changes Apply Only to New [Entities]` | When setting changes should NOT retroactively affect existing records |
| `Cross-Portal Sync with [Feature]` | Multi-portal consistency (RCRM, Client Portal, Contractor Portal) |
 
### Export & Integration
 
| Subcategory | When to Use |
|-------------|-------------|
| `Export Functionality` | CSV/Excel export field inclusion |
| `Automated Workflow & Email Templates` | Placeholder availability in templates |
| `Public API` | API CRUD endpoint field support |
| `Zapier & Workato Integration` | Third-party integration support |
| `Mobile Application` | Mobile app field support and sync |
 
### Backend Validation
 
| Subcategory | When to Use |
|-------------|-------------|
| `Backend Validation - Add [Entity] API` | All frontend validations mirrored on create API |
| `Backend Validation - Edit [Entity] API` | All frontend validations mirrored on edit API |
| `Backend Validation - [Feature] API` | Feature-specific API validation (e.g. Enable Timesheet) |
| `Backend Validation - Edge Cases via API/Postman` | Division by zero, injection, overflow, type mismatch |
 
### System Behavior
 
| Subcategory | When to Use |
|-------------|-------------|
| `Existing User Migration / Backward Compatibility` | How existing data is handled after feature deployment |
| `Audit Log` | Audit log entries for field changes |
| `Analytics Events` | Event firing with correct properties |
 
### Calculations & Rule Engine
 
| Subcategory | When to Use |
|-------------|-------------|
| `Rule Engine - [Rate/Feature] Impact on [Calculation]` | How a rate change flows through rule engine to pay/bill |
| `Rule Engine - Break + [Feature]` | Break handling (BPY/BPN) with the feature |
| `Rule Engine - Daily OT + [Feature]` | Daily overtime interaction |
| `Rule Engine - Weekly OT + [Feature]` | Weekly overtime interaction |
| `Rule Engine - Multiple Timesheet Periods with Rate Changes` | Rate change propagation across timesheet periods |
| `Rule Engine - Single Day Entry Calculation with [Feature]` | Per-day, per-entry level calculation verification |
| `Rule Engine - Tiered DOT with [Feature]` | Multi-tier DOT with the feature |
| `Rule Engine - Unallocated Hours with [Feature]` | Unallocated/gap hour handling |
| `Timesheet View - Pay and Bill Amount Display` | Timesheet detail view amounts and totals |
 
### Edge Cases
 
| Subcategory | When to Use |
|-------------|-------------|
| `Edge Cases - General` | Zero values, boundary values, undefined results, rapid switching |
| `Edge Cases - [Specific Area]` | Area-specific edge cases (e.g. Edge Cases - Admin Hidden Fields) |
 
---
 
## 5. Analysis Checklist — What to Extract
 
Before writing scenarios, build this checklist from the inputs:
 
### From PRD
 
- [ ] All new fields with names, types, and validation ranges
- [ ] All modes/states of the feature (e.g. Fixed Rate / Margin / Markup)
- [ ] Default state and what is selected by default
- [ ] Which fields are enabled vs disabled in each mode
- [ ] Required field conditions
- [ ] All formulas with examples
- [ ] All edge cases explicitly listed
- [ ] Admin settings: visibility toggle, required toggle
- [ ] Conditional visibility rules (e.g. "visible only when job type is contract")
- [ ] All pages/screens where the feature appears
- [ ] Filter types per page (number, dropdown, text)
- [ ] Export inclusion list
- [ ] API endpoints that need the fields
- [ ] Integration list (Zapier, Workato, mobile)
- [ ] Analytics events with properties
- [ ] Audit log format and trigger
- [ ] Backward compatibility / migration behavior
- [ ] What is explicitly out of scope (e.g. "not in bulk update")
### From Wireframes
 
- [ ] Exact field values shown in screenshots (use these in scenarios)
- [ ] Exact error message text with red border/icon styling
- [ ] Info banner text (e.g. "Since the 'Pay Rate' field is hidden...")
- [ ] Dropdown options shown in open state
- [ ] Which fields show % suffix, /hour suffix, currency prefix
- [ ] Column headers in list views
- [ ] Filter popup options (Is, Is not, Contains, etc.)
- [ ] Export modal checkbox field names
- [ ] Disabled/greyed field visual states
- [ ] Notes/callouts annotated in the design (e.g. sticky notes saying "Need to add X")
- [ ] Footer totals in timesheet views (Total Pay Amt, Total Bill Amt)
- [ ] Status badges (Approved, Submitted, Open)
- [ ] Action buttons available per status
### From User Callouts
 
- [ ] Specific behaviors the user explicitly asks to test
- [ ] Change propagation boundaries (old records vs new records)
- [ ] Rule engine integration expectations
- [ ] Cross-portal sync expectations
---
 
## 6. Scenario Generation Patterns
 
### Pattern: Field Enable/Disable by Mode
 
For features with multiple modes, generate a matrix:
 
```
For each mode:
  - Which fields are user-editable?
  - Which fields are auto-calculated and disabled?
  - What happens when the user tries to interact with a disabled field?
  - What triggers the mode switch?
  - What recalculates when the mode switches?
```
 
**Template:**
```
Verify that when "[Mode]" is selected, the [Editable Field] is enabled and the user can enter a value.
Verify that when "[Mode]" is selected, the [Disabled Field] is disabled and auto-calculated by the system.
Verify that switching from "[Mode A]" to "[Mode B]" recalculates [Fields] correctly.
```
 
### Pattern: Validation Rules
 
For each validated field:
 
```
Verify that [Field] cannot be negative — entering a negative value shows a validation error.
Verify that [Field] must be between [Min] and [Max] — entering [Invalid Value] shows error "[Exact error text]".
Verify that [Field] allows up to [N] decimal places and rejects more.
Verify that when an invalid [Field] value is entered, dependent fields show 0/default until a valid value is provided.
```
 
### Pattern: Page/Screen Presence
 
For each page where the feature appears:
 
```
Verify that [Field 1], [Field 2], [Field 3] are displayed on the [Page Name].
Verify that the values shown on [Page Name] match the values set during [Source Action].
Verify that [Fields] are available under Hide/Show Columns on [Page Name] (if applicable).
```
 
### Pattern: Filter Availability
 
For each filterable field on each page:
 
```
Verify that a [filter type] filter for [Field] is available on the [Page Name] with [parameter options].
Verify that filtering by [Field] with condition "[Condition]" and value "[Value]" returns correct results.
Verify that combining [Field] filter with other existing filters works correctly.
```
 
### Pattern: Auto-Population Across Forms
 
```
Verify that values entered in [Source Form] are auto-populated into [Target Form].
Verify that auto-populated values in [Target Form] are editable by the user.
Verify that edited values in [Target Form] are saved independently from [Source Form] values.
```
 
### Pattern: Change Propagation Boundary
 
```
Verify that when [N] records exist with setting X, and the user changes settings to X+1, existing [N] records retain setting X.
Verify that only newly created records after the settings change use the new setting X+1.
Verify this by checking [specific column/field] on [specific page] — old records show [old value], new records show [new value].
```
 
---
 
## 7. Backend Validation Pattern
 
**Every frontend validation MUST have a corresponding backend validation scenario.**
 
For each API endpoint (Add, Edit, Enable, etc.):
 
### Mandatory Backend Scenarios
 
```
1. Range validation — reject values outside allowed range via API
2. Negative value rejection — reject negative values via API
3. Decimal precision — reject values with too many decimal places via API
4. Required field enforcement — reject missing mandatory fields via API
5. Auto-calculation enforcement — backend should recalculate derived fields even if client sends manual overrides
6. Conflicting value handling — if client sends a calculated field that doesn't match the formula, backend recalculates
7. Mode-specific validation — reject empty Margin when mode is Margin, etc.
8. HTTP error codes — proper 400 Bad Request with descriptive error messages
```
 
### Security-Focused Backend Scenarios
 
```
1. SQL injection in text/numeric fields
2. Script injection (XSS) in text fields
3. Non-numeric values in numeric fields
4. Extremely large values (overflow testing)
5. Division-by-zero edge cases via API
6. Consistency between frontend and backend error messages
```
 
---
 
## 8. Rule Engine Integration Pattern
 
When the feature affects pay rate or bill rate, and those rates flow into rule engine calculations:
 
### Read `references/rule-engine-summary.md` for detailed rule types.
 
### Key Principle
 
> **Bill Rate derived from margin/markup is NOT a separate concept for the rule engine. It's just the bill rate.** The rule engine uses whatever pay rate and bill rate are on the timesheet settings. The margin/markup feature changes HOW the bill rate is determined, but once determined, it flows through rule engine calculations identically to a manually entered bill rate.
 
### Required Scenarios per Rule Type
 
For each active rule type (Regular, Before Shift, After Shift, Specific Range, Daily OT, Weekly OT):
 
```
Verify that [Rule Type] with [multiplier]x multiplier correctly applies to the [feature]-derived rates:
Pay = hours × Pay Rate × [multiplier], Bill = hours × [derived Bill Rate] × [multiplier].
```
 
### Required Timesheet View Scenarios
 
```
Verify that individual day entries show correct Pay Amount and Bill Amount using [feature]-derived rates.
Verify that Total Pay Amt and Total Bill Amt at the bottom are correctly summed across all daily entries.
```
 
### Required Rate Change Propagation Scenarios
 
```
Verify that existing timesheets retain old rates after settings change.
Verify that new timesheets use updated rates after settings change.
Verify that the [specific amount column] on [specific page] shows different amounts for old vs new timesheets.
```
 
---
 
## 9. Cross-Portal Sync Pattern
 
When the feature's data is visible across multiple portals:
 
### Portals to Check
 
| Portal | Key Considerations |
|--------|-------------------|
| **RCRM App** | Source of truth — all fields visible |
| **Client Portal (HMP)** | May hide Pay Amount; check bill amounts only |
| **Contractor Portal** | May have limited fields; check pay amounts |
 
### Required Sync Scenarios
 
```
Verify that [feature values] are consistent across RCRM app, Client Portal, and Contractor Portal.
Verify that editing settings in RCRM and creating new records reflects updated values in all portals.
Verify that [Portal-specific restriction] is enforced (e.g. Client Portal hides Pay Amount).
```
 
---
 
## 10. Edge Case Thinking Framework
 
### Systematic Edge Case Generation
 
For **numeric fields**, always test:
 
| Case | Example |
|------|---------|
| Both values zero | Pay = 0, Bill = 0 |
| One value zero, other non-zero | Pay = 0, Bill = 20 |
| Division by zero | Margin = 100% (bill = pay / 0) |
| Boundary minimum | Margin = 0%, Markup = 0% |
| Boundary maximum | Margin = 99%, Markup = 10000% |
| Just outside boundary | Margin = 100%, Markup = 10001% |
| Negative values | Margin = -5% |
| Maximum decimal precision | 33.33% (2 decimals) |
| Exceeding decimal precision | 33.333% (3 decimals) |
| Very large valid value | Markup = 10000% |
| Result undefined → default to 0% | Any case where formula produces NaN or Infinity |
 
For **mode switching**, always test:
 
| Case | Description |
|------|------------|
| Rapid switching | Switch modes quickly back and forth |
| Switch with partial data | Switch mode when only some fields are filled |
| Switch preserving Pay Rate | Pay Rate should typically persist across mode switches |
| Switch and recalculate | All dependent fields recalculate on switch |
 
For **conditional visibility**, always test:
 
| Case | Description |
|------|------------|
| Show trigger | Switch to the condition that shows the fields |
| Hide trigger | Switch away from the condition |
| Hidden field default | What value do hidden fields default to? |
| Required + hidden conflict | What if a field is required in admin but hidden by condition? |
 
---
 
## 11. Domain Knowledge Reference
 
### Recruit CRM Feature Areas
 
For detailed rule engine formulas and behavior, read:
`references/rule-engine-summary.md`
 
### Margin & Markup Formulas
 
```
Markup Mode:
  Bill Rate = Pay Rate × (1 + Markup% / 100)
  Margin% = ((Bill Rate - Pay Rate) / Bill Rate) × 100
 
Margin Mode:
  Bill Rate = Pay Rate / (1 - Margin% / 100)
  Markup% = ((Bill Rate - Pay Rate) / Pay Rate) × 100
 
Fixed Rate Mode:
  Margin% = ((Bill Rate - Pay Rate) / Bill Rate) × 100
  Markup% = ((Bill Rate - Pay Rate) / Pay Rate) × 100
```
 
### Edge Case Defaults
 
| Condition | Margin | Markup | Bill Rate |
|-----------|--------|--------|-----------|
| Pay = 0, Bill = 0 | 0% | 0% | — |
| Pay = 0, Bill > 0 | 100% | 0% | — |
| Pay > 0, Bill = 0 | 0% | 0% | — |
| Margin = 100% | — | — | 0 (division by zero) |
| Pay = 0 + any % | — | — | $0 |
 
### Rule Engine Pay/Bill Formula
 
```
Pay Amount = Hours × Pay Rate × Pay Multiplier
Bill Amount = Hours × Bill Rate × Bill Multiplier
```
 
Where Bill Rate may be manually entered (Fixed Rate) or derived (Margin/Markup mode). Once derived, the rule engine treats it identically.
 
### Timesheet Settings Change Boundary
 
**Critical rule:** Changes to pay/bill/margin/markup settings apply ONLY to newly created timesheets. Existing timesheets retain the rates they were created with.
 
### Pages Where Job/Timesheet Fields Typically Appear
 
1. Add Job Form
2. Edit Job Form/Modal
3. Job Details Page
4. Job List Page (columns + filters)
5. Enable Timesheet Form
6. Contractor Details Page (timesheet info overview)
7. All Timesheets Page (columns + filters)
8. Reports Pages (columns + filters)
9. Timesheets under Deals
10. Export Modals (Job export, Timesheet export)
11. Admin Settings (visibility + required toggles)
12. Public API endpoints
13. Zapier / Workato
14. Mobile App
15. Automated Workflow email template placeholders
---
 
## Quick Start Checklist
 
When the user provides a PRD and/or wireframes and asks for test scenarios:
 
- [ ] Read the PRD completely — extract fields, modes, validations, formulas, edge cases
- [ ] Examine every wireframe screenshot — extract exact values, error messages, UI states
- [ ] Note any specific callouts from the user
- [ ] If rule engine is involved, read `references/rule-engine-summary.md`
- [ ] Build the subcategory list applicable to this feature
- [ ] Generate scenarios subcategory by subcategory
- [ ] Add backend validation scenarios mirroring every frontend validation
- [ ] Add edge case scenarios using the systematic framework
- [ ] Add cross-portal sync scenarios if applicable
- [ ] Add rule engine integration scenarios if pay/bill rates are affected
- [ ] Add rate change propagation scenarios if settings can be edited after records exist
- [ ] Output as CSV to `/mnt/user-data/outputs/` and present to user
- [ ] Report total scenario count and subcategory summary
