# Project Context

**Language**: typescript
**Project Path**: .

## Project Structure

- **Total Files**: 23
- **Total Functions**: 155
- **Median Cyclomatic**: 1.00
- **Median Cognitive**: 0.00

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

### ./backend/src/main/resources/application-local.yml


### ./backend/src/main/resources/application.yml


### ./docker-compose.yml


### ./frontend/package.json


### ./frontend/src/App.tsx

**File Complexity**: 6 | **Functions**: 6

- **Function**: `App` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `anonymous` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `HomePage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `SignUpPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `LoginPage` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]
- **Function**: `Dashboard` [complexity: 1] [cognitive: 1] [big-o: O(1)] [provability: 43%] [satd: 0] [churn: low(1)] [tdg: 2.5]

### ./frontend/src/main.tsx

**File Complexity**: 1 | **Functions**: 0


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

