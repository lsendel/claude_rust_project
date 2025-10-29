//! REPL helper module - Provides advanced REPL features via rustyline traits
//!
//! This module implements:
//! - Tab completion for functions, variables, and commands
//! - Syntax highlighting with colors
//! - Multi-line expression support (bracket matching)
//! - Input hints and suggestions

use ansi_term::Color;
use rustyline::completion::{Completer, Pair};
use rustyline::highlight::Highlighter;
use rustyline::hint::Hinter;
use rustyline::validate::{ValidationContext, ValidationResult, Validator};
use rustyline::{Context, Helper};
use std::borrow::Cow;

use crate::calculator::Calculator;

/// Available mathematical functions
const FUNCTIONS: &[&str] = &["sin", "cos", "tan", "sqrt", "log", "ln"];

/// Available mathematical constants
const CONSTANTS: &[&str] = &["pi", "e"];

/// Available REPL commands
const COMMANDS: &[&str] = &[
    "help",
    "quit",
    "exit",
    "clear",
    "cls",
    "vars",
    "list",
    "clearvars",
    "delete",
];

/// REPL helper that provides completion, highlighting, validation, and hints
#[derive(Clone)]
pub struct ReplHelper {
    /// Calculator reference for accessing variables
    calculator: Option<Calculator>,
}

impl ReplHelper {
    /// Creates a new REPL helper
    pub fn new() -> Self {
        ReplHelper { calculator: None }
    }

    /// Updates the calculator reference for variable access
    pub fn update_calculator(&mut self, calculator: &Calculator) {
        self.calculator = Some(calculator.clone());
    }

    /// Gets all variable names from the calculator
    fn get_variable_names(&self) -> Vec<String> {
        if let Some(ref calc) = self.calculator {
            calc.list_variables()
                .into_iter()
                .map(|(name, _)| name)
                .collect()
        } else {
            Vec::new()
        }
    }

    /// Gets all completion candidates (functions, constants, commands, variables)
    fn get_all_candidates(&self) -> Vec<String> {
        let mut candidates = Vec::new();

        // Add functions
        candidates.extend(FUNCTIONS.iter().map(|s| s.to_string()));

        // Add constants
        candidates.extend(CONSTANTS.iter().map(|s| s.to_string()));

        // Add commands
        candidates.extend(COMMANDS.iter().map(|s| s.to_string()));

        // Add variables
        candidates.extend(self.get_variable_names());

        candidates
    }

    /// Checks if parentheses are balanced in the input
    fn has_unmatched_parens(&self, input: &str) -> bool {
        let mut count = 0;
        for c in input.chars() {
            match c {
                '(' => count += 1,
                ')' => {
                    count -= 1;
                    if count < 0 {
                        return true; // Too many closing parens
                    }
                }
                _ => {}
            }
        }
        count != 0
    }
}

impl Default for ReplHelper {
    fn default() -> Self {
        Self::new()
    }
}

// Implement the Helper trait (marker trait that combines all others)
impl Helper for ReplHelper {}

// Implement Completer for tab completion
impl Completer for ReplHelper {
    type Candidate = Pair;

    fn complete(
        &self,
        line: &str,
        pos: usize,
        _ctx: &Context<'_>,
    ) -> rustyline::Result<(usize, Vec<Pair>)> {
        // Get the word being completed
        let word_start = line[..pos]
            .rfind(|c: char| !c.is_alphanumeric() && c != '_')
            .map(|i| i + 1)
            .unwrap_or(0);
        let prefix = &line[word_start..pos];

        if prefix.is_empty() {
            return Ok((pos, Vec::new()));
        }

        // Get all candidates
        let candidates = self.get_all_candidates();

        // Filter candidates that start with the prefix
        let matches: Vec<Pair> = candidates
            .into_iter()
            .filter(|c| c.starts_with(prefix))
            .map(|c| Pair {
                display: c.clone(),
                replacement: c,
            })
            .collect();

        Ok((word_start, matches))
    }
}

// Implement Highlighter for syntax highlighting
impl Highlighter for ReplHelper {
    fn highlight<'l>(&self, line: &'l str, _pos: usize) -> Cow<'l, str> {
        let mut result = String::new();
        let mut chars = line.chars().peekable();

        while let Some(&c) = chars.peek() {
            match c {
                // Numbers (cyan)
                '0'..='9' | '.' => {
                    let mut num_str = String::new();
                    while let Some(&ch) = chars.peek() {
                        if ch.is_ascii_digit() || ch == '.' {
                            num_str.push(ch);
                            chars.next();
                        } else {
                            break;
                        }
                    }
                    result.push_str(&Color::Cyan.paint(&num_str).to_string());
                }
                // Operators (yellow)
                '+' | '-' | '*' | '/' | '%' | '^' | '!' | '=' => {
                    result.push_str(&Color::Yellow.paint(c.to_string()).to_string());
                    chars.next();
                }
                // Parentheses (white/normal)
                '(' | ')' => {
                    result.push(c);
                    chars.next();
                }
                // Identifiers (functions, constants, variables, commands)
                'a'..='z' | 'A'..='Z' | '_' => {
                    let mut identifier = String::new();
                    while let Some(&ch) = chars.peek() {
                        if ch.is_alphanumeric() || ch == '_' {
                            identifier.push(ch);
                            chars.next();
                        } else {
                            break;
                        }
                    }

                    // Color based on type
                    let colored = if FUNCTIONS.contains(&identifier.as_str()) {
                        Color::Green.paint(&identifier).to_string()
                    } else if CONSTANTS.contains(&identifier.as_str()) {
                        Color::Purple.paint(&identifier).to_string()
                    } else if COMMANDS.contains(&identifier.as_str()) {
                        Color::Blue.paint(&identifier).to_string()
                    } else if self
                        .get_variable_names()
                        .contains(&identifier)
                    {
                        Color::Blue.paint(&identifier).to_string()
                    } else {
                        // Unknown identifier - leave normal or highlight as potential error
                        identifier.clone()
                    };

                    result.push_str(&colored);
                }
                // Whitespace and other characters
                _ => {
                    result.push(c);
                    chars.next();
                }
            }
        }

        Cow::Owned(result)
    }

    fn highlight_char(&self, _line: &str, _pos: usize, _forced: bool) -> bool {
        true
    }
}

// Implement Validator for multi-line support
impl Validator for ReplHelper {
    fn validate(&self, ctx: &mut ValidationContext) -> rustyline::Result<ValidationResult> {
        let input = ctx.input();

        // Check for unmatched parentheses
        if self.has_unmatched_parens(input) {
            Ok(ValidationResult::Incomplete)
        } else {
            Ok(ValidationResult::Valid(None))
        }
    }
}

// Implement Hinter for inline hints (currently no hints, but required for Helper)
impl Hinter for ReplHelper {
    type Hint = String;

    fn hint(&self, _line: &str, _pos: usize, _ctx: &Context<'_>) -> Option<String> {
        // Could add hints here in the future (e.g., showing last result, suggesting next command)
        None
    }
}
