import { describe, it, expect } from 'vitest';
// මෙන්න නිවැරදි නම් (validateURL සහ validateTextContent)
import { validateURL, validateTextContent } from './validation';

describe('Validation Utilities', () => {

  // 1. URL Validation Tests
  describe('validateURL', () => {
    it('validates correct URLs', () => {
      // මෙතන validateUrl වෙනුවට validateURL
      expect(validateURL('https://example.com').isValid).toBe(true);
      expect(validateURL('http://test.com').isValid).toBe(true);
      expect(validateURL('https://sub.domain.com/path').isValid).toBe(true);
    });

    it('rejects invalid URLs', () => {
      expect(validateURL('not-a-url').isValid).toBe(false);
      expect(validateURL('').isValid).toBe(false);
      expect(validateURL('ftp://invalid.com').isValid).toBe(false);
    });

    it('handles null and undefined', () => {
      expect(validateURL(null).isValid).toBe(false);
      expect(validateURL(undefined).isValid).toBe(false);
    });
  });

  // 2. Content Validation Tests
  describe('validateTextContent', () => {
    it('validates content with minimum length', () => {
      // මෙතන validateContent වෙනුවට validateTextContent
      const longText = 'a'.repeat(60);
      expect(validateTextContent(longText).isValid).toBe(true);
    });

    it('rejects content that is too short', () => {
      expect(validateTextContent('Short').isValid).toBe(false);
      expect(validateTextContent('').isValid).toBe(false);
    });

    it('handles null and undefined', () => {
      expect(validateTextContent(null).isValid).toBe(false);
      expect(validateTextContent(undefined).isValid).toBe(false);
    });

    it('trims whitespace before validation', () => {
      const validTextWithSpaces = '   ' + 'a'.repeat(55) + '   ';
      expect(validateTextContent(validTextWithSpaces).isValid).toBe(true);
      expect(validateTextContent('     ').isValid).toBe(false);
    });
  });
});
