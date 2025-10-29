//! Integration tests for the calculator CLI
//!
//! These tests verify end-to-end functionality of the calculator.

use pmatinit::calculator::evaluate_expression;

#[test]
fn test_simple_addition() {
    let result = evaluate_expression("2 + 2").unwrap();
    assert_eq!(result, 4.0);
}

#[test]
fn test_simple_subtraction() {
    let result = evaluate_expression("5 - 3").unwrap();
    assert_eq!(result, 2.0);
}

#[test]
fn test_simple_multiplication() {
    let result = evaluate_expression("3 * 4").unwrap();
    assert_eq!(result, 12.0);
}

#[test]
fn test_simple_division() {
    let result = evaluate_expression("10 / 2").unwrap();
    assert_eq!(result, 5.0);
}

#[test]
fn test_modulo() {
    let result = evaluate_expression("10 % 3").unwrap();
    assert_eq!(result, 1.0);
}

#[test]
fn test_operator_precedence() {
    // Should evaluate as 2 + (3 * 4) = 14
    let result = evaluate_expression("2 + 3 * 4").unwrap();
    assert_eq!(result, 14.0);
}

#[test]
fn test_parentheses() {
    // Should evaluate as (2 + 3) * 4 = 20
    let result = evaluate_expression("(2 + 3) * 4").unwrap();
    assert_eq!(result, 20.0);
}

#[test]
fn test_complex_expression() {
    // (10 + 5) * 3 - 8 / 2 = 15 * 3 - 4 = 45 - 4 = 41
    let result = evaluate_expression("(10 + 5) * 3 - 8 / 2").unwrap();
    assert_eq!(result, 41.0);
}

#[test]
fn test_negative_numbers() {
    let result = evaluate_expression("-5 + 3").unwrap();
    assert_eq!(result, -2.0);
}

#[test]
fn test_decimal_numbers() {
    let result = evaluate_expression("3.14 + 2.86").unwrap();
    assert!((result - 6.0).abs() < 0.001);
}

#[test]
fn test_division_by_zero_error() {
    let result = evaluate_expression("10 / 0");
    assert!(result.is_err());
    let err_msg = result.unwrap_err().to_string();
    eprintln!("Error message: '{}'", err_msg);
    let err_lower = err_msg.to_lowercase();
    assert!(err_lower.contains("division") && err_lower.contains("zero"),
        "Expected error message to contain 'division' and 'zero', got: '{}'", err_msg);
}

#[test]
fn test_invalid_syntax_error() {
    let result = evaluate_expression("2 + + 3");
    assert!(result.is_err());
}

#[test]
fn test_mismatched_parentheses_error() {
    let result = evaluate_expression("(2 + 3");
    assert!(result.is_err());
}

#[test]
fn test_empty_expression() {
    let result = evaluate_expression("");
    assert!(result.is_err());
}

#[test]
fn test_whitespace_handling() {
    let result = evaluate_expression("  2   +   3  ").unwrap();
    assert_eq!(result, 5.0);
}

#[test]
fn test_power_operator() {
    let result = evaluate_expression("2 ^ 3").unwrap();
    assert_eq!(result, 8.0);
}

#[test]
fn test_power_precedence() {
    // 2 * 3 ^ 2 = 2 * 9 = 18
    let result = evaluate_expression("2 * 3 ^ 2").unwrap();
    assert_eq!(result, 18.0);
}

#[test]
fn test_power_right_associativity() {
    // 2 ^ 3 ^ 2 = 2 ^ (3 ^ 2) = 2 ^ 9 = 512
    let result = evaluate_expression("2 ^ 3 ^ 2").unwrap();
    assert_eq!(result, 512.0);
}

#[test]
fn test_power_with_parentheses() {
    // (2 ^ 3) ^ 2 = 8 ^ 2 = 64
    let result = evaluate_expression("(2 ^ 3) ^ 2").unwrap();
    assert_eq!(result, 64.0);
}

#[test]
fn test_power_with_negative_exponent() {
    let result = evaluate_expression("2 ^ -2").unwrap();
    assert_eq!(result, 0.25);
}

#[test]
fn test_power_zero_exponent() {
    let result = evaluate_expression("5 ^ 0").unwrap();
    assert_eq!(result, 1.0);
}

