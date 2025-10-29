//! Comprehensive calculator tests
//!
//! These tests focus on various edge cases and calculator-specific behavior.

use pmatinit::calculator::{evaluate_expression, Calculator};

#[test]
fn test_associativity_left() {
    // Left associative: 10 - 5 - 2 = (10 - 5) - 2 = 3
    let result = evaluate_expression("10 - 5 - 2").unwrap();
    assert_eq!(result, 3.0);
}

#[test]
fn test_chained_operations() {
    // 2 + 3 + 4 + 5 = 14
    let result = evaluate_expression("2 + 3 + 4 + 5").unwrap();
    assert_eq!(result, 14.0);
}

#[test]
fn test_nested_parentheses() {
    // ((2 + 3) * (4 + 5)) = (5 * 9) = 45
    let result = evaluate_expression("((2 + 3) * (4 + 5))").unwrap();
    assert_eq!(result, 45.0);
}

#[test]
fn test_all_operators() {
    // 10 + 5 - 3 * 2 / 2 % 4 = 10 + 5 - ((3 * 2) / 2) % 4 = 10 + 5 - 3 % 4 = 10 + 5 - 3 = 12
    let result = evaluate_expression("10 + 5 - 3 * 2 / 2 % 4").unwrap();
    assert_eq!(result, 12.0);
}

#[test]
fn test_negative_result() {
    let result = evaluate_expression("5 - 10").unwrap();
    assert_eq!(result, -5.0);
}

#[test]
fn test_large_numbers() {
    let result = evaluate_expression("1000000 + 2000000").unwrap();
    assert_eq!(result, 3000000.0);
}

#[test]
fn test_very_small_decimals() {
    let result = evaluate_expression("0.001 + 0.002").unwrap();
    assert!((result - 0.003).abs() < 0.0001);
}

#[test]
fn test_multiplication_by_zero() {
    let result = evaluate_expression("100 * 0").unwrap();
    assert_eq!(result, 0.0);
}

#[test]
fn test_division_resulting_in_decimal() {
    let result = evaluate_expression("5 / 2").unwrap();
    assert_eq!(result, 2.5);
}

#[test]
fn test_multiple_nested_parentheses() {
    // (((2 + 3) * 4) - 5) / 3 = ((5 * 4) - 5) / 3 = (20 - 5) / 3 = 15 / 3 = 5
    let result = evaluate_expression("(((2 + 3) * 4) - 5) / 3").unwrap();
    assert_eq!(result, 5.0);
}

#[test]
fn test_expression_starting_with_parentheses() {
    let result = evaluate_expression("(2 + 3) + (4 * 5)").unwrap();
    assert_eq!(result, 25.0);
}

#[test]
fn test_expression_ending_with_parentheses() {
    let result = evaluate_expression("10 - (3 + 2)").unwrap();
    assert_eq!(result, 5.0);
}

#[test]
fn test_modulo_with_decimals() {
    let result = evaluate_expression("5.5 % 2.0").unwrap();
    assert!((result - 1.5).abs() < 0.001);
}

#[test]
fn test_negative_in_parentheses() {
    let result = evaluate_expression("10 + (-5)").unwrap();
    assert_eq!(result, 5.0);
}

#[test]
fn test_multiple_multiplications() {
    let result = evaluate_expression("2 * 3 * 4").unwrap();
    assert_eq!(result, 24.0);
}

#[test]
fn test_multiple_divisions() {
    let result = evaluate_expression("100 / 5 / 2").unwrap();
    assert_eq!(result, 10.0);
}

#[test]
fn test_mixed_precedence_complex() {
    // 2 + 3 * 4 - 5 / 5 + 6 = 2 + 12 - 1 + 6 = 19
    let result = evaluate_expression("2 + 3 * 4 - 5 / 5 + 6").unwrap();
    assert_eq!(result, 19.0);
}

#[test]
fn test_invalid_character_error() {
    let result = evaluate_expression("2 + $3");
    assert!(result.is_err());
}

#[test]
fn test_multiple_decimal_points_error() {
    let result = evaluate_expression("2.5.3 + 1");
    assert!(result.is_err());
}

// ============================================================================
// Phase 2 Tests: Variables and Memory
// ============================================================================

#[test]
fn test_variable_assignment_simple() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("x = 5").unwrap();
    assert_eq!(result, 5.0);
    assert_eq!(calc.get_variable("x"), Some(5.0));
}

#[test]
fn test_variable_usage() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 10").unwrap();
    let result = calc.evaluate("x + 5").unwrap();
    assert_eq!(result, 15.0);
}

#[test]
fn test_variable_in_complex_expression() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 3").unwrap();
    calc.evaluate("y = 4").unwrap();
    let result = calc.evaluate("x * y + 2").unwrap();
    assert_eq!(result, 14.0);
}

#[test]
fn test_variable_reassignment() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    assert_eq!(calc.get_variable("x"), Some(5.0));

    calc.evaluate("x = 10").unwrap();
    assert_eq!(calc.get_variable("x"), Some(10.0));
}

#[test]
fn test_variable_with_expression() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("x = 2 + 3 * 4").unwrap();
    assert_eq!(result, 14.0);
    assert_eq!(calc.get_variable("x"), Some(14.0));
}

#[test]
fn test_ans_variable_simple() {
    let mut calc = Calculator::new();
    calc.evaluate("2 + 3").unwrap();
    let result = calc.evaluate("ans * 2").unwrap();
    assert_eq!(result, 10.0);
}

