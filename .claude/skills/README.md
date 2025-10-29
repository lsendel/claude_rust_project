# Rust Development Skills

This directory contains Claude Code skills for Rust development. Skills are focused, reusable prompts that help with specific development tasks.

## What are Skills?

Skills are different from subagents:
- **Skills**: Focused prompts for specific tasks (debugging, TDD, explaining errors)
- **Subagents**: Comprehensive agents for larger workflows (building features, testing entire modules)

Use skills when you need quick, focused help. Use subagents (in `.claude/agents/`) for complex, multi-step tasks.

## Available Skills

| Skill | Purpose | When to Use |
|-------|---------|-------------|
| `rust-debug` | Debug Rust code | Fixing bugs, understanding errors, adding logging |
| `rust-tdd` | Test-Driven Development | Writing tests first, TDD workflow |
| `rust-lifetimes` | Lifetime & borrow checker help | Resolving ownership/borrowing issues |
| `rust-compiler-errors` | Explain compiler errors | Understanding cryptic error messages |
| `rust-port` | Port code to Rust | Translating from Python, JS, Go, C++ |
| `rust-learning` | Interactive learning assistant | Learning Rust concepts, practicing |
| `rust-concurrency` | Concurrent programming | Threads, async/await, channels |
| `rust-clean-code` | Clean code principles | Writing maintainable, idiomatic code |

## How to Use Skills

### In Claude Code

Simply mention the skill name in your message:

```
Can you help me debug this Rust code?
[paste code]
```

Claude Code will automatically use the appropriate skill based on context, or you can explicitly invoke:

```
Using rust-debug: Help me fix this borrow checker error
[paste code]
```

## Skill Descriptions

### 1. rust-debug 🐛
**Debugging Assistant**

Help with:
- Analyzing error messages
- Adding strategic logging
- Fixing panics and logic errors
- Explaining what went wrong

**Example use**:
```
rust-debug: Why does this code panic?

fn main() {
    let v = vec![1, 2, 3];
    println!("{}", v[5]); // Index out of bounds
}
```

### 2. rust-tdd ✅
**Test-Driven Development**

Help with:
- Writing tests before implementation
- Following Red-Green-Refactor cycle
- Structuring test suites
- Property-based testing

**Example use**:
```
rust-tdd: I want to implement a function that reverses a string.
Let's start with tests.
```

### 3. rust-lifetimes 🔗
**Lifetime & Borrow Checker Helper**

Help with:
- Understanding ownership errors
- Adding lifetime annotations
- Fixing borrow checker errors
- Resolving move errors

**Example use**:
```
rust-lifetimes: How do I fix this error?

error[E0597]: `s` does not live long enough
```

### 4. rust-compiler-errors 📋
**Error Translator**

Help with:
- Explaining cryptic error messages
- Understanding error codes (E0XXX)
- Fixing type mismatches
- Resolving trait bounds

**Example use**:
```
rust-compiler-errors: What does E0382 mean and how do I fix it?

error[E0382]: use of moved value: `s`
```

### 5. rust-port 🔄
**Code Translation**

Help with:
- Porting Python/JS/Go/C++ to Rust
- Finding Rust equivalents
- Idiomatic translations
- Library mappings

**Example use**:
```
rust-port: Convert this Python code to Rust:

def find_max(numbers):
    return max(numbers) if numbers else None
```

### 6. rust-learning 📚
**Learning Assistant**

Help with:
- Understanding Rust concepts
- Interactive learning
- Practice exercises
- Project guidance

**Example use**:
```
rust-learning: Explain how ownership works in Rust with examples
```

### 7. rust-concurrency 🔀
**Concurrent Programming**

Help with:
- Threading (std::thread)
- Async/await (tokio)
- Message passing (channels)
- Synchronization primitives

**Example use**:
```
rust-concurrency: How do I share data between threads safely?
```

### 8. rust-clean-code 🧹
**Code Quality & Best Practices**

Help with:
- Writing maintainable code
- Refactoring
- Design patterns
- SOLID principles in Rust

**Example use**:
```
rust-clean-code: Review this code and suggest improvements

[paste code]
```

## Skill Combinations

Skills work great together:

### Example 1: Learn and Practice
```
1. rust-learning: Teach me about Result types
2. rust-tdd: Now let's write tests for error handling
3. rust-clean-code: Review my implementation
```

### Example 2: Debug and Improve
```
1. rust-debug: Fix this bug
2. rust-compiler-errors: Explain the errors
3. rust-clean-code: Suggest improvements
```

### Example 3: Port and Test
```
1. rust-port: Convert this Python code
2. rust-tdd: Write tests for it
3. rust-clean-code: Make it idiomatic
```

## Skill vs Subagent Decision

