import { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { Icons } from "../constants";
import { analyzeContent } from "../services/geminiService.js";

const Analyzer = ({ onComplete }) => {
  const [activeTab, setActiveTab] = useState("url");
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [loadingMsg, setLoadingMsg] = useState("Initializing scan...");
  const [error, setError] = useState("");
  const [isVisible, setIsVisible] = useState(false);
  const [tipsVisible, setTipsVisible] = useState(false);
  const tipsRef = useRef(null);

  const loadingSteps = [
    { delay: 0, message: "Initializing scan..." },
    { delay: 1500, message: "Checking source structure..." },
    { delay: 3000, message: "Analyzing for AI readability..." },
    { delay: 4500, message: "Evaluating Schema.org metadata..." },
    { delay: 6000, message: "Generating fix suggestions..." },
  ];

  // Entry animation
  useEffect(() => {
    setTimeout(() => setIsVisible(true), 100);
  }, []);

  // Tips section observer
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setTipsVisible(true);
          } else {
            setTipsVisible(false);
          }
        });
      },
      { threshold: 0.1 }
    );

    if (tipsRef.current) {
      observer.observe(tipsRef.current);
    }

    return () => observer.disconnect();
  }, []);

  const handleAnalyze = async () => {
    // Clear previous errors
    setError("");

    // Empty check
    if (!inputValue.trim()) {
      setError(
        "Please enter a valid " + (activeTab === "url" ? "URL" : "content")
      );
      return;
    }

    // URL-specific validation
    if (activeTab === "url") {
      try {
        new URL(inputValue);
      } catch {
        setError("Please enter a valid URL (e.g., https://example.com)");
        return;
      }
    }

    // Text-specific validation
    if (activeTab === "text") {
      if (inputValue.length < 50) {
        setError("Content must be at least 50 characters long");
        return;
      }
      if (inputValue.length > 100000) {
        setError("Content is too large (max 100,000 characters)");
        return;
      }
    }

    // Start loading
    setIsLoading(true);

    // Loading message progression
    const timeouts = loadingSteps.map((step) =>
      setTimeout(() => setLoadingMsg(step.message), step.delay)
    );

    try {
      // Call API
      const result = await analyzeContent(inputValue, activeTab);

      // Clear timeouts
      timeouts.forEach(clearTimeout);

      // 8. Pass result to parent
      onComplete(result);
    } catch (err) {
      // lear timeouts on error
      timeouts.forEach(clearTimeout);

      console.error("Analysis error:", err);

      // Show user-friendly error
      setError(
        err.message ||
          "Analysis failed. Please try again or check your connection."
      );

      // Stop loading
      setIsLoading(false);
    }
  };

  const proTips = [
    {
      number: 1,
      title: "Structured Headings",
      description:
        "Use hierarchical H2s and H3s for better AI parsing and content structure.",
    },
    {
      number: 2,
      title: "Executive Summary",
      description:
        "Include a 200-word summary at the top to provide context quickly.",
    },
    {
      number: 3,
      title: "Highlight Answers",
      description:
        "Answer primary questions in bold text segments for easy identification.",
    },
  ];

  if (isLoading) {
    return (
      <div className="max-w-xl mx-auto py-20 flex flex-col items-center justify-center text-center animate-in fade-in duration-500">
        <div className="relative w-24 h-24 mb-8 animate-in zoom-in-50 duration-500">
          <div className="absolute inset-0 border-4 border-indigo-200 dark:border-indigo-800/50 rounded-full"></div>
          <div className="absolute inset-0 border-4 border-t-indigo-600 border-r-transparent border-b-transparent border-l-transparent rounded-full animate-spin"></div>
          <div className="absolute inset-4 bg-indigo-100 dark:bg-indigo-900/30 rounded-full flex items-center justify-center">
            <div className="w-2 h-2 bg-indigo-600 rounded-full animate-ping"></div>
          </div>
        </div>
        <h2 className="text-2xl font-bold mb-2 text-gray-900 dark:text-white animate-in fade-in slide-in-from-bottom-4 duration-500 delay-100">
          Analyzing Content
        </h2>
        <p className="text-gray-600 dark:text-gray-300 animate-pulse font-normal">
          {loadingMsg}
        </p>
        <div className="mt-6 w-64 h-1 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden animate-in fade-in slide-in-from-bottom-2 duration-500 delay-200">
          <div
            className="h-full bg-indigo-600 rounded-full animate-pulse"
            style={{ width: "60%" }}
          ></div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-10 px-4">
      <div
        className={`text-center mb-12 transition-all duration-700 ${
          isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
        }`}
      >
        <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-indigo-100 to-indigo-200 dark:from-indigo-900/40 dark:to-indigo-800/40 rounded-full mb-4 shadow-lg shadow-indigo-200/50 dark:shadow-indigo-900/30 hover:scale-110 hover:rotate-6 transition-all duration-300">
          <Icons.Search className="w-8 h-8 text-indigo-600 dark:text-indigo-400" />
        </div>
        <h2 className="text-4xl font-bold mb-4 text-gray-900 dark:text-white tracking-tight">
          Start Your AEO Audit
        </h2>
        <p className="text-lg text-gray-600 dark:text-gray-300 max-w-2xl mx-auto font-normal">
          Analyze your content for AI search optimization. Get actionable
          insights to improve visibility in AI-powered search results.
        </p>
      </div>

      <div
        className={`bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-2xl overflow-hidden transition-all duration-700 ${
          isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
        }`}
        style={{ transitionDelay: "200ms" }}
      >
        {/* Tabs */}
        <div className="flex border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
          <button
            onClick={() => {
              setActiveTab("url");
              setInputValue("");
              setError("");
            }}
            className={`flex-1 py-4 px-6 font-medium text-sm transition-all flex items-center justify-center gap-2 hover:scale-105 active:scale-95 ${
              activeTab === "url"
                ? "bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400"
                : "text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-white/50 dark:hover:bg-gray-800/30"
            }`}
          >
            <Icons.Search className="w-4 h-4" /> URL Analyzer
          </button>
          <button
            onClick={() => {
              setActiveTab("text");
              setInputValue("");
              setError("");
            }}
            className={`flex-1 py-4 px-6 font-medium text-sm transition-all flex items-center justify-center gap-2 hover:scale-105 active:scale-95 ${
              activeTab === "text"
                ? "bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400"
                : "text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 hover:bg-white/50 dark:hover:bg-gray-800/30"
            }`}
          >
            <Icons.FileText className="w-4 h-4" /> Text Analyzer
          </button>
        </div>

        <div className="p-8 bg-white dark:bg-gray-800">
          {activeTab === "url" ? (
            <div className="animate-in fade-in slide-in-from-right-4 duration-300">
              <label
                htmlFor="url-input"
                className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-3"
              >
                Website URL
              </label>
              <div className="flex flex-col sm:flex-row gap-3">
                <input
                  id="url-input"
                  type="url"
                  value={inputValue}
                  onChange={(e) => {
                    setInputValue(e.target.value);
                    setError("");
                  }}
                  placeholder="https://example.com/blog-post"
                  className="flex-1 bg-gray-50 dark:bg-gray-900 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white rounded-full px-5 py-3 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:scale-105 transition-all font-normal"
                  aria-label="Enter website URL"
                />
                <button
                  onClick={handleAnalyze}
                  disabled={!inputValue.trim()}
                  className="px-7 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full font-medium text-sm hover:scale-105 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-indigo-600 disabled:hover:scale-100 shadow-lg shadow-indigo-500/30"
                >
                  Analyze
                </button>
              </div>
              {error && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-3 flex items-center gap-2 bg-red-50 dark:bg-red-900/20 px-4 py-2.5 rounded-full border border-red-200 dark:border-red-800 font-normal animate-in fade-in slide-in-from-top-2 duration-300">
                  <svg
                    className="w-4 h-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                      clipRule="evenodd"
                    />
                  </svg>
                  {error}
                </p>
              )}
              <div className="mt-4 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border border-blue-200 dark:border-blue-800 rounded-2xl hover:shadow-lg transition-shadow">
                <p className="text-sm text-blue-800 dark:text-blue-200 flex items-start gap-2 font-normal">
                  <svg
                    className="w-5 h-5 mt-0.5 flex-shrink-0"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                      clipRule="evenodd"
                    />
                  </svg>
                  <span>
                    <strong className="font-semibold">Note:</strong> URL
                    scanning works best on publicly accessible articles, blog
                    posts, and landing pages. Password-protected content cannot
                    be analyzed.
                  </span>
                </p>
              </div>
            </div>
          ) : (
            <div className="animate-in fade-in slide-in-from-left-4 duration-300">
              <label
                htmlFor="text-input"
                className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-3"
              >
                Paste Article Content
              </label>
              <textarea
                id="text-input"
                rows={12}
                value={inputValue}
                onChange={(e) => {
                  setInputValue(e.target.value);
                  setError("");
                }}
                placeholder="Paste your markdown or plain text content here...

Example:
# Article Title
Your introduction text...

## Main Section
Content goes here..."
                className="w-full bg-gray-50 dark:bg-gray-900 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white rounded-2xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all resize-none font-mono text-sm hover:shadow-lg"
                aria-label="Paste your content"
              />
              <div className="flex items-center justify-between mt-4">
                <p className="text-sm text-gray-600 dark:text-gray-300 font-normal">
                  {inputValue.length} characters{" "}
                  {inputValue.length < 50 && `(minimum 50 required)`}
                </p>
                <button
                  onClick={handleAnalyze}
                  disabled={inputValue.length < 50}
                  className="px-7 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full font-medium text-sm hover:scale-105 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-indigo-600 disabled:hover:scale-100 shadow-lg shadow-indigo-500/30"
                >
                  Analyze Content
                </button>
              </div>
              {error && (
                <p className="text-sm text-red-600 dark:text-red-400 mt-3 flex items-center gap-2 bg-red-50 dark:bg-red-900/20 px-4 py-2.5 rounded-full border border-red-200 dark:border-red-800 font-normal animate-in fade-in slide-in-from-top-2 duration-300">
                  <svg
                    className="w-4 h-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                      clipRule="evenodd"
                    />
                  </svg>
                  {error}
                </p>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Pro Tips */}
      <div ref={tipsRef} className="mt-12">
        <h3
          className={`text-xl font-bold text-gray-900 dark:text-white mb-6 text-center tracking-tight transition-all duration-700 ${
            tipsVisible
              ? "opacity-100 translate-y-0"
              : "opacity-0 translate-y-4"
          }`}
        >
          Pro Tips for Better Results
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {proTips.map((tip, idx) => (
            <div
              key={`tip-${tip.number}`}
              className={`group p-6 bg-white dark:bg-gray-800 rounded-2xl border-2 border-gray-200 dark:border-gray-700 hover:shadow-2xl hover:border-indigo-300 dark:hover:border-indigo-600 hover:-translate-y-2 transition-all duration-300 cursor-default ${
                tipsVisible
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-8"
              }`}
              style={{ transitionDelay: `${idx * 100}ms` }}
            >
              <div className="flex items-start gap-4">
                <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-indigo-100 to-indigo-200 dark:from-indigo-900/40 dark:to-indigo-800/40 rounded-full flex items-center justify-center text-indigo-600 dark:text-indigo-400 font-semibold text-sm group-hover:scale-110 group-hover:rotate-6 transition-all shadow-sm">
                  {tip.number}
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white mb-1 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                    {tip.title}
                  </p>
                  <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed font-normal">
                    {tip.description}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// PropTypes validation
Analyzer.propTypes = {
  onComplete: PropTypes.func.isRequired,
};

export default Analyzer;