#[test]
fn test_ans_variable_updates() {
    let mut calc = Calculator::new();
    calc.evaluate("5").unwrap();
    assert_eq!(calc.get_variable("ans"), Some(5.0));

    calc.evaluate("10").unwrap();
    assert_eq!(calc.get_variable("ans"), Some(10.0));

    calc.evaluate("ans + 5").unwrap();
    assert_eq!(calc.get_variable("ans"), Some(15.0));
}

#[test]
fn test_ans_with_assignment() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 10").unwrap();
    assert_eq!(calc.get_variable("ans"), Some(10.0));

    calc.evaluate("ans + 5").unwrap();
    assert_eq!(calc.get_variable("ans"), Some(15.0));
}

#[test]
fn test_variable_with_functions() {
    let mut calc = Calculator::new();
    calc.evaluate("x = pi").unwrap();
    let result = calc.evaluate("sin(x / 2)").unwrap();
    assert!((result - 1.0).abs() < 0.0001);
}

#[test]
fn test_variable_with_power() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 2").unwrap();
    let result = calc.evaluate("x ^ 10").unwrap();
    assert_eq!(result, 1024.0);
}

#[test]
fn test_multiple_variables() {
    let mut calc = Calculator::new();
    calc.evaluate("a = 1").unwrap();
    calc.evaluate("b = 2").unwrap();
    calc.evaluate("c = 3").unwrap();
    let result = calc.evaluate("a + b + c").unwrap();
    assert_eq!(result, 6.0);
}

#[test]
fn test_variable_names_with_underscores() {
    let mut calc = Calculator::new();
    calc.evaluate("my_var = 42").unwrap();
    let result = calc.evaluate("my_var * 2").unwrap();
    assert_eq!(result, 84.0);
}

#[test]
fn test_variable_names_with_numbers() {
    let mut calc = Calculator::new();
    calc.evaluate("var1 = 10").unwrap();
    calc.evaluate("var2 = 20").unwrap();
    let result = calc.evaluate("var1 + var2").unwrap();
    assert_eq!(result, 30.0);
}

#[test]
fn test_undefined_variable_error() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("x + 5");
    assert!(result.is_err());
    assert!(result.unwrap_err().to_string().contains("Undefined variable"));
}

#[test]
fn test_invalid_variable_name_error() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("2x = 5");
    assert!(result.is_err());
}

#[test]
fn test_reserved_name_assignment_error() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("pi = 5");
    assert!(result.is_err());
    assert!(result.unwrap_err().to_string().contains("reserved name"));
}

#[test]
fn test_empty_assignment_error() {
    let mut calc = Calculator::new();
    let result = calc.evaluate("x =");
    assert!(result.is_err());
}

#[test]
fn test_calculator_clear_variables() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    calc.evaluate("y = 10").unwrap();

    calc.clear_variables();

    let result = calc.evaluate("x + y");
    assert!(result.is_err());
}

#[test]
fn test_calculator_delete_variable() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    calc.evaluate("y = 10").unwrap();

    assert!(calc.delete_variable("x"));
    assert_eq!(calc.get_variable("x"), None);
    assert_eq!(calc.get_variable("y"), Some(10.0));

    assert!(!calc.delete_variable("x")); // Already deleted
}

#[test]
fn test_calculator_list_variables() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    calc.evaluate("y = 10").unwrap();
    calc.evaluate("z = 15").unwrap();

    let vars = calc.list_variables();
    assert!(vars.len() >= 3); // At least x, y, z (might have ans)

    // Check that variables are present
    assert!(vars.iter().any(|(name, val)| name == "x" && *val == 5.0));
    assert!(vars.iter().any(|(name, val)| name == "y" && *val == 10.0));
    assert!(vars.iter().any(|(name, val)| name == "z" && *val == 15.0));
}

#[test]
fn test_calculator_set_variable_directly() {
    let mut calc = Calculator::new();
    calc.set_variable("x", 42.0);
    let result = calc.evaluate("x * 2").unwrap();
    assert_eq!(result, 84.0);
}

#[test]
fn test_variable_in_nested_expression() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    let result = calc.evaluate("((x + 3) * 2) - 1").unwrap();
    assert_eq!(result, 15.0);
}

#[test]
fn test_chained_variable_usage() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 2").unwrap();
    calc.evaluate("y = x * 3").unwrap();
    calc.evaluate("z = y + x").unwrap();
    assert_eq!(calc.get_variable("z"), Some(8.0));
}

#[test]
fn test_ans_in_assignment() {
    let mut calc = Calculator::new();
    calc.evaluate("5 + 5").unwrap();
    calc.evaluate("x = ans * 2").unwrap();
    assert_eq!(calc.get_variable("x"), Some(20.0));
}

#[test]
fn test_variables_with_factorial() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 5").unwrap();
    let result = calc.evaluate("x!").unwrap();
    assert_eq!(result, 120.0);
}

#[test]
fn test_variables_persist_across_evaluations() {
    let mut calc = Calculator::new();
    calc.evaluate("x = 10").unwrap();
    calc.evaluate("y = 20").unwrap();

    // Do some other calculations
    calc.evaluate("5 + 5").unwrap();
    calc.evaluate("100 / 2").unwrap();

    // Variables should still be there
    let result = calc.evaluate("x + y").unwrap();
    assert_eq!(result, 30.0);
}
