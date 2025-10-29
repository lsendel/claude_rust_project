//! Expression parser - Converts infix expressions to postfix notation
//!
//! This module provides functionality for parsing mathematical expressions from
//! strings into tokens, and converting them from infix notation (standard mathematical
//! notation) to postfix notation (Reverse Polish Notation) using the Shunting Yard algorithm.
//!
//! # Parsing Pipeline
//!
//! 1. **Tokenization**: Convert string → tokens (numbers, operators, functions, etc.)
//! 2. **Shunting Yard**: Convert infix tokens → postfix tokens
//! 3. **Evaluation**: Evaluate postfix tokens → result (in evaluator module)
//!
//! # Supported Tokens
//!
//! - **Numbers**: Integers and decimals (e.g., `42`, `3.14`, `-5`)
//! - **Operators**: `+`, `-`, `*`, `/`, `%`, `^`, `!`
//! - **Functions**: `sin`, `cos`, `tan`, `sqrt`, `log`, `ln`
//! - **Constants**: `pi`, `e`
//! - **Parentheses**: `(`, `)`
//! - **Variables**: User-defined variable names
//!
//! # Examples
//!
//! ```
//! use pmatinit::calculator::parser::{tokenize, infix_to_postfix};
//!
//! // Tokenize a simple expression
//! let tokens = tokenize("2 + 3 * 4").unwrap();
//! println!("Tokens: {:?}", tokens);
//!
//! // Convert to postfix notation
//! let postfix = infix_to_postfix(tokens).unwrap();
//! println!("Postfix: {:?}", postfix);
//! // Postfix respects operator precedence: 2, 3, 4, *, +
//! ```

use super::operators::Operator;
use anyhow::{Context, Result};
use std::collections::HashMap;

/// Represents a token in the expression
#[derive(Debug, Clone, PartialEq)]
pub enum Token {
    /// A numeric value
    Number(f64),
    /// A mathematical operator
    Operator(Operator),
    /// Left parenthesis
    LeftParen,
    /// Right parenthesis
    RightParen,
    /// A mathematical constant (pi, e)
    Constant(String),
    /// A mathematical function (sin, cos, tan, log, ln, sqrt)
    Function(String),
    /// Factorial operator (postfix !)
    Factorial,
    /// A variable name (for variables that couldn't be resolved)
    Variable(String),
}

/// Tokenizes a mathematical expression string
///
/// # Arguments
///
/// * `expression` - The expression string to tokenize
///
/// # Returns
///
/// A vector of tokens
///
/// # Errors
///
/// Returns an error if the expression contains invalid characters or malformed numbers
pub fn tokenize(expression: &str) -> Result<Vec<Token>> {
    let mut tokens = Vec::new();
    let mut chars = expression.chars().peekable();

    while let Some(&c) = chars.peek() {
        match c {
            // Skip whitespace
            ' ' | '\t' | '\n' | '\r' => {
                chars.next();
            }
            // Parentheses
            '(' => {
                tokens.push(Token::LeftParen);
                chars.next();
            }
            ')' => {
                tokens.push(Token::RightParen);
                chars.next();
            }
            // Factorial operator (postfix)
            '!' => {
                tokens.push(Token::Factorial);
                chars.next();
            }
            // Operators
            '+' | '*' | '/' | '%' | '^' => {
                if let Some(op) = Operator::from_char(c) {
                    tokens.push(Token::Operator(op));
                }
                chars.next();
            }
            // Handle minus (could be operator or negative number)
            '-' => {
                // Check if this is a negative number or subtraction operator
                let is_negative_number = tokens.is_empty()
                    || matches!(
                        tokens.last(),
                        Some(Token::Operator(_)) | Some(Token::LeftParen)
                    );

                if is_negative_number {
                    // Parse as negative number
                    chars.next(); // consume '-'
                    let num_str = parse_number_string(&mut chars)?;
                    let num: f64 = format!("-{}", num_str)
                        .parse()
                        .context("Failed to parse negative number")?;
                    tokens.push(Token::Number(num));
                } else {
                    // Parse as operator
                    tokens.push(Token::Operator(Operator::Subtract));
                    chars.next();
                }
            }
            // Numbers
            '0'..='9' | '.' => {
                let num_str = parse_number_string(&mut chars)?;
                let num: f64 = num_str.parse().context("Failed to parse number")?;
                tokens.push(Token::Number(num));
            }
            // Identifiers (constants and functions)
            'a'..='z' | 'A'..='Z' => {
                let identifier = parse_identifier(&mut chars);

                // Check if it's a constant
                match identifier.as_str() {
                    "pi" | "e" => {
                        tokens.push(Token::Constant(identifier));
                    }
                    "sin" | "cos" | "tan" | "log" | "ln" | "sqrt" => {
                        tokens.push(Token::Function(identifier));
                    }
                    _ => {
                        anyhow::bail!("Unknown identifier: '{}'", identifier);
                    }
                }
            }
            // Invalid character
            _ => {
                anyhow::bail!("Invalid character in expression: '{}'", c);
            }
        }
    }

    Ok(tokens)
}

