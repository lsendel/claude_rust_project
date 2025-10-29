# Project Context

**Language**: typescript
**Project Path**: .

## Project Structure

- **Total Files**: 42
- **Total Functions**: 339
- **Median Cyclomatic**: 1.00
- **Median Cognitive**: 1.00

## Quality Scorecard

- **Overall Health**: 68.3%
- **Maintainability Index**: 70.0
- **Complexity Score**: 50.0
- **Test Coverage**: 65.0%

## Files

### ./archive/pmatinit-calculator/examples/basic_usage.rs

**File Complexity**: 6 | **Functions**: 1

- **Function**: `main` [complexity: 6] [cognitive: 5] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/examples/error_handling.rs

**File Complexity**: 34 | **Functions**: 1

- **Function**: `main` [complexity: 34] [cognitive: 40] [big-o: O(?)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `calculate_circle_area` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `safe_divide` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `safe_evaluate` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/examples/scientific_calculator.rs

**File Complexity**: 4 | **Functions**: 1

- **Function**: `main` [complexity: 4] [cognitive: 3] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/examples/variable_management.rs

**File Complexity**: 13 | **Functions**: 1

- **Function**: `main` [complexity: 13] [cognitive: 12] [big-o: O(n log n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/calculator/evaluator.rs

**File Complexity**: 21 | **Functions**: 1

- **Function**: `evaluate_postfix` [complexity: 21] [cognitive: 58] [big-o: O(n²)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_simple_addition` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_precedence` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_with_parentheses` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_division` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_division_by_zero` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_negative_numbers` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_evaluate_complex_expression` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/calculator/mod.rs

**File Complexity**: 4 | **Functions**: 1

- **Struct**: `Calculator` [fields: 1]
- **Impl**: `impl Calculator { # [doc = " Creates a new Calculator instance with empty variable storage"] pub fn new () -> Self { Calculator { variables : HashMap :: new () , } } # [doc = " Evaluates an expression with variable support"] # [doc = ""] # [doc = " Supports:"] # [doc = " - Variable assignment: `x = 5`"] # [doc = " - Variable usage: `x + 3`"] # [doc = " - Special `ans` variable (last result)"] # [doc = ""] # [doc = " # Arguments"] # [doc = ""] # [doc = " * `expression` - The expression string to evaluate"] # [doc = ""] # [doc = " # Returns"] # [doc = ""] # [doc = " The calculated result, also stored in `ans`"] # [doc = ""] # [doc = " # Examples"] # [doc = ""] # [doc = " ```"] # [doc = " use pmatinit::calculator::Calculator;"] # [doc = ""] # [doc = " let mut calc = Calculator::new();"] # [doc = ""] # [doc = " // Simple calculation"] # [doc = " let result = calc.evaluate(\"2 + 3\").unwrap();"] # [doc = " assert_eq!(result, 5.0);"] # [doc = ""] # [doc = " // Variable assignment"] # [doc = " let result = calc.evaluate(\"x = 10\").unwrap();"] # [doc = " assert_eq!(result, 10.0);"] # [doc = ""] # [doc = " // Variable usage"] # [doc = " let result = calc.evaluate(\"x * 2\").unwrap();"] # [doc = " assert_eq!(result, 20.0);"] # [doc = ""] # [doc = " // ans variable"] # [doc = " let result = calc.evaluate(\"ans + 5\").unwrap();"] # [doc = " assert_eq!(result, 25.0);"] # [doc = " ```"] pub fn evaluate (& mut self , expression : & str) -> Result < f64 > { if let Some ((var_name , value_expr)) = parser :: parse_assignment (expression) ? { let value = self . evaluate_internal (value_expr) ? ; self . variables . insert (var_name . to_string () , value) ; self . variables . insert ("ans" . to_string () , value) ; return Ok (value) ; } let result = self . evaluate_internal (expression) ? ; self . variables . insert ("ans" . to_string () , result) ; Ok (result) } # [doc = " Internal evaluation without updating ans (used for recursive calls)"] fn evaluate_internal (& self , expression : & str) -> Result < f64 > { let tokens = parser :: tokenize_with_variables (expression , & self . variables) ? ; let postfix = parser :: infix_to_postfix (tokens) ? ; let result = evaluator :: evaluate_postfix (postfix) ? ; Ok (result) } # [doc = " Gets the value of a variable"] pub fn get_variable (& self , name : & str) -> Option < f64 > { self . variables . get (name) . copied () } # [doc = " Sets a variable value directly"] pub fn set_variable (& mut self , name : & str , value : f64) { self . variables . insert (name . to_string () , value) ; } # [doc = " Deletes a variable"] pub fn delete_variable (& mut self , name : & str) -> bool { self . variables . remove (name) . is_some () } # [doc = " Clears all variables"] pub fn clear_variables (& mut self) { self . variables . clear () ; } # [doc = " Lists all variables"] pub fn list_variables (& self) -> Vec < (String , f64) > { let mut vars : Vec < _ > = self . variables . iter () . map (| (k , v) | (k . clone () , * v)) . collect () ; vars . sort_by (| a , b | a . 0 . cmp (& b . 0)) ; vars } } . self_ty`
- **Impl**: `Default` for `impl Default for Calculator { fn default () -> Self { Self :: new () } } . self_ty`
- **Function**: `evaluate_expression` [complexity: 4] [cognitive: 3] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/calculator/operators.rs

**File Complexity**: 1 | **Functions**: 0

- **Enum**: `Operator` [variants: 6]
- **Impl**: `impl Operator { # [doc = " Returns the precedence level of the operator"] # [doc = ""] # [doc = " Higher numbers indicate higher precedence."] # [doc = " Follows standard mathematical order of operations (PEMDAS)."] pub fn precedence (& self) -> u8 { match self { Operator :: Add | Operator :: Subtract => 1 , Operator :: Multiply | Operator :: Divide | Operator :: Modulo => 2 , Operator :: Power => 3 , } } # [doc = " Returns the associativity of the operator"] # [doc = ""] # [doc = " Most operators are left-associative, but Power is right-associative."] pub fn is_left_associative (& self) -> bool { match self { Operator :: Power => false , _ => true , } } # [doc = " Converts a character to an operator if valid"] pub fn from_char (c : char) -> Option < Self > { match c { '+' => Some (Operator :: Add) , '-' => Some (Operator :: Subtract) , '*' => Some (Operator :: Multiply) , '/' => Some (Operator :: Divide) , '%' => Some (Operator :: Modulo) , '^' => Some (Operator :: Power) , _ => None , } } # [doc = " Applies the operator to two operands"] # [doc = ""] # [doc = " # Arguments"] # [doc = ""] # [doc = " * `left` - The left operand"] # [doc = " * `right` - The right operand"] # [doc = ""] # [doc = " # Returns"] # [doc = ""] # [doc = " The result of applying the operator"] pub fn apply (& self , left : f64 , right : f64) -> anyhow :: Result < f64 > { match self { Operator :: Add => Ok (left + right) , Operator :: Subtract => Ok (left - right) , Operator :: Multiply => Ok (left * right) , Operator :: Divide => { if right == 0.0 { anyhow :: bail ! ("Division by zero") } Ok (left / right) } Operator :: Modulo => { if right == 0.0 { anyhow :: bail ! ("Modulo by zero") } Ok (left % right) } Operator :: Power => { let result = left . powf (right) ; if result . is_infinite () || result . is_nan () { anyhow :: bail ! ("Power operation resulted in invalid value") } Ok (result) } } } } . self_ty`
- **Function**: `tests::test_operator_precedence` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_operator_from_char` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_operator_apply` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_division_by_zero` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_operator` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_precedence` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_associativity` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_from_char` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/calculator/parser.rs

**File Complexity**: 8 | **Functions**: 7

- **Enum**: `Token` [variants: 8]
- **Function**: `tokenize` [complexity: 11] [cognitive: 29] [big-o: O(n log n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `parse_number_string` [complexity: 6] [cognitive: 11] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `parse_identifier` [complexity: 4] [cognitive: 8] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `infix_to_postfix` [complexity: 13] [cognitive: 44] [big-o: O(n log n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `parse_assignment` [complexity: 6] [cognitive: 11] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `is_valid_variable_name` [complexity: 4] [cognitive: 7] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tokenize_with_variables` [complexity: 12] [cognitive: 33] [big-o: O(n log n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_tokenize_simple` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_tokenize_with_parentheses` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_tokenize_negative_number` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_tokenize_decimal` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_infix_to_postfix_simple` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_infix_to_postfix_precedence` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_tokenize_power_operator` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_precedence_over_multiply` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_power_right_associativity` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/cli/mod.rs

**File Complexity**: 1 | **Functions**: 0

- **Struct**: `Cli` [fields: 3]
- **Impl**: `impl Cli { # [doc = " Parses command-line arguments"] pub fn parse_args () -> Self { Self :: parse () } # [doc = " Returns true if the calculator should run in interactive mode"] pub fn is_interactive (& self) -> bool { self . interactive || self . expression . is_none () } } . self_ty`
- **Function**: `tests::test_cli_defaults` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_cli_with_expression` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_cli_force_interactive` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/lib.rs

**File Complexity**: 1 | **Functions**: 0


### ./archive/pmatinit-calculator/src/main.rs

**File Complexity**: 4 | **Functions**: 1

- **Function**: `main` [complexity: 4] [cognitive: 5] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/repl/commands.rs

**File Complexity**: 1 | **Functions**: 7

- **Function**: `handle_command` [complexity: 3] [cognitive: 6] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `show_help` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `quit` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `clear_screen` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `list_variables` [complexity: 3] [cognitive: 3] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `clear_variables` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `delete_variable` [complexity: 2] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/repl/errors.rs

**File Complexity**: 8 | **Functions**: 3

- **Function**: `find_similar_strings` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `enhance_error_message` [complexity: 20] [cognitive: 33] [big-o: O(n²)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `extract_quoted_text` [complexity: 3] [cognitive: 2] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_find_similar_strings` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_find_similar_constants` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_enhance_unknown_identifier` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_enhance_undefined_variable` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tests::test_extract_quoted_text` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/src/repl/helper.rs

**File Complexity**: 1 | **Functions**: 0

- **Struct**: `ReplHelper` [fields: 1]
- **Impl**: `impl ReplHelper { # [doc = " Creates a new REPL helper"] pub fn new () -> Self { ReplHelper { calculator : None } } # [doc = " Updates the calculator reference for variable access"] pub fn update_calculator (& mut self , calculator : & Calculator) { self . calculator = Some (calculator . clone ()) ; } # [doc = " Gets all variable names from the calculator"] fn get_variable_names (& self) -> Vec < String > { if let Some (ref calc) = self . calculator { calc . list_variables () . into_iter () . map (| (name , _) | name) . collect () } else { Vec :: new () } } # [doc = " Gets all completion candidates (functions, constants, commands, variables)"] fn get_all_candidates (& self) -> Vec < String > { let mut candidates = Vec :: new () ; candidates . extend (FUNCTIONS . iter () . map (| s | s . to_string ())) ; candidates . extend (CONSTANTS . iter () . map (| s | s . to_string ())) ; candidates . extend (COMMANDS . iter () . map (| s | s . to_string ())) ; candidates . extend (self . get_variable_names ()) ; candidates } # [doc = " Checks if parentheses are balanced in the input"] fn has_unmatched_parens (& self , input : & str) -> bool { let mut count = 0 ; for c in input . chars () { match c { '(' => count += 1 , ')' => { count -= 1 ; if count < 0 { return true ; } } _ => { } } } count != 0 } } . self_ty`
- **Impl**: `Default` for `impl Default for ReplHelper { fn default () -> Self { Self :: new () } } . self_ty`
- **Impl**: `Helper` for `impl Helper for ReplHelper { } . self_ty`
- **Impl**: `Completer` for `impl Completer for ReplHelper { type Candidate = Pair ; fn complete (& self , line : & str , pos : usize , _ctx : & Context < '_ > ,) -> rustyline :: Result < (usize , Vec < Pair >) > { let word_start = line [.. pos] . rfind (| c : char | ! c . is_alphanumeric () && c != '_') . map (| i | i + 1) . unwrap_or (0) ; let prefix = & line [word_start .. pos] ; if prefix . is_empty () { return Ok ((pos , Vec :: new ())) ; } let candidates = self . get_all_candidates () ; let matches : Vec < Pair > = candidates . into_iter () . filter (| c | c . starts_with (prefix)) . map (| c | Pair { display : c . clone () , replacement : c , }) . collect () ; Ok ((word_start , matches)) } } . self_ty`
- **Impl**: `Highlighter` for `impl Highlighter for ReplHelper { fn highlight < 'l > (& self , line : & 'l str , _pos : usize) -> Cow < 'l , str > { let mut result = String :: new () ; let mut chars = line . chars () . peekable () ; while let Some (& c) = chars . peek () { match c { '0' ..= '9' | '.' => { let mut num_str = String :: new () ; while let Some (& ch) = chars . peek () { if ch . is_ascii_digit () || ch == '.' { num_str . push (ch) ; chars . next () ; } else { break ; } } result . push_str (& Color :: Cyan . paint (& num_str) . to_string ()) ; } '+' | '-' | '*' | '/' | '%' | '^' | '!' | '=' => { result . push_str (& Color :: Yellow . paint (c . to_string ()) . to_string ()) ; chars . next () ; } '(' | ')' => { result . push (c) ; chars . next () ; } 'a' ..= 'z' | 'A' ..= 'Z' | '_' => { let mut identifier = String :: new () ; while let Some (& ch) = chars . peek () { if ch . is_alphanumeric () || ch == '_' { identifier . push (ch) ; chars . next () ; } else { break ; } } let colored = if FUNCTIONS . contains (& identifier . as_str ()) { Color :: Green . paint (& identifier) . to_string () } else if CONSTANTS . contains (& identifier . as_str ()) { Color :: Purple . paint (& identifier) . to_string () } else if COMMANDS . contains (& identifier . as_str ()) { Color :: Blue . paint (& identifier) . to_string () } else if self . get_variable_names () . contains (& identifier) { Color :: Blue . paint (& identifier) . to_string () } else { identifier . clone () } ; result . push_str (& colored) ; } _ => { result . push (c) ; chars . next () ; } } } Cow :: Owned (result) } fn highlight_char (& self , _line : & str , _pos : usize , _forced : bool) -> bool { true } } . self_ty`
- **Impl**: `Validator` for `impl Validator for ReplHelper { fn validate (& self , ctx : & mut ValidationContext) -> rustyline :: Result < ValidationResult > { let input = ctx . input () ; if self . has_unmatched_parens (input) { Ok (ValidationResult :: Incomplete) } else { Ok (ValidationResult :: Valid (None)) } } } . self_ty`
- **Impl**: `Hinter` for `impl Hinter for ReplHelper { type Hint = String ; fn hint (& self , _line : & str , _pos : usize , _ctx : & Context < '_ >) -> Option < String > { None } } . self_ty`

### ./archive/pmatinit-calculator/src/repl/mod.rs

**File Complexity**: 6 | **Functions**: 2

- **Function**: `get_history_path` [complexity: 2] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `start` [complexity: 11] [cognitive: 48] [big-o: O(n log n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/tests/calculator_tests.rs

**File Complexity**: 1 | **Functions**: 45

- **Function**: `test_associativity_left` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_chained_operations` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_nested_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_all_operators` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_negative_result` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_large_numbers` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_very_small_decimals` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiplication_by_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_division_resulting_in_decimal` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_nested_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_expression_starting_with_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_expression_ending_with_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_modulo_with_decimals` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_negative_in_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_multiplications` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_divisions` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_mixed_precedence_complex` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_invalid_character_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_decimal_points_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_assignment_simple` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_usage` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_in_complex_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_reassignment` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_with_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ans_variable_simple` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ans_variable_updates` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ans_with_assignment` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_with_functions` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_with_power` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_variables` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_names_with_underscores` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_names_with_numbers` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_undefined_variable_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_invalid_variable_name_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_reserved_name_assignment_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_empty_assignment_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_calculator_clear_variables` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_calculator_delete_variable` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_calculator_list_variables` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_calculator_set_variable_directly` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variable_in_nested_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_chained_variable_usage` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ans_in_assignment` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variables_with_factorial` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_variables_persist_across_evaluations` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/tests/integration_tests.rs

**File Complexity**: 1 | **Functions**: 65

- **Function**: `test_simple_addition` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_simple_subtraction` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_simple_multiplication` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_simple_division` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_modulo` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_operator_precedence` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_complex_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_negative_numbers` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_decimal_numbers` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_division_by_zero_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_invalid_syntax_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_mismatched_parentheses_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_empty_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_whitespace_handling` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_operator` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_precedence` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_right_associativity` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_with_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_with_negative_exponent` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_zero_exponent` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_constant_pi` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_constant_e` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_constant_in_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_constants` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_constant_with_power` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sqrt_basic` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sqrt_with_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sqrt_negative_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sqrt_in_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sin_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_sin_pi_over_2` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_cos_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_cos_pi` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_tan_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_tan_pi_over_4` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_log_10` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_log_100` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_log_negative_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_log_zero_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ln_e` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ln_e_squared` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_ln_negative_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_complex_with_functions` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_nested_functions` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_function_with_constant` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_functions_in_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_basic` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_one` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_in_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_with_multiplication` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_negative_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_non_integer_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_with_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_power_with_sqrt` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_factorial_with_power` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_trig_with_constants` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_complex_scientific_expression` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_logarithm_with_power` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_natural_log_with_e` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_nested_scientific_functions` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_all_features_combined` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_precedence_with_advanced_ops` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_multiple_factorials` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./archive/pmatinit-calculator/tests/repl_tests.rs

**File Complexity**: 1 | **Functions**: 12

- **Function**: `test_enhance_unknown_function_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_unknown_constant_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_undefined_variable_error` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_undefined_variable_empty` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_mismatched_parentheses` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_division_by_zero` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_sqrt_negative` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_log_non_positive` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_factorial_negative` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_enhance_reserved_name_assignment` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_helper_creation` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `test_helper_with_calculator` [complexity: 1] [cognitive: 0] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/pom.xml


### ./backend/src/main/java/com/platform/saas/SaasPlatformApplication.java

- **Struct**: `com.platform.saas::SaasPlatformApplication` [fields: 1]
- **Function**: `com.platform.saas::main` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/config/SecurityConfig.java

- **Struct**: `com.platform.saas.config::SecurityConfig` [fields: 3]
- **Function**: `com.platform.saas.config::securityFilterChain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.config::jwtDecoder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.config::corsConfigurationSource` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.config::CorsConfiguration` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.config::UrlBasedCorsConfigurationSource` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/AuthController.java

- **Struct**: `com.platform.saas.controller::AuthController` [fields: 2]
- **Function**: `com.platform.saas.controller::getCurrentUser` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::checkAuthStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/AutomationController.java

- **Struct**: `com.platform.saas.controller::AutomationController` [fields: 7]
- **Function**: `com.platform.saas.controller::createRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::IllegalArgumentException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::deleteRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getRuleCount` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getLogsForRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getFailedLogs` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getAutomationStats` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/GlobalExceptionHandler.java

- **Struct**: `com.platform.saas.controller::GlobalExceptionHandler` [fields: 0]

### ./backend/src/main/java/com/platform/saas/controller/InternalApiController.java

- **Struct**: `com.platform.saas.controller::InternalApiController` [fields: 1]
- **Struct**: `com.platform.saas.controller::CognitoUserRequest` [fields: 0]
- **Function**: `com.platform.saas.controller::health` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/ProjectController.java

- **Struct**: `com.platform.saas.controller::ProjectController` [fields: 4]
- **Function**: `com.platform.saas.controller::createProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::deleteProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::countProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/TaskController.java

- **Struct**: `com.platform.saas.controller::TaskController` [fields: 5]
- **Function**: `com.platform.saas.controller::createTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 2 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 2 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::deleteTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 2 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::countTasks` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 2 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getAverageProgress` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 2 items] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/TenantController.java

- **Struct**: `com.platform.saas.controller::TenantController` [fields: 4]
- **Function**: `com.platform.saas.controller::getTenantById` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getTenantBySubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::validateSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getTenantUsage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/controller/UserController.java

- **Struct**: `com.platform.saas.controller::UserController` [fields: 3]
- **Function**: `com.platform.saas.controller::getCurrentUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getTenantUsers` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::mapToUserResponse` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.controller::getCurrentUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/dto/ErrorResponse.java

- **Struct**: `com.platform.saas.dto::ErrorResponse` [fields: 2]
- **Function**: `com.platform.saas.dto::of` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.dto::of` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/dto/InviteUserRequest.java

- **Struct**: `com.platform.saas.dto::InviteUserRequest` [fields: 0]

### ./backend/src/main/java/com/platform/saas/dto/InviteUserResponse.java

- **Struct**: `com.platform.saas.dto::InviteUserResponse` [fields: 0]

### ./backend/src/main/java/com/platform/saas/dto/TenantRegistrationRequest.java

- **Struct**: `com.platform.saas.dto::TenantRegistrationRequest` [fields: 0]

### ./backend/src/main/java/com/platform/saas/dto/TenantResponse.java

- **Struct**: `com.platform.saas.dto::TenantResponse` [fields: 0]

### ./backend/src/main/java/com/platform/saas/dto/TenantUsageResponse.java

- **Struct**: `com.platform.saas.dto::TenantUsageResponse` [fields: 1]
- **Function**: `com.platform.saas.dto::calculateUsagePercentage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/dto/UserResponse.java

- **Struct**: `com.platform.saas.dto::UserResponse` [fields: 0]

### ./backend/src/main/java/com/platform/saas/exception/InvalidSubdomainException.java

- **Struct**: `com.platform.saas.exception::InvalidSubdomainException` [fields: 2]
- **Function**: `com.platform.saas.exception::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.exception::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/exception/QuotaExceededException.java

- **Struct**: `com.platform.saas.exception::QuotaExceededException` [fields: 2]
- **Function**: `com.platform.saas.exception::QuotaExceededException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.exception::QuotaExceededException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/exception/SubdomainAlreadyExistsException.java

- **Struct**: `com.platform.saas.exception::SubdomainAlreadyExistsException` [fields: 1]
- **Function**: `com.platform.saas.exception::SubdomainAlreadyExistsException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/exception/TenantNotFoundException.java

- **Struct**: `com.platform.saas.exception::TenantNotFoundException` [fields: 2]
- **Function**: `com.platform.saas.exception::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.exception::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/exception/UserNotFoundException.java

- **Struct**: `com.platform.saas.exception::UserNotFoundException` [fields: 2]
- **Function**: `com.platform.saas.exception::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.exception::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/AutomationRule.java

- **Struct**: `com.platform.saas.model::AutomationRule` [fields: 2]
- **Function**: `com.platform.saas.model::recordExecution` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/EventLog.java

- **Struct**: `com.platform.saas.model::EventLog` [fields: 1]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/Priority.java


### ./backend/src/main/java/com/platform/saas/model/Project.java

- **Struct**: `com.platform.saas.model::Project` [fields: 6]
- **Function**: `com.platform.saas.model::isOverdue` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isActive` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::canBeEdited` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/ProjectStatus.java


### ./backend/src/main/java/com/platform/saas/model/SubscriptionTier.java


### ./backend/src/main/java/com/platform/saas/model/Task.java

- **Struct**: `com.platform.saas.model::Task` [fields: 9]
- **Function**: `com.platform.saas.model::isOverdue` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isInProgress` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isBlocked` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isCompleted` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::complete` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::startWork` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/TaskAssignee.java

- **Struct**: `com.platform.saas.model::TaskAssignee` [fields: 4]
- **Function**: `com.platform.saas.model::TaskAssignee` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/TaskAssigneeId.java

- **Struct**: `com.platform.saas.model::TaskAssigneeId` [fields: 2]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/TaskDependency.java

- **Struct**: `com.platform.saas.model::TaskDependency` [fields: 4]
- **Function**: `com.platform.saas.model::TaskDependency` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/TaskDependencyId.java

- **Struct**: `com.platform.saas.model::TaskDependencyId` [fields: 2]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/TaskStatus.java


### ./backend/src/main/java/com/platform/saas/model/Tenant.java

- **Struct**: `com.platform.saas.model::Tenant` [fields: 8]
- **Function**: `com.platform.saas.model::prePersist` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getDefaultQuotaForTier` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::preUpdate` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getDefaultQuotaForTier` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::validateSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::IllegalArgumentException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isQuotaExceeded` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/User.java

- **Struct**: `com.platform.saas.model::User` [fields: 4]
- **Function**: `com.platform.saas.model::updateLastLogin` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/UserRole.java


### ./backend/src/main/java/com/platform/saas/model/UserTenant.java

- **Struct**: `com.platform.saas.model::UserTenant` [fields: 4]
- **Function**: `com.platform.saas.model::isAdministrator` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::canEdit` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::isViewerOnly` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::toString` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/model/UserTenantId.java

- **Struct**: `com.platform.saas.model::UserTenantId` [fields: 2]
- **Function**: `com.platform.saas.model::equals` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::getClass` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.model::hashCode` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/repository/AutomationRuleRepository.java

- **Function**: `com.platform.saas.repository::findByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndIsActive` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByEventTypeAndIsActive` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantIdAndIsActive` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByCreatedBy` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndActionType` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findTopExecutedRules` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::AutomationRuleRepository`

### ./backend/src/main/java/com/platform/saas/repository/EventLogRepository.java

- **Function**: `com.platform.saas.repository::findByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByAutomationRuleId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdOrderByCreatedAtDesc` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findRecentLogs` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantIdAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByResourceIdAndResourceType` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::deleteByCreatedAtBefore` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::AVG` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::getAverageExecutionDuration` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::EventLogRepository`

### ./backend/src/main/java/com/platform/saas/repository/ProjectRepository.java

- **Function**: `com.platform.saas.repository::findByIdAndTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndPriority` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndOwnerId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findOverdueProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findActiveProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::COUNT` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countActiveProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::ProjectRepository`

### ./backend/src/main/java/com/platform/saas/repository/TaskRepository.java

- **Function**: `com.platform.saas.repository::findByIdAndTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndProjectId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndProjectIdAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndPriority` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findOverdueTasks` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findOverdueTasksForProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countByTenantIdAndProjectId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::AVG` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::calculateAverageProgress` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::TaskRepository`

### ./backend/src/main/java/com/platform/saas/repository/TenantRepository.java

- **Function**: `com.platform.saas.repository::findBySubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::existsBySubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::TenantRepository`

### ./backend/src/main/java/com/platform/saas/repository/UserRepository.java

- **Function**: `com.platform.saas.repository::findByCognitoUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::existsByEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::existsByCognitoUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::UserRepository`

### ./backend/src/main/java/com/platform/saas/repository/UserTenantRepository.java

- **Function**: `com.platform.saas.repository::findByUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByUserIdAndTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::existsByUserIdAndTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::findByTenantIdAndRole` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::COUNT` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countUsersByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::COUNT` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.repository::countAdministratorsByTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Trait**: `com.platform.saas.repository::UserTenantRepository`

### ./backend/src/main/java/com/platform/saas/security/JwtUserInfo.java

- **Struct**: `com.platform.saas.security::JwtUserInfo` [fields: 1]

### ./backend/src/main/java/com/platform/saas/security/JwtUserInfoExtractor.java

- **Struct**: `com.platform.saas.security::JwtUserInfoExtractor` [fields: 3]
- **Function**: `com.platform.saas.security::extractUserInfo` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::extractCognitoUserId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::extractEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/security/SecurityConfig.java

- **Struct**: `com.platform.saas.security::SecurityConfig` [fields: 6]
- **Function**: `com.platform.saas.security::securityFilterChain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::jwtDecoder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::jwtAuthenticationConverter` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::JwtAuthenticationConverter` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::jwtGrantedAuthoritiesConverter` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::JwtGrantedAuthoritiesConverter` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::corsConfigurationSource` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::CorsConfiguration` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::UrlBasedCorsConfigurationSource` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::userInfoExtractor` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/security/TenantContext.java

- **Struct**: `com.platform.saas.security::TenantContext` [fields: 10]
- **Function**: `com.platform.saas.security::getTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::setTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::getTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::setTenantSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::getTenantSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::isSet` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::clear` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::executeWithTenantId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::TenantContext` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/security/TenantContextFilter.java

- **Struct**: `com.platform.saas.security::TenantContextFilter` [fields: 5]
- **Function**: `com.platform.saas.security::extractSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::extractSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::isPublicEndpoint` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.security::shouldNotFilter` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/AutomationService.java

- **Struct**: `com.platform.saas.service::AutomationService` [fields: 16]
- **Function**: `com.platform.saas.service::createRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::IllegalStateException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getAllRules` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getActiveRules` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getRulesByEventType` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTopExecutedRules` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::updateRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::deleteRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::toggleRuleStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countRules` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getRecentLogs` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getLogsForRule` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getFailedLogs` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getLogsByDateRange` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countLogsByStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getAverageExecutionDuration` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/EmailService.java

- **Struct**: `com.platform.saas.service::EmailService` [fields: 2]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::sendEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::sendEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/EventPublisher.java

- **Struct**: `com.platform.saas.service::EventPublisher` [fields: 2]
- **Function**: `com.platform.saas.service::buildEventDetailJson` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::StringBuilder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::convertMapToJson` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::StringBuilder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getStackTrace` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::StringBuilder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/InvitationService.java

- **Struct**: `com.platform.saas.service::InvitationService` [fields: 5]
- **Function**: `com.platform.saas.service::inviteUser` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::User` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserTenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::sendInvitationEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::removeUserFromTenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::IllegalArgumentException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::sendInvitationEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::buildInvitationLink` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::buildInvitationEmailBody` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::buildInvitationLink` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::StringBuilder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::extractNameFromEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::StringBuilder` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/ProjectService.java

- **Struct**: `com.platform.saas.service::ProjectService` [fields: 16]
- **Function**: `com.platform.saas.service::createProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::IllegalStateException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getAllProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getProjectsByStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getProjectsByPriority` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getProjectsByOwner` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getOverdueProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getActiveProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::updateProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::deleteProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countActiveProjects` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::checkQuota` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::QuotaExceededException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishProjectCreatedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishProjectUpdatedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishProjectDeletedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/TaskService.java

- **Struct**: `com.platform.saas.service::TaskService` [fields: 21]
- **Function**: `com.platform.saas.service::createTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::IllegalStateException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getAllTasks` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTasksByProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTasksByStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTasksByProjectAndStatus` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTasksByPriority` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getOverdueTasks` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getOverdueTasksForProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::updateTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::deleteTask` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countTasks` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::countTasksByProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::calculateAverageProgress` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::validateDependency` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::IllegalArgumentException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::validateProject` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::RuntimeException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::checkQuota` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::QuotaExceededException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishTaskCreatedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishTaskUpdatedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishTaskStatusChangedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::publishTaskDeletedEvent` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 1 items] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/TenantService.java

- **Struct**: `com.platform.saas.service::TenantService` [fields: 8]
- **Function**: `com.platform.saas.service::registerTenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::SubdomainAlreadyExistsException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::Tenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::toTenantResponse` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTenantById` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::calculateCurrentUsage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::toTenantResponse` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTenantBySubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::calculateCurrentUsage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::toTenantResponse` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::validateSubdomain` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::InvalidSubdomainException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::enforceQuota` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::calculateCurrentUsage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::QuotaExceededException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::calculateCurrentUsage` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getQuotaForTier` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::toTenantResponse` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/java/com/platform/saas/service/UserService.java

- **Struct**: `com.platform.saas.service::UserService` [fields: 13]
- **Function**: `com.platform.saas.service::createUserFromCognito` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::User` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::findOrCreateUserByEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::User` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserById` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserByCognitoId` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserByEmail` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::addUserToTenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserById` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::TenantNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserTenant` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserTenants` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getTenantUsers` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserTenantRelationship` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::UserNotFoundException` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::updateLastLogin` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::getUserById` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::hasRole` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::canEdit` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `com.platform.saas.service::isAdministrator` [complexity: 3] [cognitive: 2] [big-o: O(n)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./backend/src/main/resources/application-local.yml


### ./backend/src/main/resources/application.yml


### ./docker-compose.yml


### ./frontend/package.json


### ./frontend/src/App.tsx

**File Complexity**: 3 | **Functions**: 3

- **Function**: `App` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `HomePage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/contexts/AuthContext.tsx

**File Complexity**: 7 | **Functions**: 7

- **Trait**: `User`
- **Trait**: `AuthContextType`
- **Trait**: `AuthProviderProps`
- **Function**: `AuthProvider` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `initializeAuth` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `fetchUserInfo` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `login` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `logout` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `refreshUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `useAuth` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/contexts/TenantContext.tsx

**File Complexity**: 5 | **Functions**: 5

- **Trait**: `Tenant`
- **Trait**: `TenantContextType`
- **Trait**: `TenantProviderProps`
- **Function**: `extractSubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `TenantProvider` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `fetchTenant` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `refreshTenant` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `useTenant` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/main.tsx

**File Complexity**: 1 | **Functions**: 0


### ./frontend/src/pages/AutomationPage.tsx

**File Complexity**: 12 | **Functions**: 12

- **Function**: `AutomationPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadRules` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleCreate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleUpdate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleToggleStatus` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleDelete` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `openEditModal` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `resetForm` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getStatusBadgeStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tabStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `toggleButtonStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/Dashboard.tsx

**File Complexity**: 3 | **Functions**: 3

- **Function**: `Dashboard` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadUsage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadAutomationLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/LoginPage.tsx

**File Complexity**: 2 | **Functions**: 2

- **Function**: `LoginPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleLogin` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/OAuthCallbackPage.tsx

**File Complexity**: 2 | **Functions**: 2

- **Function**: `OAuthCallbackPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleCallback` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/ProjectsPage.tsx

**File Complexity**: 10 | **Functions**: 10

- **Function**: `ProjectsPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleCreate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleUpdate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleDelete` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `openEditModal` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `resetForm` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `isOverdue` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getPriorityStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getStatusStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/SettingsPage.tsx

**File Complexity**: 5 | **Functions**: 5

- **Function**: `SettingsPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadUsers` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleInvite` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleRemove` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getRoleBadgeStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/SignUpPage.tsx

**File Complexity**: 4 | **Functions**: 4

- **Function**: `SignUpPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `validateSubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleChange` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleSubmit` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/pages/TasksPage.tsx

**File Complexity**: 14 | **Functions**: 14

- **Function**: `TasksPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `loadTasks` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleCreate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleUpdate` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleDelete` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handleProgressChange` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `openEditModal` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `resetForm` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `isOverdue` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getStatusStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getPriorityStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getProjectName` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `progressBarFillStyle` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/api.ts

**File Complexity**: 1 | **Functions**: 1

- **Trait**: `ApiError`
- **Function**: `getErrorMessage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/authService.ts

**File Complexity**: 12 | **Functions**: 12

- **Function**: `authService::exchangeCodeForToken` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `authService::getCurrentUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `authService::checkAuthStatus` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `authService::redirectToCognitoLogin` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `exchangeCodeForToken` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getCurrentUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `checkAuthStatus` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `redirectToCognitoLogin` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/automationService.ts

**File Complexity**: 42 | **Functions**: 42

- **Trait**: `AutomationRule`
- **Trait**: `EventLog`
- **Trait**: `AutomationStats`
- **Function**: `automationService::createRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getAllRules` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getRulesByEventType` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getTopExecutedRules` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::updateRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::toggleRuleStatus` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::deleteRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getRuleCount` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getRecentLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getLogsForRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getFailedLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getLogsByDateRange` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `automationService::getStats` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `createRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getAllRules` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getRulesByEventType` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getTopExecutedRules` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `updateRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `toggleRuleStatus` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `deleteRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getRuleCount` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getRecentLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getLogsForRule` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getFailedLogs` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getLogsByDateRange` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getStats` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/projectService.ts

**File Complexity**: 18 | **Functions**: 18

- **Trait**: `Project`
- **Trait**: `CreateProjectRequest`
- **Trait**: `UpdateProjectRequest`
- **Function**: `projectService::getAllProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `projectService::getProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `projectService::createProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `projectService::updateProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `projectService::deleteProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `projectService::countProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getAllProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `createProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `updateProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `deleteProject` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `countProjects` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/taskService.ts

**File Complexity**: 21 | **Functions**: 21

- **Trait**: `Task`
- **Trait**: `CreateTaskRequest`
- **Trait**: `UpdateTaskRequest`
- **Function**: `taskService::getAllTasks` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::getTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::createTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::updateTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::deleteTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::countTasks` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `taskService::getAverageProgress` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getAllTasks` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `createTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `updateTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `deleteTask` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `countTasks` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getAverageProgress` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/tenantService.ts

**File Complexity**: 15 | **Functions**: 15

- **Trait**: `TenantRegistrationRequest`
- **Trait**: `TenantUsageResponse`
- **Function**: `tenantService::registerTenant` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tenantService::getTenantById` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tenantService::getTenantBySubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tenantService::validateSubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `tenantService::getTenantUsage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `registerTenant` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getTenantById` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getTenantBySubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `validateSubdomain` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `getTenantUsage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/services/userService.ts

**File Complexity**: 9 | **Functions**: 9

- **Trait**: `User`
- **Trait**: `InviteUserRequest`
- **Trait**: `InviteUserResponse`
- **Function**: `userService::inviteUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `userService::listUsers` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `userService::removeUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `inviteUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `listUsers` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `removeUser` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/vite-env.d.ts

**File Complexity**: 1 | **Functions**: 0

- **Trait**: `ImportMetaEnv`
- **Trait**: `ImportMeta`

### ./frontend/tests/setup.ts

**File Complexity**: 1 | **Functions**: 0


### ./frontend/tsconfig.json


### ./frontend/tsconfig.node.json


### ./frontend/vite.config.ts

**File Complexity**: 1 | **Functions**: 0


### ./lambda-functions/automation-engine/package.json


### ./lambda-functions/automation-engine/src/index.ts

**File Complexity**: 1 | **Functions**: 1

- **Function**: `handler` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 4 items] [churn: low(1)] [tdg: 2.5]

### ./lambda-functions/cognito-triggers/package.json


### ./lambda-functions/cognito-triggers/src/post-confirmation.ts

**File Complexity**: 5 | **Functions**: 5

- **Trait**: `UserCreationRequest`
- **Trait**: `ApiConfig`
- **Function**: `getApiConfig` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `createUserInBackend` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `handler` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./lambda-functions/cognito-triggers/tsconfig.json


### ./pmat-analysis.json


### ./specs/001-saas-platform/contracts/api-spec.yaml


### ./specs/001-saas-platform/contracts/events-schema.json


## Key Components

Key architectural components identified in the codebase.

## Big-O Complexity Analysis

Complexity analysis results integrated in function annotations above.

## Entropy Analysis

Code entropy and organization metrics.

## Provability Analysis

Formal verification and provability insights.

## Graph Metrics

Dependency graph and PageRank analysis.

## Technical Debt Gradient (TDG)

Technical debt progression and accumulation patterns.

## Dead Code Analysis

Unused code detection and removal recommendations.

## Self-Admitted Technical Debt (SATD)

TODO, FIXME, and HACK comments indicating technical debt.

## Quality Insights

Overall code quality assessment and trends.

## Recommendations

Actionable suggestions for code improvement.

