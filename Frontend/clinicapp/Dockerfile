# Multi-stage build for React frontend

# Stage 1: Build
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Set API URL for build (overrides .env.production)
ARG REACT_APP_API_BASE_URL=http://localhost:8080
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

# Build the React app
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:1.25-alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build files from previous stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
