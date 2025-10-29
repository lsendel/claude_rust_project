# Rust Calculator CLI Project - Subagents Guide

This directory contains specialized subagents designed to help complete all phases of the Rust calculator CLI project. Each subagent is an expert in a specific domain and can be invoked using Claude Code's Task tool.

## Quick Reference

| Subagent | Use For | Key Phases |
|----------|---------|------------|
| `rust-orchestrator` | Coordinating all subagents, project management | All phases |
| `rust-feature-builder` | Implementing features, writing code | 1-3 |
| `rust-tester` | Writing tests, achieving coverage | 4 |
| `rust-documentation` | API docs, user guides, architecture docs | 5 |
| `rust-code-reviewer` | Code review, quality improvements | 5 |
| `rust-performance-optimizer` | Profiling, benchmarking, optimization | 5 |
| `aws-deployment-expert` | Cloud deployment, CI/CD, infrastructure | 6+ |
| `pmat-mcp-expert` | Code quality analysis, PMAT tooling | 4-5 |

## Subagent Details

### 1. rust-orchestrator
**The Project Manager**

The orchestrator coordinates all other subagents and manages the overall project workflow. Start here for complex multi-phase tasks.

**When to use:**
- Starting a new phase
- Need to coordinate multiple specialists
- Planning complex features
- Managing project dependencies

**Example usage:**
```
Task: rust-orchestrator
Prompt: "Execute Phase 1 of the calculator project: Core Calculator Engine. Coordinate all necessary subagents to complete the 5 tasks."
```

### 2. rust-feature-builder
**The Implementation Specialist**

Expert Rust developer for building new features and functionality.

**When to use:**
- Implementing new features
- Writing module code
- Creating algorithms (parser, evaluator)
- Building CLI/REPL components

**Example usage:**
```
Task: rust-feature-builder
Prompt: "Implement Task 1.2: Create a tokenizer for mathematical expressions. Include comprehensive unit tests inline."
```

### 3. rust-tester
**The Quality Assurance Expert**

Specialist in writing comprehensive test suites and achieving high code coverage.

**When to use:**
- Writing unit tests
- Creating integration tests
- Property-based testing
- Improving test coverage
- Finding edge cases

**Example usage:**
```
Task: rust-tester
Prompt: "Write comprehensive tests for the parser module. Include unit tests, integration tests, and property-based tests. Target > 90% coverage."
```

### 4. rust-documentation
**The Documentation Specialist**

Expert in writing clear, comprehensive API documentation and user guides.

**When to use:**
- Writing rustdoc comments
- Creating user guides
- Architecture documentation
- README updates
- Examples

**Example usage:**
```
Task: rust-documentation
Prompt: "Document the calculator module with comprehensive API documentation. Include examples, error documentation, and module overview."
```

### 5. rust-code-reviewer
**The Quality Guardian**

Code review expert who ensures best practices, security, and maintainability.

**When to use:**
- Code review
- Finding code smells
- Security audit
- Refactoring suggestions
- Enforcing Rust idioms

**Example usage:**
```
Task: rust-code-reviewer
Prompt: "Review the parser and evaluator modules. Check for quality issues, security problems, and suggest improvements."
```

### 6. rust-performance-optimizer
**The Performance Specialist**

Expert in profiling, benchmarking, and optimizing Rust applications.

**When to use:**
- Performance profiling
- Creating benchmarks
- Optimizing hot paths
- Reducing binary size
- Memory optimization

**Example usage:**
```
Task: rust-performance-optimizer
Prompt: "Profile the calculator performance and optimize the parser. Create benchmarks to measure improvements."
```

### 7. aws-deployment-expert
**The Cloud Architect**

AWS specialist for deploying Rust applications to the cloud.

**When to use:**
- Cloud deployment
- Infrastructure as Code
- CI/CD pipelines
- Containerization
- Serverless deployment

**Example usage:**
```
Task: aws-deployment-expert
Prompt: "Deploy the calculator as an AWS Lambda function with API Gateway. Set up CI/CD pipeline for automated deployments."
```

### 8. pmat-mcp-expert
**The Code Quality Analyst**

Specialist in PMAT tooling and Model Context Protocol integration.

**When to use:**
- Code quality analysis
- Mutation testing
- Generating AI context
- Refactoring guidance
- MCP integration

**Example usage:**
```
Task: pmat-mcp-expert
Prompt: "Run PMAT analysis on the calculator codebase. Generate quality report and mutation testing results. Provide refactoring suggestions."
```

## Recommended Workflows

### Starting a New Phase

1. **Use rust-orchestrator** to plan and coordinate
2. Orchestrator delegates to appropriate specialists
3. Monitor progress through TodoWrite updates
4. Verify quality gates before advancing

### Implementing a Feature

1. **rust-feature-builder** - Implement the feature
2. **rust-tester** - Add comprehensive tests
3. **rust-code-reviewer** - Review for quality
4. **rust-documentation** - Document the feature

### Optimizing Existing Code

1. **pmat-mcp-expert** - Analyze code quality
2. **rust-code-reviewer** - Identify issues
3. **rust-performance-optimizer** - Profile and optimize
4. **rust-tester** - Verify correctness maintained

### Preparing for Production

1. **rust-code-reviewer** - Final quality check
2. **rust-tester** - Ensure > 80% coverage
3. **rust-documentation** - Complete all docs
4. **rust-performance-optimizer** - Benchmark performance
5. **aws-deployment-expert** - Deploy to cloud

## Usage Tips

### Invoking Subagents

In Claude Code, use the Task tool:

```
Task: <subagent-name>
Prompt: "Detailed description of what you want the subagent to do"
```

### Running Multiple Subagents in Parallel

For independent tasks, invoke multiple subagents simultaneously:

