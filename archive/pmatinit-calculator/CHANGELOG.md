# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-10-25

### Added

#### Core Calculator (Phase 0-1)
- Basic arithmetic operations: addition, subtraction, multiplication, division, modulo
- Power operator `^` with right-associativity
- Factorial operator `!` (postfix)
- Mathematical functions: `sin`, `cos`, `tan`, `sqrt`, `log`, `ln`
- Mathematical constants: `pi`, `e`
- Proper operator precedence (PEMDAS)
- Parentheses support for grouping expressions
- Comprehensive error handling for invalid operations

#### Variables and Memory (Phase 2)
- Variable assignment with `=` operator
- Variable storage and retrieval
- Special `ans` variable that stores the last result
- Variable management commands: `vars`, `delete`, `clearvars`
- Direct variable manipulation via API (`get_variable`, `set_variable`)

#### Enhanced REPL (Phase 3)
- Command history persistence to `~/.pmatinit_history`
- Tab completion for functions, constants, commands, and variables
- Syntax highlighting with color-coded input:
  - Numbers in cyan
  - Operators in yellow
  - Functions in green
  - Constants in purple
  - Commands/variables in blue
- Multi-line expression support with automatic continuation
- Enhanced error messages with fuzzy matching suggestions
- Helpful tips for common errors

#### CLI Interface
- Interactive REPL mode (default)
- Single-expression mode for quick calculations
- Force interactive mode with `--interactive` or `-i` flag
- Clean command-line argument parsing with clap

#### Documentation
- Comprehensive README with examples and usage guide
- Detailed USER_GUIDE.md with tutorials and advanced examples
- Four runnable example programs:
  - `basic_usage.rs` - Fundamental operations
  - `scientific_calculator.rs` - Advanced mathematical functions
  - `variable_management.rs` - Working with variables
  - `error_handling.rs` - Error handling patterns
- Extensive rustdoc comments with examples
- API documentation for library usage

#### Testing
- 160+ comprehensive tests covering:
  - Unit tests for all calculator components
  - Integration tests for REPL functionality
  - Doc tests for examples in documentation
  - Zero test failures, zero compiler warnings

#### Library Support
- Stateless evaluation with `evaluate_expression`
- Stateful calculator with `Calculator` struct
- Full API for programmatic use in Rust applications
- Clean, documented public API

### Technical Details

#### Dependencies
- `clap` 4.5 - CLI argument parsing
- `rustyline` 14.0 - REPL line editing
- `anyhow` 1.0 - Error handling
- `thiserror` 2.0 - Custom error types
- `ansi_term` 0.12 - Terminal colors
- `similar` 2.4 - Fuzzy string matching
- `dirs` 5.0 - Home directory support

#### Architecture
- Modular design with clean separation of concerns
- Parser using tokenization and Shunting Yard algorithm
- Postfix notation evaluation for expressions
- Rustyline helper implementation for REPL features
- Comprehensive error types with context

#### Quality Metrics
- Zero compiler warnings
- Zero test failures
- Full documentation coverage
- Clean, idiomatic Rust code
- Production-ready quality

### Fixed
- N/A (initial release)

### Changed
- N/A (initial release)

### Deprecated
- N/A (initial release)

### Removed
- N/A (initial release)

### Security
- No known security issues
- Input validation for all operations
- Safe handling of mathematical operations
- Overflow/underflow detection

## [Unreleased]

### Planned Features
- Complex number support
- Matrix operations
- Additional mathematical functions (sinh, cosh, asin, acos, etc.)
- Unit conversions
- Custom function definitions
- Plotting capabilities
- Export/import calculation history
- Configuration file support
- Web interface

---

## Release Notes

### Version 0.1.0 - Initial Release

This is the first stable release of pmatinit, a powerful command-line calculator built with Rust. The project includes:

- **Full-featured calculator**: All basic and advanced mathematical operations
- **Interactive REPL**: Modern, user-friendly interface with history, completion, and colors
- **Variable support**: Store and reuse values across calculations
- **Comprehensive documentation**: Guides, examples, and API documentation
- **Production quality**: 160+ tests, zero warnings, extensive error handling

The calculator is ready for daily use and can be integrated into other Rust projects as a library.

### Breaking Changes
- None (initial release)

### Migration Guide
- N/A (initial release)

### Known Issues
- Trigonometric functions use radians only (no degree mode yet)
- Factorial limited to non-negative integers
- Very large factorials may overflow
- History file has no size limit (OS/filesystem limits apply)

### Contributors
- Your Name - Initial implementation and documentation

---

**Note**: Links to releases will be added when the project is published to GitHub and crates.io.

[0.1.0]: https://github.com/yourusername/pmatinit/releases/tag/v0.1.0
[Unreleased]: https://github.com/yourusername/pmatinit/compare/v0.1.0...HEAD
