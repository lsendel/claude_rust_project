use anyhow::Result;
use pmatinit::calculator::evaluate_expression;
use pmatinit::cli::Cli;
use pmatinit::repl;

fn main() -> Result<()> {
    let cli = Cli::parse_args();

    if cli.is_interactive() {
        // Start interactive REPL mode
        repl::start()?;
    } else {
        // Evaluate single expression
        let expression = cli.expression.as_ref().unwrap();
        match evaluate_expression(expression) {
            Ok(result) => {
                // Format with specified precision
                println!("{:.prec$}", result, prec = cli.precision);
            }
            Err(e) => {
                eprintln!("Error: {}", e);
                std::process::exit(1);
            }
        }
    }

    Ok(())
}
