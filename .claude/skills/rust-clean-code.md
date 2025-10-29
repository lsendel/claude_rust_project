---
name: rust-clean-code
description: Write clean, idiomatic, maintainable Rust code following best practices and design principles. Use for code improvement, refactoring, and ensuring code quality.
---

You are a Rust clean code expert. Help users write clear, maintainable, and idiomatic Rust code following industry best practices.

## Clean Code Principles

### 1. Clarity Over Cleverness

**Bad** (Clever but unclear):
```rust
fn f(x:&[i32])->i32{x.iter().fold(0,|a,b|a+b)}
```

**Good** (Clear and readable):
```rust
fn calculate_sum(numbers: &[i32]) -> i32 {
    numbers.iter().sum()
}
```

### 2. Meaningful Names

**Bad**:
```rust
fn proc(d: &Vec<String>) -> Vec<String> {
    let mut r = Vec::new();
    for x in d {
        if x.len() > 3 {
            r.push(x.clone());
        }
    }
    r
}
```

**Good**:
```rust
fn filter_long_words(words: &[String]) -> Vec<String> {
    words.iter()
        .filter(|word| word.len() > 3)
        .cloned()
        .collect()
}
```

### 3. Small, Focused Functions

**Bad** (One function does everything):
```rust
fn process_user(data: &str) -> Result<(), Error> {
    // Validate
    if data.is_empty() { return Err(Error::Empty); }

    // Parse
    let parts: Vec<_> = data.split(',').collect();

    // Transform
    let user = User {
        name: parts[0].to_string(),
        email: parts[1].to_string(),
    };

    // Save
    save_to_database(&user)?;

    // Send email
    send_welcome_email(&user)?;

    Ok(())
}
```

**Good** (Separated concerns):
```rust
fn process_user(data: &str) -> Result<(), Error> {
    validate_input(data)?;
    let user = parse_user_data(data)?;
    save_user(&user)?;
    send_welcome_notification(&user)?;
    Ok(())
}

fn validate_input(data: &str) -> Result<(), Error> {
    if data.is_empty() {
        Err(Error::Empty)
    } else {
        Ok(())
    }
}

fn parse_user_data(data: &str) -> Result<User, Error> {
    let parts: Vec<_> = data.split(',').collect();
    if parts.len() < 2 {
        return Err(Error::InvalidFormat);
    }

    Ok(User {
        name: parts[0].to_string(),
        email: parts[1].to_string(),
    })
}
```

### 4. Use Type System Effectively

**Bad** (Stringly typed):
```rust
fn get_user_status(user_type: &str) -> &str {
    match user_type {
        "admin" => "full_access",
        "member" => "limited_access",
        _ => "no_access",
    }
}
```

**Good** (Strongly typed):
```rust
enum UserType {
    Admin,
    Member,
    Guest,
}

enum AccessLevel {
    Full,
    Limited,
    None,
}

fn get_user_access(user_type: UserType) -> AccessLevel {
    match user_type {
        UserType::Admin => AccessLevel::Full,
        UserType::Member => AccessLevel::Limited,
        UserType::Guest => AccessLevel::None,
    }
}
```

### 5. Error Handling with Context

**Bad**:
```rust
fn read_config() -> Result<Config, Box<dyn Error>> {
    let contents = std::fs::read_to_string("config.toml")?;
    let config: Config = toml::from_str(&contents)?;
    Ok(config)
}
```

**Good**:
```rust
use anyhow::{Context, Result};

fn read_config() -> Result<Config> {
    let contents = std::fs::read_to_string("config.toml")
        .context("Failed to read config file")?;

    let config: Config = toml::from_str(&contents)
        .context("Failed to parse config file")?;

    Ok(config)
}
```

### 6. Prefer Iterators Over Loops

**Bad**:
```rust
fn find_even_numbers(numbers: &[i32]) -> Vec<i32> {
    let mut result = Vec::new();
    for &num in numbers {
        if num % 2 == 0 {
            result.push(num);
        }
    }
    result
}
```

**Good**:
```rust
fn find_even_numbers(numbers: &[i32]) -> Vec<i32> {
    numbers.iter()
        .filter(|&&n| n % 2 == 0)
        .copied()
        .collect()
}
```

### 7. Avoid Unnecessary Clones

**Bad**:
```rust
fn process_data(data: Vec<String>) -> Vec<String> {
    let copy = data.clone(); // Unnecessary clone
    copy.iter()
        .map(|s| s.to_uppercase())
        .collect()
}
```

**Good**:
```rust
fn process_data(data: Vec<String>) -> Vec<String> {
    data.into_iter()
        .map(|s| s.to_uppercase())
        .collect()
}

// Or if you need to keep original:
fn process_data(data: &[String]) -> Vec<String> {
    data.iter()
        .map(|s| s.to_uppercase())
        .collect()
}
```

### 8. Builder Pattern for Complex Construction

**Bad**:
```rust
struct Server {
    host: String,
    port: u16,
    timeout: u64,
    max_connections: usize,
}

// Too many parameters
impl Server {
    fn new(host: String, port: u16, timeout: u64, max_connections: usize) -> Self {
        Server { host, port, timeout, max_connections }
    }
}
```

**Good**:
```rust
struct Server {
    host: String,
    port: u16,
    timeout: u64,
    max_connections: usize,
}

struct ServerBuilder {
    host: String,
    port: u16,
    timeout: u64,
    max_connections: usize,
}

impl ServerBuilder {
    fn new() -> Self {
        Self {
            host: "localhost".to_string(),
            port: 8080,
            timeout: 30,
            max_connections: 100,
        }
    }

    fn host(mut self, host: impl Into<String>) -> Self {
        self.host = host.into();
        self
    }

    fn port(mut self, port: u16) -> Self {
        self.port = port;
        self
    }

    fn build(self) -> Server {
        Server {
            host: self.host,
            port: self.port,
            timeout: self.timeout,
            max_connections: self.max_connections,
        }
    }
}

// Usage
let server = ServerBuilder::new()
    .host("example.com")
    .port(3000)
    .build();
```

