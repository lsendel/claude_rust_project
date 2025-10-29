# pmatinit User Guide

Welcome to the comprehensive user guide for pmatinit, a powerful command-line calculator with an interactive REPL. This guide will walk you through all features, from basic usage to advanced techniques.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Basic Calculations](#basic-calculations)
3. [Advanced Operations](#advanced-operations)
4. [Working with Variables](#working-with-variables)
5. [REPL Features](#repl-features)
6. [Commands Reference](#commands-reference)
7. [Error Handling](#error-handling)
8. [Tips and Tricks](#tips-and-tricks)
9. [Advanced Examples](#advanced-examples)
10. [Troubleshooting](#troubleshooting)

---

## Getting Started

### Installation

Install pmatinit using cargo:

```bash
cargo install pmatinit
```

Or build from source:

```bash
git clone https://github.com/yourusername/pmatinit.git
cd pmatinit
cargo build --release
cargo install --path .
```

### Running the Calculator

**Interactive Mode:**
```bash
pmatinit
```

This starts the interactive REPL (Read-Eval-Print Loop) where you can enter expressions and see results immediately.

**Single Expression Mode:**
```bash
pmatinit "2 + 2"
```

Evaluates a single expression and prints the result.

### First Steps

Once you start the interactive mode, you'll see the prompt:

```
calc>
```

Try your first calculation:

```bash
calc> 2 + 2
4.00
```

Type `help` to see available commands and features:

```bash
calc> help
```

Type `quit` or `exit` to leave the calculator:

```bash
calc> quit
Goodbye!
```

---

## Basic Calculations

### Arithmetic Operations

pmatinit supports all standard arithmetic operations:

**Addition:**
```bash
calc> 5 + 3
8.00

calc> 10.5 + 2.3
12.80
```

**Subtraction:**
```bash
calc> 10 - 3
7.00

calc> 5 - 8
-3.00
```

**Multiplication:**
```bash
calc> 4 * 7
28.00

calc> 2.5 * 4
10.00
```

**Division:**
```bash
calc> 20 / 4
5.00

calc> 7 / 2
3.50
```

**Modulo (Remainder):**
```bash
calc> 17 % 5
2.00

calc> 20 % 3
2.00
```

### Negative Numbers

Negative numbers are fully supported:

```bash
calc> -5 + 3
-2.00

calc> -10 * -2
20.00

calc> 5 - -3
8.00
```

### Operator Precedence

Operations follow standard mathematical precedence (PEMDAS):

1. Parentheses
2. Factorial
3. Power (Exponentiation)
4. Multiplication, Division, Modulo
5. Addition, Subtraction

**Examples:**
```bash
calc> 2 + 3 * 4
14.00          # Multiplication first: 2 + (3 * 4)

calc> (2 + 3) * 4
20.00          # Parentheses first: (2 + 3) * 4

calc> 2 ^ 3 * 4
32.00          # Power first: (2 ^ 3) * 4

calc> 5!
120.00
```

### Parentheses

Use parentheses to group operations and override precedence:

```bash
calc> (2 + 3) * 4
20.00

calc> 2 + (3 * 4)
14.00

calc> ((2 + 3) * 4) / 5
4.00

calc> (10 + (5 * 2)) - 3
17.00
```

---

## Advanced Operations

### Power Operator

The power operator `^` raises a number to an exponent. It is **right-associative**, meaning `2^3^4` evaluates as `2^(3^4)`.

**Basic Powers:**
```bash
calc> 2 ^ 3
8.00

calc> 10 ^ 2
100.00

calc> 2 ^ 10
1024.00
```

**Right Associativity:**
```bash
calc> 2 ^ 3 ^ 2
512.00         # 2 ^ (3 ^ 2) = 2 ^ 9 = 512
```

**Fractional Powers:**
```bash
calc> 4 ^ 0.5
2.00           # Square root

calc> 8 ^ (1/3)
2.00           # Cube root
```

### Factorial

The factorial operator `!` is a postfix operator that computes the factorial of a number.

**Basic Factorials:**
```bash
calc> 5!
120.00

calc> 0!
1.00           # By definition

calc> 10!
3628800.00
```

**Combining with Other Operations:**
```bash
calc> 2 ^ 3!
64.00          # 2 ^ (3!) = 2 ^ 6 = 64

calc> 5! / 4!
5.00           # 120 / 24 = 5
```

**Limitations:**
- Factorial only works with non-negative integers
- Large factorials may overflow

### Mathematical Functions

#### Trigonometric Functions (Radians)

All trigonometric functions use **radians**, not degrees.

**Sine:**
```bash
calc> sin(0)
0.00

calc> sin(pi / 2)
1.00

calc> sin(pi)
0.00
```

**Cosine:**
```bash
calc> cos(0)
1.00

calc> cos(pi / 2)
0.00

calc> cos(pi)
-1.00
```

**Tangent:**
```bash
calc> tan(0)
0.00

calc> tan(pi / 4)
1.00
```

**Converting Degrees to Radians:**
```bash
calc> degrees = 45
45.00

calc> radians = degrees * pi / 180
0.79

calc> sin(radians)
0.71           # sin(45°) ≈ 0.707
```

#### Square Root

```bash
calc> sqrt(4)
2.00

calc> sqrt(16)
4.00

calc> sqrt(2)
1.41

calc> sqrt(0)
0.00
```

**Note:** Square root of negative numbers will produce an error.

#### Logarithms

**Base 10 Logarithm:**
```bash
calc> log(10)
1.00

calc> log(100)
2.00

calc> log(1000)
3.00
```

**Natural Logarithm (base e):**
```bash
calc> ln(e)
1.00

calc> ln(1)
0.00

calc> ln(2.718281828)
1.00
```

**Logarithm Properties:**
```bash
calc> log(10 * 10)
2.00           # log(100)

calc> log(10) + log(10)
2.00           # log(a*b) = log(a) + log(b)
```

### Mathematical Constants

#### Pi (π)

The mathematical constant π ≈ 3.14159...

```bash
calc> pi
3.14

calc> 2 * pi
6.28           # Circumference formula with radius=1

calc> pi * 5 * 5
78.54          # Area of circle with radius=5
```

#### Euler's Number (e)

The mathematical constant e ≈ 2.71828...

```bash
calc> e
2.72

calc> ln(e)
1.00

calc> e ^ 1
2.72
```

### Combining Operations

You can combine multiple operations in complex expressions:

```bash
calc> 2 * pi * sqrt(16)
25.13          # 2 * π * 4

calc> sin(pi / 2) + cos(0)
2.00           # 1 + 1

calc> log(100) * sqrt(4) + 5!
124.00         # 2 * 2 + 120

calc> (2 ^ 10) / sqrt(16) + ln(e)
257.00         # 1024 / 4 + 1
```

---

## Working with Variables

Variables allow you to store values and reuse them in calculations.

### Variable Assignment

Assign a value to a variable using the `=` operator:

```bash
calc> x = 10
10.00

calc> radius = 5
5.00

calc> temperature = 98.6
98.60
```

**Variable Naming Rules:**
- Must start with a letter (a-z, A-Z)
- Can contain letters, numbers, and underscores
- Case-sensitive: `x` and `X` are different variables
- Cannot use reserved keywords: `pi`, `e`, `sin`, `cos`, etc.

### Using Variables

Once assigned, variables can be used in any expression:

```bash
calc> x = 10
10.00

calc> x + 5
15.00

calc> x * 2
20.00

calc> y = x + 10
20.00

calc> x + y
30.00
```

### Complex Expressions with Variables

```bash
calc> radius = 5
5.00

calc> area = pi * radius ^ 2
78.54

calc> circumference = 2 * pi * radius
31.42

calc> ratio = area / circumference
2.50
```

### The Special `ans` Variable

The `ans` variable automatically stores the result of the last calculation:

```bash
calc> 5 * 5
25.00

calc> ans
25.00

calc> ans + 10
35.00

calc> sqrt(ans)
5.92

calc> ans * 2
11.83
```

**Note:** `ans` is updated after every successful calculation, including variable assignments.

### Variable Management

**List all variables:**
```bash
calc> vars
Variables:
  ans = 11.83
  area = 78.54
  circumference = 31.42
  radius = 5.00
  ratio = 2.50
  x = 10.00
  y = 20.00
```

**Delete a specific variable:**
```bash
calc> delete x
Variable 'x' deleted.

calc> delete notexist
Variable 'notexist' not found.
```

**Clear all variables:**
```bash
calc> clearvars
All variables cleared.

calc> vars
No variables defined.
```

**Note:** The `ans` variable will be recreated on the next calculation.

---

## REPL Features

The interactive REPL includes several modern features to enhance your experience.

### Command History

Navigate through your previous commands using the arrow keys:

- **UP Arrow**: Move to previous command
- **DOWN Arrow**: Move to next command

History is automatically saved to `~/.pmatinit_history` and loaded when you start the calculator.

**Example:**
```bash
calc> 2 + 2
4.00
calc> 3 * 3
9.00
calc> [Press UP]        # Shows: 3 * 3
calc> [Press UP]        # Shows: 2 + 2
calc> [Press DOWN]      # Shows: 3 * 3
```

### Tab Completion

Press `TAB` to auto-complete functions, constants, commands, and variables.

**Completing Functions:**
```bash
calc> s[TAB]
sin  sqrt

calc> sq[TAB]
calc> sqrt(
```

**Completing Constants:**
```bash
calc> p[TAB]
pi

calc> pi
3.14
```

**Completing Commands:**
```bash
calc> cl[TAB]
clear  clearvars

calc> cle[TAB]
clear  clearvars
```

**Completing Variables:**
```bash
calc> x = 10
10.00

calc> x[TAB]
x

calc> x + 5
15.00
```

### Syntax Highlighting

As you type, your input is highlighted with colors:

- **Numbers**: Cyan (e.g., `42`, `3.14`)
- **Operators**: Yellow (`+`, `-`, `*`, `/`, `%`, `^`, `!`, `=`)
- **Functions**: Green (`sin`, `cos`, `sqrt`, etc.)
- **Constants**: Purple (`pi`, `e`)
- **Commands/Variables**: Blue (`help`, `quit`, `x`, `y`)

**Example:**
```
calc> 2 + sin(pi / 2) * x
      ↑   ↑   ↑    ↑   ↑
      cyan yellow green purple blue
```

### Multi-line Support

For complex expressions, you can spread them across multiple lines by leaving parentheses unclosed:

**Example:**
```bash
calc> (2 + 3
....>  * 4)
20.00

calc> sin(
....> pi / 2
....> )
1.00

calc> (
....> (2 + 3) * 4
....> ) / 2
10.00
```

**How it works:**
- When you press Enter with unclosed parentheses, the REPL shows a continuation prompt (`....>`)
- Continue typing your expression
- When all parentheses are balanced, the expression evaluates

### Enhanced Error Messages

When you make a mistake, pmatinit provides helpful error messages with suggestions:

**Typos in Function Names:**
```bash
calc> sine(0)
Error: Unknown identifier: 'sine'
  Did you mean: sin?
```

**Typos in Constants:**
```bash
calc> 2 * pii
Error: Unknown identifier: 'pii'
  Did you mean: pi?
```

**Invalid Function Arguments:**
```bash
calc> sqrt(-1)
Error: Square root of negative number
  Tip: sqrt requires a non-negative argument

calc> log(0)
Error: Logarithm of zero or negative number
  Tip: log requires a positive argument
```

**Division by Zero:**
```bash
calc> 10 / 0
Error: Division by zero
```

---

## Commands Reference

### help

Displays help information with all available commands, operators, functions, and examples.

```bash
calc> help
```

### quit, exit

Exits the calculator. History is automatically saved.

```bash
calc> quit
Goodbye!

calc> exit
Goodbye!
```

### clear, cls

Clears the terminal screen.

```bash
calc> clear
```

### vars, list

Lists all currently defined variables with their values.

```bash
calc> vars
Variables:
  ans = 42.00
  x = 10.00
  y = 20.00
```

If no variables are defined:

```bash
calc> vars
No variables defined.
```

### clearvars

Clears all variables. This removes all stored values, including `ans`.

```bash
calc> clearvars
All variables cleared.
```

### delete <variable>

Deletes a specific variable by name.

```bash
calc> delete x
Variable 'x' deleted.

calc> delete notexist
Variable 'notexist' not found.
```

---

## Error Handling

pmatinit provides comprehensive error handling with helpful messages.

### Common Errors

**Syntax Errors:**
```bash
calc> 2 +
Error: Invalid syntax

calc> (2 + 3
Error: Unmatched parentheses
```

**Unknown Identifiers:**
```bash
calc> undefined_var + 5
Error: Unknown identifier: 'undefined_var'
  Available variables: x, y, ans
```

**Invalid Function Arguments:**
```bash
calc> sqrt(-1)
Error: Square root of negative number
  Tip: sqrt requires a non-negative argument

calc> log(-5)
Error: Logarithm of zero or negative number
  Tip: log requires a positive argument

calc> 5.5!
Error: Factorial requires a non-negative integer
  Tip: Factorial is only defined for whole numbers
```

**Division by Zero:**
```bash
calc> 10 / 0
Error: Division by zero

calc> 5 % 0
Error: Modulo by zero
```

**Overflow/Underflow:**
```bash
calc> 1000!
Error: Numeric overflow
```

### Tips for Avoiding Errors

1. **Check parentheses balance**: Make sure every `(` has a matching `)`
2. **Verify function arguments**: Check that arguments are in valid ranges
3. **Use tab completion**: Reduces typos in function names
4. **Check variable names**: Use `vars` to see defined variables
5. **Test complex expressions**: Build up complex expressions step by step

---

## Tips and Tricks

### Quick Calculations

Use single-expression mode for quick calculations without entering the REPL:

```bash
$ pmatinit "2 + 2"
4.00

$ pmatinit "sqrt(16)"
4.00

$ pmatinit "sin(pi / 2)"
1.00
```

### Building Complex Expressions

Break complex calculations into steps:

```bash
calc> a = 5
5.00

calc> b = 3
3.00

calc> c = 4
4.00

calc> s = (a + b + c) / 2
6.00

calc> area = sqrt(s * (s - a) * (s - b) * (s - c))
6.00           # Heron's formula for triangle area
```

### Using ans for Chaining

Chain calculations using the `ans` variable:

```bash
calc> 100
100.00

calc> ans / 2
50.00

calc> ans - 10
40.00

calc> sqrt(ans)
6.32

calc> ans ^ 2
40.00          # Verify: square root squared returns original
```

### Converting Units

Store conversion factors in variables:

```bash
calc> miles_to_km = 1.60934
1.61

calc> miles = 10
10.00

calc> miles * miles_to_km
16.09          # 10 miles in kilometers

calc> km_to_miles = 1 / miles_to_km
0.62

calc> 5 * km_to_miles
3.11           # 5 km in miles
```

### Solving Equations

Use variables to solve equations step by step:

```bash
# Solve: x = (-b ± sqrt(b^2 - 4ac)) / 2a
# For: 2x^2 + 5x - 3 = 0

calc> a = 2
2.00

calc> b = 5
5.00

calc> c = -3
-3.00

calc> discriminant = b ^ 2 - 4 * a * c
49.00

calc> x1 = (-b + sqrt(discriminant)) / (2 * a)
0.50

calc> x2 = (-b - sqrt(discriminant)) / (2 * a)
-3.00
```

### Trigonometry Helpers

Create helpers for degree-based trigonometry:

```bash
calc> deg2rad = pi / 180
0.02

calc> rad2deg = 180 / pi
57.30

calc> angle_deg = 45
45.00

calc> angle_rad = angle_deg * deg2rad
0.79

calc> sin(angle_rad)
0.71           # sin(45°)
```

---

## Advanced Examples

### Example 1: Circle Calculations

Calculate various properties of a circle:

```bash
calc> radius = 7
7.00

calc> diameter = 2 * radius
14.00

calc> circumference = 2 * pi * radius
43.98

calc> area = pi * radius ^ 2
153.94

calc> area / circumference
3.50           # Ratio equals radius/2
```

### Example 2: Compound Interest

Calculate compound interest:

```bash
# Formula: A = P(1 + r/n)^(nt)
# P = principal, r = rate, n = compounds/year, t = years

calc> principal = 1000
1000.00

calc> rate = 0.05
0.05

calc> compounds_per_year = 12
12.00

calc> years = 10
10.00

calc> amount = principal * (1 + rate / compounds_per_year) ^ (compounds_per_year * years)
1647.01

calc> interest = amount - principal
647.01         # Total interest earned
```

### Example 3: Pythagorean Theorem

Calculate the hypotenuse of a right triangle:

```bash
calc> a = 3
3.00

calc> b = 4
4.00

calc> c = sqrt(a ^ 2 + b ^ 2)
5.00           # 3-4-5 triangle
```

### Example 4: Distance Formula

Calculate distance between two points:

```bash
# Distance = sqrt((x2-x1)^2 + (y2-y1)^2)

calc> x1 = 1
1.00

calc> y1 = 2
2.00

calc> x2 = 4
4.00

calc> y2 = 6
6.00

calc> distance = sqrt((x2 - x1) ^ 2 + (y2 - y1) ^ 2)
5.00
```

### Example 5: Physics - Projectile Motion

Calculate maximum height of a projectile:

```bash
# h = (v^2 * sin^2(θ)) / (2g)

calc> velocity = 20
20.00

calc> angle_deg = 45
45.00

calc> angle_rad = angle_deg * pi / 180
0.79

calc> gravity = 9.8
9.80

calc> max_height = (velocity ^ 2 * sin(angle_rad) ^ 2) / (2 * gravity)
10.20          # Maximum height in meters
```

---

## Troubleshooting

### Issue: History not persisting

**Symptom:** Commands are not saved between sessions.

**Solution:**
- Check that you have write permissions to your home directory
- Verify `~/.pmatinit_history` can be created/modified
- Try running with appropriate permissions

### Issue: Tab completion not working

**Symptom:** Pressing TAB doesn't show completions.

**Solution:**
- Ensure you're running in an interactive terminal
- Tab completion doesn't work when input/output is piped
- Try running with `pmatinit -i` to force interactive mode

### Issue: Colors not displaying

**Symptom:** Syntax highlighting doesn't appear.

**Solution:**
- Verify your terminal supports ANSI color codes
- Most modern terminals support colors
- Try a different terminal emulator if needed

### Issue: Multi-line not working

**Symptom:** Expression evaluates immediately even with unclosed parentheses.

**Solution:**
- Make sure you're in interactive mode (not single-expression mode)
- Verify parentheses are actually unclosed
- Check that your terminal supports line editing

### Issue: Function not found

**Symptom:** Error saying a function is unknown.

**Solution:**
- Check spelling (functions are case-sensitive)
- Use tab completion to see available functions
- Type `help` to see list of supported functions

### Issue: Variable not found

**Symptom:** Error saying a variable is undefined.

**Solution:**
- Use `vars` command to list defined variables
- Remember variables are case-sensitive
- Assign the variable before using it

### Issue: Unexpected result

**Symptom:** Calculation gives unexpected result.

**Solution:**
- Check operator precedence (use parentheses to be explicit)
- Remember trigonometric functions use radians, not degrees
- Verify factorial only works with non-negative integers
- Check that variables have expected values using `vars`

### Issue: Overflow/underflow

**Symptom:** Error about numeric overflow.

**Solution:**
- Break calculation into smaller steps
- Factorials of large numbers overflow quickly
- Use logarithms for very large/small numbers

---

## Appendix: Function Reference

### Trigonometric Functions

| Function | Description | Domain | Range |
|----------|-------------|--------|-------|
| `sin(x)` | Sine of x (radians) | All real | [-1, 1] |
| `cos(x)` | Cosine of x (radians) | All real | [-1, 1] |
| `tan(x)` | Tangent of x (radians) | All real except π/2 + nπ | All real |

### Other Functions

| Function | Description | Domain | Range |
|----------|-------------|--------|-------|
| `sqrt(x)` | Square root of x | [0, ∞) | [0, ∞) |
| `log(x)` | Logarithm base 10 | (0, ∞) | All real |
| `ln(x)` | Natural logarithm (base e) | (0, ∞) | All real |

### Operators

| Operator | Description | Associativity | Precedence |
|----------|-------------|---------------|------------|
| `()` | Parentheses | N/A | Highest |
| `!` | Factorial (postfix) | Left | Very high |
| `^` | Power | Right | High |
| `*` | Multiplication | Left | Medium |
| `/` | Division | Left | Medium |
| `%` | Modulo | Left | Medium |
| `+` | Addition | Left | Low |
| `-` | Subtraction | Left | Low |
| `=` | Assignment | Right | Lowest |

### Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `pi` | 3.14159265358979... | Ratio of circle circumference to diameter |
| `e` | 2.71828182845904... | Euler's number (base of natural logarithm) |

---

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| `UP` | Previous command in history |
| `DOWN` | Next command in history |
| `TAB` | Auto-complete |
| `CTRL+C` | Cancel current line |
| `CTRL+D` | Exit calculator (same as quit) |
| `CTRL+L` | Clear screen (some terminals) |

---

## Getting Help

- Type `help` in the calculator for quick reference
- See [README.md](../README.md) for project overview
- Check [examples/](../examples/) for code examples
- Visit the GitHub repository for issues and discussions

---

**Version:** 0.1.0
**Last Updated:** 2025-10-25