/// Parses a number string from the character iterator
fn parse_number_string(chars: &mut std::iter::Peekable<std::str::Chars>) -> Result<String> {
    let mut num_str = String::new();
    let mut has_decimal = false;

    while let Some(&c) = chars.peek() {
        match c {
            '0'..='9' => {
                num_str.push(c);
                chars.next();
            }
            '.' => {
                if has_decimal {
                    anyhow::bail!("Multiple decimal points in number");
                }
                has_decimal = true;
                num_str.push(c);
                chars.next();
            }
            _ => break,
        }
    }

    if num_str.is_empty() || num_str == "." {
        anyhow::bail!("Invalid number format");
    }

    Ok(num_str)
}

/// Parses an identifier (constant, function name, or variable) from the character iterator
fn parse_identifier(chars: &mut std::iter::Peekable<std::str::Chars>) -> String {
    let mut identifier = String::new();

    while let Some(&c) = chars.peek() {
        if c.is_ascii_alphanumeric() || c == '_' {
            identifier.push(c);
            chars.next();
        } else {
            break;
        }
    }

    identifier
}

/// Converts infix notation tokens to postfix notation using the Shunting Yard algorithm
///
/// # Arguments
///
/// * `tokens` - Vector of tokens in infix notation
///
/// # Returns
///
/// A vector of tokens in postfix notation (Reverse Polish Notation)
///
/// # Errors
///
/// Returns an error if the expression has mismatched parentheses
pub fn infix_to_postfix(tokens: Vec<Token>) -> Result<Vec<Token>> {
    let mut output = Vec::new();
    let mut operator_stack: Vec<Token> = Vec::new();

    for token in tokens {
        match token {
            Token::Number(_) | Token::Constant(_) | Token::Variable(_) => {
                output.push(token);
            }
            Token::Factorial => {
                // Factorial is a postfix operator, push it directly to output
                output.push(token);
            }
            Token::Function(_) => {
                operator_stack.push(token);
            }
            Token::Operator(op) => {
                while let Some(Token::Operator(stack_op)) = operator_stack.last() {
                    if stack_op.precedence() > op.precedence()
                        || (stack_op.precedence() == op.precedence() && op.is_left_associative())
                    {
                        output.push(operator_stack.pop().unwrap());
                    } else {
                        break;
                    }
                }
                operator_stack.push(Token::Operator(op));
            }
            Token::LeftParen => {
                operator_stack.push(token);
            }
            Token::RightParen => {
                let mut found_left_paren = false;
                while let Some(top) = operator_stack.pop() {
                    if matches!(top, Token::LeftParen) {
                        found_left_paren = true;
                        break;
                    }
                    output.push(top);
                }
                if !found_left_paren {
                    anyhow::bail!("Mismatched parentheses: missing '('");
                }
                // If there's a function on top of the stack, pop it to output
                if let Some(Token::Function(_)) = operator_stack.last() {
                    output.push(operator_stack.pop().unwrap());
                }
            }
        }
    }

    // Pop remaining operators
    while let Some(op) = operator_stack.pop() {
        if matches!(op, Token::LeftParen) {
            anyhow::bail!("Mismatched parentheses: missing ')'");
        }
        output.push(op);
    }

    Ok(output)
}

