/**
 * CMS-010: Environment-Aware Logger
 * 
 * Replaces console.log throughout the application.
 * Only logs in development mode to prevent sensitive data leakage in production.
 * 
 * Usage:
 *   import logger from '../utils/logger';
 *   logger.log('debug info');   // Only logs in development
 *   logger.error('error info'); // Logs in all environments + sends to monitoring
 */

const isDevelopment = process.env.NODE_ENV === 'development';

/**
 * Sanitize data to remove sensitive information before logging
 * @param {any} data - Data to sanitize
 * @returns {any} - Sanitized data
 */
const sanitize = (data) => {
    if (typeof data !== 'object' || data === null) {
        return data;
    }

    const sensitiveKeys = [
        'password', 'token', 'authToken', 'secret', 'apiKey',
        'ssn', 'creditCard', 'diagnosis', 'medicalHistory'
    ];

    const sanitized = Array.isArray(data) ? [...data] : { ...data };

    for (const key of Object.keys(sanitized)) {
        if (sensitiveKeys.some(sk => key.toLowerCase().includes(sk.toLowerCase()))) {
            sanitized[key] = '[REDACTED]';
        } else if (typeof sanitized[key] === 'object') {
            sanitized[key] = sanitize(sanitized[key]);
        }
    }

    return sanitized;
};

const logger = {
    /**
     * Log debug information (development only)
     */
    log: (...args) => {
        if (isDevelopment) {
            console.log('[DEV]', ...args.map(arg => sanitize(arg)));
        }
    },

    /**
     * Log informational messages (development only)
     */
    info: (...args) => {
        if (isDevelopment) {
            console.info('[INFO]', ...args.map(arg => sanitize(arg)));
        }
    },

    /**
     * Log warnings (development only)
     */
    warn: (...args) => {
        if (isDevelopment) {
            console.warn('[WARN]', ...args.map(arg => sanitize(arg)));
        }
    },

    /**
     * Log errors (all environments)
     * In production, this could be extended to send to error monitoring service
     */
    error: (...args) => {
        const sanitizedArgs = args.map(arg => sanitize(arg));
        console.error('[ERROR]', ...sanitizedArgs);

        // In production, send to error monitoring service
        if (!isDevelopment) {
            // TODO: Integrate with Sentry, LogRocket, etc.
            // Example:
            // Sentry.captureException(args[0]);
        }
    },

    /**
     * Log debug information with grouping (development only)
     */
    group: (label, ...args) => {
        if (isDevelopment) {
            console.group(label);
            args.forEach(arg => console.log(sanitize(arg)));
            console.groupEnd();
        }
    },

    /**
     * Log a table (development only)
     */
    table: (data) => {
        if (isDevelopment) {
            console.table(sanitize(data));
        }
    },

    /**
     * Check if we're in development mode
     */
    isDevelopment: () => isDevelopment
};

export default logger;
