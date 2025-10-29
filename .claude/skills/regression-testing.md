---
name: regression-testing
description: Write regression tests to prevent bugs from returning and ensure stability across changes. Use when fixing bugs or adding regression test coverage.
---

You are a regression testing assistant. Help users write tests that prevent bugs from reappearing.

## What are Regression Tests?

Tests that ensure:
- Fixed bugs stay fixed
- Existing features keep working
- Changes don't break things

## Quick Regression Test Template

```rust
/// Regression test for Issue #XX: [Description]
/// Fixed in: [commit/PR]
/// Date: [date]
#[test]
fn test_issue_XX_description() {
    // Reproduce the original bug scenario
    let result = function_that_had_bug(input);

    // Verify the fix works
    assert!(result.is_ok());
    // or
    assert_eq!(result.unwrap(), expected_value);
}
```

## When to Add Regression Tests

**Always add for**:
1. Bug fixes (every single one!)
2. Production incidents
3. User-reported issues
4. Edge cases that failed

**Example**:
```rust
#[test]
fn test_bug_division_by_zero() {
    // Bug: App crashed on division by zero
    // Fix: Now returns error
    let result = divide(10.0, 0.0);
    assert!(result.is_err());
}
```

## Organizing Regression Tests

```
tests/
├── regression/
│   ├── bug_fixes.rs        # Fixed bugs
│   ├── edge_cases.rs       # Edge cases
│   └── prod_incidents.rs   # Production issues
```

## Golden Master Testing

Compare output against known-good baseline:

```rust
#[test]
fn test_golden_master() {
    let output = generate_output(input);
    let expected = include_str!("golden/expected.txt");
    assert_eq!(output, expected);
}
```

Update baseline:
```bash
UPDATE_GOLDEN=1 cargo test golden_master
```

## Performance Regression

```rust
use std::time::Instant;

#[test]
fn test_performance_regression() {
    let start = Instant::now();
    expensive_operation();
    let duration = start.elapsed();

    // Should complete in under 100ms
    assert!(duration.as_millis() < 100);
}
```

## Best Practices

1. **Document the bug** - Link to issue number
2. **Reproduce first** - Make test fail without fix
3. **Keep focused** - One bug per test
4. **Test the fix** - Verify bug doesn't return
5. **Run regularly** - Include in CI/CD

Always explain what bug the test prevents!
