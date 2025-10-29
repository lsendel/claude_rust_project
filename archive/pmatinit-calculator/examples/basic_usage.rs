//! Basic Usage Example
//!
//! This example demonstrates the fundamental features of pmatinit:
//! - Basic arithmetic operations
//! - Using the Calculator struct
//! - Variable assignment and usage
//! - The special 'ans' variable
//!
//! Run with: cargo run --example basic_usage

use pmatinit::calculator::{evaluate_expression, Calculator};

fn main() {
    println!("=== pmatinit Basic Usage Examples ===\n");

    // Example 1: Stateless evaluation (no variables)
    println!("1. Stateless Evaluation (using evaluate_expression):");
    println!("   Expression: 2 + 2");
    let result = evaluate_expression("2 + 2").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: (10 + 5) * 3");
    let result = evaluate_expression("(10 + 5) * 3").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: 17 % 5");
    let result = evaluate_expression("17 % 5").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 2: Using Calculator with variables
    println!("2. Stateful Calculator (using Calculator struct):");
    let mut calc = Calculator::new();

    // Basic calculation
    println!("   Expression: 5 * 5");
    let result = calc.evaluate("5 * 5").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Variable assignment
    println!("   Expression: x = 10");
    let result = calc.evaluate("x = 10").expect("Failed to evaluate");
    println!("   Result: {} (stored in variable 'x')\n", result);

    println!("   Expression: y = 20");
    let result = calc.evaluate("y = 20").expect("Failed to evaluate");
    println!("   Result: {} (stored in variable 'y')\n", result);

    // Using variables
    println!("   Expression: x + y");
    let result = calc.evaluate("x + y").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // The 'ans' variable
    println!("3. The 'ans' Variable:");
    println!("   Expression: 7 * 8");
    calc.evaluate("7 * 8").expect("Failed to evaluate");
    println!("   Result: 56 (automatically stored in 'ans')\n");

    println!("   Expression: ans + 10");
    let result = calc.evaluate("ans + 10").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: ans / 2");
    let result = calc.evaluate("ans / 2").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 4: Negative numbers
    println!("4. Negative Numbers:");
    println!("   Expression: -5 + 3");
    let result = calc.evaluate("-5 + 3").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: -10 * -2");
    let result = calc.evaluate("-10 * -2").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 5: Operator precedence
    println!("5. Operator Precedence:");
    println!("   Expression: 2 + 3 * 4  (multiplication first)");
    let result = calc.evaluate("2 + 3 * 4").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: (2 + 3) * 4  (parentheses override)");
    let result = calc.evaluate("(2 + 3) * 4").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 6: Variable management
    println!("6. Variable Management:");
    let vars = calc.list_variables();
    println!("   Current variables:");
    for (name, value) in vars {
        println!("     {} = {}", name, value);
    }
    println!();

    println!("   Deleting variable 'x'...");
    calc.delete_variable("x");

    println!("   Setting 'z' to 100 using set_variable()...");
    calc.set_variable("z", 100.0);

    let vars = calc.list_variables();
    println!("   Updated variables:");
    for (name, value) in vars {
        println!("     {} = {}", name, value);
    }
    println!();

    println!("   Clearing all variables...");
    calc.clear_variables();

    let vars = calc.list_variables();
    if vars.is_empty() {
        println!("   All variables cleared successfully.\n");
    }

    // Example 7: Error handling
    println!("7. Error Handling:");
    println!("   Expression: 10 / 0  (division by zero)");
    match calc.evaluate("10 / 0") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    println!("   Expression: unknown_var + 5  (undefined variable)");
    match calc.evaluate("unknown_var + 5") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    println!("=== End of Basic Usage Examples ===");
}
