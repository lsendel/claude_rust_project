# Contributing to pmatinit

Thank you for your interest in contributing to pmatinit! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Development Guidelines](#development-guidelines)
- [Testing](#testing)
- [Documentation](#documentation)
- [Pull Request Process](#pull-request-process)
- [Project Structure](#project-structure)
- [Communication](#communication)

## Code of Conduct

This project adheres to a code of conduct that promotes a welcoming and inclusive environment. By participating, you are expected to uphold this code:

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on what is best for the community
- Show empathy towards other community members
- Accept constructive criticism gracefully
- Use welcoming and inclusive language

## Getting Started

### Prerequisites

- Rust 1.70 or later (2021 edition)
- Cargo (comes with Rust)
- Git
- A text editor or IDE with Rust support (recommended: VS Code with rust-analyzer)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/yourusername/pmatinit.git
   cd pmatinit
   ```

3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/originalowner/pmatinit.git
   ```

## Development Setup

### Building the Project

```bash
# Build in debug mode
cargo build

# Build in release mode
cargo build --release

# Run the calculator
cargo run

# Run with a single expression
cargo run -- "2 + 2"
```

### Running Tests

```bash
# Run all tests
cargo test

# Run with output
cargo test -- --nocapture

# Run specific test
cargo test test_basic_arithmetic

# Run only unit tests
cargo test --lib

# Run only integration tests
cargo test --test '*'
```

### Checking Code Quality

```bash
# Format code
cargo fmt

# Check formatting without modifying
cargo fmt -- --check

# Run Clippy linter
cargo clippy

# Clippy with all warnings
cargo clippy -- -D warnings
```

### Generate Documentation

```bash
# Generate and open documentation
cargo doc --no-deps --open

# Generate documentation with all features
cargo doc --all-features --no-deps
```

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check existing issues to avoid duplicates.

When reporting a bug, include:
- Clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Your environment (OS, Rust version, pmatinit version)
- Minimal code example demonstrating the issue
- Error messages or stack traces (if applicable)

### Suggesting Enhancements

Enhancement suggestions are welcome! Include:
- Clear, descriptive title
- Detailed description of the proposed feature
- Use cases and motivation
- Example usage (if applicable)
- Potential implementation approach (optional)

### Contributing Code

1. **Pick an issue** or create one for discussion
2. **Comment on the issue** to let others know you're working on it
3. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/my-feature
   # or
   git checkout -b fix/my-bugfix
   ```
4. **Make your changes** following the development guidelines
5. **Write tests** for new functionality
6. **Update documentation** as needed
7. **Run tests and checks** to ensure everything passes
8. **Commit your changes** with clear, descriptive messages
9. **Push to your fork** and create a pull request

## Development Guidelines

### Code Style

- Follow standard Rust conventions and idioms
- Use `cargo fmt` to format code before committing
- Address all `cargo clippy` warnings
- Use descriptive variable and function names
- Keep functions focused and concise
- Add comments for complex logic

### Rust Best Practices

- Prefer immutability when possible
- Use `Result` and `Option` for error handling (avoid `unwrap()` in library code)
- Leverage the type system for safety
- Write idiomatic Rust code
- Avoid unsafe code unless absolutely necessary
- Use appropriate error types (anyhow, thiserror)

### Commit Messages

Follow conventional commit format:

```
type(scope): brief description

Longer description if needed, explaining:
- Why the change is needed
- How it addresses the issue
- Any side effects or considerations

Fixes #123
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Test additions or changes
- `chore`: Build process or auxiliary tool changes

Examples:
```
feat(calculator): add support for complex numbers

Implements complex number arithmetic with real and imaginary components.
Supports all basic operations: +, -, *, /

Fixes #45

---

fix(repl): prevent crash on empty input

Check for empty input before processing to avoid panic.

Fixes #67

---

docs(readme): update installation instructions

Add instructions for building from source and troubleshooting section.
```

### Code Organization

- **src/calculator/**: Core calculation engine
  - `mod.rs`: Calculator struct and main API
  - `parser.rs`: Expression parsing and tokenization
  - `evaluator.rs`: Expression evaluation
  - `operators.rs`: Operator definitions

- **src/repl/**: Interactive REPL
  - `mod.rs`: Main REPL loop
  - `commands.rs`: Command handlers
  - `helper.rs`: Completion, highlighting, validation
  - `errors.rs`: Error enhancement

- **src/cli/**: CLI argument parsing
- **tests/**: Integration tests
- **examples/**: Example programs

### Adding New Features

When adding a new feature:

1. **Design**: Think about the API and user experience
2. **Implement**: Write clean, well-documented code
3. **Test**: Add comprehensive tests
4. **Document**: Update README, USER_GUIDE, and rustdoc comments
5. **Examples**: Add example usage if appropriate

### Adding New Functions

To add a new mathematical function:

1. Add the function name to `FUNCTIONS` list in `parser.rs`
2. Add evaluation logic in `evaluator.rs`
3. Add to completion list in `helper.rs`
4. Update documentation
5. Add tests
6. Update help text in `commands.rs`

Example:
```rust
// In parser.rs
const FUNCTIONS: &[&str] = &["sin", "cos", "tan", "sqrt", "log", "ln", "your_function"];

// In evaluator.rs
"your_function" => {
    // Implementation
    let result = your_calculation(arg);
    result
}

// Add tests
#[test]
fn test_your_function() {
    let result = evaluate_expression("your_function(10)").unwrap();
    assert_eq!(result, expected);
}
```

## Testing

### Test Coverage

Maintain high test coverage:
- Unit tests for individual functions
- Integration tests for full workflows
- Doc tests for examples in documentation
- Edge case tests
- Error condition tests

### Writing Tests

```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_feature_name() {
        // Arrange
        let input = "test input";

        // Act
        let result = function_under_test(input);

        // Assert
        assert_eq!(result, expected_value);
    }

    #[test]
    fn test_error_case() {
        let result = function_that_fails("bad input");
        assert!(result.is_err());
    }
}
```

### Running Specific Tests

```bash
# Test a specific module
cargo test calculator::

# Test with specific pattern
cargo test variable

# Run ignored tests
cargo test -- --ignored

# Run tests in parallel
cargo test -- --test-threads=4
```

## Documentation

### Code Documentation

Use rustdoc comments for all public items:

```rust
/// Brief description of the function
///
/// More detailed explanation if needed.
///
/// # Arguments
///
/// * `param1` - Description of parameter
/// * `param2` - Description of parameter
///
/// # Returns
///
/// Description of return value
///
/// # Examples
///
/// ```
/// use pmatinit::calculator::evaluate_expression;
///
/// let result = evaluate_expression("2 + 2").unwrap();
/// assert_eq!(result, 4.0);
/// ```
///
/// # Errors
///
/// Returns an error if:
/// - Invalid syntax
/// - Division by zero
/// - Invalid function arguments
pub fn function_name(param1: Type1, param2: Type2) -> Result<ReturnType> {
    // Implementation
}
```

### User Documentation

When adding features, update:
- `README.md` - Overview and quick start
- `docs/USER_GUIDE.md` - Detailed usage guide
- `examples/` - Add example programs if appropriate
- `CHANGELOG.md` - Document changes

## Pull Request Process

### Before Submitting

1. ✓ Run all tests: `cargo test`
2. ✓ Format code: `cargo fmt`
3. ✓ Check lints: `cargo clippy`
4. ✓ Update documentation
5. ✓ Add/update tests
6. ✓ Update CHANGELOG.md
7. ✓ Ensure examples compile: `cargo build --examples`

### Submitting a Pull Request

1. **Push your branch** to your fork
2. **Create a pull request** against the `main` branch
3. **Fill out the PR template** with:
   - Description of changes
   - Motivation and context
   - Related issues
   - Type of change (bug fix, feature, etc.)
   - Testing performed
   - Breaking changes (if any)

4. **Wait for review** and address feedback
5. **Update your PR** if changes are requested
6. **Merge**: Once approved, your PR will be merged

### Pull Request Template

```markdown
## Description
Brief description of what this PR does

## Related Issue
Fixes #(issue number)

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Documentation update

## How Has This Been Tested?
Describe the tests you ran and their results

## Checklist
- [ ] My code follows the code style of this project
- [ ] I have updated the documentation accordingly
- [ ] I have added tests to cover my changes
- [ ] All new and existing tests pass
- [ ] I have updated CHANGELOG.md
```

## Project Structure

```
pmatinit/
├── src/
│   ├── main.rs              # Entry point
│   ├── lib.rs               # Library root
│   ├── cli/                 # CLI argument parsing
│   ├── calculator/          # Core calculator
│   │   ├── mod.rs
│   │   ├── parser.rs
│   │   ├── evaluator.rs
│   │   └── operators.rs
│   └── repl/                # Interactive REPL
│       ├── mod.rs
│       ├── commands.rs
│       ├── helper.rs
│       └── errors.rs
├── tests/                   # Integration tests
│   ├── calculator_tests.rs
│   ├── repl_tests.rs
│   └── integration_tests.rs
├── examples/                # Example programs
├── docs/                    # Documentation
├── Cargo.toml              # Project metadata
├── README.md               # Main documentation
├── CHANGELOG.md            # Version history
└── CONTRIBUTING.md         # This file
```

## Communication

### Channels

- **GitHub Issues**: Bug reports, feature requests, discussions
- **Pull Requests**: Code contributions and reviews
- **Discussions**: General questions and ideas

### Getting Help

- Check the [README.md](README.md) and [USER_GUIDE.md](docs/USER_GUIDE.md)
- Search existing issues
- Ask questions in GitHub Discussions
- Comment on relevant issues

## Recognition

Contributors are recognized in:
- Git commit history
- CHANGELOG.md for significant contributions
- GitHub contributors page

## License

By contributing to pmatinit, you agree that your contributions will be licensed under the project's dual license (MIT OR Apache-2.0).

---

## Quick Reference

### Common Commands

```bash
# Development
cargo build                   # Build debug
cargo run                     # Run calculator
cargo test                    # Run tests
cargo fmt                     # Format code
cargo clippy                  # Lint code
cargo doc --open             # Generate docs

# Testing
cargo test -- --nocapture    # Tests with output
cargo test <test_name>       # Specific test
cargo test --lib             # Unit tests only
cargo test --test '*'        # Integration tests

# Examples
cargo run --example basic_usage
cargo build --examples

# Validation
cargo fmt -- --check         # Check formatting
cargo clippy -- -D warnings  # Strict linting
cargo test --all-features    # Test everything
```

### Workflow Summary

1. Fork and clone
2. Create feature branch
3. Make changes
4. Write tests
5. Update docs
6. Run checks
7. Commit and push
8. Create PR
9. Address feedback
10. Merge!

---

Thank you for contributing to pmatinit! Your contributions help make this project better for everyone.