#[test]
fn test_constant_pi() {
    let result = evaluate_expression("pi").unwrap();
    assert!((result - std::f64::consts::PI).abs() < 0.0001);
}

#[test]
fn test_constant_e() {
    let result = evaluate_expression("e").unwrap();
    assert!((result - std::f64::consts::E).abs() < 0.0001);
}

#[test]
fn test_constant_in_expression() {
    // 2 * pi
    let result = evaluate_expression("2 * pi").unwrap();
    assert!((result - 2.0 * std::f64::consts::PI).abs() < 0.0001);
}

#[test]
fn test_multiple_constants() {
    // pi + e
    let result = evaluate_expression("pi + e").unwrap();
    assert!((result - (std::f64::consts::PI + std::f64::consts::E)).abs() < 0.0001);
}

#[test]
fn test_constant_with_power() {
    // e ^ 2
    let result = evaluate_expression("e ^ 2").unwrap();
    assert!((result - std::f64::consts::E.powi(2)).abs() < 0.0001);
}

// Square root tests
#[test]
fn test_sqrt_basic() {
    let result = evaluate_expression("sqrt(16)").unwrap();
    assert_eq!(result, 4.0);
}

#[test]
fn test_sqrt_with_expression() {
    // sqrt(2 + 2)
    let result = evaluate_expression("sqrt(2 + 2)").unwrap();
    assert_eq!(result, 2.0);
}

#[test]
fn test_sqrt_negative_error() {
    let result = evaluate_expression("sqrt(-1)");
    assert!(result.is_err());
}

#[test]
fn test_sqrt_in_expression() {
    // 2 * sqrt(9)
    let result = evaluate_expression("2 * sqrt(9)").unwrap();
    assert_eq!(result, 6.0);
}

// Trigonometric function tests
#[test]
fn test_sin_zero() {
    let result = evaluate_expression("sin(0)").unwrap();
    assert!(result.abs() < 0.0001);
}

