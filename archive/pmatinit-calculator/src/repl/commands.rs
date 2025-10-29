//! REPL command handlers

use anyhow::Result;
use crate::calculator::Calculator;

/// Handles special REPL commands
///
/// # Arguments
///
/// * `input` - The user input string
/// * `calculator` - The calculator instance for managing variables
///
/// # Returns
///
/// - `Some(Ok(true))` if a command was handled and REPL should continue
/// - `Some(Ok(false))` if a quit command was issued
/// - `Some(Err(_))` if a command was handled but encountered an error
/// - `None` if the input is not a command and should be evaluated as an expression
pub fn handle_command(input: &str, calculator: &mut Calculator) -> Option<Result<bool>> {
    let input_lower = input.to_lowercase();

    match input_lower.as_str() {
        "help" => Some(Ok(show_help())),
        "quit" | "exit" => Some(Ok(quit())),
        "clear" | "cls" => Some(Ok(clear_screen())),
        "vars" | "list" => Some(Ok(list_variables(calculator))),
        "clearvars" => Some(Ok(clear_variables(calculator))),
        _ => {
            // Check for delete command
            if input_lower.starts_with("delete ") {
                let var_name = input[7..].trim();
                return Some(Ok(delete_variable(calculator, var_name)));
            }
            None
        }
    }
}

/// Displays help information
fn show_help() -> bool {
    println!("Calculator CLI - Available Commands:");
    println!();
    println!("  help            Show this help message");
    println!("  quit, exit      Exit the calculator");
    println!("  clear, cls      Clear the screen");
    println!("  vars, list      List all variables");
    println!("  clearvars       Clear all variables");
    println!("  delete <var>    Delete a specific variable");
    println!();
    println!("Supported Operators:");
    println!("  +   Addition");
    println!("  -   Subtraction");
    println!("  *   Multiplication");
    println!("  /   Division");
    println!("  %   Modulo");
    println!("  ^   Power");
    println!("  !   Factorial (postfix)");
    println!("  ()  Parentheses for grouping");
    println!();
    println!("Functions:");
    println!("  sin(x), cos(x), tan(x)   Trigonometric functions");
    println!("  sqrt(x)                   Square root");
    println!("  log(x), ln(x)             Logarithms");
    println!();
    println!("Constants:");
    println!("  pi   3.14159...");
    println!("  e    2.71828...");
    println!();
    println!("Variables:");
    println!("  x = 5         Assign value to variable");
    println!("  x + 3         Use variable in expression");
    println!("  ans           Last result");
    println!();
    println!("REPL Features (Phase 3):");
    println!("  TAB           Auto-complete functions, variables, commands");
    println!("  UP/DOWN       Navigate command history");
    println!("  \\             Continue expression on next line (unclosed parentheses)");
    println!("  Colors        Syntax highlighting (functions, numbers, operators)");
    println!("  History       Saved to ~/.pmatinit_history");
    println!();
    println!("Examples:");
    println!("  2 + 2");
    println!("  (10 + 5) * 3");
    println!("  x = 10");
    println!("  x ^ 2 + ans");
    println!("  sin(pi / 2)");
    println!("  (2 + 3    <- Press Enter to continue on next line");
    println!("   * 4)     <- Multi-line expression");
    println!();
    true
}

/// Exits the REPL
fn quit() -> bool {
    println!("Goodbye!");
    false
}

/// Clears the terminal screen
fn clear_screen() -> bool {
    // ANSI escape code to clear screen and move cursor to top-left
    print!("\x1B[2J\x1B[1;1H");
    true
}

/// Lists all variables
fn list_variables(calculator: &Calculator) -> bool {
    let vars = calculator.list_variables();

    if vars.is_empty() {
        println!("No variables defined.");
    } else {
        println!("Variables:");
        for (name, value) in vars {
            println!("  {} = {}", name, value);
        }
    }

    true
}

/// Clears all variables
fn clear_variables(calculator: &mut Calculator) -> bool {
    calculator.clear_variables();
    println!("All variables cleared.");
    true
}

/// Deletes a specific variable
fn delete_variable(calculator: &mut Calculator, var_name: &str) -> bool {
    if calculator.delete_variable(var_name) {
        println!("Variable '{}' deleted.", var_name);
    } else {
        println!("Variable '{}' not found.", var_name);
    }
    true
}
