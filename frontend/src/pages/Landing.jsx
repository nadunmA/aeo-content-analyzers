import { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { Icons } from "../constants";

const Landing = ({ onNavigate }) => {
  const [visibleSections, setVisibleSections] = useState(new Set());
  const sectionRefs = useRef({});

  // Intersection Observer for scroll animations - triggers every time
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const section = entry.target.dataset.section;
          if (entry.isIntersecting) {
            // Add section when it enters viewport
            setVisibleSections((prev) => new Set([...prev, section]));
          } else {
            // Remove section when it leaves viewport (enables re-animation)
            setVisibleSections((prev) => {
              const newSet = new Set(prev);
              newSet.delete(section);
              return newSet;
            });
          }
        });
      },
      { threshold: 0.1, rootMargin: "0px 0px -100px 0px" }
    );

    Object.values(sectionRefs.current).forEach((ref) => {
      if (ref) observer.observe(ref);
    });

    return () => observer.disconnect();
  }, []);

  const features = [
    {
      icon: (
        <div className="p-3 bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-2xl">
          <Icons.Search className="w-6 h-6" />
        </div>
      ),
      title: "AEO Score",
      description:
        "Get a comprehensive 0-100 score on how readable your content is for AI models like ChatGPT and Gemini.",
    },
    {
      icon: (
        <div className="p-3 bg-violet-100 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400 rounded-2xl">
          <Icons.CheckCircle className="w-6 h-6" />
        </div>
      ),
      title: "Detailed Audit",
      description:
        "Identify gaps in structured data, conversational markers, and information density with actionable insights.",
    },
    {
      icon: (
        <div className="p-3 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 rounded-2xl">
          <Icons.FileText className="w-6 h-6" />
        </div>
      ),
      title: "Smart Suggestions",
      description:
        "Generate ready-to-use JSON-LD schemas and Q&A blocks to boost your AI search presence instantly.",
    },
    {
      icon: (
        <div className="p-3 bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 rounded-2xl">
          <Icons.History className="w-6 h-6" />
        </div>
      ),
      title: "Audit History",
      description:
        "Track your optimization progress over time with built-in history, trends, and comparison tools.",
    },
  ];

  const stats = [
    {
      value: "50+",
      label: "Content Audits",
      color: "text-indigo-600 dark:text-indigo-400",
    },
    {
      value: "95%",
      label: "Avg Score Improvement",
      color: "text-emerald-600 dark:text-emerald-400",
    },
    {
      value: "24/7",
      label: "AI Analysis Available",
      color: "text-violet-600 dark:text-violet-400",
    },
  ];

  const benefits = [
    "📊 Real-time AI readability scoring",
    "🎯 Schema markup generation",
    "✅ Conversational query optimization",
    "🔍 Competitor content analysis",
  ];

  const howItWorksSteps = [
    {
      num: 1,
      color: "bg-indigo-600",
      shadow: "shadow-indigo-200/50 dark:shadow-indigo-900/30",
      title: "Input Your Content",
      desc: "Paste your URL or article text into our analyzer",
    },
    {
      num: 2,
      color: "bg-violet-600",
      shadow: "shadow-violet-200/50 dark:shadow-violet-900/30",
      title: "AI Analysis",
      desc: "Our AI evaluates readability, structure, and schema",
    },
    {
      num: 3,
      color: "bg-emerald-600",
      shadow: "shadow-emerald-200/50 dark:shadow-emerald-900/30",
      title: "Get Recommendations",
      desc: "Receive actionable suggestions to improve your score",
    },
  ];

  const brands = ["AI SEARCH", "TECHCORP", "WEBAUDIT", "NEXTGEN", "DATAHUB"];

  return (
    <div className="overflow-hidden">
      {/* Hero Section */}
      <div className="text-center py-16 md:py-24 px-4 relative overflow-hidden">
        {/* Background gradient effect */}
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 via-transparent to-violet-50 dark:from-indigo-950/20 dark:to-violet-950/20 pointer-events-none animate-in fade-in duration-1000"></div>

        <div className="relative z-10">
          <div className="inline-flex items-center gap-2 px-4 py-2 mb-8 text-sm font-medium text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 border border-indigo-100 dark:border-indigo-800 rounded-full backdrop-blur-sm animate-in fade-in slide-in-from-top-4 duration-700">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-600"></span>
            </span>
            The Future of SEO: Answer Engine Optimization
          </div>

          <h1 className="text-5xl md:text-7xl lg:text-8xl font-bold mb-8 leading-tight text-gray-900 dark:text-white tracking-tight animate-in fade-in slide-in-from-bottom-6 duration-700 delay-100">
            Optimize Your Content for{" "}
            <span className="block mt-2 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 bg-clip-text text-transparent animate-in fade-in zoom-in-50 duration-700 delay-300">
              AI Answer Engines
            </span>
          </h1>

          <p className="text-lg md:text-xl text-gray-600 dark:text-gray-400 max-w-3xl mx-auto mb-12 leading-relaxed font-normal animate-in fade-in slide-in-from-bottom-4 duration-700 delay-500">
            Google Search is evolving.{" "}
            <strong className="font-semibold text-gray-900 dark:text-white">
              ChatGPT, Perplexity, and Gemini
            </strong>{" "}
            are the new gateways to information. Make sure your website is their{" "}
            <strong className="font-semibold text-gray-900 dark:text-white">
              primary source of truth.
            </strong>
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12 animate-in fade-in zoom-in-95 duration-700 delay-700">
            <button
              onClick={() => onNavigate("analyzer")}
              className="group px-7 py-3.5 bg-indigo-600 text-white rounded-full font-medium text-base hover:bg-indigo-700 hover:scale-105 active:scale-95 transition-all shadow-lg shadow-indigo-200/50 dark:shadow-indigo-900/30 flex items-center justify-center gap-2"
            >
              Start Free Analysis
              <Icons.ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </button>
            <button
              onClick={() => onNavigate("history")}
              className="px-7 py-3.5 bg-gray-100 dark:bg-gray-800 rounded-full font-medium text-base text-gray-900 dark:text-white hover:bg-gray-200 dark:hover:bg-gray-700 hover:scale-105 active:scale-95 transition-all"
            >
              View History
            </button>
          </div>

          {/* Quick Benefits */}
          <div className="flex flex-wrap justify-center gap-4 md:gap-6 text-sm text-gray-600 dark:text-gray-400 font-normal">
            {benefits.map((benefit, idx) => (
              <div
                key={`benefit-${idx}`}
                className="flex items-center gap-2 animate-in fade-in slide-in-from-bottom-2 duration-500"
                style={{ animationDelay: `${800 + idx * 100}ms` }}
              >
                <span>{benefit}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Stats Section */}
      <div
        ref={(el) => (sectionRefs.current.stats = el)}
        data-section="stats"
        className={`py-12 border-y border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900/50 transition-all duration-700 ${
          visibleSections.has("stats")
            ? "opacity-100 translate-y-0"
            : "opacity-0 translate-y-8"
        }`}
      >
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 max-w-4xl mx-auto px-4">
          {stats.map((stat, idx) => (
            <div
              key={`stat-${idx}`}
              className={`text-center transition-all duration-700 ${
                visibleSections.has("stats")
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-4"
              }`}
              style={{ transitionDelay: `${idx * 150}ms` }}
            >
              <div
                className={`text-4xl md:text-5xl font-bold mb-2 ${stat.color} hover:scale-110 transition-transform cursor-default`}
              >
                {stat.value}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400 font-normal">
                {stat.label}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Feature Grid */}
      <div
        ref={(el) => (sectionRefs.current.features = el)}
        data-section="features"
        className="py-20 px-4"
      >
        <div
          className={`text-center mb-16 transition-all duration-700 ${
            visibleSections.has("features")
              ? "opacity-100 translate-y-0"
              : "opacity-0 translate-y-8"
          }`}
        >
          <h2 className="text-3xl md:text-4xl font-bold mb-4 text-gray-900 dark:text-white tracking-tight">
            Everything You Need to Rank in AI
          </h2>
          <p className="text-gray-600 dark:text-gray-400 max-w-2xl mx-auto font-normal">
            Our comprehensive suite of tools helps you optimize content for the
            next generation of search.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-7xl mx-auto">
          {features.map((feature, idx) => (
            <div
              key={`feature-${idx}`}
              className={`group p-8 bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-800 hover:shadow-2xl hover:border-indigo-300 dark:hover:border-indigo-600 transition-all hover:-translate-y-2 duration-300 cursor-pointer ${
                visibleSections.has("features")
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-8"
              }`}
              style={{ transitionDelay: `${200 + idx * 100}ms` }}
            >
              <div className="group-hover:scale-110 group-hover:rotate-3 transition-all duration-300">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold mt-6 mb-3 text-gray-900 dark:text-white group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                {feature.title}
              </h3>
              <p className="text-gray-600 dark:text-gray-400 text-sm leading-relaxed font-normal">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* How It Works Section */}
      <div
        ref={(el) => (sectionRefs.current.howto = el)}
        data-section="howto"
        className={`py-20 px-4 bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 rounded-3xl mx-4 transition-all duration-700 ${
          visibleSections.has("howto")
            ? "opacity-100 scale-100"
            : "opacity-0 scale-95"
        }`}
      >
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold mb-4 text-gray-900 dark:text-white tracking-tight">
            How It Works
          </h2>
          <p className="text-gray-600 dark:text-gray-400 max-w-2xl mx-auto font-normal">
            Get your AEO score in three simple steps
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {howItWorksSteps.map((step, idx) => (
            <div
              key={`step-${step.num}`}
              className={`text-center transition-all duration-700 ${
                visibleSections.has("howto")
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-8"
              }`}
              style={{ transitionDelay: `${idx * 150}ms` }}
            >
              <div
                className={`w-16 h-16 ${step.color} text-white rounded-full flex items-center justify-center text-2xl font-semibold mx-auto mb-6 shadow-lg ${step.shadow} hover:scale-110 transition-transform cursor-default`}
              >
                {step.num}
              </div>
              <h3 className="text-xl font-semibold mb-3 text-gray-900 dark:text-white">
                {step.title}
              </h3>
              <p className="text-gray-600 dark:text-gray-400 font-normal">
                {step.desc}
              </p>
            </div>
          ))}
        </div>

        <div
          className={`text-center mt-12 transition-all duration-700 ${
            visibleSections.has("howto")
              ? "opacity-100 scale-100"
              : "opacity-0 scale-90"
          }`}
          style={{ transitionDelay: "600ms" }}
        >
          <button
            onClick={() => onNavigate("analyzer")}
            className="px-7 py-3.5 bg-gray-900 dark:bg-white text-white dark:text-gray-900 rounded-full font-medium text-base hover:bg-gray-800 dark:hover:bg-gray-100 hover:scale-105 active:scale-95 transition-all shadow-lg"
          >
            Try It Now - It&apos;s Free
          </button>
        </div>
      </div>

      {/* Social Proof */}
      <div
        ref={(el) => (sectionRefs.current.social = el)}
        data-section="social"
        className={`mt-24 py-12 text-center transition-all duration-700 ${
          visibleSections.has("social")
            ? "opacity-60"
            : "opacity-0 translate-y-8"
        }`}
      >
        <p className="text-xs uppercase tracking-widest font-medium mb-8 text-gray-500 dark:text-gray-400">
          Trusted by content teams worldwide
        </p>
        <div className="flex flex-wrap justify-center gap-8 md:gap-16 text-xl md:text-2xl font-semibold text-gray-400 dark:text-gray-600">
          {brands.map((brand, idx) => (
            <span
              key={`brand-${brand}`}
              className="hover:text-gray-600 dark:hover:text-gray-400 hover:scale-110 transition-all cursor-default"
              style={{
                animationDelay: `${idx * 100}ms`,
                opacity: visibleSections.has("social") ? 1 : 0,
                transform: visibleSections.has("social")
                  ? "translateY(0)"
                  : "translateY(20px)",
                transition: `all 0.5s ease ${idx * 100}ms`,
              }}
            >
              {brand}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};

// PropTypes validation
Landing.propTypes = {
  onNavigate: PropTypes.func.isRequired,
};

export default Landing;
