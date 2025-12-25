import React, { useState, useEffect, useRef } from "react";
import { Icons } from "../constants";

const FAQGenerator = ({ onBack }) => {
  const [faqs, setFaqs] = useState([{ question: "", answer: "" }]);
  const [generatedSchema, setGeneratedSchema] = useState("");
  const [isVisible, setIsVisible] = useState(false);
  const [tipsVisible, setTipsVisible] = useState(false);

  // Notification State
  const [toast, setToast] = useState({
    show: false,
    message: "",
    type: "error",
  });

  const tipsRef = useRef(null);

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

  // Notification Helper Function
  const showToast = (message, type = "error") => {
    setToast({ show: true, message, type });
    setTimeout(() => {
      setToast((prev) => ({ ...prev, show: false }));
    }, 3000);
  };

  const addFAQ = () => {
    setFaqs([...faqs, { question: "", answer: "" }]);
  };

  const removeFAQ = (index) => {
    if (faqs.length > 1) {
      const newFaqs = faqs.filter((_, i) => i !== index);
      setFaqs(newFaqs);
    }
  };

  const updateFAQ = (index, field, value) => {
    const newFaqs = [...faqs];
    newFaqs[index][field] = value;
    setFaqs(newFaqs);
  };

  const generateSchema = () => {
    const validFaqs = faqs.filter(
      (faq) => faq.question.trim() && faq.answer.trim()
    );

    if (validFaqs.length === 0) {
      showToast("Please add at least one question and answer!", "error");
      return;
    }

    const schema = {
      "@context": "https://schema.org",
      "@type": "FAQPage",
      mainEntity: validFaqs.map((faq) => ({
        "@type": "Question",
        name: faq.question,
        acceptedAnswer: {
          "@type": "Answer",
          text: faq.answer,
        },
      })),
    };

    const schemaString = JSON.stringify(schema, null, 2);
    setGeneratedSchema(schemaString);
    showToast("Schema generated successfully!", "success");
  };

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(generatedSchema);
      showToast("Schema code copied to clipboard!", "success");
    } catch (err) {
      console.error("Failed to copy:", err);
      showToast("Failed to copy code.", "error");
    }
  };

  const clearAll = () => {
    setFaqs([{ question: "", answer: "" }]);
    setGeneratedSchema("");
    showToast("All fields cleared.", "success");
  };

  const tips = [
    {
      number: 1,
      title: "Be Specific",
      description:
        "Write clear, specific questions that users actually search for.",
    },
    {
      number: 2,
      title: "Concise Answers",
      description:
        "Keep answers under 300 characters for better AI extraction.",
    },
    {
      number: 3,
      title: "Natural Language",
      description:
        "Use conversational tone that matches how people ask questions.",
    },
  ];

  return (
    <div className="max-w-6xl mx-auto py-10 px-4 relative">
      {/* ✅ UPDATED: Toast දැන් එන්නේ උඩින් (Top Center) */}
      <div
        className={`fixed top-6 left-1/2 transform -translate-x-1/2 z-[100] transition-all duration-500 ease-out ${
          toast.show
            ? "translate-y-0 opacity-100"
            : "-translate-y-10 opacity-0 pointer-events-none"
        }`}
      >
        <div
          className={`flex items-center gap-3 px-6 py-3 rounded-full shadow-2xl border backdrop-blur-xl ${
            toast.type === "error"
              ? "bg-white/95 dark:bg-gray-900/95 border-red-200 dark:border-red-800 text-red-600 dark:text-red-400"
              : "bg-white/95 dark:bg-gray-900/95 border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400"
          }`}
        >
          <div
            className={`p-1.5 rounded-full ${
              toast.type === "error"
                ? "bg-red-100 dark:bg-red-900/50"
                : "bg-emerald-100 dark:bg-emerald-900/50"
            }`}
          >
            {toast.type === "error" ? (
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            ) : (
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            )}
          </div>
          <div>
            <p className="font-semibold text-sm pr-2">{toast.message}</p>
          </div>
          <button
            onClick={() => setToast((prev) => ({ ...prev, show: false }))}
            className="pl-2 border-l border-gray-200 dark:border-gray-700 hover:opacity-70"
          >
            <svg
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* Header */}
      <div
        className={`text-center mb-12 transition-all duration-700 ${
          isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
        }`}
      >
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors mb-3 group font-normal"
        >
          <span className="rotate-180 group-hover:-translate-x-1 transition-transform">
            <Icons.ArrowRight />
          </span>
          Back to Home
        </button>

        <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-indigo-100 to-indigo-200 dark:from-indigo-900/40 dark:to-indigo-800/40 rounded-full mb-4 shadow-lg shadow-indigo-200/50 dark:shadow-indigo-900/30 hover:scale-110 hover:rotate-6 transition-all duration-300">
          <svg
            className="w-8 h-8 text-indigo-600 dark:text-indigo-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>

        <h2 className="text-4xl font-bold mb-4 text-gray-900 dark:text-white tracking-tight">
          FAQ Schema Generator
        </h2>
        <p className="text-lg text-gray-600 dark:text-gray-300 max-w-2xl mx-auto font-normal">
          Create structured FAQ schema markup for better AI search visibility.
          Google, ChatGPT, and Gemini can easily understand your FAQs.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left Column: FAQ Input */}
        <div
          className={`transition-all duration-700 ${
            isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
          }`}
          style={{ transitionDelay: "200ms" }}
        >
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white">
              Your FAQs ({faqs.length})
            </h3>
            <button
              onClick={clearAll}
              className="text-sm text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 font-semibold transition-colors hover:scale-105 active:scale-95"
            >
              Clear All
            </button>
          </div>

          {/* FAQ Items */}
          <div className="space-y-4 max-h-[600px] overflow-y-auto pr-2">
            {faqs.map((faq, index) => (
              <div
                key={index}
                className="bg-white dark:bg-gray-800 rounded-2xl border-2 border-gray-200 dark:border-gray-700 p-6 shadow-sm hover:shadow-xl hover:border-indigo-300 dark:hover:border-indigo-600 transition-all"
              >
                <div className="flex items-start justify-between mb-4">
                  <span className="text-sm font-bold text-indigo-600 dark:text-indigo-400">
                    FAQ #{index + 1}
                  </span>
                  {faqs.length > 1 && (
                    <button
                      onClick={() => removeFAQ(index)}
                      className="text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors hover:scale-110 active:scale-95"
                      aria-label="Remove FAQ"
                    >
                      <svg
                        className="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  )}
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">
                      Question
                    </label>
                    <input
                      type="text"
                      value={faq.question}
                      onChange={(e) =>
                        updateFAQ(index, "question", e.target.value)
                      }
                      placeholder="What is AEO?"
                      className="w-full px-4 py-2.5 bg-gray-50 dark:bg-gray-900 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white rounded-full text-sm outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all font-normal"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">
                      Answer
                    </label>
                    <textarea
                      value={faq.answer}
                      onChange={(e) =>
                        updateFAQ(index, "answer", e.target.value)
                      }
                      placeholder="Answer Engine Optimization (AEO) is the practice of optimizing content for AI-powered search engines..."
                      rows={4}
                      className="w-full px-4 py-2.5 bg-gray-50 dark:bg-gray-900 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white rounded-2xl text-sm outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all resize-none font-normal"
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3 mt-6">
            <button
              onClick={addFAQ}
              className="flex-1 px-6 py-3 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white rounded-full font-medium hover:bg-gray-50 dark:hover:bg-gray-700 hover:scale-105 active:scale-95 transition-all flex items-center justify-center gap-2"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                />
              </svg>
              Add FAQ
            </button>
            <button
              onClick={generateSchema}
              className="flex-1 px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full font-medium hover:scale-105 active:scale-95 transition-all shadow-lg shadow-indigo-500/30 flex items-center justify-center gap-2"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
                />
              </svg>
              Generate Schema
            </button>
          </div>
        </div>

        {/* Right Column: Generated Schema */}
        <div
          className={`transition-all duration-700 ${
            isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
          }`}
          style={{ transitionDelay: "300ms" }}
        >
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white">
              Generated Schema
            </h3>
            {generatedSchema && (
              <button
                onClick={copyToClipboard}
                className="px-4 py-2 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400 rounded-full font-semibold text-sm hover:bg-emerald-100 dark:hover:bg-emerald-900/30 hover:scale-105 active:scale-95 transition-all flex items-center gap-2"
              >
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
                  />
                </svg>
                Copy Code
              </button>
            )}
          </div>

          {generatedSchema ? (
            <div className="bg-gray-900 dark:bg-black rounded-2xl border-2 border-gray-800 overflow-hidden shadow-2xl hover:shadow-indigo-500/20 transition-shadow">
              <div className="bg-gray-800 dark:bg-gray-900 px-4 py-3 flex items-center justify-between border-b border-gray-700">
                <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
                  JSON-LD Schema Markup
                </span>
                <div className="flex gap-2">
                  <div className="w-3 h-3 rounded-full bg-red-500"></div>
                  <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                  <div className="w-3 h-3 rounded-full bg-green-500"></div>
                </div>
              </div>
              <pre className="p-6 overflow-x-auto max-h-[600px] text-sm">
                <code className="text-emerald-400 font-mono leading-relaxed">
                  {generatedSchema}
                </code>
              </pre>
            </div>
          ) : (
            <div className="bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 rounded-2xl border-2 border-dashed border-gray-300 dark:border-gray-700 p-12 text-center hover:border-indigo-300 dark:hover:border-indigo-700 transition-all">
              <div className="w-16 h-16 bg-gradient-to-br from-indigo-100 to-indigo-200 dark:from-indigo-900/40 dark:to-indigo-800/40 rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
                <svg
                  className="w-8 h-8 text-indigo-600 dark:text-indigo-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
                  />
                </svg>
              </div>
              <h4 className="text-lg font-bold text-gray-900 dark:text-white mb-2">
                No Schema Generated Yet
              </h4>
              <p className="text-sm text-gray-600 dark:text-gray-300 max-w-sm mx-auto font-normal">
                Add your questions and answers, then click "Generate Schema" to
                create your FAQ markup code.
              </p>
            </div>
          )}

          {/* How to Use */}
          <div className="mt-6 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border-2 border-blue-200 dark:border-blue-800 rounded-2xl p-6 hover:shadow-lg transition-shadow">
            <h4 className="font-bold text-blue-900 dark:text-blue-200 mb-3 flex items-center gap-2">
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              How to Use This Schema
            </h4>
            <ol className="text-sm text-blue-900 dark:text-blue-200 space-y-2 ml-7 list-decimal font-normal">
              <li>Copy the generated JSON-LD code</li>
              <li>
                Paste it inside the{" "}
                <code className="bg-blue-100 dark:bg-blue-900/40 px-1.5 py-0.5 rounded font-mono">
                  &lt;head&gt;
                </code>{" "}
                section of your webpage
              </li>
              <li>
                Wrap it in{" "}
                <code className="bg-blue-100 dark:bg-blue-900/40 px-1.5 py-0.5 rounded font-mono">
                  &lt;script type="application/ld+json"&gt;
                </code>{" "}
                tags
              </li>
              <li>
                Google and AI engines will automatically detect and use your
                FAQs
              </li>
            </ol>
          </div>
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
          Pro Tips for Better FAQs
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {tips.map((tip, idx) => (
            <div
              key={tip.number}
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

export default FAQGenerator;
