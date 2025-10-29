//! Enhanced error handling with suggestions
//!
//! This module provides improved error messages with:
//! - Suggestions for similar function/variable names
//! - Contextual help based on error type
//! - Friendly error formatting

use similar::TextDiff;

/// Available functions for suggestions
const FUNCTIONS: &[&str] = &["sin", "cos", "tan", "sqrt", "log", "ln"];

/// Available constants for suggestions
const CONSTANTS: &[&str] = &["pi", "e"];

/// Finds similar strings from a list using fuzzy matching
///
/// Returns up to 3 most similar strings with similarity > 0.6
fn find_similar_strings(input: &str, candidates: &[&str]) -> Vec<String> {
    let mut similarities: Vec<(f64, &str)> = candidates
        .iter()
        .map(|&candidate| {
            // Calculate similarity ratio using Levenshtein distance
            let diff = TextDiff::from_chars(input, candidate);
            let ratio = diff.ratio() as f64;
            (ratio, candidate)
        })
        .filter(|(ratio, _)| *ratio > 0.6)
        .collect();

    similarities.sort_by(|a, b| b.0.partial_cmp(&a.0).unwrap());

    similarities
        .iter()
        .take(3)
        .map(|(_, s)| s.to_string())
        .collect()
}

/// Enhances an error message with suggestions and context
pub fn enhance_error_message(error_msg: &str, variable_names: &[String]) -> String {
    let mut enhanced = error_msg.to_string();

    // Check for unknown identifier errors
    if error_msg.starts_with("Unknown identifier:") {
        if let Some(identifier) = extract_quoted_text(error_msg) {
            // Try to find similar functions
            let similar_functions = find_similar_strings(&identifier, FUNCTIONS);
            if !similar_functions.is_empty() {
                enhanced.push_str(&format!(
                    "\n  Did you mean: {}?",
                    similar_functions.join(", ")
                ));
            }

            // Try to find similar constants
            let similar_constants = find_similar_strings(&identifier, CONSTANTS);
            if !similar_constants.is_empty() {
                enhanced.push_str(&format!(
                    "\n  Or perhaps: {}?",
                    similar_constants.join(", ")
                ));
            }

            // Suggest checking variables
            if !variable_names.is_empty() {
                enhanced.push_str("\n  Use 'vars' to list all defined variables");
            }
        }
    }

    // Check for undefined variable errors
    if error_msg.starts_with("Undefined variable:") {
        if let Some(var_name) = extract_quoted_text(error_msg) {
            // Try to find similar variable names
            let var_name_strs: Vec<&str> = variable_names.iter().map(|s| s.as_str()).collect();
            let similar_vars = find_similar_strings(&var_name, &var_name_strs);

            if !similar_vars.is_empty() {
                enhanced.push_str(&format!(
                    "\n  Did you mean: {}?",
                    similar_vars.join(", ")
                ));
            } else if !variable_names.is_empty() {
                enhanced.push_str(&format!(
                    "\n  Available variables: {}",
                    variable_names.join(", ")
                ));
            } else {
                enhanced.push_str("\n  No variables defined. Define a variable with: x = 5");
            }
        }
    }

    // Check for syntax errors
    if error_msg.contains("Mismatched parentheses") {
        enhanced.push_str("\n  Tip: Check that every '(' has a matching ')'");
    }

    // Check for division by zero
    if error_msg.contains("Division by zero") {
        enhanced.push_str("\n  Tip: Division by zero is undefined in mathematics");
    }

    // Check for invalid function arguments
    if error_msg.contains("sqrt") && error_msg.contains("negative") {
        enhanced.push_str("\n  Tip: Square root of negative numbers is not supported (complex numbers)");
    }

    if error_msg.contains("log") && error_msg.contains("non-positive") {
        enhanced.push_str("\n  Tip: Logarithm is only defined for positive numbers");
    }

    // Check for factorial errors
    if error_msg.contains("factorial") {
        if error_msg.contains("negative") {
            enhanced.push_str("\n  Tip: Factorial is only defined for non-negative integers");
        } else if error_msg.contains("non-integer") {
            enhanced.push_str("\n  Tip: Factorial requires an integer (e.g., 5! not 5.5!)");
        }
    }

    // Check for assignment errors
    if error_msg.contains("Cannot assign to reserved name") {
        enhanced.push_str("\n  Tip: You cannot redefine built-in functions or constants");
        enhanced.push_str("\n  Reserved names: pi, e, sin, cos, tan, sqrt, log, ln");
    }

    enhanced
}

/// Extracts text within single quotes from an error message
fn extract_quoted_text(msg: &str) -> Option<String> {
    let start = msg.find('\'')?;
    let end = msg[start + 1..].find('\'')?;
    Some(msg[start + 1..start + 1 + end].to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_find_similar_strings() {
        let similar = find_similar_strings("sine", FUNCTIONS);
        assert!(similar.contains(&"sin".to_string()));
    }

    #[test]
    fn test_find_similar_constants() {
        let similar = find_similar_strings("pie", CONSTANTS);
        assert!(similar.contains(&"pi".to_string()));
    }

    #[test]
    fn test_enhance_unknown_identifier() {
        let error = "Unknown identifier: 'sine'";
        let enhanced = enhance_error_message(error, &[]);
        assert!(enhanced.contains("Did you mean: sin"));
    }

    #[test]
    fn test_enhance_undefined_variable() {
        let error = "Undefined variable: 'y'";
        let variables = vec!["x".to_string(), "ans".to_string()];
        let enhanced = enhance_error_message(error, &variables);
        assert!(enhanced.contains("Available variables"));
    }

    #[test]
    fn test_extract_quoted_text() {
        let msg = "Unknown identifier: 'test'";
        assert_eq!(extract_quoted_text(msg), Some("test".to_string()));
    }
}
