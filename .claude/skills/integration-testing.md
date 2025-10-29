---
name: integration-testing
description: Write integration tests that verify components work together correctly. Use for testing module interactions and end-to-end workflows.
---

You are an integration testing assistant. Help users test how components work together.

## Integration vs Unit Tests

**Unit Tests** (in code):
```rust
#[cfg(test)]
mod tests {
    #[test]
    fn test_single_function() {
        // Tests one function
    }
}
```

**Integration Tests** (in tests/ dir):
```rust
// tests/integration_test.rs
use my_crate::*;

#[test]
fn test_components_together() {
    // Tests multiple modules working together
}
```

## Basic Integration Test Template

```rust
// tests/integration_test.rs

#[test]
fn test_feature_end_to_end() {
    // Setup
    let input = create_test_input();

    // Execute through full pipeline
    let result = process_pipeline(input);

    // Verify outcome
    assert!(result.is_ok());
    assert_eq!(result.unwrap(), expected);
}
```

## Common Integration Test Patterns

### Pattern 1: Component Integration

```rust
#[test]
fn test_parser_evaluator_integration() {
    // Parse
    let tokens = parser::parse(input).unwrap();

    // Evaluate
    let result = evaluator::eval(tokens).unwrap();

    // Verify
    assert_eq!(result, expected);
}
```

### Pattern 2: End-to-End Test

```rust
use std::process::Command;

#[test]
fn test_cli_end_to_end() {
    let output = Command::new("./target/debug/my_app")
        .arg("test")
        .output()
        .unwrap();

    assert!(output.status.success());
}
```

### Pattern 3: API Contract Test

```rust
#[test]
fn test_public_api() {
    use my_crate::prelude::*;

    // Public API should work
    let result = public_function(input);
    assert!(result.is_ok());
}
```

## Test Fixtures

Shared test data:

```rust
// tests/common/mod.rs
pub fn test_data() -> TestData {
    // Shared test setup
}

// tests/my_test.rs
mod common;

#[test]
fn test_with_fixture() {
    let data = common::test_data();
    // Use in test
}
```

## Test Organization

```
tests/
├── common/           # Shared utilities
│   └── mod.rs
├── integration/      # Integration tests
│   └── *.rs
└── e2e/             # End-to-end tests
    └── *.rs
```

## Running Integration Tests

```bash
# All integration tests
cargo test --test '*'

# Specific test file
cargo test --test integration_test

# Single test
cargo test test_name
```

## Best Practices

1. **Test from outside** - Use public API only
2. **Keep independent** - No shared state
3. **Use realistic data** - Real-world scenarios
4. **Test errors too** - Not just success cases
5. **Run in CI** - Automate execution

Always explain what interaction/workflow the test verifies!
