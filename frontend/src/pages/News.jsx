import React, { useState, useEffect, useRef } from "react";
import { Icons } from "../constants";

const News = ({ onBack }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [cardsAnimated, setCardsAnimated] = useState(false);
  const newsRef = useRef(null);

  // Entry animation
  useEffect(() => {
    const timer1 = setTimeout(() => setIsVisible(true), 100);
    const timer2 = setTimeout(() => setCardsAnimated(true), 400);
    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
    };
  }, []);

  // Reset animation on category change
  useEffect(() => {
    const timer1 = setTimeout(() => setCardsAnimated(false), 0);
    const timer2 = setTimeout(() => setCardsAnimated(true), 100);
    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
    };
  }, [selectedCategory]);

  const categories = [
    { id: "all", label: "All News", icon: "📰" },
    { id: "product", label: "Product Updates", icon: "🚀" },
    { id: "ai", label: "AI Trends", icon: "🤖" },
    { id: "tips", label: "SEO Tips", icon: "💡" },
  ];

  const newsArticles = [
    {
      id: 1,
      category: "product",
      title: "Introducing Real-Time AEO Score Analysis",
      excerpt:
        "Get instant feedback on your content optimization with our new real-time scoring engine powered by advanced AI models.",
      date: "2024-12-20",
      readTime: "3 min read",
      image: "🎯",
      featured: true,
    },
    {
      id: 2,
      category: "ai",
      title: "How ChatGPT Search Changes Content Strategy",
      excerpt:
        "OpenAI's new search feature is revolutionizing how users discover content. Learn how to optimize for this new paradigm.",
      date: "2024-12-18",
      readTime: "5 min read",
      image: "🔍",
      featured: true,
    },
    {
      id: 3,
      category: "tips",
      title: "10 Quick Wins for Better AI Visibility",
      excerpt:
        "Simple yet effective techniques to improve how AI models understand and cite your content in search results.",
      date: "2024-12-15",
      readTime: "4 min read",
      image: "✨",
      featured: false,
    },
    {
      id: 4,
      category: "product",
      title: "FAQ Schema Generator Now Available",
      excerpt:
        "Create structured FAQ markup in seconds with our new tool. Improve your chances of appearing in AI-powered answer boxes.",
      date: "2024-12-12",
      readTime: "2 min read",
      image: "❓",
      featured: false,
    },
    {
      id: 5,
      category: "ai",
      title: "Google's AI Overview: What You Need to Know",
      excerpt:
        "Understanding Google's AI-generated search summaries and how to optimize your content to be featured in them.",
      date: "2024-12-10",
      readTime: "6 min read",
      image: "🌐",
      featured: false,
    },
    {
      id: 6,
      category: "tips",
      title: "Schema Markup Best Practices for 2025",
      excerpt:
        "Essential schema types every website should implement to stay competitive in AI-powered search results.",
      date: "2024-12-08",
      readTime: "5 min read",
      image: "📊",
      featured: false,
    },
    {
      id: 7,
      category: "ai",
      title: "Perplexity AI: The Rising Search Alternative",
      excerpt:
        "How Perplexity is changing the search landscape and what it means for content creators and marketers.",
      date: "2024-12-05",
      readTime: "4 min read",
      image: "🔮",
      featured: false,
    },
    {
      id: 8,
      category: "product",
      title: "New History Dashboard with Analytics",
      excerpt:
        "Track your optimization progress over time with detailed analytics, trend charts, and performance insights.",
      date: "2024-12-01",
      readTime: "3 min read",
      image: "📈",
      featured: false,
    },
  ];

  const filteredNews =
    selectedCategory === "all"
      ? newsArticles
      : newsArticles.filter((article) => article.category === selectedCategory);

  const featuredArticles = newsArticles.filter((article) => article.featured);

  return (
    <div className="max-w-7xl mx-auto py-10 px-4">
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
              d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"
            />
          </svg>
        </div>

        <h2 className="text-4xl font-bold mb-4 text-gray-900 dark:text-white tracking-tight">
          News & Updates
        </h2>
        <p className="text-lg text-gray-600 dark:text-gray-300 max-w-2xl mx-auto font-normal">
          Stay informed about the latest in AEO, AI search trends, and product
          updates.
        </p>
      </div>

      {/* Category Filter */}
      <div
        className={`flex flex-wrap justify-center gap-3 mb-12 transition-all duration-700 ${
          isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
        }`}
        style={{ transitionDelay: "200ms" }}
      >
        {categories.map((category) => (
          <button
            key={category.id}
            onClick={() => setSelectedCategory(category.id)}
            className={`px-6 py-3 rounded-full font-medium text-sm transition-all hover:scale-105 active:scale-95 ${
              selectedCategory === category.id
                ? "bg-indigo-600 text-white shadow-lg shadow-indigo-500/30"
                : "bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 text-gray-900 dark:text-white hover:border-indigo-300 dark:hover:border-indigo-600"
            }`}
          >
            <span className="mr-2">{category.icon}</span>
            {category.label}
          </button>
        ))}
      </div>

      {/* Featured Articles */}
      {selectedCategory === "all" && featuredArticles.length > 0 && (
        <div className="mb-16">
          <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">
            Featured Stories
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {featuredArticles.map((article, idx) => (
              <div
                key={article.id}
                className={`news-card group bg-gradient-to-br from-indigo-50 to-violet-50 dark:from-indigo-900/20 dark:to-violet-900/20 rounded-3xl border-2 border-indigo-200 dark:border-indigo-800 p-8 hover:shadow-2xl hover:-translate-y-2 cursor-pointer transition-all duration-500 ${
                  cardsAnimated
                    ? "opacity-100 translate-y-0"
                    : "opacity-0 translate-y-8"
                }`}
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <div className="text-5xl mb-4 group-hover:scale-110 group-hover:rotate-6 transition-transform">
                  {article.image}
                </div>
                <div className="flex items-center gap-2 mb-3">
                  <span className="px-3 py-1 bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400 rounded-full text-xs font-bold uppercase tracking-wider">
                    Featured
                  </span>
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {article.date}
                  </span>
                </div>
                <h4 className="text-xl font-bold text-gray-900 dark:text-white mb-3 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                  {article.title}
                </h4>
                <p className="text-sm text-gray-600 dark:text-gray-300 mb-4 leading-relaxed font-normal">
                  {article.excerpt}
                </p>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                    {article.readTime}
                  </span>
                  <span className="text-indigo-600 dark:text-indigo-400 text-sm font-semibold group-hover:gap-2 flex items-center gap-1 transition-all">
                    Read More
                    <svg
                      className="w-4 h-4 group-hover:translate-x-1 transition-transform"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* All Articles */}
      <div>
        <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          {selectedCategory === "all" ? "All Articles" : "Filtered Results"}
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredNews
            .filter(
              (article) => !article.featured || selectedCategory !== "all"
            )
            .map((article, idx) => (
              <div
                key={article.id}
                className={`news-card group bg-white dark:bg-gray-800 rounded-2xl border-2 border-gray-200 dark:border-gray-700 p-6 hover:shadow-xl hover:border-indigo-300 dark:hover:border-indigo-600 hover:-translate-y-1 cursor-pointer transition-all duration-500 ${
                  cardsAnimated
                    ? "opacity-100 translate-y-0"
                    : "opacity-0 translate-y-4"
                }`}
                style={{ transitionDelay: `${idx * 50}ms` }}
              >
                <div className="text-4xl mb-4 group-hover:scale-110 transition-transform">
                  {article.image}
                </div>
                <div className="flex items-center gap-2 mb-3">
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {article.date}
                  </span>
                  <span className="text-gray-300 dark:text-gray-700">•</span>
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {article.readTime}
                  </span>
                </div>
                <h4 className="text-lg font-bold text-gray-900 dark:text-white mb-2 line-clamp-2 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                  {article.title}
                </h4>
                <p className="text-sm text-gray-600 dark:text-gray-300 mb-4 line-clamp-3 leading-relaxed font-normal">
                  {article.excerpt}
                </p>
                <span className="text-indigo-600 dark:text-indigo-400 text-sm font-semibold group-hover:gap-2 flex items-center gap-1 transition-all">
                  Read More
                  <svg
                    className="w-4 h-4 group-hover:translate-x-1 transition-transform"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </span>
              </div>
            ))}
        </div>
      </div>

      {/* Newsletter Signup */}
      <div
        className={`mt-16 bg-gradient-to-r from-indigo-50 to-violet-50 dark:from-indigo-900/20 dark:to-violet-900/20 rounded-3xl border-2 border-indigo-200 dark:border-indigo-800 p-12 text-center transition-all duration-700 ${
          isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
        }`}
        style={{ transitionDelay: "600ms" }}
      >
        <h3 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">
          Stay in the Loop
        </h3>
        <p className="text-gray-600 dark:text-gray-300 mb-8 max-w-2xl mx-auto font-normal">
          Get the latest AEO insights, AI search trends, and product updates
          delivered straight to your inbox.
        </p>
        <div className="flex flex-col sm:flex-row gap-3 max-w-md mx-auto">
          <input
            type="email"
            placeholder="Enter your email"
            className="flex-1 px-6 py-3 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 rounded-full text-sm text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all font-normal"
          />
          <button className="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full font-semibold text-sm hover:scale-105 active:scale-95 transition-all shadow-lg shadow-indigo-500/30 whitespace-nowrap">
            Subscribe
          </button>
        </div>
      </div>
    </div>
  );
};

export default News;