**Use Skills When**:
- Quick, focused task
- Learning or understanding
- Debugging single issue
- Translating small code snippet

**Use Subagents When**:
- Building complete features
- Testing entire modules
- Writing full documentation
- Complex multi-step workflows
- Coordinating multiple tasks

### Example Comparison

**Skill**: "rust-debug: Fix this borrow checker error" (2-5 minutes)

**Subagent**: "rust-feature-builder: Implement the complete parser module with tests" (30-60 minutes)

## Best Practices

### 1. Be Specific
```
Bad: "Help with Rust"
Good: "rust-lifetimes: Why can't I return a reference to a local variable?"
```

### 2. Provide Context
```
Good: "rust-debug: This function panics when input is empty
[paste code]
[paste error]"
```

### 3. One Skill Per Task
Don't try to combine multiple skills in one request

### 4. Use Skills for Learning
Skills are great for understanding concepts:
```
"rust-learning: Explain what async/await does"
```

### 5. Iterate
Use skills to progressively improve code:
```
1. rust-port (translate)
2. rust-clean-code (improve)
3. rust-tdd (add tests)
```

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────────┐
│  Rust Development Skills Quick Reference                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  🐛 rust-debug           → Fix bugs and errors              │
│  ✅ rust-tdd             → Test-driven development          │
│  🔗 rust-lifetimes       → Ownership & borrowing            │
│  📋 rust-compiler-errors → Explain error messages           │
│  🔄 rust-port            → Translate to Rust                │
│  📚 rust-learning        → Learn Rust concepts              │
│  🔀 rust-concurrency     → Threading & async                │
│  🧹 rust-clean-code      → Write clean code                 │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  Usage: Just mention what you need help with!               │
│                                                              │
│  Example: "I'm getting a borrow checker error..."           │
│  → Automatically uses rust-lifetimes skill                  │
└─────────────────────────────────────────────────────────────┘
```

## Common Workflows

### Workflow 1: New Rust Developer

```
Day 1-7:
- rust-learning: Core concepts
- rust-compiler-errors: Understand errors
- rust-debug: Fix simple bugs

Day 8-14:
- rust-tdd: Write tests
- rust-lifetimes: Master borrowing
- rust-clean-code: Write better code

Day 15-21:
- rust-concurrency: Learn async
- rust-port: Translate existing code
- All skills: Build projects
```

### Workflow 2: Experienced Developer New to Rust

```
Week 1:
- rust-port: Translate familiar patterns
- rust-lifetimes: Understand ownership
- rust-compiler-errors: Quick error resolution

Week 2:
- rust-clean-code: Learn Rust idioms
- rust-concurrency: Safe concurrency
- rust-tdd: Rust testing patterns

Week 3+:
- Build projects with all skills
```

### Workflow 3: Debugging Session

```
1. rust-debug: Identify the bug
2. rust-compiler-errors: Understand errors
3. rust-lifetimes: Fix ownership issues
4. rust-tdd: Add tests to prevent regression
5. rust-clean-code: Clean up the fix
```

### Workflow 4: Feature Development

```
1. rust-tdd: Write tests first
2. rust-clean-code: Implement cleanly
3. rust-debug: Fix any issues
4. rust-compiler-errors: Resolve warnings
5. rust-clean-code: Final polish
```

## Integration with Subagents

Skills complement subagents:

**For Project Work**: Use subagents
- rust-orchestrator: Coordinate phases
- rust-feature-builder: Build features
- rust-tester: Full test suites

**For Specific Issues**: Use skills
- rust-debug: Fix specific bug
- rust-lifetimes: Resolve borrow error
- rust-compiler-errors: Understand error

**Combined Example**:
```
1. Subagent: rust-feature-builder implements feature
2. Skill: rust-debug fixes error that comes up
3. Skill: rust-clean-code improves implementation
4. Subagent: rust-tester adds comprehensive tests
```

## Tips for Success

✅ **Start with skills** for learning and quick tasks
✅ **Progress to subagents** for larger features
✅ **Combine skills** for complex problem-solving
✅ **Use skills to understand** subagent suggestions
✅ **Practice regularly** with rust-tdd and rust-learning

## Resources

- [The Rust Book](https://doc.rust-lang.org/book/)
- [Rust by Example](https://doc.rust-lang.org/rust-by-example/)
- [Rustlings](https://github.com/rust-lang/rustlings)
- [Exercism Rust Track](https://exercism.org/tracks/rust)

## Getting Help

If you're unsure which skill to use:
1. Describe your problem naturally
2. Claude Code will suggest the right skill
3. Or use rust-learning to understand options

---

**Remember**: Skills are your quick helpers for focused Rust development tasks. Use them often!

Happy Rust coding! 🦀