```
# In a single message, use multiple Task tool calls
Task: rust-feature-builder (for CLI module)
Task: rust-feature-builder (for REPL module)
```

### Sequential Task Chains

For dependent tasks, run sequentially:

```
1. Task: rust-feature-builder → Implement feature
2. Wait for completion
3. Task: rust-tester → Test feature
4. Wait for completion
5. Task: rust-code-reviewer → Review code
```

## Subagent Capabilities Summary

### Tools Access

All subagents have access to:
- Read/Write/Edit files
- Bash commands
- Glob (file pattern matching)
- Grep (code search)
- TodoWrite (task tracking)

Some subagents have additional tools:
- **rust-orchestrator**: Task (can launch other subagents)
- **aws-deployment-expert**: WebFetch (for AWS docs)
- **pmat-mcp-expert**: WebFetch (for MCP/PMAT docs)

### Model Selection

All subagents use the `sonnet` model for balanced performance and capability.

## Project Phases Reference

From `/docs/todo/hello-world-rust-calculator-cli.md`:

- **Phase 0**: Project Setup ✅ (COMPLETED)
- **Phase 1**: Core Calculator Engine
  - Tasks 1.1-1.5 (Operators, Tokenizer, Parser, Evaluator, Module Interface)
- **Phase 2**: CLI Interface
  - Tasks 2.1-2.3 (Argument Parsing, Main Entry, Single Expression Mode)
- **Phase 3**: REPL Implementation
  - Tasks 3.1-3.4 (Infrastructure, Commands, Integration, Polish)
- **Phase 4**: Testing & Quality Assurance
  - Tasks 4.1-4.4 (Unit Tests, Integration Tests, Property Tests, Manual Testing)
- **Phase 5**: Documentation & Polish
  - Tasks 5.1-5.4 (Code Docs, User Docs, Quality Improvements, Performance)
- **Phase 6**: Release Preparation
  - Tasks 6.1-6.3 (Version/Metadata, Build/Test, Distribution)

## Getting Started

### Option 1: Use the Orchestrator (Recommended)

The easiest way to proceed is to use the orchestrator:

```
Task: rust-orchestrator
Prompt: "I want to complete Phase 1 of the calculator project. Please coordinate all necessary subagents to implement the core calculator engine."
```

The orchestrator will:
1. Read the spec
2. Plan the work
3. Delegate to appropriate subagents
4. Track progress
5. Report completion

### Option 2: Direct Subagent Usage

For specific tasks, invoke subagents directly:

```
Task: rust-feature-builder
Prompt: "Implement the expression parser using the Shunting Yard algorithm as specified in Task 1.3 of the spec."
```

### Option 3: Mixed Approach

Use orchestrator for phases, specialists for specific improvements:

```
# Complete a phase
Task: rust-orchestrator
Prompt: "Execute Phase 2"

# Then optimize specific parts
Task: rust-performance-optimizer
Prompt: "Optimize the parser performance"
```

## Quality Gates

Before marking any phase complete, ensure:

✅ **Code Quality**
- All tests pass (`cargo test`)
- No clippy warnings (`cargo clippy`)
- Code reviewed by rust-code-reviewer

✅ **Coverage**
- Test coverage > 80%
- Critical paths have 100% coverage
- Edge cases tested

✅ **Documentation**
- Public API documented
- Examples provided and tested
- README updated

✅ **Performance**
- Benchmarks created
- Performance targets met
- No obvious bottlenecks

## Troubleshooting

### Subagent Not Found

Ensure you're in the project directory where `.claude/agents/` exists.

### Task Failures

If a subagent task fails:
1. Review the error message
2. Check if prerequisites are met
3. Try breaking the task into smaller pieces
4. Use rust-orchestrator to coordinate complex tasks

### Conflicting Changes

If multiple subagents make conflicting changes:
1. Use rust-code-reviewer to assess both approaches
2. Manually merge the best parts
3. Run tests to verify correctness

## Examples

### Complete Example: Adding a New Operator

```
# Step 1: Implement the feature
Task: rust-feature-builder
Prompt: "Add power (^) operator to the calculator. Update operators.rs with Power variant, add to precedence rules, implement in parser and evaluator. Include unit tests."

# Step 2: Test thoroughly
Task: rust-tester
Prompt: "Write comprehensive tests for the power operator. Include edge cases: negative exponents, zero exponent, fractional exponents. Add property-based tests."

# Step 3: Review code quality
Task: rust-code-reviewer
Prompt: "Review the power operator implementation. Check error handling, performance, and Rust idioms."

# Step 4: Document
Task: rust-documentation
Prompt: "Document the power operator in API docs and user guide. Add examples showing usage."
```

### Complete Example: Deploying to AWS

```
# Use orchestrator for complex deployment
Task: rust-orchestrator
Prompt: "I want to deploy the calculator to AWS Lambda with API Gateway. Coordinate aws-deployment-expert to set up infrastructure, rust-tester to create deployment tests, and rust-documentation to update deployment docs."
```

## Next Steps

1. **Review the spec**: Read `/docs/todo/hello-world-rust-calculator-cli.md`
2. **Choose approach**: Orchestrator-driven or direct subagent usage
3. **Start Phase 1**: Begin with core calculator implementation
4. **Track progress**: Use TodoWrite to monitor completion
5. **Verify quality**: Check quality gates before advancing phases

## Getting Help

- **Check subagent files**: Each `.md` file contains detailed instructions
- **Use orchestrator**: When unsure, let the orchestrator coordinate
- **Break down tasks**: Complex tasks into smaller, manageable pieces
- **Read the spec**: Always refer to the project specification

---

**Remember**: These subagents are tools to help you complete the project efficiently. The orchestrator can coordinate them, or you can use them directly based on your needs.

Happy coding! 🦀
