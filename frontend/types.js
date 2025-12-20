// Since JavaScript doesn't have interfaces or type definitions,
// these are just reference comments for documentation purposes.
// You can remove this file entirely or keep it as documentation.

/**
 * @typedef {Object} AEOScore
 * @property {number} total
 * @property {number} schema
 * @property {number} structure
 * @property {number} readability
 */

/**
 * @typedef {Object} AuditItem
 * @property {string} title
 * @property {'pass' | 'fail' | 'warning'} status
 * @property {string} description
 */

/**
 * @typedef {Object} FixSuggestion
 * @property {'schema' | 'qa' | 'summary'} type
 * @property {string} title
 * @property {string} code
 * @property {string} explanation
 */

/**
 * @typedef {Object} AnalysisResult
 * @property {string} id
 * @property {string} timestamp
 * @property {string} title
 * @property {'url' | 'text'} type
 * @property {string} content
 * @property {AEOScore} score
 * @property {AuditItem[]} audits
 * @property {FixSuggestion[]} suggestions
 */

/**
 * @typedef {'landing' | 'analyzer' | 'results' | 'history'} Page
 */

/**
 * @typedef {Object} AppState
 * @property {Page} currentPage
 * @property {AnalysisResult | null} currentResult
 * @property {AnalysisResult[]} history
 * @property {boolean} isDark
 */

// No exports needed in plain JavaScript - these are just JSDoc comments for reference