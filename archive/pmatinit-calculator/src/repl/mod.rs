//! REPL (Read-Eval-Print Loop) module for interactive calculator sessions
//!
//! Phase 3 enhancements:
//! - History persistence across sessions
//! - Tab completion for functions, variables, and commands
//! - Syntax highlighting with colors
//! - Multi-line expression support (bracket matching)
//! - Enhanced error messages with suggestions

pub mod commands;
pub mod errors;
pub mod helper;

use anyhow::Result;
use rustyline::error::ReadlineError;
use rustyline::history::FileHistory;
use rustyline::Editor;
use std::path::PathBuf;

use crate::calculator::Calculator;
use helper::ReplHelper;

/// Gets the path to the history file
///
/// Uses ~/.pmatinit_history or falls back to .pmatinit_history in current directory
fn get_history_path() -> PathBuf {
    if let Some(home) = dirs::home_dir() {
        home.join(".pmatinit_history")
    } else {
        PathBuf::from(".pmatinit_history")
    }
}

/// Starts the interactive REPL
///
/// This provides an interactive prompt where users can enter mathematical
/// expressions and see the results immediately. Maintains state for variables.
///
/// Phase 3 features:
/// - Persistent command history
/// - Tab completion (press TAB)
/// - Syntax highlighting
/// - Multi-line support (unclosed parentheses)
/// - Enhanced error messages
pub fn start() -> Result<()> {
    // Create editor with helper
    let helper = ReplHelper::new();
    let mut editor: Editor<ReplHelper, FileHistory> = Editor::new()?;
    editor.set_helper(Some(helper));

    // Load history from file
    let history_path = get_history_path();
    if history_path.exists() {
        let _ = editor.load_history(&history_path);
    }

    let mut calculator = Calculator::new();

    println!("Calculator v{}", env!("CARGO_PKG_VERSION"));
    println!("Type 'help' for commands, 'quit' to exit");
    println!("Features: Tab completion, syntax highlighting, history (↑/↓)");
    println!();

    loop {
        // Update helper with current calculator state for variable completion
        if let Some(helper) = editor.helper_mut() {
            helper.update_calculator(&calculator);
        }

        let readline = editor.readline("calc> ");
        match readline {
            Ok(line) => {
                let line = line.trim();

                // Skip empty lines
                if line.is_empty() {
                    continue;
                }

                // Add to history
                let _ = editor.add_history_entry(line);

                // Check for REPL commands
                if let Some(result) = commands::handle_command(line, &mut calculator) {
                    match result {
                        Ok(should_continue) => {
                            if !should_continue {
                                break;
                            }
                        }
                        Err(e) => {
                            eprintln!("Error: {}", e);
                        }
                    }
                    continue;
                }

                // Evaluate mathematical expression
                match calculator.evaluate(line) {
                    Ok(result) => {
                        println!("{}", result);
                    }
                    Err(e) => {
                        // Enhanced error messages with suggestions
                        let var_names: Vec<String> = calculator
                            .list_variables()
                            .into_iter()
                            .map(|(name, _)| name)
                            .collect();
                        let enhanced_error = errors::enhance_error_message(&e.to_string(), &var_names);
                        eprintln!("Error: {}", enhanced_error);
                    }
                }
            }
            Err(ReadlineError::Interrupted) => {
                // Ctrl-C
                println!("^C");
                continue;
            }
            Err(ReadlineError::Eof) => {
                // Ctrl-D
                println!("Goodbye!");
                break;
            }
            Err(err) => {
                eprintln!("Error reading line: {}", err);
                break;
            }
        }
    }

    // Save history on exit
    let _ = editor.save_history(&history_path);

    Ok(())
}
