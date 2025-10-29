# Hello World Rust Calculator CLI

## Project Overview

A command-line calculator application written in Rust that provides basic arithmetic operations with a clean, interactive interface. This project serves as a learning exercise in Rust development, CLI design, and proper error handling.

## Project Goals

- Build a functional calculator CLI application
- Learn Rust fundamentals (ownership, error handling, pattern matching)
- Implement clean code architecture with proper separation of concerns
- Write comprehensive tests for all functionality
- Create user-friendly command-line experience

## Features

### Phase 1: Core Functionality (MVP)
- **Basic Arithmetic Operations**
  - Addition (+)
  - Subtraction (-)
  - Multiplication (*)
  - Division (/)
  - Modulo (%)

- **Input Methods**
  - Interactive REPL mode (default)
  - Single expression evaluation via command-line arguments
  - Expression parsing from stdin

- **Expression Parsing**
  - Support for basic operator precedence (PEMDAS)
  - Parentheses support for grouping
  - Decimal number support
  - Negative number support

### Phase 2: Enhanced Features (Future)
- Scientific functions (sin, cos, tan, sqrt, pow, log)
- Variable storage and recall
- History of previous calculations
- Multi-line expressions
- Configuration file support
- Colorized output
- Result formatting options (precision, scientific notation)

## Technical Specifications

### Language & Tools
- **Language**: Rust (Edition 2021)
- **Minimum Rust Version**: 1.80.0
- **Key Dependencies**:
  - `clap` - Command-line argument parsing
  - `rustyline` - Interactive readline for REPL
  - `anyhow` - Error handling
  - `thiserror` - Custom error types

### Project Structure
```
pmatinit/
├── src/
│   ├── main.rs              # Entry point, CLI setup
│   ├── lib.rs               # Library root
│   ├── calculator/
│   │   ├── mod.rs           # Calculator module
│   │   ├── parser.rs        # Expression parser
│   │   ├── evaluator.rs     # Expression evaluator
│   │   └── operators.rs     # Operator definitions
│   ├── repl/
│   │   ├── mod.rs           # REPL module
│   │   └── commands.rs      # REPL commands (help, quit, etc.)
│   └── cli/
│       └── mod.rs           # CLI argument definitions
├── tests/
│   ├── integration_tests.rs # Integration tests
│   └── calculator_tests.rs  # Calculator unit tests
├── docs/
│   ├── todo/
│   │   └── hello-world-rust-calculator-cli.md
│   ├── architecture.md      # Architecture documentation
│   └── usage.md            # User guide
└── examples/
    └── basic_usage.rs       # Example usage
```

### Error Handling Strategy
- Use `Result<T, E>` for all fallible operations
- Define custom error types using `thiserror`
- Provide helpful error messages to users
- Never panic in user-facing code

### Testing Strategy
- Unit tests for all calculator functions
- Integration tests for CLI interface
- Property-based testing for mathematical operations
- Edge case coverage (division by zero, overflow, etc.)

## User Interface Design

### REPL Mode
```
Calculator v0.1.0
Type 'help' for commands, 'quit' to exit

calc> 2 + 2
4

calc> (10 + 5) * 3
45

calc> 10 / 0
Error: Division by zero

calc> quit
Goodbye!
```

### Command-Line Mode
```bash
# Single expression
$ pmatinit "2 + 2"
4

# From stdin
$ echo "5 * 6" | pmatinit
30

# With options
$ pmatinit --precision 4 "22 / 7"
3.1429
```

### Help Output
```
Calculator CLI - A simple command-line calculator

Usage: pmatinit [OPTIONS] [EXPRESSION]

Arguments:
  [EXPRESSION]  Mathematical expression to evaluate

Options:
  -i, --interactive     Start in interactive REPL mode
  -p, --precision <N>   Number of decimal places [default: 2]
  -h, --help           Print help
  -V, --version        Print version

REPL Commands:
  help         Show this help message
  quit, exit   Exit the calculator
  clear        Clear the screen
```

## Granular Task List

### Phase 0: Project Setup
- [x] Initialize Rust project with Cargo
- [x] Install pmat for code quality
- [x] Create .gitignore for Rust
- [ ] Set up project directory structure (src/calculator, src/repl, src/cli)
- [ ] Add dependencies to Cargo.toml (clap, rustyline, anyhow, thiserror)
- [ ] Create initial lib.rs and module structure
- [ ] Set up tests directory with skeleton files

### Phase 1: Core Calculator Engine
- [ ] **Task 1.1**: Define operator types and precedence
  - Create `src/calculator/operators.rs`
  - Define `Operator` enum (Add, Subtract, Multiply, Divide, Modulo)
  - Implement operator precedence function
  - Write unit tests for operator precedence

- [ ] **Task 1.2**: Implement tokenizer
  - Create token types (Number, Operator, Parenthesis)
  - Implement tokenization function in `parser.rs`
  - Handle whitespace and invalid characters
  - Write tests for tokenization edge cases

- [ ] **Task 1.3**: Implement expression parser
  - Implement Shunting Yard algorithm for infix to postfix conversion
  - Handle operator precedence and associativity
  - Support parentheses for grouping
  - Write comprehensive parser tests

- [ ] **Task 1.4**: Implement expression evaluator
  - Create `src/calculator/evaluator.rs`
  - Implement postfix expression evaluation
  - Handle division by zero errors
  - Handle overflow/underflow errors
  - Write evaluator unit tests

