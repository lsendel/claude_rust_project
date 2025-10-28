#!/bin/bash
# Local Environment Setup Script
# Multi-Tenant SaaS Platform - Automated Development Environment Setup

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if command exists
command_exists() {
    command -v "$1" &> /dev/null
}

# Install Homebrew (macOS)
install_homebrew() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if ! command_exists brew; then
            print_info "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            print_success "Homebrew installed"
        else
            print_success "Homebrew already installed"
        fi
    fi
}

# Install Java 21
install_java() {
    print_header "Installing Java 21 LTS"

    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            print_success "Java $JAVA_VERSION already installed"
            return 0
        fi
    fi

    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Installing Java 21 via Homebrew..."
        brew install --cask temurin@21
        print_success "Java 21 installed"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Installing Java 21 via apt..."
        sudo apt-get update
        sudo apt-get install -y openjdk-21-jdk
        print_success "Java 21 installed"
    else
        print_error "Unsupported OS. Please install Java 21 manually from https://adoptium.net/"
        return 1
    fi
}

# Install Maven
install_maven() {
    print_header "Installing Maven"

    if command_exists mvn; then
        print_success "Maven already installed: $(mvn -version | head -n 1)"
        return 0
    fi

    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Installing Maven via Homebrew..."
        brew install maven
        print_success "Maven installed"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Installing Maven via apt..."
        sudo apt-get update
        sudo apt-get install -y maven
        print_success "Maven installed"
    else
        print_error "Unsupported OS. Please install Maven manually from https://maven.apache.org/"
        return 1
    fi
}

# Install Node.js
install_node() {
    print_header "Installing Node.js 18+"

    if command_exists node; then
        NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [ "$NODE_VERSION" -ge 18 ]; then
            print_success "Node.js $NODE_VERSION already installed"
            return 0
        fi
    fi

    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Installing Node.js via Homebrew..."
        brew install node@18
        print_success "Node.js installed"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Installing Node.js via nvm..."
        curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
        nvm install 18
        nvm use 18
        print_success "Node.js installed"
    else
        print_error "Unsupported OS. Please install Node.js manually from https://nodejs.org/"
        return 1
    fi
}

# Install Docker
install_docker() {
    print_header "Installing Docker"

    if command_exists docker; then
        print_success "Docker already installed: $(docker --version)"
        return 0
    fi

    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Installing Docker Desktop via Homebrew..."
        brew install --cask docker
        print_success "Docker Desktop installed"
        print_warning "Please start Docker Desktop manually"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Installing Docker via apt..."
        sudo apt-get update
        sudo apt-get install -y docker.io docker-compose
        sudo systemctl start docker
        sudo systemctl enable docker
        sudo usermod -aG docker $USER
        print_success "Docker installed"
        print_warning "Please log out and log back in for Docker group membership to take effect"
    else
        print_error "Unsupported OS. Please install Docker manually from https://www.docker.com/"
        return 1
    fi
}

# Install PostgreSQL client
install_postgres_client() {
    print_header "Installing PostgreSQL Client"

    if command_exists psql; then
        print_success "PostgreSQL client already installed: $(psql --version)"
        return 0
    fi

    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Installing PostgreSQL client via Homebrew..."
        brew install libpq
        brew link --force libpq
        print_success "PostgreSQL client installed"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Installing PostgreSQL client via apt..."
        sudo apt-get update
        sudo apt-get install -y postgresql-client
        print_success "PostgreSQL client installed"
    else
        print_warning "PostgreSQL client not installed (optional)"
    fi
}

# Setup PostgreSQL Docker container
setup_postgres() {
    print_header "Setting Up PostgreSQL Database"

    if docker ps -a | grep -q saasplatform-postgres; then
        print_warning "PostgreSQL container already exists"

        # Check if it's running
        if docker ps | grep -q saasplatform-postgres; then
            print_success "PostgreSQL container is running"
        else
            print_info "Starting PostgreSQL container..."
            docker start saasplatform-postgres
            print_success "PostgreSQL container started"
        fi
        return 0
    fi

    print_info "Starting PostgreSQL via Docker Compose..."
    docker-compose up -d postgres

    print_info "Waiting for PostgreSQL to be ready..."
    sleep 5

    # Test connection
    if docker exec saasplatform-postgres pg_isready -U postgres &> /dev/null; then
        print_success "PostgreSQL is ready"
    else
        print_error "PostgreSQL failed to start"
        return 1
    fi
}

# Setup backend dependencies
setup_backend() {
    print_header "Setting Up Backend"

    cd backend

    print_info "Installing Maven dependencies..."
    mvn clean install -DskipTests

    print_info "Running Flyway migrations..."
    mvn flyway:migrate

    cd ..

    print_success "Backend setup complete"
}

