//! Operator definitions and precedence rules
//!
//! This module defines the mathematical operators supported by the calculator,
//! along with their precedence and associativity rules.
//!
//! # Operator Precedence
//!
//! From highest to lowest precedence:
//! 1. Power (`^`) - precedence 3
//! 2. Multiplication (`*`), Division (`/`), Modulo (`%`) - precedence 2
//! 3. Addition (`+`), Subtraction (`-`) - precedence 1
//!
//! # Associativity
//!
//! - **Left-associative**: `+`, `-`, `*`, `/`, `%`
//!   - Example: `5 - 3 - 1` = `(5 - 3) - 1` = `1`
//! - **Right-associative**: `^`
//!   - Example: `2 ^ 3 ^ 2` = `2 ^ (3 ^ 2)` = `512`
//!
//! # Examples
//!
//! ```
//! use pmatinit::calculator::operators::Operator;
//!
//! // Check precedence
//! assert!(Operator::Power.precedence() > Operator::Multiply.precedence());
//! assert!(Operator::Multiply.precedence() > Operator::Add.precedence());
//!
//! // Check associativity
//! assert!(Operator::Add.is_left_associative());
//! assert!(!Operator::Power.is_left_associative());
//! ```

/// Supported mathematical operators
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Operator {
    /// Addition operator (+)
    Add,
    /// Subtraction operator (-)
    Subtract,
    /// Multiplication operator (*)
    Multiply,
    /// Division operator (/)
    Divide,
    /// Modulo operator (%)
    Modulo,
    /// Power operator (^)
    Power,
}

impl Operator {
    /// Returns the precedence level of the operator
    ///
    /// Higher numbers indicate higher precedence.
    /// Follows standard mathematical order of operations (PEMDAS).
    pub fn precedence(&self) -> u8 {
        match self {
            Operator::Add | Operator::Subtract => 1,
            Operator::Multiply | Operator::Divide | Operator::Modulo => 2,
            Operator::Power => 3,
        }
    }

    /// Returns the associativity of the operator
    ///
    /// Most operators are left-associative, but Power is right-associative.
    pub fn is_left_associative(&self) -> bool {
        match self {
            Operator::Power => false,  // Right-associative: 2^3^2 = 2^(3^2) = 2^9 = 512
            _ => true,
        }
    }

    /// Converts a character to an operator if valid
    pub fn from_char(c: char) -> Option<Self> {
        match c {
            '+' => Some(Operator::Add),
            '-' => Some(Operator::Subtract),
            '*' => Some(Operator::Multiply),
            '/' => Some(Operator::Divide),
            '%' => Some(Operator::Modulo),
            '^' => Some(Operator::Power),
            _ => None,
        }
    }

    /// Applies the operator to two operands
    ///
    /// # Arguments
    ///
    /// * `left` - The left operand
    /// * `right` - The right operand
    ///
    /// # Returns
    ///
    /// The result of applying the operator
    pub fn apply(&self, left: f64, right: f64) -> anyhow::Result<f64> {
        match self {
            Operator::Add => Ok(left + right),
            Operator::Subtract => Ok(left - right),
            Operator::Multiply => Ok(left * right),
            Operator::Divide => {
                if right == 0.0 {
                    anyhow::bail!("Division by zero")
                }
                Ok(left / right)
            }
            Operator::Modulo => {
                if right == 0.0 {
                    anyhow::bail!("Modulo by zero")
                }
                Ok(left % right)
            }
            Operator::Power => {
                let result = left.powf(right);
                if result.is_infinite() || result.is_nan() {
                    anyhow::bail!("Power operation resulted in invalid value")
                }
                Ok(result)
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_operator_precedence() {
        assert!(Operator::Multiply.precedence() > Operator::Add.precedence());
        assert!(Operator::Divide.precedence() > Operator::Subtract.precedence());
        assert_eq!(Operator::Add.precedence(), Operator::Subtract.precedence());
    }

    #[test]
    fn test_operator_from_char() {
        assert_eq!(Operator::from_char('+'), Some(Operator::Add));
        assert_eq!(Operator::from_char('-'), Some(Operator::Subtract));
        assert_eq!(Operator::from_char('*'), Some(Operator::Multiply));
        assert_eq!(Operator::from_char('/'), Some(Operator::Divide));
        assert_eq!(Operator::from_char('%'), Some(Operator::Modulo));
        assert_eq!(Operator::from_char('x'), None);
    }

    #[test]
    fn test_operator_apply() {
        assert_eq!(Operator::Add.apply(2.0, 3.0).unwrap(), 5.0);
        assert_eq!(Operator::Subtract.apply(5.0, 3.0).unwrap(), 2.0);
        assert_eq!(Operator::Multiply.apply(4.0, 3.0).unwrap(), 12.0);
        assert_eq!(Operator::Divide.apply(10.0, 2.0).unwrap(), 5.0);
        assert_eq!(Operator::Modulo.apply(10.0, 3.0).unwrap(), 1.0);
    }

    #[test]
    fn test_division_by_zero() {
        assert!(Operator::Divide.apply(10.0, 0.0).is_err());
        assert!(Operator::Modulo.apply(10.0, 0.0).is_err());
    }

    #[test]
    fn test_power_operator() {
        assert_eq!(Operator::Power.apply(2.0, 3.0).unwrap(), 8.0);
        assert_eq!(Operator::Power.apply(5.0, 2.0).unwrap(), 25.0);
        assert_eq!(Operator::Power.apply(10.0, 0.0).unwrap(), 1.0);
        assert_eq!(Operator::Power.apply(2.0, -1.0).unwrap(), 0.5);
    }

    #[test]
    fn test_power_precedence() {
        // Power should have higher precedence than multiplication
        assert!(Operator::Power.precedence() > Operator::Multiply.precedence());
        assert!(Operator::Power.precedence() > Operator::Add.precedence());
    }

    #[test]
    fn test_power_associativity() {
        // Power is right-associative
        assert!(!Operator::Power.is_left_associative());
    }

    #[test]
    fn test_power_from_char() {
        assert_eq!(Operator::from_char('^'), Some(Operator::Power));
    }
}
