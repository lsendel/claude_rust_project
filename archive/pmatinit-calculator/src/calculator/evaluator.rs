//! Expression evaluator - Evaluates postfix expressions
//!
//! This module handles the evaluation of expressions that have been converted to
//! postfix notation (Reverse Polish Notation). It processes tokens sequentially,
//! maintaining a stack of intermediate results.
//!
//! # Supported Operations
//!
//! - **Arithmetic operators**: `+`, `-`, `*`, `/`, `%`, `^`
//! - **Functions**: `sin`, `cos`, `tan`, `sqrt`, `log`, `ln`
//! - **Constants**: `pi`, `e`
//! - **Special operators**: Factorial (`!`)
//!
//! # Algorithm
//!
//! The evaluator uses a stack-based approach:
//! 1. Numbers and constants are pushed onto the stack
//! 2. Operators pop operands from the stack, compute results, and push back
//! 3. Functions pop one operand, apply the function, and push the result
//! 4. The final stack should contain exactly one value - the result
//!
//! # Examples
//!
//! ```
//! use pmatinit::calculator::parser::{tokenize, infix_to_postfix};
//! use pmatinit::calculator::evaluator::evaluate_postfix;
//!
//! // Simple arithmetic
//! let tokens = tokenize("2 + 3").unwrap();
//! let postfix = infix_to_postfix(tokens).unwrap();
//! let result = evaluate_postfix(postfix).unwrap();
//! assert_eq!(result, 5.0);
//!
//! // With functions and constants
//! let tokens = tokenize("2 * pi").unwrap();
//! let postfix = infix_to_postfix(tokens).unwrap();
//! let result = evaluate_postfix(postfix).unwrap();
//! assert!((result - 2.0 * std::f64::consts::PI).abs() < 0.0001);
//! ```

use super::parser::Token;
use anyhow::Result;

/// Evaluates a postfix expression
///
/// # Arguments
///
/// * `postfix` - Vector of tokens in postfix notation (Reverse Polish Notation)
///
/// # Returns
///
/// The calculated result as a floating-point number
///
/// # Errors
///
/// Returns an error if:
/// - The expression is invalid (insufficient operands)
/// - Division by zero occurs
/// - Numeric overflow/underflow occurs
pub fn evaluate_postfix(postfix: Vec<Token>) -> Result<f64> {
    let mut stack: Vec<f64> = Vec::new();

    for token in postfix {
        match token {
            Token::Number(n) => {
                stack.push(n);
            }
            Token::Constant(name) => {
                let value = match name.as_str() {
                    "pi" => std::f64::consts::PI,
                    "e" => std::f64::consts::E,
                    _ => anyhow::bail!("Unknown constant: {}", name),
                };
                stack.push(value);
            }
            Token::Factorial => {
                if stack.is_empty() {
                    anyhow::bail!("Invalid expression: factorial requires an operand");
                }
                let arg = stack.pop().unwrap();

                // Check if the number is a non-negative integer
                if arg < 0.0 {
                    anyhow::bail!("Factorial requires non-negative number");
                }
                if arg.fract() != 0.0 {
                    anyhow::bail!("Factorial requires integer argument");
                }
                if arg > 170.0 {
                    anyhow::bail!("Factorial argument too large (max 170)");
                }

                let n = arg as u64;
                let mut result = 1.0;
                for i in 2..=n {
                    result *= i as f64;
                }

                stack.push(result);
            }
            Token::Function(name) => {
                if stack.is_empty() {
                    anyhow::bail!("Invalid expression: function {} requires an argument", name);
                }
                let arg = stack.pop().unwrap();
                let result = match name.as_str() {
                    "sqrt" => {
                        if arg < 0.0 {
                            anyhow::bail!("Cannot take square root of negative number");
                        }
                        arg.sqrt()
                    }
                    "sin" => arg.sin(),
                    "cos" => arg.cos(),
                    "tan" => arg.tan(),
                    "log" => {
                        if arg <= 0.0 {
                            anyhow::bail!("Logarithm requires positive argument");
                        }
                        arg.log10()
                    }
                    "ln" => {
                        if arg <= 0.0 {
                            anyhow::bail!("Natural logarithm requires positive argument");
                        }
                        arg.ln()
                    }
                    _ => anyhow::bail!("Unknown function: {}", name),
                };
                if result.is_infinite() || result.is_nan() {
                    anyhow::bail!("Function {} resulted in invalid value", name);
                }
                stack.push(result);
            }
            Token::Operator(op) => {
                if stack.len() < 2 {
                    anyhow::bail!("Invalid expression: insufficient operands for operator");
                }

                let right = stack.pop().unwrap();
                let left = stack.pop().unwrap();

                let result = op.apply(left, right)?;

                // Check for overflow/infinity
                if result.is_infinite() {
                    anyhow::bail!("Numeric overflow");
                }
                if result.is_nan() {
                    anyhow::bail!("Invalid numeric result");
                }

                stack.push(result);
            }
            Token::LeftParen | Token::RightParen => {
                anyhow::bail!("Unexpected parenthesis in postfix expression");
            }
            Token::Variable(name) => {
                anyhow::bail!("Undefined variable in expression: '{}'", name);
            }
        }
    }

    if stack.len() != 1 {
        anyhow::bail!(
            "Invalid expression: expected single result, found {} values",
            stack.len()
        );
    }

    Ok(stack.pop().unwrap())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::calculator::parser::tokenize;
    use crate::calculator::parser::infix_to_postfix;

    #[test]
    fn test_evaluate_simple_addition() {
        let tokens = tokenize("2 + 3").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        assert_eq!(result, 5.0);
    }

    #[test]
    fn test_evaluate_precedence() {
        let tokens = tokenize("2 + 3 * 4").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        assert_eq!(result, 14.0);
    }

    #[test]
    fn test_evaluate_with_parentheses() {
        let tokens = tokenize("(2 + 3) * 4").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        assert_eq!(result, 20.0);
    }

    #[test]
    fn test_evaluate_division() {
        let tokens = tokenize("10 / 2").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        assert_eq!(result, 5.0);
    }

    #[test]
    fn test_evaluate_division_by_zero() {
        let tokens = tokenize("10 / 0").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix);
        assert!(result.is_err());
    }

    #[test]
    fn test_evaluate_negative_numbers() {
        let tokens = tokenize("-5 + 3").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        assert_eq!(result, -2.0);
    }

    #[test]
    fn test_evaluate_complex_expression() {
        let tokens = tokenize("(10 + 5) * 3 - 8 / 2").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        let result = evaluate_postfix(postfix).unwrap();
        // (10 + 5) * 3 - 8 / 2 = 15 * 3 - 4 = 45 - 4 = 41
        assert_eq!(result, 41.0);
    }
}
