//! CLI argument parsing and configuration

use clap::Parser;

/// Calculator CLI - A simple command-line calculator
#[derive(Parser, Debug)]
#[command(name = "pmatinit")]
#[command(author = "Calculator Team")]
#[command(version)]
#[command(about = "A simple command-line calculator", long_about = None)]
pub struct Cli {
    /// Mathematical expression to evaluate
    ///
    /// If not provided, starts in interactive REPL mode
    #[arg(value_name = "EXPRESSION")]
    pub expression: Option<String>,

    /// Start in interactive REPL mode (default if no expression provided)
    #[arg(short, long)]
    pub interactive: bool,

    /// Number of decimal places to display
    #[arg(short, long, default_value = "2")]
    pub precision: usize,
}

impl Cli {
    /// Parses command-line arguments
    pub fn parse_args() -> Self {
        Self::parse()
    }

    /// Returns true if the calculator should run in interactive mode
    pub fn is_interactive(&self) -> bool {
        self.interactive || self.expression.is_none()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_cli_defaults() {
        // This test verifies the CLI structure is valid
        // Actual parsing tests would require command-line argument simulation
        let cli = Cli {
            expression: None,
            interactive: false,
            precision: 2,
        };
        assert!(cli.is_interactive());
    }

    #[test]
    fn test_cli_with_expression() {
        let cli = Cli {
            expression: Some("2 + 2".to_string()),
            interactive: false,
            precision: 2,
        };
        assert!(!cli.is_interactive());
    }

    #[test]
    fn test_cli_force_interactive() {
        let cli = Cli {
            expression: Some("2 + 2".to_string()),
            interactive: true,
            precision: 2,
        };
        assert!(cli.is_interactive());
    }
}
