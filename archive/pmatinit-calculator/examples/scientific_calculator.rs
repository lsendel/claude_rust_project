//! Scientific Calculator Example
//!
//! This example demonstrates advanced mathematical operations:
//! - Power operator
//! - Factorial
//! - Trigonometric functions (sin, cos, tan)
//! - Mathematical constants (pi, e)
//! - Logarithmic functions (log, ln)
//! - Square root
//!
//! Run with: cargo run --example scientific_calculator

use pmatinit::calculator::Calculator;

fn main() {
    println!("=== Scientific Calculator Examples ===\n");

    let mut calc = Calculator::new();

    // Example 1: Power operator
    println!("1. Power Operator (^):");
    println!("   Expression: 2 ^ 10");
    let result = calc.evaluate("2 ^ 10").expect("Failed to evaluate");
    println!("   Result: {} (2 to the power of 10)\n", result);

    println!("   Expression: 10 ^ 2");
    let result = calc.evaluate("10 ^ 2").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: 2 ^ 3 ^ 2  (right-associative)");
    let result = calc.evaluate("2 ^ 3 ^ 2").expect("Failed to evaluate");
    println!("   Result: {} (2 ^ (3 ^ 2) = 2 ^ 9 = 512)\n", result);

    // Example 2: Factorial
    println!("2. Factorial (!):");
    println!("   Expression: 5!");
    let result = calc.evaluate("5!").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: 0!");
    let result = calc.evaluate("0!").expect("Failed to evaluate");
    println!("   Result: {} (by definition)\n", result);

    println!("   Expression: 2 ^ 3!");
    let result = calc.evaluate("2 ^ 3!").expect("Failed to evaluate");
    println!("   Result: {} (2 ^ 6 = 64)\n", result);

    // Example 3: Mathematical constants
    println!("3. Mathematical Constants:");
    println!("   Constant: pi");
    let result = calc.evaluate("pi").expect("Failed to evaluate");
    println!("   Value: {}\n", result);

    println!("   Constant: e");
    let result = calc.evaluate("e").expect("Failed to evaluate");
    println!("   Value: {}\n", result);

    println!("   Expression: 2 * pi");
    let result = calc.evaluate("2 * pi").expect("Failed to evaluate");
    println!("   Result: {} (tau)\n", result);

    // Example 4: Trigonometric functions (radians)
    println!("4. Trigonometric Functions (in radians):");
    println!("   Expression: sin(0)");
    let result = calc.evaluate("sin(0)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: sin(pi / 2)");
    let result = calc.evaluate("sin(pi / 2)").expect("Failed to evaluate");
    println!("   Result: {} (sin(90°))\n", result);

    println!("   Expression: cos(0)");
    let result = calc.evaluate("cos(0)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: cos(pi)");
    let result = calc.evaluate("cos(pi)").expect("Failed to evaluate");
    println!("   Result: {} (cos(180°))\n", result);

    println!("   Expression: tan(pi / 4)");
    let result = calc.evaluate("tan(pi / 4)").expect("Failed to evaluate");
    println!("   Result: {} (tan(45°))\n", result);

    // Example 5: Square root
    println!("5. Square Root:");
    println!("   Expression: sqrt(4)");
    let result = calc.evaluate("sqrt(4)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: sqrt(16)");
    let result = calc.evaluate("sqrt(16)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: sqrt(2)");
    let result = calc.evaluate("sqrt(2)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 6: Logarithms
    println!("6. Logarithmic Functions:");
    println!("   Expression: log(10)  (base 10)");
    let result = calc.evaluate("log(10)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: log(100)");
    let result = calc.evaluate("log(100)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: ln(e)  (natural log)");
    let result = calc.evaluate("ln(e)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: ln(1)");
    let result = calc.evaluate("ln(1)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    // Example 7: Complex expressions
    println!("7. Complex Scientific Expressions:");
    println!("   Expression: 2 * pi * sqrt(16)");
    let result = calc.evaluate("2 * pi * sqrt(16)").expect("Failed to evaluate");
    println!("   Result: {}\n", result);

    println!("   Expression: sin(pi / 2) + cos(0)");
    let result = calc.evaluate("sin(pi / 2) + cos(0)").expect("Failed to evaluate");
    println!("   Result: {} (1 + 1)\n", result);

    println!("   Expression: log(100) * sqrt(4) + 5!");
    let result = calc.evaluate("log(100) * sqrt(4) + 5!").expect("Failed to evaluate");
    println!("   Result: {} (2 * 2 + 120)\n", result);

    // Example 8: Practical calculations
    println!("8. Practical Scientific Calculations:");

    // Circle calculations
    println!("   Circle with radius = 5:");
    calc.evaluate("radius = 5").unwrap();
    println!("     radius = 5.00");

    let circumference = calc.evaluate("2 * pi * radius").unwrap();
    println!("     circumference = 2 * pi * radius = {:.2}", circumference);

    let area = calc.evaluate("pi * radius ^ 2").unwrap();
    println!("     area = pi * radius^2 = {:.2}\n", area);

    // Converting degrees to radians
    println!("   Trigonometry with degrees:");
    calc.evaluate("deg2rad = pi / 180").unwrap();
    println!("     Conversion factor: deg2rad = pi / 180");

    calc.evaluate("angle_deg = 45").unwrap();
    println!("     angle_deg = 45");

    let angle_rad = calc.evaluate("angle_deg * deg2rad").unwrap();
    println!("     angle_rad = angle_deg * deg2rad = {:.4}", angle_rad);

    let sin_45 = calc.evaluate("sin(angle_rad)").unwrap();
    println!("     sin(45°) = {:.4}\n", sin_45);

    // Pythagorean theorem
    println!("   Pythagorean theorem (3-4-5 triangle):");
    calc.evaluate("a = 3").unwrap();
    calc.evaluate("b = 4").unwrap();
    println!("     a = 3, b = 4");

    let c = calc.evaluate("sqrt(a ^ 2 + b ^ 2)").unwrap();
    println!("     c = sqrt(a^2 + b^2) = {:.2}\n", c);

    // Example 9: Error handling
    println!("9. Error Handling:");
    println!("   Expression: sqrt(-1)  (invalid argument)");
    match calc.evaluate("sqrt(-1)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    println!("   Expression: log(0)  (invalid argument)");
    match calc.evaluate("log(0)") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    println!("   Expression: 5.5!  (factorial of non-integer)");
    match calc.evaluate("5.5!") {
        Ok(result) => println!("   Result: {}", result),
        Err(e) => println!("   Error: {}\n", e),
    }

    println!("=== End of Scientific Calculator Examples ===");
}
