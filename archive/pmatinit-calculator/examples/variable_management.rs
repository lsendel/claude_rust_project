//! Variable Management Example
//!
//! This example demonstrates comprehensive variable management:
//! - Variable assignment and retrieval
//! - Using variables in calculations
//! - The special 'ans' variable
//! - Listing, deleting, and clearing variables
//! - Building complex calculations with variables
//!
//! Run with: cargo run --example variable_management

use pmatinit::calculator::Calculator;

fn main() {
    println!("=== Variable Management Examples ===\n");

    let mut calc = Calculator::new();

    // Example 1: Basic variable assignment
    println!("1. Basic Variable Assignment:");
    calc.evaluate("x = 10").expect("Failed to assign x");
    println!("   x = 10");

    calc.evaluate("y = 20").expect("Failed to assign y");
    println!("   y = 20");

    calc.evaluate("z = 30").expect("Failed to assign z");
    println!("   z = 30\n");

    // Example 2: Using variables in calculations
    println!("2. Using Variables in Calculations:");
    let result = calc.evaluate("x + y").expect("Failed to evaluate");
    println!("   x + y = {}", result);

    let result = calc.evaluate("x * y").expect("Failed to evaluate");
    println!("   x * y = {}", result);

    let result = calc.evaluate("(x + y + z) / 3").expect("Failed to evaluate");
    println!("   (x + y + z) / 3 = {} (average)\n", result);

    // Example 3: The 'ans' variable
    println!("3. The Special 'ans' Variable:");
    calc.evaluate("5 * 5").expect("Failed to evaluate");
    println!("   5 * 5 = 25 (stored in 'ans')");

    let result = calc.evaluate("ans + 10").expect("Failed to evaluate");
    println!("   ans + 10 = {}", result);

    let result = calc.evaluate("ans / 5").expect("Failed to evaluate");
    println!("   ans / 5 = {}", result);

    let result = calc.evaluate("sqrt(ans)").expect("Failed to evaluate");
    println!("   sqrt(ans) = {:.4}\n", result);

    // Example 4: Variables with expressions
    println!("4. Assigning Expressions to Variables:");
    calc.evaluate("radius = 7").expect("Failed to assign radius");
    println!("   radius = 7");

    calc.evaluate("area = pi * radius ^ 2").expect("Failed to assign area");
    println!("   area = pi * radius ^ 2");

    let area = calc.get_variable("area").expect("Failed to get area");
    println!("   area value: {:.2}", area);

    calc.evaluate("circumference = 2 * pi * radius").expect("Failed to assign circumference");
    println!("   circumference = 2 * pi * radius");

    let circumference = calc.get_variable("circumference").expect("Failed to get circumference");
    println!("   circumference value: {:.2}\n", circumference);

    // Example 5: Listing variables
    println!("5. Listing All Variables:");
    let vars = calc.list_variables();
    println!("   Current variables:");
    for (name, value) in &vars {
        println!("     {} = {:.4}", name, value);
    }
    println!("   Total: {} variables\n", vars.len());

    // Example 6: Getting specific variables
    println!("6. Getting Specific Variables:");
    if let Some(value) = calc.get_variable("x") {
        println!("   x = {}", value);
    }

    if let Some(value) = calc.get_variable("radius") {
        println!("   radius = {}", value);
    }

    match calc.get_variable("nonexistent") {
        Some(value) => println!("   nonexistent = {}", value),
        None => println!("   Variable 'nonexistent' not found"),
    }
    println!();

    // Example 7: Setting variables directly
    println!("7. Setting Variables Programmatically:");
    calc.set_variable("temp_celsius", 25.0);
    println!("   Set temp_celsius = 25.0");

    calc.set_variable("temp_fahrenheit", 77.0);
    println!("   Set temp_fahrenheit = 77.0");

    let result = calc.evaluate("temp_celsius * 9 / 5 + 32").expect("Failed to evaluate");
    println!("   temp_celsius * 9 / 5 + 32 = {:.2} (celsius to fahrenheit)\n", result);

    // Example 8: Deleting variables
    println!("8. Deleting Specific Variables:");
    println!("   Before deletion:");
    let vars = calc.list_variables();
    println!("     Total variables: {}", vars.len());

    let deleted = calc.delete_variable("temp_celsius");
    if deleted {
        println!("   Deleted 'temp_celsius'");
    }

    let deleted = calc.delete_variable("temp_fahrenheit");
    if deleted {
        println!("   Deleted 'temp_fahrenheit'");
    }

    let deleted = calc.delete_variable("nonexistent");
    if deleted {
        println!("   Deleted 'nonexistent'");
    } else {
        println!("   Failed to delete 'nonexistent' (doesn't exist)");
    }

    println!("   After deletion:");
    let vars = calc.list_variables();
    println!("     Total variables: {}\n", vars.len());

    // Example 9: Building complex calculations step by step
    println!("9. Complex Calculation with Variables:");
    println!("   Solving quadratic equation: ax^2 + bx + c = 0");
    println!("   Using the quadratic formula: x = (-b ± sqrt(b^2 - 4ac)) / 2a");
    println!();

    // Equation: 2x^2 + 5x - 3 = 0
    calc.evaluate("a = 2").unwrap();
    println!("   a = 2");

    calc.evaluate("b = 5").unwrap();
    println!("   b = 5");

    calc.evaluate("c = -3").unwrap();
    println!("   c = -3");

    calc.evaluate("discriminant = b ^ 2 - 4 * a * c").unwrap();
    let disc = calc.get_variable("discriminant").unwrap();
    println!("   discriminant = b^2 - 4ac = {:.2}", disc);

    calc.evaluate("x1 = (-b + sqrt(discriminant)) / (2 * a)").unwrap();
    let x1 = calc.get_variable("x1").unwrap();
    println!("   x1 = (-b + sqrt(discriminant)) / 2a = {:.2}", x1);

    calc.evaluate("x2 = (-b - sqrt(discriminant)) / (2 * a)").unwrap();
    let x2 = calc.get_variable("x2").unwrap();
    println!("   x2 = (-b - sqrt(discriminant)) / 2a = {:.2}", x2);

    println!("\n   Solutions: x1 = {:.2}, x2 = {:.2}\n", x1, x2);

    // Example 10: Clearing all variables
    println!("10. Clearing All Variables:");
    println!("    Before clearing:");
    let vars = calc.list_variables();
    println!("      Total variables: {}", vars.len());
    for (name, value) in vars.iter().take(5) {
        println!("        {} = {:.4}", name, value);
    }
    if vars.len() > 5 {
        println!("        ... and {} more", vars.len() - 5);
    }

    calc.clear_variables();
    println!("\n    After clearing:");
    let vars = calc.list_variables();
    if vars.is_empty() {
        println!("      All variables cleared successfully");
    } else {
        println!("      Remaining variables: {}", vars.len());
    }
    println!();

    // Example 11: Variable scopes and persistence
    println!("11. Variables Persist Across Calculations:");
    calc.evaluate("count = 0").unwrap();
    println!("    count = 0");

    for i in 1..=5 {
        calc.evaluate("count = count + 1").unwrap();
        let count = calc.get_variable("count").unwrap();
        println!("    Iteration {}: count = {}", i, count);
    }
    println!();

    // Example 12: Using variables with scientific functions
    println!("12. Variables with Scientific Functions:");
    calc.evaluate("angle = pi / 4").unwrap();
    println!("    angle = pi / 4 (45 degrees in radians)");

    let sin_val = calc.evaluate("sin(angle)").unwrap();
    println!("    sin(angle) = {:.4}", sin_val);

    let cos_val = calc.evaluate("cos(angle)").unwrap();
    println!("    cos(angle) = {:.4}", cos_val);

    let tan_val = calc.evaluate("tan(angle)").unwrap();
    println!("    tan(angle) = {:.4}", tan_val);

    let identity = calc.evaluate("sin(angle) ^ 2 + cos(angle) ^ 2").unwrap();
    println!("    sin^2(angle) + cos^2(angle) = {:.4} (trigonometric identity)\n", identity);

    // Example 13: Error handling with variables
    println!("13. Error Handling:");
    println!("    Trying to use undefined variable:");
    match calc.evaluate("undefined_var + 5") {
        Ok(result) => println!("      Result: {}", result),
        Err(e) => println!("      Error: {}", e),
    }
    println!();

    println!("=== End of Variable Management Examples ===");
}
