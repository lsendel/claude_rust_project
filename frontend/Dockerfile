# T093: Multi-Stage Dockerfile for React Frontend
# Multi-Tenant SaaS Platform - Optimized Production Build with Nginx

##############################################################################
# Stage 1: Build Stage
##############################################################################
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files first (for layer caching)
COPY package*.json ./
COPY yarn.lock* ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build the application for production
RUN npm run build

##############################################################################
# Stage 2: Runtime Stage with Nginx
##############################################################################
FROM nginx:1.25-alpine

# Add labels for image metadata
LABEL maintainer="SaaS Platform Team" \
      version="1.0.0" \
      description="Multi-Tenant SaaS Platform Frontend"

# Remove default Nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/

# Copy built application from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Create non-root user for security
RUN addgroup -g 101 -S nginx-runner && \
    adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -G nginx-runner -g nginx-runner nginx-runner && \
    chown -R nginx-runner:nginx-runner /usr/share/nginx/html && \
    chown -R nginx-runner:nginx-runner /var/cache/nginx && \
    chown -R nginx-runner:nginx-runner /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx-runner:nginx-runner /var/run/nginx.pid

# Switch to non-root user
USER nginx-runner

# Expose HTTP port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

# Start Nginx in foreground mode
CMD ["nginx", "-g", "daemon off;"]
