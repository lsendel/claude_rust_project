//! Calculator module - Core calculation engine
//!
//! This module contains the core calculator functionality including:
//! - Expression parsing (tokenization and Shunting Yard algorithm)
//! - Expression evaluation (postfix notation evaluation)
//! - Operator definitions and precedence
//! - Variable storage and management (Phase 2)
//!
//! # Supported Operations
//!
//! ## Basic Operators
//! - Addition: `+`
//! - Subtraction: `-`
//! - Multiplication: `*`
//! - Division: `/`
//! - Modulo: `%`
//! - Power: `^` (right-associative, higher precedence)
//!
//! ## Mathematical Functions
//! - Square root: `sqrt(x)`
//! - Trigonometric: `sin(x)`, `cos(x)`, `tan(x)`
//! - Logarithmic: `log(x)` (base 10), `ln(x)` (natural)
//!
//! ## Mathematical Constants
//! - Pi: `pi` (3.14159...)
//! - Euler's number: `e` (2.71828...)
//!
//! ## Special Operators
//! - Factorial: `x!` (postfix operator, requires non-negative integer)
//!
//! ## Variables (Phase 2)
//! - Variable assignment: `x = 5`
//! - Variable usage: `x + 3`
//! - Special variable `ans`: stores last result

pub mod evaluator;
pub mod operators;
pub mod parser;

use anyhow::Result;
use std::collections::HashMap;

/// Calculator struct with variable storage support
///
/// This struct maintains state across evaluations, including:
/// - User-defined variables
/// - Special `ans` variable (stores last result)
#[derive(Debug, Clone)]
pub struct Calculator {
    /// Variable storage: maps variable names to their values
    variables: HashMap<String, f64>,
}

impl Calculator {
    /// Creates a new Calculator instance with empty variable storage
    pub fn new() -> Self {
        Calculator {
            variables: HashMap::new(),
        }
    }

    /// Evaluates an expression with variable support
    ///
    /// Supports:
    /// - Variable assignment: `x = 5`
    /// - Variable usage: `x + 3`
    /// - Special `ans` variable (last result)
    ///
    /// # Arguments
    ///
    /// * `expression` - The expression string to evaluate
    ///
    /// # Returns
    ///
    /// The calculated result, also stored in `ans`
    ///
    /// # Examples
    ///
    /// ```
    /// use pmatinit::calculator::Calculator;
    ///
    /// let mut calc = Calculator::new();
    ///
    /// // Simple calculation
    /// let result = calc.evaluate("2 + 3").unwrap();
    /// assert_eq!(result, 5.0);
    ///
    /// // Variable assignment
    /// let result = calc.evaluate("x = 10").unwrap();
    /// assert_eq!(result, 10.0);
    ///
    /// // Variable usage
    /// let result = calc.evaluate("x * 2").unwrap();
    /// assert_eq!(result, 20.0);
    ///
    /// // ans variable
    /// let result = calc.evaluate("ans + 5").unwrap();
    /// assert_eq!(result, 25.0);
    /// ```
    pub fn evaluate(&mut self, expression: &str) -> Result<f64> {
        // Check if this is an assignment expression
        if let Some((var_name, value_expr)) = parser::parse_assignment(expression)? {
            // Evaluate the right-hand side
            let value = self.evaluate_internal(value_expr)?;

            // Store the variable
            self.variables.insert(var_name.to_string(), value);

            // Update ans
            self.variables.insert("ans".to_string(), value);

            return Ok(value);
        }

        // Regular expression evaluation
        let result = self.evaluate_internal(expression)?;

        // Update ans variable
        self.variables.insert("ans".to_string(), result);

        Ok(result)
    }

    /// Internal evaluation without updating ans (used for recursive calls)
    fn evaluate_internal(&self, expression: &str) -> Result<f64> {
        // 1. Parse expression into tokens (with variable substitution)
        let tokens = parser::tokenize_with_variables(expression, &self.variables)?;

        // 2. Convert to postfix notation
        let postfix = parser::infix_to_postfix(tokens)?;

        // 3. Evaluate postfix expression
        let result = evaluator::evaluate_postfix(postfix)?;

        Ok(result)
    }

    /// Gets the value of a variable
    pub fn get_variable(&self, name: &str) -> Option<f64> {
        self.variables.get(name).copied()
    }

    /// Sets a variable value directly
    pub fn set_variable(&mut self, name: &str, value: f64) {
        self.variables.insert(name.to_string(), value);
    }

    /// Deletes a variable
    pub fn delete_variable(&mut self, name: &str) -> bool {
        self.variables.remove(name).is_some()
    }

    /// Clears all variables
    pub fn clear_variables(&mut self) {
        self.variables.clear();
    }

    /// Lists all variables
    pub fn list_variables(&self) -> Vec<(String, f64)> {
        let mut vars: Vec<_> = self.variables.iter()
            .map(|(k, v)| (k.clone(), *v))
            .collect();
        vars.sort_by(|a, b| a.0.cmp(&b.0));
        vars
    }
}

impl Default for Calculator {
    fn default() -> Self {
        Self::new()
    }
}

/// Evaluates a mathematical expression string and returns the result (stateless)
///
/// This is a convenience function for single-expression evaluation without variables.
/// For variable support, use the `Calculator` struct.
///
/// Supports basic arithmetic, advanced operations (power, functions, constants),
/// and proper operator precedence following mathematical conventions.
///
/// # Arguments
///
/// * `expression` - A string slice containing the mathematical expression
///
/// # Examples
///
/// ## Basic arithmetic
/// ```
/// use pmatinit::calculator::evaluate_expression;
///
/// let result = evaluate_expression("2 + 2").unwrap();
/// assert_eq!(result, 4.0);
/// ```
///
/// ## Advanced operations
/// ```
/// use pmatinit::calculator::evaluate_expression;
///
/// // Power and factorial
/// let result = evaluate_expression("2 ^ 3!").unwrap();
/// assert_eq!(result, 64.0);
///
/// // Functions and constants
/// let result = evaluate_expression("sqrt(16) + sin(0)").unwrap();
/// assert_eq!(result, 4.0);
/// ```
///
/// # Errors
///
/// Returns an error if:
/// - The expression contains invalid syntax
/// - Division by zero occurs
/// - Invalid function arguments (e.g., sqrt of negative, log of non-positive)
/// - Factorial of non-integer or negative number
/// - Numeric overflow/underflow occurs
pub fn evaluate_expression(expression: &str) -> Result<f64> {
    // 1. Parse expression into tokens
    let tokens = parser::tokenize(expression)?;

    // 2. Convert to postfix notation
    let postfix = parser::infix_to_postfix(tokens)?;

    // 3. Evaluate postfix expression
    let result = evaluator::evaluate_postfix(postfix)?;

    Ok(result)
}
