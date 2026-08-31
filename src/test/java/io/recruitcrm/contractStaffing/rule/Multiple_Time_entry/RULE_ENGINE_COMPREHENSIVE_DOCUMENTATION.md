# Rule Engine — Comprehensive Documentation

## Table of Contents

1. [Overview](#1-overview)
2. [Template Configuration](#2-template-configuration)
3. [Rule 1 — Regular Hours](#3-rule-1--regular-hours)
4. [Break Concept](#4-break-concept)
5. [Rule 2 — Before Shift](#5-rule-2--before-shift)
6. [Rule 3 — After Shift](#6-rule-3--after-shift)
7. [Rule 4 — Specific Hours Range](#7-rule-4--specific-hours-range)
8. [Rule 5 — Daily Overtime](#8-rule-5--daily-overtime)
9. [Rule 6 — Weekly Overtime](#9-rule-6--weekly-overtime)
10. [Rule Priority & Hour Allocation](#10-rule-priority--hour-allocation)
11. [Comprehensive Examples](#11-comprehensive-examples)
12. [Edge Cases & Special Scenarios](#12-edge-cases--special-scenarios)
13. [Validation Rules Summary](#13-validation-rules-summary)
14. [Formulas Reference](#14-formulas-reference)

---

## 1. Overview

The Rule Engine enables users to define and configure custom templates for calculating pay and bill data on timesheets. Templates can include up to 6 rule types, each with its own multiplier and conditions. The engine evaluates rules in a strict priority order, allocating worked hours to the highest-priority applicable rule first.

### Core Principles

- **No rule = no pay**: Hours not covered by any configured rule are unallocated and unpaid/unbilled.
- **No overlap**: Range-based rules cannot overlap with each other on the same day.
- **Priority-based allocation**: Rules are evaluated in a fixed priority order; higher-priority rules claim hours first.
- **Threshold-based OT**: Daily and Weekly Overtime use total-hours thresholds, not time ranges.
- **Break applies everywhere**: Break logic (paid/unpaid) applies across all range-based rules.

### Two Logging Methods

| Method | Description | Available Rules |
|--------|-------------|-----------------|
| **Start & End Time** (Shift) | User logs clock-in and clock-out times | All 6 rules |
| **Work Hours** (Duration) | User logs total hours worked | Regular, Daily OT, Weekly OT |

---

## 2. Template Configuration

### Regular Workdays and Working Hours

- User selects working days (e.g., Mon–Fri) and defines a time range per day (e.g., 9:00 AM – 5:00 PM).
- Days not selected are marked as **Day Off**.
- Different days can have different schedules (e.g., Mon–Thu 8 hrs, Fri 7 hrs).

### Break Configuration

| Setting | Options |
|---------|---------|
| Break Hours Paid and Billed? | **Yes** / **No** |
| Set Anticipated Daily Break | Only visible when Break = **No** (e.g., 0h 30min, 1h 0min) |

### Custom Rules

Users can add custom rules under the "Custom Rules" section. Each rule has:
- **Rule Name** (user-defined label)
- **Rule Type** (Before Shift, After Shift, Specific Time Range, Daily Overtime, Weekly Overtime)
- **Apply on Days** (selectable, except Weekly OT which is always all days)
- **Time/Threshold Configuration** (depends on rule type)
- **Calculate Charge By** (Multiplier)
- **Pay Rate Multiplier** and **Bill Rate Multiplier**

---

## 3. Rule 1 — Regular Hours

### Description

Regular Hours is the foundational rule. It defines the standard daily working schedule and pays at the base rate (1x multiplier). This is a **system rule** — it is always present and cannot be removed.

### Configuration

| Field | Example |
|-------|---------|
| Working Days | Mon–Fri |
| Schedule per day | 09:00 AM – 05:00 PM (8 hrs) |

### Behavior

- Work **within** the defined schedule → paid at **1x** (regular rate).
- Work **less** than the schedule → paid for **actual hours worked** only.
- Work **beyond** the schedule → the extra hours are allocated to other configured rules (Before Shift, After Shift, Daily OT, etc.). If no rule covers them, they are **unallocated (unpaid)**.

### Effective Regular Hours

When an anticipated break is configured (Break Paid = No):

```
Effective Regular Hours = Scheduled Regular Hours − Anticipated Break
```

| Scheduled | Anticipated Break | Effective Regular |
|-----------|-------------------|-------------------|
| 8 hrs | 0 hr | 8 hrs |
| 8 hrs | 1 hr | 7 hrs |
| 8 hrs | 1.5 hrs | 6.5 hrs |

The **Effective Regular Hours** is the maximum number of hours paid at the regular rate. Any work beyond this threshold (within the regular range) is excess that flows to overtime rules.

### Examples

**Setup:** Regular 9 AM – 5 PM (8 hrs), Anticipated Break = 1 hr, Effective = 7 hrs

| User Entry | Actual Break | Effective Work | Regular Pay | Excess | Notes |
|-----------|-------------|----------------|-------------|--------|-------|
| 9 AM – 5 PM | 0 hrs | 8 hrs | 7 hrs | 1 hr → OT (if threshold met) | User didn't take break |
| 9 AM – 5 PM | 1 hr | 7 hrs | 7 hrs | 0 hrs | Break = anticipated |
| 9 AM – 5 PM | 2 hrs | 6 hrs | 6 hrs | 0 hrs | More break than anticipated |
| 9 AM – 5 PM | 0.5 hrs | 7.5 hrs | 7 hrs | 0.5 hrs → OT (if threshold met) | Less break than anticipated |
| 9 AM – 4 PM | 0 hrs | 7 hrs | 7 hrs | 0 hrs | Worked exactly effective hours |

---

## 4. Break Concept

### Option A — Break Paid and Billed = YES

- Break time is **paid** at the **regular rate (1x)**, regardless of which rule segment the break falls in.
- If break occurs within a premium rule (e.g., Before Shift at 2x), the break portion is paid at 1x and the remaining work time at the rule's multiplier.
- No change to overtime threshold logic.
- No "Anticipated Break" field.

**Example:** Before Shift (2x), user works 5 AM – 7 AM with break 5:30 – 6:00 AM:
- 30 min break → paid at **1x**
- 1.5 hrs remaining → paid at **2x** (Before Shift rate)

### Option B — Break Paid and Billed = NO

- Break time is **unpaid** — deducted from working hours.
- **Anticipated Daily Break** field appears (e.g., 30 min, 1 hr).
- Overtime calculations are based on actual working hours (excluding break).

#### Anticipated Break Scenarios

**Setup:** Regular 9 AM – 5 PM (8 hrs), Anticipated Break = 1 hr, Effective Regular = 7 hrs

**Scenario 1 — Actual Break = Anticipated Break (1 hr)**
```
Actual Working Hours = 8 − 1 = 7 hrs (paid for 7 hrs)
Unused anticipated = 0 → nothing moves to OT
```

**Scenario 2 — Actual Break > Anticipated Break (2 hrs)**
```
Actual Working Hours = 8 − 2 = 6 hrs (paid for 6 hrs only)
User took more break than anticipated → extra deducted
```

**Scenario 3 — Actual Break < Anticipated Break (0.5 hrs)**
```
Actual Working Hours in regular = 7 hrs (capped at effective regular)
Unused anticipated = 1 − 0.5 = 0.5 hrs
0.5 hrs excess → moves to Daily OT or Weekly OT (if threshold is met)
```

#### Anticipated Break Constraints

- Only available when Break Paid = **No**.
- Duration entered in time format (e.g., 30 min, 1 hr).
- Cannot exceed the shortest scheduled workday.
- Does **not** modify the scheduled time range — only affects:
  - Effective working hours calculation
  - Overtime threshold calculation

### Break in All Rule Segments

Break can occur within **any** range-based rule:

| Rule Segment | Break Paid = YES | Break Paid = NO |
|-------------|------------------|-----------------|
| Regular Hours | Break paid at 1x | Break deducted, anticipated logic applies |
| Before Shift | Break paid at 1x, rest at rule multiplier | Break deducted from before-shift hours |
| After Shift | Break paid at 1x, rest at rule multiplier | Break deducted from after-shift hours |
| Specific Hours Range | Break paid at 1x, rest at rule multiplier | Break deducted from specific range hours |

### Multiple Breaks Per Day

Users can log multiple break intervals in a single day (e.g., 30 min morning break + 1 hr lunch). All intervals are summed and compared against the single anticipated break duration.

---

## 5. Rule 2 — Before Shift

### Description

Applies a premium rate to hours worked **before** the regular shift start time. Covers the period from midnight (12:00 AM) to the user-specified time.

### Configuration

| Field | Description |
|-------|------------|
| Rule Type | Before Shift |
| Enter Start Time | Single time value (e.g., 7:00 AM) |
| Apply on Days | Selectable (can include weekdays, weekends, days off) |
| Pay Rate Multiplier | e.g., 2 |
| Bill Rate Multiplier | e.g., 2 |

### How the Range is Calculated

User enters a single time value. The system auto-calculates the range:

```
Before Shift Range = 12:00 AM (midnight) → Entered Time
```

| User Enters | Before Shift Window |
|------------|-------------------|
| 7:00 AM | 12:00 AM – 7:00 AM |
| 5:00 AM | 12:00 AM – 5:00 AM |
| 9:00 AM | 12:00 AM – 9:00 AM (no gap to regular) |

### Validation Rules

| Constraint | Detail |
|-----------|--------|
| Cannot be 00:00 | 12:00 AM is invalid (zero-length window) |
| Must be ≤ Regular start time | If regular starts at 9 AM, before shift time must be ≤ 9:00 AM |
| One per day | Cannot apply Before Shift twice on the same day |
| Day off exception | If the day is a day off (no regular hours), any time is allowed (no conflict) |

### Gap Between Before Shift and Regular

If Before Shift ends before Regular starts, there is a **gap** that is unallocated:

```
Before Shift: 7:00 AM → Gap: 7:00 AM – 9:00 AM (unpaid) → Regular: 9:00 AM
```

To eliminate the gap, set Before Shift time = Regular start time (e.g., 9:00 AM).

### Example

**Setup:** Regular 9 AM – 5 PM (1x), Before Shift 7 AM (2x), Pay rate = 1, Bill rate = 2

User works 6 AM – 6 PM:

| Time Range | Rule | Hours | Pay | Bill |
|-----------|------|-------|-----|------|
| 6:00 – 7:00 AM | Before Shift | 1 hr | 1 × 1 × 2 = **2** | 1 × 2 × 2 = **4** |
| 7:00 – 9:00 AM | No rule (gap) | 2 hrs | **0** | **0** |
| 9:00 AM – 5:00 PM | Regular | 8 hrs | 8 × 1 × 1 = **8** | 8 × 2 × 1 = **16** |
| 5:00 – 6:00 PM | No rule | 1 hr | **0** | **0** |
| **Total** | | **12 hrs** | **10** | **20** |

---

## 6. Rule 3 — After Shift

### Description

Applies a premium rate to hours worked **after** the regular shift end time. Covers the period from the user-specified time to midnight (12:00 AM). This is the mirror of Before Shift.

### Configuration

| Field | Description |
|-------|------------|
| Rule Type | After Shift |
| Enter Start Time | Single time value (e.g., 6:00 PM) |
| Apply on Days | Selectable |
| Pay Rate Multiplier | e.g., 2 |
| Bill Rate Multiplier | e.g., 2 |

### How the Range is Calculated

```
After Shift Range = Entered Time → 12:00 AM (midnight)
```

| User Enters | After Shift Window |
|------------|-------------------|
| 6:00 PM | 6:00 PM – 12:00 AM |
| 8:00 PM | 8:00 PM – 12:00 AM |
| 5:00 PM | 5:00 PM – 12:00 AM (no gap from regular) |

### Validation Rules

| Constraint | Detail |
|-----------|--------|
| Cannot be 00:00 | 12:00 AM is invalid |
| Must be ≥ Regular end time | If regular ends at 5 PM, after shift time must be ≥ 5:00 PM |
| One per day | Cannot apply After Shift twice on the same day |
| Day off exception | Any time allowed on days off |

### Mirror Symmetry

| Rule | User Enters | Window | Direction |
|------|-----------|--------|-----------|
| Before Shift | 7:00 AM | 12:00 AM → 7:00 AM | Midnight to entered time |
| After Shift | 6:00 PM | 6:00 PM → 12:00 AM | Entered time to midnight |

### Example

**Setup:** Regular 9 AM – 5 PM (1x), After Shift 6 PM (2x), Pay rate = 1, Bill rate = 2

User works 7 AM – 10 PM:

| Time Range | Rule | Hours | Pay | Bill |
|-----------|------|-------|-----|------|
| 7:00 – 9:00 AM | No rule | 2 hrs | **0** | **0** |
| 9:00 AM – 5:00 PM | Regular | 8 hrs | 8 × 1 × 1 = **8** | 8 × 2 × 1 = **16** |
| 5:00 – 6:00 PM | No rule (gap) | 1 hr | **0** | **0** |
| 6:00 – 10:00 PM | After Shift | 4 hrs | 4 × 1 × 2 = **8** | 4 × 2 × 2 = **16** |
| **Total** | | **15 hrs** | **16** | **32** |

---

## 7. Rule 4 — Specific Hours Range

### Description

Allows users to define custom time windows with specific start and end times. Unlike Before/After Shift (single time value), this rule takes a full range. Multiple non-overlapping Specific Hours Range rules can be added per day.

### Configuration

| Field | Description |
|-------|------------|
| Rule Type | Specific Time Range |
| Enter Start – End Time | User-defined range (e.g., 05:00 AM – 08:00 AM) |
| Apply on Days | Selectable (including weekends/days off) |
| Pay Rate Multiplier | Per rule (e.g., 2) |
| Bill Rate Multiplier | Per rule (e.g., 2) |

### Key Differences from Before/After Shift

| Feature | Before Shift | After Shift | Specific Hours Range |
|---------|-------------|-------------|---------------------|
| User enters | Single time value | Single time value | **Start AND End time** |
| Window | 12 AM → entered time | Entered time → 12 AM | **Exact user-defined range** |
| Instances per day | 1 max | 1 max | **Multiple allowed** (non-overlapping) |
| Different multipliers | One multiplier | One multiplier | **Each instance can have different multipliers** |

### Validation — No Overlap

Specific Hours Range **cannot overlap** with any existing rule on the same day:

| Existing Rule | Blocked Range |
|--------------|---------------|
| Regular (9 AM – 5 PM) | Cannot overlap 9 AM – 5 PM |
| Before Shift (7 AM) | Cannot overlap 12 AM – 7 AM |
| After Shift (6 PM) | Cannot overlap 6 PM – 12 AM |
| Another Specific Range (7 PM – 10 PM) | Cannot overlap 7 PM – 10 PM |

If overlap is detected, the system shows: *"The selected time range overlaps with an existing rule for the same day. Please adjust the time."*

### Example

**Setup:** Regular 9 AM – 5 PM (1x), Specific Range: 5 AM – 8 AM (2x) and 7 PM – 10 PM (2x), Pay = 1, Bill = 2

User works 7 AM – 11 PM:

| Time Range | Rule | Hours | Pay | Bill |
|-----------|------|-------|-----|------|
| 7:00 – 8:00 AM | Specific Range | 1 hr | 1 × 1 × 2 = **2** | 1 × 2 × 2 = **4** |
| 8:00 – 9:00 AM | No rule (gap) | 1 hr | **0** | **0** |
| 9:00 AM – 5:00 PM | Regular | 8 hrs | 8 × 1 × 1 = **8** | 8 × 2 × 1 = **16** |
| 5:00 – 7:00 PM | No rule (gap) | 2 hrs | **0** | **0** |
| 7:00 – 10:00 PM | Specific Range | 3 hrs | 3 × 1 × 2 = **6** | 3 × 2 × 2 = **12** |
| 10:00 – 11:00 PM | No rule | 1 hr | **0** | **0** |
| **Total** | | **16 hrs** | **16** | **32** |

---

## 8. Rule 5 — Daily Overtime

### Description

A threshold-based rule that applies a premium rate when total daily working hours exceed a defined threshold. Unlike range-based rules, Daily OT does not define a time window — it claims **unallocated hours** based on total-hours calculation.

### Configuration

| Field | Description |
|-------|------------|
| Rule Type | Daily Overtime |
| Enter Daily Hours Threshold | Hours + minutes (e.g., 9h 0min) |
| Apply on Days | Selectable |
| Pay Rate Multiplier | e.g., 2 |
| Bill Rate Multiplier | e.g., 2 |

### Key Features

| Feature | Detail |
|---------|--------|
| **Multiple tiers** | Allowed — each must have a unique threshold per day |
| **Minimum threshold** | Must be ≥ scheduled regular hours for that day |
| **Based on** | Total daily working hours (all rules count) |
| **Claims** | Unallocated time ranges (not already claimed by higher-priority rules) |

### Tiered Daily Overtime

Multiple tiers create bands with different multipliers:

**Example:** Tier 1: > 9h (2x), Tier 2: > 10h (3x)

| Total Hours | Band | Multiplier |
|------------|------|-----------|
| 0 – 9 hrs | Regular + other rules | 1x (or rule-specific) |
| 9 – 10 hrs | Daily OT Tier 1 | 2x |
| 10+ hrs | Daily OT Tier 2 | 3x |

Duplicate thresholds on the same day are invalid: *"A daily overtime rule already exists for the selected time range on one or more days."*

### Threshold as a Gate

The daily OT threshold acts as a **gate**:

- **Gate CLOSED** (total daily work ≤ threshold): excess above effective regular = **unallocated (unpaid)**
- **Gate OPEN** (total daily work > threshold): excess above effective regular becomes **Daily OT**

When the gate opens:
- **Without Weekly OT**: Daily OT = all excess above effective regular
- **With Weekly OT**: Daily OT = total − daily_threshold; the gap between effective regular and daily threshold goes to Weekly OT (if weekly threshold is met)

### Total Hours — What Counts Toward DOT Threshold

**All clocked hours from ALL rules** count toward the Daily OT threshold, including:
- Regular Hours (work within scheduled range)
- Before Shift hours (work in BS zone)
- After Shift hours (work in AS zone)
- Specific Hours Range hours (work in SR zone)
- Unallocated gap hours (work between rule zones)

The threshold check uses the **total** of all the above. The Daily OT rule only **claims unallocated time ranges** (those not already taken by higher-priority rules).

#### Break Impact on Threshold

| Break Type | Threshold Calculation |
|------------|----------------------|
| **No Break** | `toward = total_clocked` |
| **Break Paid = YES** | `toward = total_clocked` (breaks are paid at 1x and counted) |
| **Break Paid = NO** | `toward = total_clocked − actual_break_taken` (unpaid break subtracted) |

For **BPN with Anticipated Break**: if `anticipated > actual_break`, the unused anticipated portion (`anticipated − actual`) reduces regular pay and frees those hours into the unallocated pool, making them available for DOT allocation.

### Examples

#### Example 1 — Before Shift + Daily OT (No Break)

**Setup:** Regular 9 AM – 5 PM (8 hrs), Before Shift 7 AM (2x), Daily OT > 8h (2x), Pay = 1, Bill = 2

**User works 3 AM – 12 PM = 9 hrs:**

| Time Range | Rule | Hours |
|-----------|------|-------|
| 3:00 – 7:00 AM | Before Shift | 4 hrs |
| 7:00 – 9:00 AM | Gap (unallocated) | 2 hrs |
| 9:00 AM – 12:00 PM | Regular | 3 hrs |

- **Toward threshold:** BS(4) + gap(2) + reg(3) = **9 hrs**
- **9 > 8** → gate OPEN, excess = 1
- Unallocated pool = 2 hrs (gap)
- **DOT = min(1, 2) = 1 hr** (from gap)
- Remaining 1 hr gap = unpaid

| Rule | Hours | Pay | Bill |
|------|-------|-----|------|
| Before Shift | 4 hrs | 4 × 1 × 2 = **8** | 4 × 2 × 2 = **16** |
| Regular | 3 hrs | 3 × 1 × 1 = **3** | 3 × 2 × 1 = **6** |
| Daily OT | 1 hr | 1 × 1 × 2 = **2** | 1 × 2 × 2 = **4** |
| **Total** | **9 hrs** | **13** | **26** |

#### Example 2 — Before Shift + Daily OT (Threshold Exactly Met)

**User works 2 AM – 12 PM = 10 hrs:**

| Time Range | Rule | Hours |
|-----------|------|-------|
| 2:00 – 7:00 AM | Before Shift | 5 hrs |
| 7:00 – 9:00 AM | Gap (unallocated) | 2 hrs |
| 9:00 AM – 12:00 PM | Regular | 3 hrs |

- **Toward threshold:** BS(5) + gap(2) + reg(3) = **10 hrs**
- **10 > 8** → gate OPEN, excess = 2
- Unallocated pool = 2 hrs (gap)
- **DOT = min(2, 2) = 2 hrs** (all gap becomes DOT)

| Rule | Hours | Pay | Bill |
|------|-------|-----|------|
| Before Shift | 5 hrs | 5 × 1 × 2 = **10** | 5 × 2 × 2 = **20** |
| Regular | 3 hrs | 3 × 1 × 1 = **3** | 3 × 2 × 1 = **6** |
| Daily OT | 2 hrs | 2 × 1 × 2 = **4** | 2 × 2 × 2 = **8** |
| **Total** | **10 hrs** | **17** | **34** |

#### Example 3 — After Shift + Daily OT

**Setup:** Regular 9 AM – 5 PM (8 hrs), After Shift 19:00 (2x), Daily OT > 8h (2x)

**User works 9 AM – 11 PM = 14 hrs:**

| Time Range | Rule | Hours |
|-----------|------|-------|
| 9:00 AM – 5:00 PM | Regular | 8 hrs |
| 5:00 – 7:00 PM | Gap (unallocated) | 2 hrs |
| 7:00 – 11:00 PM | After Shift | 4 hrs |

- **Toward threshold:** reg(8) + gap(2) + AS(4) = **14 hrs**
- **14 > 8** → gate OPEN, excess = 6
- Unallocated pool = 2 hrs (gap)
- **DOT = min(6, 2) = 2 hrs**

#### Example 4 — Before Shift + Daily OT with BPN

**Setup:** Regular 9 AM – 5 PM (8 hrs), Before Shift 7 AM (2x), Daily OT > 8h (2x), Break Paid = No, Anticipated = 1 hr

**User works 5 AM – 6 PM = 13 hrs, break 12–1 PM (1 hr):**

- **Toward threshold:** total − actual_break = 13 − 1 = **12 hrs**
- **12 > 8** → gate OPEN, excess = 4
- BS=2h, reg_paid=7h (8−1ant), gap=3h (7–9 + 17–18)
- Unallocated pool = 3 hrs (gap)
- **DOT = min(4, 3) = 3 hrs**

### Daily OT with Anticipated Break (Regular Hours Only)

**Setup:** Regular 9 AM – 5 PM (8 hrs), Anticipated Break = 1 hr, Effective = 7 hrs, Daily OT > 8h (2x), Pay = 1

| User Entry | Break Taken | Effective Work | Threshold Check | Regular | Daily OT | Pay |
|-----------|------------|----------------|-----------------|---------|----------|-----|
| 9 AM – 5 PM | 0 hrs | 8 hrs | 8 NOT > 8 → closed | 7 hrs | 0 (1 hr unallocated) | 7 |
| 9 AM – 6 PM | 0 hrs | 9 hrs | 9 > 8 → open | 7 hrs | 2 hrs | 7 + 4 = 11 |
| 9 AM – 6 PM | 1 hr (4–5 PM) | 8 hrs | 8 NOT > 8 → closed | 7 hrs | 0 (1 hr unallocated) | 7 |

---

## 9. Rule 6 — Weekly Overtime

### Description

A threshold-based rule applied across the entire week. When total weekly hours exceed the threshold, excess hours are treated as overtime. This is the **lowest priority** rule.

### Configuration

| Field | Description |
|-------|------------|
| Rule Type | Weekly Overtime |
| Weekly Hours Threshold | Hours + minutes (e.g., 48:00) |
| Apply on Days | **All days** (auto-selected, cannot be changed) |
| Pay Rate Multiplier | e.g., 3 |
| Bill Rate Multiplier | e.g., 2 |

### Key Features

| Feature | Detail |
|---------|--------|
| **Single tier only** | Cannot be applied twice (no tiered weekly OT) |
| **All days** | Applies to every day in the template |
| **Minimum threshold** | Sum of all scheduled daily hours |
| **Biweekly** | Threshold applied **per week** independently |
| **Timesheet types** | Only for weekly and biweekly timesheets |

### Minimum Threshold Calculation

Based on **scheduled hours** (not effective hours after anticipated break):

```
Example:
Mon–Thu: 9 AM – 5 PM = 8 hrs × 4 = 32
Fri:     9 AM – 4 PM = 7 hrs × 1 =  7
────────────────────────────────────────
Minimum weekly threshold = 39 hrs
```

The threshold picker in the UI starts at this calculated minimum.

### How It Works

1. Sum all effective working hours across the entire week (all days including day-off work).
2. If total weekly work > weekly threshold → excess hours = Weekly Overtime.
3. Weekly OT claims **remaining unallocated hours** after all higher-priority rules.

### Total Hours — What Counts Toward WOT Threshold

The same rules that apply to Daily OT also apply to Weekly OT:

**All clocked hours from ALL rules** count toward the Weekly OT threshold, including:
- Regular Hours
- Before Shift hours
- After Shift hours
- Specific Hours Range hours
- Unallocated gap hours

#### Break Impact on WOT Threshold

| Break Type | Threshold Calculation |
|------------|----------------------|
| **No Break** | `weekly_toward = sum of all daily total_clocked` |
| **Break Paid = YES** | `weekly_toward = sum of all daily total_clocked` (breaks counted) |
| **Break Paid = NO** | `weekly_toward = sum of all daily (total_clocked − actual_break)` |

For **BPN with Anticipated Break**: same logic as DOT — if `anticipated > actual_break`, unused anticipated frees regular hours into the unallocated pool for WOT allocation.

### Day Off / Weekend Work

When a user works on a **non-working day** (Day Off):
- No regular hours are defined for that day.
- **All working hours on that day go to Weekly Overtime** (if threshold is met).
- Break still applies:
  - Break Paid = NO → break deducted from hours
  - Break Paid = YES → no deduction

### Examples — Before Shift + Weekly OT

#### Example 1 — BS + WOT (No Break, Single Day)

**Setup:** Regular 9 AM – 5 PM, Before Shift 7 AM (2x), Weekly OT > 8h (2x), Pay = 1, Bill = 2

**User works Mon 5 AM – 6 PM = 13 hrs:**

| Time Range | Rule | Hours |
|-----------|------|-------|
| 5:00 – 7:00 AM | Before Shift | 2 hrs |
| 7:00 – 9:00 AM | Gap (unallocated) | 2 hrs |
| 9:00 AM – 5:00 PM | Regular | 8 hrs |
| 5:00 – 6:00 PM | Gap (unallocated) | 1 hr |

- **Toward threshold:** BS(2) + gap(2+1) + reg(8) = **13 hrs**
- **13 > 8** → excess = 5. Unallocated pool = 3 hrs.
- **WOT = min(5, 3) = 3 hrs**

#### Example 2 — BS + WOT (BPN, Anticipated Break)

**Setup:** Same as above + Break Paid = No, Anticipated = 1 hr

**User works Mon 5 AM – 6 PM = 13 hrs, no break taken:**

- Total = 13, actual break = 0, ant = 1 hr
- **Toward:** 13 − 0 = **13 hrs**. 13 > 8, excess = 5.
- reg_paid = 8 − 1(ant) = 7. Ant freed = 1 hr → unallocated pool.
- Unallocated = gap(3) + ant_freed(1) = 4
- **WOT = min(5, 4) = 4 hrs**

#### Example 3 — BS + WOT (Multi-Day, BPN)

**Setup:** Regular 9 AM – 5 PM, Before Shift 7 AM (2x), Weekly OT > 40h (2x), Break Paid = No, Ant = 1hr

**Mon–Fri: Each day 5 AM – 6 PM = 13 hrs, break 12–1 PM:**

Per day: total=13, break=1(=ant). toward = 13−1 = 12. BS=2, reg=7, gap=3.
Weekly toward = 60. **60 > 40**, excess = 20. Weekly unalloc = 15 (5×3).
**WOT = min(20, 15) = 15 hrs**

### Weekly OT Interaction with Daily OT

Weekly OT picks up the "gap" hours between effective regular and the daily OT threshold:

```
Example:
Effective Regular = 2 hrs, Daily OT > 5 hrs, Weekly OT > 6 hrs
Total daily work = 21 hrs

Regular:    2 hrs  (hours 1–2)
Gap:        3 hrs  (hours 3–5, between effective regular and daily threshold)
Daily OT:  16 hrs  (hours 6–21, above daily threshold)

Gap (3 hrs) → Weekly OT (since weekly total 21 > 6 threshold)
```

---

## 10. Rule Priority & Hour Allocation

### Priority Order

| Priority | Rule | Type | Claims |
|----------|------|------|--------|
| 1 (highest) | Regular Hours | Range-based | Scheduled time range (effective hours) |
| 2 | Before Shift | Range-based | 12 AM → entered time |
| 3 | After Shift | Range-based | Entered time → 12 AM |
| 4 | Specific Hours Range | Range-based | User-defined start–end |
| 5 | Daily Overtime | Threshold-based | Unallocated hours (daily threshold) |
| 6 (lowest) | Weekly Overtime | Threshold-based | Remaining unallocated hours (weekly threshold) |

### Allocation Flow

```
User logs work hours for a day
         │
         ▼
┌─ Priority 1: Regular Hours ─────────────────────┐
│  Claims effective hours within scheduled range   │
└──────────────────────────────────────────────────┘
         │ remaining hours
         ▼
┌─ Priority 2: Before Shift ──────────────────────┐
│  Claims hours within 12 AM → entered time        │
└──────────────────────────────────────────────────┘
         │ remaining hours
         ▼
┌─ Priority 3: After Shift ───────────────────────┐
│  Claims hours within entered time → 12 AM        │
└──────────────────────────────────────────────────┘
         │ remaining hours
         ▼
┌─ Priority 4: Specific Hours Range ──────────────┐
│  Claims hours within user-defined ranges         │
└──────────────────────────────────────────────────┘
         │ remaining hours (unallocated pool)
         ▼
┌─ Priority 5: Daily Overtime ────────────────────┐
│  If total daily > threshold → claims from pool   │
│  (gate must be open)                             │
└──────────────────────────────────────────────────┘
         │ remaining hours
         ▼
┌─ Priority 6: Weekly Overtime ───────────────────┐
│  If total weekly > threshold → claims remainder  │
└──────────────────────────────────────────────────┘
         │ remaining hours
         ▼
    Unallocated (unpaid / unbilled)
```

### Time Range Assignment

When Daily/Weekly OT claims unallocated hours, time ranges are assigned **chronologically** — starting from the earliest unallocated time and filling forward. Daily OT fills first (higher priority), Weekly OT gets the remaining tail end.

---

## 11. Comprehensive Examples

### Example A — All Rules Active

**Rules:**
- Regular: 9 AM – 5 PM (8 hrs, 1x)
- Before Shift: 7 AM (2x)
- After Shift: 6 PM (2x)
- Specific Range: 5 AM – 7 AM (1.5x)
- Daily OT: > 10 hrs (1.5x)
- Weekly OT: > 40 hrs/week (2x)
- Break Paid = YES
- Pay rate = 1, Bill rate = 2

**User works 4 AM – 11 PM = 19 hrs on Monday:**

| Time Range | Rule | Hours | Pay | Bill |
|-----------|------|-------|-----|------|
| 4:00 – 5:00 AM | Before Shift | 1 hr | 1 × 1 × 2 = 2 | 1 × 2 × 2 = 4 |
| 5:00 – 7:00 AM | Specific Range | 2 hrs | 2 × 1 × 1.5 = 3 | 2 × 2 × 1.5 = 6 |
| 7:00 – 9:00 AM | No rule (gap) | 2 hrs | 0 | 0 |
| 9:00 AM – 5:00 PM | Regular | 8 hrs | 8 × 1 × 1 = 8 | 8 × 2 × 1 = 16 |
| 5:00 – 6:00 PM | No rule (gap) | 1 hr | 0 | 0 |
| 6:00 – 11:00 PM | After Shift | 5 hrs | 5 × 1 × 2 = 10 | 5 × 2 × 2 = 20 |

Allocated = BS(1) + SR(2) + Reg(8) + AS(5) = 16 hrs. Unallocated = gap(2 + 1) = 3 hrs.
**Toward threshold** = 1 + 2 + 2 + 8 + 1 + 5 = **19 hrs** (ALL hours count, including BS, AS, SR).
19 > 10 → gate open. Excess = 9. Unallocated pool = 3.
Daily OT = min(9, 3) = 3 hrs. Claims 3 unallocated hours:

| 7:00 – 9:00 AM | Daily OT | 2 hrs | 2 × 1 × 1.5 = 3 | 2 × 2 × 1.5 = 6 |
| 5:00 – 6:00 PM | Daily OT | 1 hr | 1 × 1 × 1.5 = 1.5 | 1 × 2 × 1.5 = 3 |

**Totals: Pay = 27.5, Bill = 55**

If this is the only day worked that week (19 hrs < 40), no Weekly OT.

---

### Example B — Anticipated Break + Daily OT + Weekly OT

**Rules:**
- Regular: 9 AM – 12 PM (3 hrs), 1 working day per week
- Break Paid = No, Anticipated Break = 1 hr → Effective Regular = 2 hrs
- Daily OT: > 5 hrs (2x)
- Weekly OT: > 6 hrs/week (2x)
- Pay rate = 1, Bill rate = 2

**User works 2 AM – 11 PM = 21 hrs, no break taken:**

| Step | Rule | Time Range | Hours | Rate |
|------|------|-----------|-------|------|
| 1 | Regular | 9 AM – 11 AM | 2 hrs | 1x |
| 2 | Daily OT | 2 AM – 9 AM + 11 AM – 8 PM | 16 hrs | 2x |
| 3 | Weekly OT | 8 PM – 11 PM | 3 hrs | 2x |
| **Total** | | | **21 hrs** | |

**Calculation:**
- Effective regular = 3 − 1 = 2 hrs
- Total work = 21, Daily OT > 5: 21 > 5 → gate open
- Daily OT = 21 − 5 = 16 hrs (claims chronologically from unallocated pool)
- Gap = 5 − 2 = 3 hrs → Weekly OT (total 21 > 6 → met)
- Pay: (2 × 1) + (16 × 1 × 2) + (3 × 1 × 2) = 2 + 32 + 6 = **40**
- Bill: (2 × 2) + (16 × 2 × 2) + (3 × 2 × 2) = 4 + 64 + 12 = **80**

---

### Example C — Day Off Work

**Rules:**
- Regular: Mon–Fri 9 AM – 5 PM (8 hrs)
- Weekly OT: > 40 hrs/week (2x)
- Break Paid = No, Anticipated Break = 30 min
- Pay rate = 1, Bill rate = 2

**User works Mon–Fri (8 hrs/day = 40 hrs) + Saturday (6 hrs, 1 hr break):**

| Day | Logged | Break | Effective | Rule |
|-----|--------|-------|-----------|------|
| Mon–Fri | 8 hrs/day | 30 min/day | 7.5 hrs/day | Regular |
| Saturday | 6 hrs | 1 hr | 5 hrs | Weekly OT |

- Weekly total effective = (7.5 × 5) + 5 = 37.5 + 5 = 42.5 hrs
- Weekly OT = 42.5 − 40 = 2.5 hrs (from Saturday's hours)
- Saturday remaining = 5 − 2.5 = 2.5 hrs → unallocated (below weekly threshold cutoff, already counted)

---

## 12. Edge Cases & Special Scenarios

### Multiple Time Entries Per Day

Users can log multiple entries in a single day:
```
Monday: 7:00–9:00 AM, 9:00–11:00 AM, 1:00–6:00 PM, 7:00–8:00 PM
```
All entries are summed for daily total calculations. Each entry's time ranges are individually matched against rule windows.

### Break Across Multiple Rules

If a break spans across rule boundaries (e.g., starts in Regular, ends in After Shift), the break time is allocated proportionally to each segment. The break placement determines which rule's hours are reduced.

### Daily OT Without Weekly OT

When Daily OT threshold is met but no Weekly OT is configured:
- All excess above effective regular becomes Daily OT.
- The gap between effective regular and daily threshold is absorbed into Daily OT.

### Daily OT With Weekly OT

When both are configured and both thresholds are met:
- Daily OT claims: `total − daily_threshold` hours
- Gap (between effective regular and daily threshold) → Weekly OT
- Weekly OT claims: remaining unallocated hours (capped by what's available)

### Neither OT Threshold Met

If total hours do not exceed any OT threshold:
- Excess above effective regular = **unallocated (unpaid)**
- Only regular hours (and other range-based rules) are paid

### Different Schedules Per Day

Different days can have different regular hours (e.g., Mon–Thu 8 hrs, Fri 7 hrs). Each day's:
- Effective regular is calculated independently
- Daily OT threshold minimum is based on that day's scheduled hours
- Weekly OT minimum is the sum across all scheduled days

---

## 13. Validation Rules Summary

### Regular Hours
- At least one working day must be selected
- Start time must be before end time

### Before Shift
- Entered time must be > 00:00 and ≤ regular start time
- One per day
- Cannot overlap with other rules on the same day

### After Shift
- Entered time must be > 00:00 and ≥ regular end time
- One per day
- Cannot overlap with other rules on the same day

### Specific Hours Range
- Start time must be before end time
- Cannot overlap with regular hours, before shift, after shift, or other specific ranges on the same day

### Daily Overtime
- Threshold must be ≥ scheduled regular hours for that day
- Multiple tiers allowed but each threshold must be unique per day
- Same threshold on same day triggers error

### Weekly Overtime
- Threshold must be ≥ sum of all scheduled daily hours
- Only one instance allowed
- Applies to all days (not selectable)
- Only for weekly and biweekly timesheets
- Biweekly: threshold applied per week independently

---

## 14. Formulas Reference

### Regular Hours
```
Effective Regular Hours = Scheduled Hours − Anticipated Break
Regular Pay = min(Actual Work in Range, Effective Regular) × Pay Rate × 1
```

### Break (Paid = NO)
```
Actual Working Hours = Logged Hours − Actual Break Time
Unused Anticipated = max(0, Anticipated Break − Actual Break)
Excess = Actual Working Hours − Effective Regular Hours
→ If Daily OT threshold met: excess becomes Daily OT
→ If not met: excess is unallocated
```

### Before Shift / After Shift / Specific Range
```
Pay = Hours in Window × Pay Rate × Rule Multiplier
Bill = Hours in Window × Bill Rate × Rule Multiplier

If break taken within window and Break Paid = YES:
  Break Pay = Break Hours × Pay Rate × 1 (regular rate)
  Remaining Pay = (Hours − Break) × Pay Rate × Rule Multiplier

If break taken and Break Paid = NO:
  Pay = (Hours − Break) × Pay Rate × Rule Multiplier
```

### Daily Overtime
```
Toward Threshold (what counts):
  No Break / BPY:  toward = total_clocked (includes reg + BS + AS + SR + gap + break)
  BPN:             toward = total_clocked − actual_break_taken

Gate Check: toward > Daily Threshold?

If gate OPEN:
  excess = toward − Daily Threshold
  unallocated_pool = gap_hours + (anticipated − actual_break if BPN and ant > actual)
  Daily OT Hours = min(excess, unallocated_pool)

If gate CLOSED:
  Daily OT = 0, excess hours are unallocated (unpaid)

When Weekly OT also exists:
  Daily OT Hours = min(toward − Daily Threshold, unallocated_pool)
  Remaining unallocated → Weekly OT (if weekly threshold met)
```

### Weekly Overtime
```
Toward Weekly Threshold:
  No Break / BPY:  weekly_toward = sum of all daily total_clocked
  BPN:             weekly_toward = sum of all daily (total_clocked − actual_break)

Weekly OT = min(weekly_toward − weekly_threshold, total_weekly_unallocated_pool)

Unallocated pool includes:
  - Gap hours (work between rule zones, not claimed by any rule)
  - Anticipated break excess hours (for BPN, when ant > actual_break)
```

### Pay and Bill Calculation
```
Pay Amount = Hours × Pay Rate × Pay Multiplier
Bill Amount = Hours × Bill Rate × Bill Multiplier
```

---

*Document Version: 1.1*
*Last Updated: February 2026*
*Based on rule engine concept discussions and UI reference screenshots.*
*v1.1 — Clarified DOT/WOT threshold calculation: all clocked hours (including BS, AS, SR) count toward threshold. Added break impact tables and BS+DOT/WOT examples.*