- [ ] **Task 1.5**: Create calculator module interface
  - Define public API in `src/calculator/mod.rs`
  - Create `evaluate_expression()` function
  - Implement proper error types with thiserror
  - Write integration tests for calculator module

### Phase 2: CLI Interface
- [ ] **Task 2.1**: Set up CLI argument parsing
  - Create `src/cli/mod.rs`
  - Define CLI structure with clap
  - Add expression argument
  - Add --interactive, --precision flags
  - Write tests for CLI parsing

- [ ] **Task 2.2**: Implement main entry point
  - Update `src/main.rs`
  - Parse CLI arguments
  - Route to appropriate mode (REPL or single evaluation)
  - Handle and display errors gracefully

- [ ] **Task 2.3**: Implement single expression mode
  - Read expression from argument
  - Evaluate and print result
  - Handle stdin input mode
  - Format output according to precision flag

### Phase 3: REPL Implementation
- [ ] **Task 3.1**: Set up REPL infrastructure
  - Create `src/repl/mod.rs`
  - Initialize rustyline editor
  - Implement main REPL loop
  - Add prompt configuration

- [ ] **Task 3.2**: Implement REPL commands
  - Create `src/repl/commands.rs`
  - Implement help command
  - Implement quit/exit commands
  - Implement clear command
  - Add command parsing logic

- [ ] **Task 3.3**: Integrate calculator with REPL
  - Connect expression evaluation to REPL
  - Display results in REPL
  - Handle and display errors in REPL
  - Add calculation history tracking

- [ ] **Task 3.4**: Polish REPL experience
  - Add welcome message with version
  - Add goodbye message
  - Implement command history with up/down arrows
  - Add tab completion for commands

### Phase 4: Testing & Quality Assurance
- [ ] **Task 4.1**: Write comprehensive unit tests
  - Test all operator functions
  - Test tokenizer with edge cases
  - Test parser with complex expressions
  - Test evaluator with boundary conditions
  - Achieve >80% code coverage

- [ ] **Task 4.2**: Write integration tests
  - Test end-to-end expression evaluation
  - Test CLI argument parsing and execution
  - Test REPL interaction flows
  - Test error handling paths

- [ ] **Task 4.3**: Add property-based tests
  - Use proptest for mathematical properties
  - Test associativity properties
  - Test commutativity properties
  - Test identity properties

- [ ] **Task 4.4**: Manual testing
  - Test on different platforms (macOS, Linux, Windows)
  - Test edge cases manually
  - Test user experience flows
  - Document any issues found

### Phase 5: Documentation & Polish
- [ ] **Task 5.1**: Write code documentation
  - Add doc comments to all public functions
  - Add module-level documentation
  - Add examples in doc comments
  - Run `cargo doc` and review output

- [ ] **Task 5.2**: Create user documentation
  - Write `docs/usage.md` with examples
  - Write `docs/architecture.md` with design decisions
  - Update README.md with installation and usage
  - Add examples directory with sample code

- [ ] **Task 5.3**: Code quality improvements
  - Run `cargo clippy` and fix warnings
  - Run `cargo fmt` to format code
  - Use pmat for code analysis
  - Address any code quality issues

- [ ] **Task 5.4**: Performance optimization
  - Profile calculator performance
  - Optimize hot paths if needed
  - Benchmark against test expressions
  - Document performance characteristics

### Phase 6: Release Preparation
- [ ] **Task 6.1**: Version and metadata
  - Update Cargo.toml with proper metadata
  - Set version to 0.1.0
  - Add description, license, repository
  - Add keywords and categories

- [ ] **Task 6.2**: Build and test release
  - Build release version with `cargo build --release`
  - Test release binary
  - Verify size and performance
  - Test installation via `cargo install`

- [ ] **Task 6.3**: Create distribution artifacts
  - Create GitHub release
  - Tag version in git
  - Build binaries for multiple platforms
  - Create changelog

## Success Criteria

### Functionality
- ✅ Calculator correctly evaluates basic arithmetic expressions
- ✅ Proper operator precedence (PEMDAS)
- ✅ Handles parentheses correctly
- ✅ Supports decimal numbers
- ✅ Handles negative numbers
- ✅ Proper error handling for invalid input
- ✅ Division by zero error handling

### Code Quality
- ✅ All tests pass
- ✅ >80% code coverage
- ✅ Zero clippy warnings
- ✅ Properly formatted with rustfmt
- ✅ Clear documentation for all public APIs

### User Experience
- ✅ Clear, helpful error messages
- ✅ Intuitive REPL interface
- ✅ Fast response time (<100ms for simple expressions)
- ✅ Comprehensive help documentation

## Timeline Estimate

- **Phase 0**: 1 hour (Setup)
- **Phase 1**: 4-6 hours (Core calculator)
- **Phase 2**: 2-3 hours (CLI interface)
- **Phase 3**: 3-4 hours (REPL)
- **Phase 4**: 3-4 hours (Testing)
- **Phase 5**: 2-3 hours (Documentation)
- **Phase 6**: 1-2 hours (Release)

**Total Estimated Time**: 16-23 hours

## Future Enhancements

- Scientific calculator functions
- Variable storage and functions
- Expression history with search
- Configuration file support
- Plugin system for custom operators
- Web interface
- GraphQL/REST API mode
- Multi-user session support
- Result visualization (graphs, charts)

## Notes

- Focus on correctness over performance for MVP
- Prioritize user experience and error messages
- Keep code simple and readable
- Follow Rust idioms and best practices
- Document design decisions and trade-offs