/// Parses an assignment expression and returns (variable_name, value_expression)
///
/// Returns None if this is not an assignment expression
///
/// # Arguments
///
/// * `expression` - The expression string to parse
///
/// # Returns
///
/// - Some((var_name, value_expr)) if this is an assignment
/// - None if this is a regular expression
///
/// # Errors
///
/// Returns an error if the assignment syntax is invalid
pub fn parse_assignment(expression: &str) -> Result<Option<(&str, &str)>> {
    let expression = expression.trim();

    // Look for '=' that's not part of another operator
    if let Some(eq_pos) = expression.find('=') {
        // Split at the equals sign
        let var_part = expression[..eq_pos].trim();
        let value_part = expression[eq_pos + 1..].trim();

        // Validate variable name
        if var_part.is_empty() {
            anyhow::bail!("Assignment requires a variable name");
        }

        // Variable name must be valid identifier
        if !is_valid_variable_name(var_part) {
            anyhow::bail!("Invalid variable name: '{}'", var_part);
        }

        // Check for reserved names
        if matches!(var_part, "pi" | "e" | "sin" | "cos" | "tan" | "log" | "ln" | "sqrt") {
            anyhow::bail!("Cannot assign to reserved name: '{}'", var_part);
        }

        if value_part.is_empty() {
            anyhow::bail!("Assignment requires a value");
        }

        return Ok(Some((var_part, value_part)));
    }

    Ok(None)
}

/// Checks if a string is a valid variable name
fn is_valid_variable_name(name: &str) -> bool {
    if name.is_empty() {
        return false;
    }

    // Must start with letter
    let mut chars = name.chars();
    if !chars.next().unwrap().is_ascii_alphabetic() {
        return false;
    }

    // Rest must be letters, digits, or underscores
    chars.all(|c| c.is_ascii_alphanumeric() || c == '_')
}