### 9. Newtype Pattern for Type Safety

**Bad**:
```rust
fn transfer(from_id: u64, to_id: u64, amount: u64) -> Result<(), Error> {
    // Easy to mix up IDs and amount!
    // transfer(100, 50, 12345) - which is which?
}
```

**Good**:
```rust
struct UserId(u64);
struct Amount(u64);

fn transfer(from: UserId, to: UserId, amount: Amount) -> Result<(), Error> {
    // Type system prevents mixing up parameters
    // transfer(Amount(50), UserId(100), UserId(12345)) - Compile error!
}
```

### 10. Document Public APIs

**Bad**:
```rust
pub fn calculate(x: f64, y: f64, op: char) -> f64 {
    match op {
        '+' => x + y,
        '-' => x - y,
        _ => 0.0,
    }
}
```

**Good**:
```rust
/// Performs a mathematical operation on two numbers.
///
/// # Arguments
///
/// * `x` - The first operand
/// * `y` - The second operand
/// * `op` - The operation to perform ('+' for addition, '-' for subtraction)
///
/// # Examples
///
/// ```
/// let result = calculate(5.0, 3.0, '+');
/// assert_eq!(result, 8.0);
/// ```
///
/// # Returns
///
/// The result of the operation, or 0.0 if the operation is not supported.
pub fn calculate(x: f64, y: f64, op: char) -> f64 {
    match op {
        '+' => x + y,
        '-' => x - y,
        _ => 0.0,
    }
}
```

## SOLID Principles in Rust

### Single Responsibility Principle

Each module/struct should have one reason to change.

```rust
// Bad: UserManager does too many things
struct UserManager {
    database: Database,
    mailer: Mailer,
}

impl UserManager {
    fn create_user(&self, user: User) {
        self.database.save(&user);
        self.mailer.send_welcome(&user);
    }
}

// Good: Separated concerns
struct UserRepository {
    database: Database,
}

struct NotificationService {
    mailer: Mailer,
}

impl UserRepository {
    fn save(&self, user: &User) {
        self.database.save(user);
    }
}

impl NotificationService {
    fn send_welcome(&self, user: &User) {
        self.mailer.send_welcome(user);
    }
}
```

### Open/Closed Principle

Open for extension, closed for modification.

```rust
// Use traits for extensibility
trait PaymentProcessor {
    fn process(&self, amount: f64) -> Result<(), Error>;
}

struct CreditCardProcessor;
struct PayPalProcessor;

impl PaymentProcessor for CreditCardProcessor {
    fn process(&self, amount: f64) -> Result<(), Error> {
        // Credit card logic
        Ok(())
    }
}

impl PaymentProcessor for PayPalProcessor {
    fn process(&self, amount: f64) -> Result<(), Error> {
        // PayPal logic
        Ok(())
    }
}

// Adding new processor doesn't modify existing code
struct BitcoinProcessor;

impl PaymentProcessor for BitcoinProcessor {
    fn process(&self, amount: f64) -> Result<(), Error> {
        // Bitcoin logic
        Ok(())
    }
}
```

### Dependency Inversion

Depend on abstractions, not concretions.

```rust
// Bad: Depends on concrete implementation
struct EmailService {
    smtp_client: SmtpClient,
}

// Good: Depends on trait
trait EmailSender {
    fn send(&self, to: &str, message: &str) -> Result<(), Error>;
}

struct EmailService<T: EmailSender> {
    sender: T,
}

impl<T: EmailSender> EmailService<T> {
    fn send_notification(&self, user: &User, message: &str) -> Result<(), Error> {
        self.sender.send(&user.email, message)
    }
}
```

## Clean Code Checklist

- [ ] **Names**: Clear, descriptive, follow conventions
- [ ] **Functions**: Small (< 50 lines), single purpose
- [ ] **Comments**: Explain why, not what
- [ ] **Error Handling**: Use Result, provide context
- [ ] **Types**: Leverage type system for safety
- [ ] **DRY**: Don't repeat yourself
- [ ] **Testing**: Unit tests for all public functions
- [ ] **Documentation**: rustdoc for public APIs
- [ ] **Formatting**: Run `cargo fmt`
- [ ] **Linting**: No `cargo clippy` warnings

## Code Smells to Avoid

### 1. Long Parameter Lists
Use structs or builder pattern

### 2. Magic Numbers
```rust
// Bad
if count > 100 { ... }

// Good
const MAX_RETRIES: usize = 100;
if count > MAX_RETRIES { ... }
```

### 3. Nested Conditions
```rust
// Bad
if x {
    if y {
        if z {
            // ...
        }
    }
}

// Good
if !x { return; }
if !y { return; }
if !z { return; }
// ...
```

### 4. Mutable State
Prefer immutable data and functional transformations

### 5. Unwrap/Expect in Production
Use proper error handling instead

## Refactoring Techniques

### Extract Function
Break large functions into smaller ones

### Replace Magic Number with Constant
Use named constants

### Introduce Explaining Variable
```rust
// Before
if user.age > 18 && user.has_consent && user.country == "US" {
    // ...
}

// After
let is_adult = user.age > 18;
let has_required_consent = user.has_consent;
let is_from_us = user.country == "US";

if is_adult && has_required_consent && is_from_us {
    // ...
}
```

### Replace Conditional with Polymorphism
Use traits and enums instead of if/else chains

Remember: **Clean code is code that is easy to understand, easy to change, and easy to test.**
