export const isValidURL = (url) => {
  try {
    const urlObj = new URL(url);
    return urlObj.protocol === "http:" || urlObj.protocol === "https:";
  } catch {
    return false;
  }
};

/**
 * Sanitize text input (remove dangerous characters)
 */
export const sanitizeText = (text) => {
  if (!text) return "";

  return text
    .trim()
    .replace(/<script[^>]*>.*?<\/script>/gi, "") // Remove script tags
    .replace(/javascript:/gi, "") // Remove javascript: protocol
    .replace(/on\w+\s*=/gi, "") // Remove event handlers
    .replace(/<iframe[^>]*>.*?<\/iframe>/gi, ""); // Remove iframes
};

/**
 * Validate text content
 */
export const validateTextContent = (text) => {
  const errors = [];

  if (!text || text.trim().length === 0) {
    errors.push("Content cannot be empty");
  } else if (text.length < 50) {
    errors.push("Content must be at least 50 characters long");
  } else if (text.length > 50000) {
    errors.push("Content exceeds maximum length (50000 characters)");
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate URL content
 */
export const validateURL = (url) => {
  const errors = [];

  if (!url || url.trim().length === 0) {
    errors.push("URL cannot be empty");
  } else if (!isValidURL(url)) {
    errors.push("Please enter a valid URL (e.g., https://example.com)");
  } else if (url.length > 2000) {
    errors.push("URL is too long (max 2000 characters)");
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Main validation function
 */
export const validateInput = (value, type) => {
  if (type === "url") {
    return validateURL(value);
  } else if (type === "text") {
    return validateTextContent(value);
  }

  return { isValid: false, errors: ["Invalid type"] };
};
