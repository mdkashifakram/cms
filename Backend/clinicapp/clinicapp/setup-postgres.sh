#!/bin/bash
# ===========================================
# PostgreSQL Setup Script for CMS
# ===========================================
# This script helps set up PostgreSQL for the CMS
# Run: chmod +x setup-postgres.sh && ./setup-postgres.sh
# ===========================================

set -e

echo "============================================="
echo "  CMS PostgreSQL Setup Script"
echo "============================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
DB_NAME="clinic_management"
DB_USER="clinic_user"
DB_PASSWORD="clinic_secure_password_123"

# Check for PostgreSQL
check_postgres() {
    echo -n "Checking PostgreSQL... "
    if command -v psql &> /dev/null; then
        PSQL_VERSION=$(psql --version)
        echo -e "${GREEN}Found: $PSQL_VERSION${NC}"
        return 0
    else
        echo -e "${RED}Not found!${NC}"
        echo ""
        echo "Install PostgreSQL:"
        echo "  macOS:  brew install postgresql@15"
        echo "  Ubuntu: sudo apt install postgresql postgresql-contrib"
        echo ""
        return 1
    fi
}

# Check if PostgreSQL is running
check_running() {
    echo -n "Checking if PostgreSQL is running... "
    if pg_isready &> /dev/null; then
        echo -e "${GREEN}Running${NC}"
        return 0
    else
        echo -e "${RED}Not running${NC}"
        echo ""
        echo "Start PostgreSQL:"
        echo "  macOS:  brew services start postgresql@15"
        echo "  Linux:  sudo systemctl start postgresql"
        echo ""
        return 1
    fi
}

# Create database and user
create_database() {
    echo ""
    echo "Creating database and user..."
    echo ""
    
    read -p "Enter database name [$DB_NAME]: " input_name
    DB_NAME=${input_name:-$DB_NAME}
    
    read -p "Enter database user [$DB_USER]: " input_user
    DB_USER=${input_user:-$DB_USER}
    
    read -sp "Enter database password [$DB_PASSWORD]: " input_pass
    echo ""
    DB_PASSWORD=${input_pass:-$DB_PASSWORD}
    
    echo ""
    echo "Creating database: $DB_NAME"
    echo "Creating user: $DB_USER"
    echo ""
    
    # Create user and database
    psql postgres << EOF
-- Create user if not exists
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
        CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
    END IF;
END
\$\$;

-- Create database if not exists
SELECT 'CREATE DATABASE $DB_NAME OWNER $DB_USER'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
EOF
    
    echo -e "${GREEN}Database and user created!${NC}"
}

# Run migration script
run_migration() {
    echo ""
    echo "Running migration script..."
    
    MIGRATION_SCRIPT="src/main/resources/db/migration/V1__initial_schema.sql"
    
    if [ -f "$MIGRATION_SCRIPT" ]; then
        psql -U "$DB_USER" -d "$DB_NAME" -f "$MIGRATION_SCRIPT"
        echo -e "${GREEN}Migration complete!${NC}"
    else
        echo -e "${YELLOW}Migration script not found at $MIGRATION_SCRIPT${NC}"
        echo "Hibernate will auto-create tables on first run."
    fi
}

# Update .env file
update_env() {
    echo ""
    echo "Updating .env file for PostgreSQL..."
    
    if [ -f ".env" ]; then
        # Create backup
        cp .env .env.backup
        
        # Update values
        sed -i.bak "s/^DB_HOST=.*/DB_HOST=localhost/" .env 2>/dev/null || true
        sed -i.bak "s/^DB_PORT=.*/DB_PORT=5432/" .env 2>/dev/null || true
        sed -i.bak "s/^DB_NAME=.*/DB_NAME=$DB_NAME/" .env 2>/dev/null || true
        sed -i.bak "s/^DB_USERNAME=.*/DB_USERNAME=$DB_USER/" .env 2>/dev/null || true
        sed -i.bak "s/^DB_PASSWORD=.*/DB_PASSWORD=$DB_PASSWORD/" .env 2>/dev/null || true
        sed -i.bak "s/^SPRING_PROFILE=.*/SPRING_PROFILE=postgres/" .env 2>/dev/null || true
        
        # Clean up backup files
        rm -f .env.bak
        
        echo -e "${GREEN}.env updated!${NC}"
        echo "Backup saved as .env.backup"
    else
        echo -e "${YELLOW}.env file not found. Creating...${NC}"
        cat > .env << EOF
# PostgreSQL Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=$DB_NAME
DB_USERNAME=$DB_USER
DB_PASSWORD=$DB_PASSWORD
SPRING_PROFILE=postgres

# Other settings (copy from .env.example)
JWT_SECRET=your_jwt_secret_here
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
TWILIO_TRIAL_NUMBER=+1234567890
EOF
        echo -e "${GREEN}.env created!${NC}"
    fi
}

# Print connection info
print_info() {
    echo ""
    echo "============================================="
    echo -e "${GREEN}  PostgreSQL Setup Complete!${NC}"
    echo "============================================="
    echo ""
    echo "Connection details:"
    echo "  Host:     localhost"
    echo "  Port:     5432"
    echo "  Database: $DB_NAME"
    echo "  User:     $DB_USER"
    echo "  Password: $DB_PASSWORD"
    echo ""
    echo "JDBC URL:"
    echo "  jdbc:postgresql://localhost:5432/$DB_NAME"
    echo ""
    echo "Next steps:"
    echo "  1. Verify .env has SPRING_PROFILE=postgres"
    echo "  2. Start backend: ./mvnw spring-boot:run"
    echo ""
}

# Main
main() {
    if check_postgres && check_running; then
        create_database
        run_migration
        update_env
        print_info
    else
        echo ""
        echo "Please install and start PostgreSQL first."
        exit 1
    fi
}

main
