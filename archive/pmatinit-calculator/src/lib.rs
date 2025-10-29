//! Calculator Library
//!
//! A command-line calculator supporting basic and advanced mathematical operations,
//! including variable storage and memory management.
//!
//! # Features
//!
//! ## Basic Operations
//! - Arithmetic operations: `+`, `-`, `*`, `/`, `%`
//! - Proper operator precedence (PEMDAS)
//! - Parentheses support for grouping expressions
//!
//! ## Advanced Operations (Phase 1)
//! - **Power operator**: `^` (right-associative)
//! - **Mathematical constants**: `pi`, `e`
//! - **Scientific functions**: `sin(x)`, `cos(x)`, `tan(x)`
//! - **Logarithmic functions**: `log(x)` (base 10), `ln(x)` (natural log)
//! - **Square root**: `sqrt(x)`
//! - **Factorial**: `x!` (postfix operator)
//!
//! ## Variables and Memory (Phase 2)
//! - **Variable assignment**: `x = 5`
//! - **Variable usage**: Use variables in expressions
//! - **Special `ans` variable**: Automatically stores last result
//! - **Memory commands**: List, clear, and delete variables
//!
//! ## Enhanced REPL (Phase 3)
//! - **History persistence**: Command history saved across sessions
//! - **Tab completion**: Complete functions, variables, and commands
//! - **Syntax highlighting**: Color-coded input for better readability
//! - **Multi-line support**: Automatic continuation for unclosed parentheses
//! - **Enhanced errors**: Helpful error messages with suggestions
//!
//! ## Interface
//! - Interactive REPL mode with advanced features
//! - Single expression evaluation via CLI
//!
//! # Examples
//!
//! ## Basic arithmetic
//! ```
//! use pmatinit::calculator::evaluate_expression;
//!
//! let result = evaluate_expression("(2 + 3) * 4").unwrap();
//! assert_eq!(result, 20.0);
//! ```
//!
//! ## Advanced features
//! ```
//! use pmatinit::calculator::evaluate_expression;
//!
//! // Power operator
//! let result = evaluate_expression("2 ^ 10").unwrap();
//! assert_eq!(result, 1024.0);
//!
//! // Constants and functions
//! let result = evaluate_expression("2 * pi * sqrt(16)").unwrap();
//! assert!((result - 8.0 * std::f64::consts::PI).abs() < 0.0001);
//!
//! // Factorial
//! let result = evaluate_expression("5!").unwrap();
//! assert_eq!(result, 120.0);
//!
//! // Scientific functions
//! let result = evaluate_expression("sin(pi / 2)").unwrap();
//! assert!((result - 1.0).abs() < 0.0001);
//! ```
//!
//! ## Variables and stateful calculation
//! ```
//! use pmatinit::calculator::Calculator;
//!
//! let mut calc = Calculator::new();
//!
//! // Assign variables
//! calc.evaluate("x = 10").unwrap();
//! calc.evaluate("y = 20").unwrap();
//!
//! // Use variables in expressions
//! let result = calc.evaluate("x + y").unwrap();
//! assert_eq!(result, 30.0);
//!
//! // ans variable stores last result
//! calc.evaluate("5 * 5").unwrap();
//! let result = calc.evaluate("ans + 10").unwrap();
//! assert_eq!(result, 35.0);
//!
//! // Manage variables
//! calc.delete_variable("x");
//! calc.clear_variables();
//! ```

pub mod calculator;
pub mod cli;
pub mod repl;