/// Tokenizes an expression with variable substitution
///
/// Similar to tokenize(), but looks up variables in the provided HashMap
/// and substitutes their values.
///
/// # Arguments
///
/// * `expression` - The expression string to tokenize
/// * `variables` - HashMap of variable names to values
///
/// # Returns
///
/// A vector of tokens with variables substituted
pub fn tokenize_with_variables(
    expression: &str,
    variables: &HashMap<String, f64>,
) -> Result<Vec<Token>> {
    let mut tokens = Vec::new();
    let mut chars = expression.chars().peekable();

    while let Some(&c) = chars.peek() {
        match c {
            // Skip whitespace
            ' ' | '\t' | '\n' | '\r' => {
                chars.next();
            }
            // Parentheses
            '(' => {
                tokens.push(Token::LeftParen);
                chars.next();
            }
            ')' => {
                tokens.push(Token::RightParen);
                chars.next();
            }
            // Factorial operator (postfix)
            '!' => {
                tokens.push(Token::Factorial);
                chars.next();
            }
            // Operators
            '+' | '*' | '/' | '%' | '^' => {
                if let Some(op) = Operator::from_char(c) {
                    tokens.push(Token::Operator(op));
                }
                chars.next();
            }
            // Handle minus (could be operator or negative number)
            '-' => {
                // Check if this is a negative number or subtraction operator
                let is_negative_number = tokens.is_empty()
                    || matches!(
                        tokens.last(),
                        Some(Token::Operator(_)) | Some(Token::LeftParen)
                    );

                if is_negative_number {
                    // Parse as negative number
                    chars.next(); // consume '-'
                    let num_str = parse_number_string(&mut chars)?;
                    let num: f64 = format!("-{}", num_str)
                        .parse()
                        .context("Failed to parse negative number")?;
                    tokens.push(Token::Number(num));
                } else {
                    // Parse as operator
                    tokens.push(Token::Operator(Operator::Subtract));
                    chars.next();
                }
            }
            // Numbers
            '0'..='9' | '.' => {
                let num_str = parse_number_string(&mut chars)?;
                let num: f64 = num_str.parse().context("Failed to parse number")?;
                tokens.push(Token::Number(num));
            }
            // Identifiers (constants, functions, and variables)
            'a'..='z' | 'A'..='Z' | '_' => {
                let identifier = parse_identifier(&mut chars);

                // Check if it's a constant
                match identifier.as_str() {
                    "pi" | "e" => {
                        tokens.push(Token::Constant(identifier));
                    }
                    "sin" | "cos" | "tan" | "log" | "ln" | "sqrt" => {
                        tokens.push(Token::Function(identifier));
                    }
                    _ => {
                        // Check if it's a variable
                        if let Some(&value) = variables.get(&identifier) {
                            tokens.push(Token::Number(value));
                        } else {
                            anyhow::bail!("Undefined variable: '{}'", identifier);
                        }
                    }
                }
            }
            // Invalid character
            _ => {
                anyhow::bail!("Invalid character in expression: '{}'", c);
            }
        }
    }

    Ok(tokens)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_tokenize_simple() {
        let tokens = tokenize("2 + 3").unwrap();
        assert_eq!(tokens.len(), 3);
        assert!(matches!(tokens[0], Token::Number(2.0)));
        assert!(matches!(tokens[1], Token::Operator(Operator::Add)));
        assert!(matches!(tokens[2], Token::Number(3.0)));
    }

    #[test]
    fn test_tokenize_with_parentheses() {
        let tokens = tokenize("(2 + 3) * 4").unwrap();
        assert_eq!(tokens.len(), 7);
        assert!(matches!(tokens[0], Token::LeftParen));
        assert!(matches!(tokens[2], Token::Operator(Operator::Add)));
        assert!(matches!(tokens[4], Token::RightParen));
    }

    #[test]
    fn test_tokenize_negative_number() {
        let tokens = tokenize("-5 + 3").unwrap();
        assert_eq!(tokens.len(), 3);
        assert!(matches!(tokens[0], Token::Number(n) if n == -5.0));
    }

    #[test]
    fn test_tokenize_decimal() {
        let tokens = tokenize("3.14 + 2.5").unwrap();
        assert!(matches!(tokens[0], Token::Number(n) if (n - 3.14).abs() < 0.001));
        assert!(matches!(tokens[2], Token::Number(n) if (n - 2.5).abs() < 0.001));
    }

    #[test]
    fn test_infix_to_postfix_simple() {
        let tokens = tokenize("2 + 3").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        assert_eq!(postfix.len(), 3);
        assert!(matches!(postfix[0], Token::Number(2.0)));
        assert!(matches!(postfix[1], Token::Number(3.0)));
        assert!(matches!(postfix[2], Token::Operator(Operator::Add)));
    }

    #[test]
    fn test_infix_to_postfix_precedence() {
        let tokens = tokenize("2 + 3 * 4").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        // Should be: 2 3 4 * +
        assert!(matches!(postfix[0], Token::Number(2.0)));
        assert!(matches!(postfix[1], Token::Number(3.0)));
        assert!(matches!(postfix[2], Token::Number(4.0)));
        assert!(matches!(postfix[3], Token::Operator(Operator::Multiply)));
        assert!(matches!(postfix[4], Token::Operator(Operator::Add)));
    }

    #[test]
    fn test_tokenize_power_operator() {
        let tokens = tokenize("2 ^ 3").unwrap();
        assert_eq!(tokens.len(), 3);
        assert!(matches!(tokens[0], Token::Number(2.0)));
        assert!(matches!(tokens[1], Token::Operator(Operator::Power)));
        assert!(matches!(tokens[2], Token::Number(3.0)));
    }

    #[test]
    fn test_power_precedence_over_multiply() {
        // 2 * 3 ^ 2 should be 2 * (3 ^ 2) = 2 * 9 = 18
        let tokens = tokenize("2 * 3 ^ 2").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        // Should be: 2 3 2 ^ *
        assert!(matches!(postfix[0], Token::Number(2.0)));
        assert!(matches!(postfix[1], Token::Number(3.0)));
        assert!(matches!(postfix[2], Token::Number(2.0)));
        assert!(matches!(postfix[3], Token::Operator(Operator::Power)));
        assert!(matches!(postfix[4], Token::Operator(Operator::Multiply)));
    }

    #[test]
    fn test_power_right_associativity() {
        // 2 ^ 3 ^ 2 should be 2 ^ (3 ^ 2) = 2 ^ 9 = 512
        let tokens = tokenize("2 ^ 3 ^ 2").unwrap();
        let postfix = infix_to_postfix(tokens).unwrap();
        // Should be: 2 3 2 ^ ^
        assert!(matches!(postfix[0], Token::Number(2.0)));
        assert!(matches!(postfix[1], Token::Number(3.0)));
        assert!(matches!(postfix[2], Token::Number(2.0)));
        assert!(matches!(postfix[3], Token::Operator(Operator::Power)));
        assert!(matches!(postfix[4], Token::Operator(Operator::Power)));
    }
}
