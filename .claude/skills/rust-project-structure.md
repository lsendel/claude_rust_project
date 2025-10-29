---
name: rust-project-structure
description: Help organize Rust projects with proper directory structure, module layout, and file organization. Use for structuring new projects or reorganizing existing ones.
---

You are a Rust project organization expert. Help users structure their Rust projects following conventions and best practices.

## Quick Project Structure Reference

### Small Project (< 1000 lines)

```
my_project/
├── Cargo.toml
├── src/
│   ├── main.rs       # or lib.rs
│   ├── config.rs
│   ├── error.rs
│   └── utils.rs
├── tests/
│   └── integration.rs
└── README.md
```

### Medium Project (1000-10000 lines)

```
my_project/
├── Cargo.toml
├── src/
│   ├── main.rs
│   ├── lib.rs
│   ├── core/
│   │   ├── mod.rs
│   │   ├── parser.rs
│   │   └── evaluator.rs
│   ├── cli/
│   │   └── mod.rs
│   ├── config/
│   │   └── mod.rs
│   └── error.rs
├── tests/
│   ├── integration/
│   └── common/
├── examples/
│   └── basic.rs
├── docs/
└── README.md
```

### Large Project (> 10000 lines)

```
my_project/
├── Cargo.toml        # Workspace root
├── crates/
│   ├── core/         # Core library
│   ├── cli/          # CLI binary
│   └── web/          # Web service
├── docs/
├── examples/
└── README.md
```

## Module Organization

### Pattern 1: File Per Module

```rust
// src/lib.rs
pub mod parser;   // Looks for src/parser.rs
pub mod lexer;    // Looks for src/lexer.rs
pub mod error;    // Looks for src/error.rs
```

### Pattern 2: Directory Modules

```rust
// src/lib.rs
pub mod calculator;  // Looks for src/calculator/mod.rs

// src/calculator/mod.rs
pub mod parser;      // Looks for src/calculator/parser.rs
pub mod evaluator;   // Looks for src/calculator/evaluator.rs
```

### Pattern 3: Re-exports

```rust
// src/lib.rs
mod internal_module;  // Private

// Re-export selected items
pub use internal_module::{PublicType, public_function};
```

## Common Structures

### CLI Application

```
cli_app/
├── Cargo.toml
├── src/
│   ├── main.rs      # Entry point
│   ├── cli.rs       # CLI argument definitions
│   ├── commands/    # Command implementations
│   │   ├── mod.rs
│   │   ├── create.rs
│   │   └── delete.rs
│   ├── config.rs    # Configuration
│   └── error.rs     # Error types
└── tests/
    └── cli_tests.rs
```

### Library Crate

```
my_lib/
├── Cargo.toml
├── src/
│   ├── lib.rs       # Public API
│   ├── core/        # Core functionality
│   ├── utils/       # Utilities
│   ├── error.rs     # Error types
│   └── types.rs     # Common types
├── tests/
│   └── integration.rs
├── examples/
│   └── usage.rs
└── benches/
    └── benchmark.rs
```

### Web Service

```
web_service/
├── Cargo.toml
├── src/
│   ├── main.rs
│   ├── routes/      # API routes
│   │   ├── mod.rs
│   │   ├── users.rs
│   │   └── posts.rs
│   ├── handlers/    # Request handlers
│   ├── models/      # Data models
│   ├── db/          # Database
│   └── middleware/  # Middleware
├── migrations/      # DB migrations
└── config/
    └── default.toml
```

## File Naming Conventions

### Modules
- `mod.rs` - Module root
- `snake_case.rs` - Module files
- `lib.rs` - Library root
- `main.rs` - Binary root

### Tests
- `test_*.rs` - Integration tests
- `*_test.rs` - Alternative naming
- Tests in `#[cfg(test)]` modules

### Examples
- `basic.rs` - Simple example
- `advanced_usage.rs` - Complex example
- `feature_name.rs` - Feature-specific example

## Organizing by Feature

```
src/
├── lib.rs
├── features/
│   ├── authentication/
│   │   ├── mod.rs
│   │   ├── login.rs
│   │   ├── logout.rs
│   │   └── types.rs
│   ├── user_management/
│   │   ├── mod.rs
│   │   ├── crud.rs
│   │   └── validation.rs
│   └── reporting/
│       └── mod.rs
└── shared/
    ├── error.rs
    ├── config.rs
    └── utils.rs
```

## Organizing by Layer

```
src/
├── lib.rs
├── api/          # API layer
│   └── routes.rs
├── service/      # Business logic
│   └── calculator.rs
├── repository/   # Data access
│   └── storage.rs
└── model/        # Data models
    └── types.rs
```

## Public vs Private

```rust
// src/lib.rs

// Public module (exposed to users)
pub mod public_api;

// Private module (internal use only)
mod internal;

// Selective re-export
pub use internal::UsefulType;
```

## Cargo.toml Organization

### Basic Structure

```toml
[package]
name = "my_project"
version = "0.1.0"
edition = "2021"

[dependencies]
serde = { version = "1.0", features = ["derive"] }

[dev-dependencies]
criterion = "0.5"

[features]
default = ["std"]
std = []
```

### Workspace Structure

```toml
[workspace]
members = [
    "core",
    "cli",
    "web",
]

[workspace.dependencies]
# Shared dependencies
tokio = "1.0"
```

## Test Organization

### Unit Tests

```rust
// In same file as code
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_feature() {
        // Test code
    }
}
```

### Integration Tests

```
tests/
├── integration/
│   ├── mod.rs      # Common utilities
│   ├── api.rs      # API tests
│   └── cli.rs      # CLI tests
└── fixtures/
    └── data.json
```

## Quick Setup Commands

```bash
# Create new binary project
cargo new my_project

# Create new library
cargo new --lib my_library

# Create workspace
mkdir my_workspace && cd my_workspace
cargo new --lib core
cargo new --bin cli
# Add workspace Cargo.toml

# Add directories
mkdir -p src/{core,cli,config}
mkdir -p tests examples docs
```

## Module Template

```rust
// src/feature/mod.rs
//! Feature module
//!
//! This module provides...

mod internal;  // Private submodule

pub use internal::PublicType;

/// Public function
pub fn do_something() {
    // Implementation
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_feature() {
        // Test
    }
}
```

## When to Split Files

**Split into separate file when:**
- File > 200-300 lines
- Distinct responsibility
- Reusable component
- Multiple related functions

**Keep in same file when:**
- Tightly coupled code
- Small helper functions
- Internal implementation details

## Common Questions

### "Where should error types go?"
In `src/error.rs` at project root for visibility

### "Public API in lib.rs or separate module?"
Re-export public items in lib.rs from internal modules

### "Tests in same file or tests/ directory?"
Unit tests in same file, integration tests in tests/

### "How to organize utilities?"
Create `src/utils/` with focused modules (don't dump everything together)

## Quick Reference

```
✅ DO:
- Use snake_case for files
- Put tests in #[cfg(test)]
- Keep modules focused
- Document public APIs
- Use descriptive names

❌ DON'T:
- Create deeply nested modules
- Put everything in one file
- Use ambiguous names (utils, helpers)
- Expose internal details
- Mix concerns
```

Always aim for **clarity and maintainability**!
