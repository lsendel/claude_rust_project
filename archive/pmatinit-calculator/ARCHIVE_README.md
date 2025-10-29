# Archive: pmatinit Rust Calculator

**Archived Date**: 2025-10-27
**Reason**: Repository transitioned to Multi-Tenant SaaS Platform project

## What Was Archived

This directory contains the original **pmatinit** Rust CLI calculator application that was replaced by the Multi-Tenant SaaS Platform feature (001-saas-platform).

### Archived Files

- `Cargo.toml`, `Cargo.lock` - Rust build configuration
- `src/` - Complete Rust source code (calculator, REPL, CLI)
- `tests/` - 160+ unit and integration tests
- `examples/` - Usage examples
- `docs/` - Calculator documentation
- `README-calculator.md` - Original README
- `CHANGELOG.md` - Version history
- `CONTRIBUTING.md` - Development guidelines
- `PHASE3_*.md`, `PHASE5_*.md` - Development phase reports

### Original Project Details

- **Type**: Command-line calculator with interactive REPL
- **Language**: Rust (2021 edition)
- **Features**:
  - Basic arithmetic, power, factorial
  - Mathematical functions (sin, cos, tan, sqrt, log, ln)
  - Variables and constants (pi, e, ans)
  - Tab completion, syntax highlighting, command history
  - 160+ passing tests

### Access to Original Code

The complete calculator source code is preserved in this archive directory. To restore or reference:

```bash
# View archived source
ls archive/pmatinit-calculator/src/

# Restore if needed (from repo root)
cp -r archive/pmatinit-calculator/* .
```

### Git History

All commits and development history for the calculator are preserved in the Git history. Use:

```bash
# View calculator commits
git log --before="2025-10-27" -- src/ Cargo.toml
```

## New Project Direction

The repository has been repurposed for the **Multi-Tenant SaaS Platform** - an enterprise project management system with:
- Spring Boot 3.x backend (Java 17)
- React 18 frontend (TypeScript)
- AWS Lambda automation functions (Node.js)
- PostgreSQL database
- AWS infrastructure (Cognito, RDS, S3, CloudFront)

See the current README.md in the repository root for details on the SaaS platform.