#[test]
fn test_sin_pi_over_2() {
    // sin(pi / 2) = 1
    let result = evaluate_expression("sin(pi / 2)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

#[test]
fn test_cos_zero() {
    let result = evaluate_expression("cos(0)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

#[test]
fn test_cos_pi() {
    // cos(pi) = -1
    let result = evaluate_expression("cos(pi)").unwrap();
    assert!((result + 1.0).abs() < 0.0001);
}

#[test]
fn test_tan_zero() {
    let result = evaluate_expression("tan(0)").unwrap();
    assert!(result.abs() < 0.0001);
}

#[test]
fn test_tan_pi_over_4() {
    // tan(pi / 4) = 1
    let result = evaluate_expression("tan(pi / 4)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

// Logarithmic function tests
#[test]
fn test_log_10() {
    let result = evaluate_expression("log(10)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

#[test]
fn test_log_100() {
    let result = evaluate_expression("log(100)").unwrap();
    assert!((result - 2.0).abs() < 0.0001);
}

#[test]
fn test_log_negative_error() {
    let result = evaluate_expression("log(-1)");
    assert!(result.is_err());
}

#[test]
fn test_log_zero_error() {
    let result = evaluate_expression("log(0)");
    assert!(result.is_err());
}

#[test]
fn test_ln_e() {
    let result = evaluate_expression("ln(e)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

#[test]
fn test_ln_e_squared() {
    let result = evaluate_expression("ln(e ^ 2)").unwrap();
    assert!((result - 2.0).abs() < 0.0001);
}

#[test]
fn test_ln_negative_error() {
    let result = evaluate_expression("ln(-1)");
    assert!(result.is_err());
}

// Complex expressions with functions and constants
#[test]
fn test_complex_with_functions() {
    // 2 * sqrt(16) + 3
    let result = evaluate_expression("2 * sqrt(16) + 3").unwrap();
    assert_eq!(result, 11.0);
}

#[test]
fn test_nested_functions() {
    // sqrt(sqrt(16))
    let result = evaluate_expression("sqrt(sqrt(16))").unwrap();
    assert_eq!(result, 2.0);
}

#[test]
fn test_function_with_constant() {
    // sin(pi)
    let result = evaluate_expression("sin(pi)").unwrap();
    assert!(result.abs() < 0.0001);
}

#[test]
fn test_multiple_functions_in_expression() {
    // sin(0) + cos(0)
    let result = evaluate_expression("sin(0) + cos(0)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

// Factorial tests
#[test]
fn test_factorial_basic() {
    let result = evaluate_expression("5!").unwrap();
    assert_eq!(result, 120.0);
}

#[test]
fn test_factorial_zero() {
    let result = evaluate_expression("0!").unwrap();
    assert_eq!(result, 1.0);
}

#[test]
fn test_factorial_one() {
    let result = evaluate_expression("1!").unwrap();
    assert_eq!(result, 1.0);
}

#[test]
fn test_factorial_in_expression() {
    // 3! + 2
    let result = evaluate_expression("3! + 2").unwrap();
    assert_eq!(result, 8.0);
}

#[test]
fn test_factorial_with_multiplication() {
    // 2 * 4!
    let result = evaluate_expression("2 * 4!").unwrap();
    assert_eq!(result, 48.0);
}

#[test]
fn test_factorial_negative_error() {
    let result = evaluate_expression("(-5)!");
    assert!(result.is_err());
}

#[test]
fn test_factorial_non_integer_error() {
    let result = evaluate_expression("3.5!");
    assert!(result.is_err());
}

#[test]
fn test_factorial_with_parentheses() {
    // (2 + 3)!
    let result = evaluate_expression("(2 + 3)!").unwrap();
    assert_eq!(result, 120.0);
}

// Comprehensive integration tests combining multiple advanced features
#[test]
fn test_power_with_sqrt() {
    // sqrt(16) ^ 2 = 4 ^ 2 = 16
    let result = evaluate_expression("sqrt(16) ^ 2").unwrap();
    assert_eq!(result, 16.0);
}

#[test]
fn test_factorial_with_power() {
    // 2 ^ 3! = 2 ^ 6 = 64
    let result = evaluate_expression("2 ^ 3!").unwrap();
    assert_eq!(result, 64.0);
}

#[test]
fn test_trig_with_constants() {
    // sin(pi / 2) + cos(0)
    let result = evaluate_expression("sin(pi / 2) + cos(0)").unwrap();
    assert!((result - 2.0).abs() < 0.0001);
}

#[test]
fn test_complex_scientific_expression() {
    // 2 * pi * sqrt(4) = 2 * pi * 2 = 4 * pi
    let result = evaluate_expression("2 * pi * sqrt(4)").unwrap();
    assert!((result - 4.0 * std::f64::consts::PI).abs() < 0.0001);
}

#[test]
fn test_logarithm_with_power() {
    // log(10 ^ 3) = log(1000) = 3
    let result = evaluate_expression("log(10 ^ 3)").unwrap();
    assert!((result - 3.0).abs() < 0.0001);
}

#[test]
fn test_natural_log_with_e() {
    // ln(e ^ 3) = 3
    let result = evaluate_expression("ln(e ^ 3)").unwrap();
    assert!((result - 3.0).abs() < 0.0001);
}

#[test]
fn test_nested_scientific_functions() {
    // sqrt(log(100)) = sqrt(2)
    let result = evaluate_expression("sqrt(log(100))").unwrap();
    assert!((result - 2.0_f64.sqrt()).abs() < 0.0001);
}

#[test]
fn test_all_features_combined() {
    // 3! + sqrt(16) * 2 ^ 3 - sin(0) + pi / e
    // = 6 + 4 * 8 - 0 + pi/e
    // = 6 + 32 + pi/e
    // = 38 + pi/e
    let result = evaluate_expression("3! + sqrt(16) * 2 ^ 3 - sin(0) + pi / e").unwrap();
    let expected = 38.0 + std::f64::consts::PI / std::f64::consts::E;
    assert!((result - expected).abs() < 0.0001);
}

#[test]
fn test_precedence_with_advanced_ops() {
    // 2 + 3! * 4 = 2 + (6 * 4) = 2 + 24 = 26
    let result = evaluate_expression("2 + 3! * 4").unwrap();
    assert_eq!(result, 26.0);
}

#[test]
fn test_multiple_factorials() {
    // 4! / 2! = 24 / 2 = 12
    let result = evaluate_expression("4! / 2!").unwrap();
    assert_eq!(result, 12.0);
}
