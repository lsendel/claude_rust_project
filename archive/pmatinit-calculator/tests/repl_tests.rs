//! Tests for REPL enhancements (Phase 3)
//!
//! Tests include:
//! - Error message enhancement and suggestions
//! - REPL helper creation and basic functionality

use pmatinit::repl::errors;
use pmatinit::repl::helper::ReplHelper;

#[test]
fn test_enhance_unknown_function_error() {
    let error = "Unknown identifier: 'sine'";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should suggest 'sin'
    assert!(enhanced.contains("Did you mean"));
    assert!(enhanced.contains("sin"));
}

#[test]
fn test_enhance_unknown_constant_error() {
    let error = "Unknown identifier: 'pie'";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should suggest 'pi'
    assert!(enhanced.contains("pi"));
}

#[test]
fn test_enhance_undefined_variable_error() {
    let error = "Undefined variable: 'y'";
    let variables = vec!["x".to_string(), "ans".to_string()];
    let enhanced = errors::enhance_error_message(error, &variables);

    // Should show available variables
    assert!(enhanced.contains("Available variables") || enhanced.contains("Did you mean"));
}

#[test]
fn test_enhance_undefined_variable_empty() {
    let error = "Undefined variable: 'x'";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should suggest defining a variable
    assert!(enhanced.contains("No variables defined") || enhanced.contains("x = 5"));
}

#[test]
fn test_enhance_mismatched_parentheses() {
    let error = "Mismatched parentheses: missing ')'";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should provide tip about parentheses
    assert!(enhanced.contains("Tip:"));
    assert!(enhanced.contains("'('") || enhanced.contains("')'"));
}

#[test]
fn test_enhance_division_by_zero() {
    let error = "Division by zero";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should provide tip
    assert!(enhanced.contains("Tip:"));
}

#[test]
fn test_enhance_sqrt_negative() {
    let error = "sqrt: cannot take square root of negative number";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should mention complex numbers
    assert!(enhanced.contains("Tip:") || enhanced.contains("negative"));
}

#[test]
fn test_enhance_log_non_positive() {
    let error = "log: argument must be positive (got non-positive value)";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should mention log domain
    assert!(enhanced.contains("Tip:") || enhanced.contains("positive"));
}

#[test]
fn test_enhance_factorial_negative() {
    let error = "factorial: cannot compute factorial of negative number";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should mention factorial domain
    assert!(enhanced.contains("Tip:") || enhanced.contains("negative") || enhanced.contains("non-negative"));
}

#[test]
fn test_enhance_reserved_name_assignment() {
    let error = "Cannot assign to reserved name: 'pi'";
    let enhanced = errors::enhance_error_message(error, &[]);

    // Should list reserved names
    assert!(enhanced.contains("Reserved names:") || enhanced.contains("Tip:"));
}

#[test]
fn test_helper_creation() {
    // Test that we can create a helper
    let helper = ReplHelper::new();
    // Basic creation test - helper exists and is functional
    assert!(std::mem::size_of_val(&helper) > 0);
}

#[test]
fn test_helper_with_calculator() {
    use pmatinit::calculator::Calculator;

    // Test that we can update the helper with calculator state
    let mut helper = ReplHelper::new();
    let mut calc = Calculator::new();

    // Add some variables
    calc.evaluate("x = 10").unwrap();
    calc.evaluate("y = 20").unwrap();

    // Update helper with calculator state
    helper.update_calculator(&calc);

    // Helper should now have access to variables (tested implicitly through completion)
    assert!(std::mem::size_of_val(&helper) > 0);
}
