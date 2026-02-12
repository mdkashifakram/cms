#!/bin/bash
# ===========================================
# CMS Production Setup Script
# ===========================================
# This script helps set up the Clinic Management System for production
# Run: chmod +x setup.sh && ./setup.sh
# ===========================================

set -e

echo "============================================="
echo "  CMS Production Setup Script"
echo "============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check for Java
check_java() {
    echo -n "Checking Java... "
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo -e "${GREEN}Found: $JAVA_VERSION${NC}"
        return 0
    else
        echo -e "${RED}Not found!${NC}"
        echo "Please install Java 17 or later"
        return 1
    fi
}

# Check for Maven
check_maven() {
    echo -n "Checking Maven... "
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
        echo -e "${GREEN}Found${NC}"
        return 0
    elif [ -f "./mvnw" ]; then
        echo -e "${GREEN}Found Maven Wrapper${NC}"
        return 0
    else
        echo -e "${YELLOW}Not found (will use mvnw)${NC}"
        return 0
    fi
}

# Check for Node.js
check_node() {
    echo -n "Checking Node.js... "
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node -v)
        echo -e "${GREEN}Found: $NODE_VERSION${NC}"
        return 0
    else
        echo -e "${RED}Not found!${NC}"
        echo "Please install Node.js 18 or later"
        return 1
    fi
}

# Generate SSL keystore
generate_keystore() {
    echo ""
    echo "Generating SSL keystore..."
    
    KEYSTORE_PATH="src/main/resources/keystore.p12"
    
    if [ -f "$KEYSTORE_PATH" ]; then
        echo -e "${YELLOW}Keystore already exists. Skipping.${NC}"
        return 0
    fi
    
    if command -v keytool &> /dev/null; then
        keytool -genkeypair \
            -alias clinic \
            -keyalg RSA \
            -keysize 2048 \
            -storetype PKCS12 \
            -keystore "$KEYSTORE_PATH" \
            -validity 365 \
            -storepass changeit \
            -dname "CN=localhost, OU=CMS, O=Clinic, L=City, ST=State, C=US" \
            -noprompt
        
        echo -e "${GREEN}Keystore generated at $KEYSTORE_PATH${NC}"
        echo -e "${YELLOW}Password: changeit (change for production!)${NC}"
    else
        echo -e "${RED}keytool not found. Install Java JDK.${NC}"
        return 1
    fi
}

# Check .env file
check_env() {
    echo ""
    echo -n "Checking .env file... "
    
    if [ -f ".env" ]; then
        echo -e "${GREEN}Found${NC}"
    else
        if [ -f ".env.example" ]; then
            cp .env.example .env
            echo -e "${YELLOW}Created from .env.example${NC}"
            echo -e "${RED}Please update .env with your actual credentials!${NC}"
        else
            echo -e "${RED}Not found! Create .env file manually.${NC}"
            return 1
        fi
    fi
}

# Build backend
build_backend() {
    echo ""
    echo "Building backend..."
    
    if [ -f "./mvnw" ]; then
        ./mvnw clean compile -DskipTests
    else
        mvn clean compile -DskipTests
    fi
    
    echo -e "${GREEN}Backend build complete${NC}"
}

# Install frontend dependencies
install_frontend() {
    echo ""
    echo "Installing frontend dependencies..."
    
    FRONTEND_DIR="../../../Frontend/clinicapp"
    
    if [ -d "$FRONTEND_DIR" ]; then
        cd "$FRONTEND_DIR"
        npm install
        cd -
        echo -e "${GREEN}Frontend dependencies installed${NC}"
    else
        echo -e "${YELLOW}Frontend directory not found at $FRONTEND_DIR${NC}"
    fi
}

# Main menu
main() {
    echo "Select an option:"
    echo "1) Full setup (recommended for first time)"
    echo "2) Generate SSL keystore only"
    echo "3) Check environment only"
    echo "4) Build backend only"
    echo "5) Install frontend dependencies only"
    echo "6) Exit"
    echo ""
    read -p "Enter choice [1-6]: " choice
    
    case $choice in
        1)
            check_java
            check_maven
            check_node
            check_env
            generate_keystore
            build_backend
            install_frontend
            echo ""
            echo -e "${GREEN}=============================================${NC}"
            echo -e "${GREEN}  Setup Complete!${NC}"
            echo -e "${GREEN}=============================================${NC}"
            echo ""
            echo "Next steps:"
            echo "1. Update .env with your actual credentials"
            echo "2. Start backend: ./mvnw spring-boot:run"
            echo "3. Start frontend: cd Frontend/clinicapp && npm start"
            ;;
        2)
            generate_keystore
            ;;
        3)
            check_java
            check_maven
            check_node
            check_env
            ;;
        4)
            build_backend
            ;;
        5)
            install_frontend
            ;;
        6)
            echo "Exiting."
            exit 0
            ;;
        *)
            echo "Invalid choice"
            exit 1
            ;;
    esac
}

# Run main
main
