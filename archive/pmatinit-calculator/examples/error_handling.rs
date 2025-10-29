//! Error Handling Example
//!
//! This example demonstrates comprehensive error handling:
//! - Division by zero
//! - Invalid function arguments
//! - Undefined variables
//! - Syntax errors
//! - Overflow conditions
//! - Best practices for error handling
//!
//! Run with: cargo run --example error_handling

use pmatinit::calculator::{evaluate_expression, Calculator};

fn main() {
    println!("=== Error Handling Examples ===\n");

    let mut calc = Calculator::new();

    // Example 1: Division by zero
    println!("1. Division by Zero:");
    println!("   Expression: 10 / 0");
    match calc.evaluate("10 / 0") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Expression: 5 % 0");
    match calc.evaluate("5 % 0") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    // Example 2: Invalid function arguments
    println!("2. Invalid Function Arguments:\n");

    println!("   Square root of negative number:");
    println!("   Expression: sqrt(-1)");
    match calc.evaluate("sqrt(-1)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Logarithm of zero:");
    println!("   Expression: log(0)");
    match calc.evaluate("log(0)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Logarithm of negative number:");
    println!("   Expression: ln(-5)");
    match calc.evaluate("ln(-5)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Logarithm base 10 of negative:");
    println!("   Expression: log(-10)");
    match calc.evaluate("log(-10)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    // Example 3: Factorial errors
    println!("3. Factorial Errors:\n");

    println!("   Factorial of non-integer:");
    println!("   Expression: 5.5!");
    match calc.evaluate("5.5!") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Factorial of negative number:");
    println!("   Expression: (-3)!");
    match calc.evaluate("(-3)!") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    // Example 4: Undefined variables
    println!("4. Undefined Variables:\n");

    println!("   Using undefined variable:");
    println!("   Expression: undefined_var + 5");
    match calc.evaluate("undefined_var + 5") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Complex expression with undefined variable:");
    println!("   Expression: 2 * unknown + sqrt(16)");
    match calc.evaluate("2 * unknown + sqrt(16)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    // Example 5: Syntax errors
    println!("5. Syntax Errors:\n");

    println!("   Unmatched parentheses:");
    println!("   Expression: (2 + 3");
    match evaluate_expression("(2 + 3") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Missing operand:");
    println!("   Expression: 2 +");
    match evaluate_expression("2 +") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Invalid operator sequence:");
    println!("   Expression: 2 * * 3");
    match evaluate_expression("2 * * 3") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    // Example 6: Overflow conditions
    println!("6. Overflow Conditions:\n");

    println!("   Large factorial (overflow):");
    println!("   Expression: 1000!");
    match calc.evaluate("1000!") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    println!("\n   Very large power:");
    println!("   Expression: 10 ^ 1000");
    match calc.evaluate("10 ^ 1000") {
        Ok(result) => {
            if result.is_infinite() {
                println!("   Result: {} (infinity - overflow)", result);
            } else {
                println!("   Result: {}", result);
            }
        }
        Err(e) => println!("   Error: {}", e),
    }
    println!();

    // Example 7: Proper error handling patterns
    println!("7. Proper Error Handling Patterns:\n");

    println!("   Pattern 1: Using match");
    println!("   ---");
    match calc.evaluate("10 / 2") {
        Ok(result) => println!("   Success! Result: {}", result),
        Err(e) => println!("   Failed: {}", e),
    }

    println!("\n   Pattern 2: Using if let");
    println!("   ---");
    if let Ok(result) = calc.evaluate("sqrt(16)") {
        println!("   Success! Result: {}", result);
    } else {
        println!("   Evaluation failed");
    }

    println!("\n   Pattern 3: Using unwrap_or");
    println!("   ---");
    let result = calc.evaluate("5 * 5").unwrap_or(0.0);
    println!("   Result (with default): {}", result);

    println!("\n   Pattern 4: Using unwrap_or_else with fallback");
    println!("   ---");
    let result = calc.evaluate("sqrt(-1)").unwrap_or_else(|_| {
        println!("   Error occurred, using default value");
        0.0
    });
    println!("   Result: {}", result);

    println!("\n   Pattern 5: Propagating errors with ?");
    println!("   ---");
    fn calculate_circle_area(radius: f64) -> Result<f64, anyhow::Error> {
        let mut calc = Calculator::new();
        calc.set_variable("r", radius);
        calc.evaluate("pi * r ^ 2")
    }

    match calculate_circle_area(5.0) {
        Ok(area) => println!("   Circle area: {:.2}", area),
        Err(e) => println!("   Error calculating area: {}", e),
    }
    println!();

    // Example 8: Validating input before evaluation
    println!("8. Input Validation Best Practices:\n");

    fn safe_divide(calc: &mut Calculator, a: f64, b: f64) -> Result<f64, String> {
        if b == 0.0 {
            return Err("Cannot divide by zero".to_string());
        }
        calc.set_variable("a", a);
        calc.set_variable("b", b);
        calc.evaluate("a / b").map_err(|e| e.to_string())
    }

    println!("   Safe division with validation:");
    match safe_divide(&mut calc, 10.0, 2.0) {
        Ok(result) => println!("   10 / 2 = {}", result),
        Err(e) => println!("   Error: {}", e),
    }

    match safe_divide(&mut calc, 10.0, 0.0) {
        Ok(result) => println!("   10 / 0 = {}", result),
        Err(e) => println!("   Error: {}", e),
    }
    println!();

    // Example 9: Checking for special values
    println!("9. Checking for Special Values:\n");

    let result = calc.evaluate("10 ^ 1000").unwrap_or(f64::NAN);
    println!("   Expression: 10 ^ 1000");
    if result.is_infinite() {
        println!("   Result: Infinity (overflow)");
    } else if result.is_nan() {
        println!("   Result: NaN (error occurred)");
    } else {
        println!("   Result: {}", result);
    }

    let result = calc.evaluate("0 / 0").unwrap_or(f64::NAN);
    println!("\n   Expression: 0 / 0");
    if result.is_nan() {
        println!("   Result: NaN (indeterminate form)");
    } else {
        println!("   Result: {}", result);
    }
    println!();

    // Example 10: Building robust applications
    println!("10. Building Robust Applications:\n");

    fn safe_evaluate(calc: &mut Calculator, expr: &str) -> (bool, String) {
        match calc.evaluate(expr) {
            Ok(result) => {
                if result.is_infinite() {
                    (false, "Result is infinite (overflow)".to_string())
                } else if result.is_nan() {
                    (false, "Result is not a number (NaN)".to_string())
                } else {
                    (true, format!("{:.4}", result))
                }
            }
            Err(e) => (false, format!("Error: {}", e)),
        }
    }

    let expressions = vec![
        "2 + 2",
        "10 / 0",
        "sqrt(-1)",
        "sqrt(16)",
        "undefined + 5",
        "sin(pi / 2)",
    ];

    println!("   Evaluating multiple expressions safely:");
    for expr in expressions {
        let (success, message) = safe_evaluate(&mut calc, expr);
        let status = if success { "SUCCESS" } else { "FAILED" };
        println!("   [{}] {} => {}", status, expr, message);
    }
    println!();

    // Example 11: Error recovery
    println!("11. Error Recovery:\n");

    println!("   Attempting calculation with potential errors:");
    let expressions = vec!["x = 10", "y = x / 0", "z = x + 5"];

    for expr in expressions {
        print!("   {} => ", expr);
        match calc.evaluate(expr) {
            Ok(result) => println!("Success: {}", result),
            Err(e) => {
                println!("Error: {}", e);
                println!("      Continuing with next calculation...");
            }
        }
    }

    println!("\n   Variables after error recovery:");
    let vars = calc.list_variables();
    for (name, value) in vars {
        println!("     {} = {}", name, value);
    }
    println!();

    println!("=== End of Error Handling Examples ===");
}