# Setup frontend dependencies
setup_frontend() {
    print_header "Setting Up Frontend"

    cd frontend

    print_info "Installing npm dependencies..."
    npm install

    # Create .env.local if it doesn't exist
    if [ ! -f .env.local ]; then
        print_info "Creating .env.local file..."
        cat > .env.local <<EOF
# Local Development Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=10000

# Feature Flags
VITE_ENABLE_OAUTH=false
VITE_ENABLE_ANALYTICS=false

# AWS Cognito (Optional for local dev)
# VITE_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
# VITE_COGNITO_CLIENT_ID=your-client-id
# VITE_COGNITO_DOMAIN=your-domain.auth.us-east-1.amazoncognito.com
EOF
        print_success ".env.local created"
    else
        print_warning ".env.local already exists (skipping)"
    fi

    cd ..

    print_success "Frontend setup complete"
}

# Create run script
create_run_script() {
    print_header "Creating Run Scripts"

    # Backend run script
    cat > run-backend.sh <<'EOF'
#!/bin/bash
echo "Starting backend on http://localhost:8080"
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
EOF
    chmod +x run-backend.sh
    print_success "Created run-backend.sh"

    # Frontend run script
    cat > run-frontend.sh <<'EOF'
#!/bin/bash
echo "Starting frontend on http://localhost:5173"
cd frontend
npm run dev
EOF
    chmod +x run-frontend.sh
    print_success "Created run-frontend.sh"

    # Combined run script (uses tmux if available)
    cat > run-all.sh <<'EOF'
#!/bin/bash

if command -v tmux &> /dev/null; then
    echo "Starting backend and frontend in tmux session..."
    tmux new-session -d -s saas-platform
    tmux split-window -h -t saas-platform
    tmux send-keys -t saas-platform:0.0 'cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local' C-m
    tmux send-keys -t saas-platform:0.1 'cd frontend && npm run dev' C-m
    tmux attach -t saas-platform
else
    echo "tmux not installed. Starting backend in current terminal..."
    echo "Open a new terminal and run: ./run-frontend.sh"
    cd backend
    mvn spring-boot:run -Dspring-boot.run.profiles=local
fi
EOF
    chmod +x run-all.sh
    print_success "Created run-all.sh"
}

# Print summary
print_setup_summary() {
    print_header "Setup Complete!"

    echo ""
    echo -e "${GREEN}✓ Local development environment is ready!${NC}"
    echo ""
    echo "Services:"
    echo "  - PostgreSQL: localhost:5432 (user: postgres, password: postgres)"
    echo "  - Backend API: http://localhost:8080"
    echo "  - Frontend: http://localhost:5173"
    echo ""
    echo "Quick Start:"
    echo ""
    echo "Option 1: Run services in separate terminals"
    echo "  Terminal 1: ./run-backend.sh"
    echo "  Terminal 2: ./run-frontend.sh"
    echo ""
    echo "Option 2: Run both services (requires tmux)"
    echo "  ./run-all.sh"
    echo ""
    echo "Health Checks:"
    echo "  - Backend: curl http://localhost:8080/actuator/health"
    echo "  - Database: docker exec saasplatform-postgres pg_isready -U postgres"
    echo ""
    echo "Useful Commands:"
    echo "  - View logs: docker-compose logs -f postgres"
    echo "  - Reset database: mvn -f backend/pom.xml flyway:clean flyway:migrate"
    echo "  - Run tests: cd backend && mvn test"
    echo "  - Build: cd backend && mvn clean package"
    echo ""
    echo "Documentation:"
    echo "  - Quickstart Guide: QUICKSTART.md"
    echo "  - Deployment Guide: infrastructure/DEPLOYMENT.md"
    echo "  - API Documentation: http://localhost:8080/swagger-ui.html (if enabled)"
    echo ""
}

##############################################################################
# Main Execution
##############################################################################

print_header "Multi-Tenant SaaS Platform - Local Environment Setup"
echo ""

# Check OS
if [[ "$OSTYPE" != "darwin"* ]] && [[ "$OSTYPE" != "linux-gnu"* ]]; then
    print_error "Unsupported OS: $OSTYPE"
    echo "This script supports macOS and Linux only."
    echo "For Windows, please use WSL2 or install tools manually."
    exit 1
fi

print_success "Detected OS: $OSTYPE"
echo ""

# Install tools
install_homebrew
echo ""

install_java
echo ""

install_maven
echo ""

install_node
echo ""

install_docker
echo ""

install_postgres_client
echo ""

# Setup project
setup_postgres
echo ""

setup_backend
echo ""

setup_frontend
echo ""

create_run_script
echo ""

# Summary
print_setup_summary

print_success "Setup script completed successfully!"
