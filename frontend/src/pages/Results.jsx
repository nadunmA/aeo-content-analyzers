import { useState } from "react";
import PropTypes from "prop-types";
import { Icons } from "../constants";

// --- Helper: Check if code is valid to show ---
const hasValidCode = (code) => {
  if (!code) return false;
  if (code === "null") return false;
  if (code.trim() === "") return false;
  if (code.toLowerCase().includes("no specific code")) return false;
  return true;
};

// Comparison Section
const ComparisonSection = ({ yourScore, industryAverage, topRanking }) => {
  const scoreDiff = topRanking - yourScore;

  return (
    <div className="bg-white dark:bg-gray-900 rounded-3xl border border-gray-200 dark:border-gray-800 p-6 shadow-sm">
      <h3 className="text-xl font-bold mb-6 text-gray-900 dark:text-white flex items-center gap-2">
        📊 Competitive Analysis
      </h3>
      <div className="space-y-4">
        {[
          { label: "Your Content", value: yourScore, color: "bg-indigo-600" },
          { label: "Top Ranking", value: topRanking, color: "bg-emerald-500" },
          {
            label: "Industry Avg",
            value: industryAverage,
            color: "bg-gray-400",
          },
        ].map((item, idx) => (
          <div key={idx} className="flex items-center gap-4">
            <span className="w-32 text-sm font-medium text-gray-700 dark:text-gray-300">
              {item.label}
            </span>
            <div className="flex-1 h-8 bg-gray-100 dark:bg-gray-800 rounded-lg overflow-hidden">
              <div
                className={`h-full ${item.color} flex items-center justify-end pr-3 text-white text-sm font-bold transition-all duration-1000 ease-out`}
                style={{ width: `${Math.min(item.value || 0, 100)}%` }}
              >
                {item.value}
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="mt-6 p-4 bg-amber-50 dark:bg-amber-900/20 rounded-xl border border-amber-200 dark:border-amber-800">
        <div className="flex items-start gap-3">
          <Icons.AlertCircle className="w-5 h-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
          <div>
            <p className="text-sm font-semibold text-amber-900 dark:text-amber-200 mb-1">
              Performance Gap
            </p>
            <p className="text-sm text-amber-700 dark:text-amber-300">
              {scoreDiff > 0 ? (
                <>
                  You're <span className="font-bold">{scoreDiff} points</span>{" "}
                  behind the top-ranking page.
                </>
              ) : (
                <>
                  🎉 Excellent! You're matching or exceeding top-ranking
                  content!
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

ComparisonSection.propTypes = {
  yourScore: PropTypes.number,
  industryAverage: PropTypes.number,
  topRanking: PropTypes.number,
};

// Main Results Component
const Results = ({ result, onBack }) => {
  const [copiedIndex, setCopiedIndex] = useState(null);

  if (!result) return null;

  // Backend sends 'seo', 'structure', 'readability'
  const schemaScore = result?.score?.schema ?? result?.score?.seo ?? 0;
  const structureScore =
    result?.score?.structure ?? result?.score?.technical ?? 0;
  const readabilityScore = result?.score?.readability ?? 0;

  let totalScore = result?.score?.total ?? result?.score?.overall ?? 0;

  // Fallback calculation if total is 0
  if (totalScore === 0 && (schemaScore > 0 || structureScore > 0)) {
    totalScore = Math.round(
      (schemaScore + structureScore + readabilityScore) / 3
    );
  }

  // Helpers
  const getScoreColor = (score) =>
    score >= 80
      ? "text-emerald-500"
      : score >= 50
      ? "text-amber-500"
      : "text-rose-500";
  const getScoreBg = (score) =>
    score >= 80
      ? "bg-emerald-500"
      : score >= 50
      ? "bg-amber-500"
      : "bg-rose-500";

  const getScoreBadge = (score) => {
    if (score >= 80)
      return {
        label: "Excellent",
        bg: "bg-emerald-50 dark:bg-emerald-900/20",
        text: "text-emerald-700 dark:text-emerald-400",
      };
    if (score >= 50)
      return {
        label: "Good",
        bg: "bg-amber-50 dark:bg-amber-900/20",
        text: "text-amber-700 dark:text-amber-400",
      };
    return {
      label: "Needs Work",
      bg: "bg-rose-50 dark:bg-rose-900/20",
      text: "text-rose-700 dark:text-rose-400",
    };
  };

  const statusConfig = {
    pass: {
      icon: <Icons.CheckCircle className="w-5 h-5" />,
      color: "text-emerald-500",
      bg: "bg-emerald-50 dark:bg-emerald-900/20",
    },
    warning: {
      icon: <Icons.AlertCircle className="w-5 h-5" />,
      color: "text-amber-500",
      bg: "bg-amber-50 dark:bg-amber-900/20",
    },
    fail: {
      icon: <Icons.XCircle className="w-5 h-5" />,
      color: "text-rose-500",
      bg: "bg-rose-50 dark:bg-rose-900/20",
    },
  };

  const getAuditConfig = (status) => {
    const s = status?.toString().toLowerCase() || "fail";
    if (s.includes("pass")) return statusConfig.pass;
    if (s.includes("warn")) return statusConfig.warning;
    return statusConfig.fail;
  };

  const copyToClipboard = async (text, index) => {
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      setCopiedIndex(index);
      setTimeout(() => setCopiedIndex(null), 2000);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  const overallBadge = getScoreBadge(totalScore);
  const scoreMetrics = [
    {
      label: "Schema Markup",
      value: schemaScore,
      icon: <Icons.CheckCircle className="w-4 h-4" />,
    },
    {
      label: "Structure & Q&A",
      value: structureScore,
      icon: <Icons.FileText className="w-4 h-4" />,
    },
    {
      label: "Information Density",
      value: readabilityScore,
      icon: <Icons.Search className="w-4 h-4" />,
    },
  ];

  const auditSummary = {
    passed:
      result.audits?.filter((a) => a.status?.toLowerCase().includes("pass"))
        .length ?? 0,
    warnings:
      result.audits?.filter((a) => a.status?.toLowerCase().includes("warn"))
        .length ?? 0,
    failed:
      result.audits?.filter((a) => a.status?.toLowerCase().includes("fail"))
        .length ?? 0,
  };

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20 max-w-7xl mx-auto px-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-10 gap-4">
        <div>
          <button
            onClick={onBack}
            className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors mb-3 group"
          >
            <span className="rotate-180 group-hover:-translate-x-1 transition-transform">
              <Icons.ArrowRight />
            </span>{" "}
            Back to Analyzer
          </button>
          <h2 className="text-4xl font-extrabold text-gray-900 dark:text-white mb-2">
            Analysis Report
          </h2>
          <p className="text-gray-600 dark:text-gray-400 text-lg font-medium max-w-2xl truncate">
            {result.title || "Untitled Report"}
          </p>
        </div>
        <div className="flex gap-3">
          <button className="px-5 py-2.5 bg-indigo-600 text-white rounded-xl text-sm font-bold hover:bg-indigo-700 active:scale-95 transition-all shadow-lg shadow-indigo-200 dark:shadow-none">
            Share Report
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Scores */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-gradient-to-br from-white to-gray-50 dark:from-gray-900 dark:to-gray-800 rounded-3xl border border-gray-200 dark:border-gray-800 p-8 shadow-lg">
            <div className="text-center mb-8">
              <p className="text-sm uppercase tracking-widest font-bold text-gray-400 dark:text-gray-500 mb-2">
                Overall AEO Score
              </p>
              <span
                className={`inline-block px-3 py-1 rounded-full text-xs font-bold ${overallBadge.bg} ${overallBadge.text}`}
              >
                {overallBadge.label}
              </span>
            </div>
            <div className="relative w-48 h-48 mx-auto mb-8">
              <svg className="w-full h-full transform -rotate-90">
                <circle
                  cx="96"
                  cy="96"
                  r="80"
                  fill="transparent"
                  stroke="currentColor"
                  strokeWidth="14"
                  className="text-gray-100 dark:text-gray-800"
                />
                <circle
                  cx="96"
                  cy="96"
                  r="80"
                  fill="transparent"
                  stroke="currentColor"
                  strokeWidth="14"
                  strokeDasharray={2 * Math.PI * 80}
                  strokeDashoffset={2 * Math.PI * 80 * (1 - totalScore / 100)}
                  strokeLinecap="round"
                  className={`${getScoreColor(
                    totalScore
                  )} transition-all duration-1000 ease-out`}
                  style={{ filter: "drop-shadow(0 0 8px currentColor)" }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span
                  className={`text-6xl font-black ${getScoreColor(totalScore)}`}
                >
                  {totalScore}
                </span>
                <span className="text-xs font-bold text-gray-400 dark:text-gray-500 mt-1">
                  OUT OF 100
                </span>
              </div>
            </div>
            <div className="space-y-5">
              {scoreMetrics.map((metric, idx) => (
                <div key={idx}>
                  <div className="flex justify-between items-center mb-2">
                    <div className="flex items-center gap-2">
                      <div className={getScoreColor(metric.value)}>
                        {metric.icon}
                      </div>
                      <span className="font-semibold text-sm text-gray-700 dark:text-gray-300">
                        {metric.label}
                      </span>
                    </div>
                    <span
                      className={`font-black text-lg ${getScoreColor(
                        metric.value
                      )}`}
                    >
                      {metric.value}
                    </span>
                  </div>
                  <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full ${getScoreBg(
                        metric.value
                      )} transition-all duration-1000 ease-out rounded-full`}
                      style={{ width: `${Math.min(metric.value, 100)}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-800 p-6">
            <h4 className="text-sm font-bold text-gray-700 dark:text-gray-300 mb-4">
              Audit Summary
            </h4>
            <div className="space-y-3">
              {[
                {
                  label: "Passed",
                  value: auditSummary.passed,
                  color: "text-emerald-600",
                  icon: (
                    <Icons.CheckCircle className="w-4 h-4 text-emerald-500" />
                  ),
                },
                {
                  label: "Warnings",
                  value: auditSummary.warnings,
                  color: "text-amber-600",
                  icon: (
                    <Icons.AlertCircle className="w-4 h-4 text-amber-500" />
                  ),
                },
                {
                  label: "Failed",
                  value: auditSummary.failed,
                  color: "text-rose-600",
                  icon: <Icons.XCircle className="w-4 h-4 text-rose-500" />,
                },
              ].map((item, idx) => (
                <div key={idx} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    {item.icon}
                    <span className="text-sm text-gray-600 dark:text-gray-400">
                      {item.label}
                    </span>
                  </div>
                  <span className={`font-bold ${item.color}`}>
                    {item.value}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Detailed Audit & Suggestions */}
        <div className="lg:col-span-2 space-y-8">
          <ComparisonSection
            yourScore={totalScore}
            industryAverage={result.comparison?.industryAverage || 68}
            topRanking={result.comparison?.topRanking || 92}
          />

          {/* Audit List */}
          <div className="bg-white dark:bg-gray-900 rounded-3xl border border-gray-200 dark:border-gray-800 overflow-hidden shadow-sm">
            <div className="p-6 border-b border-gray-200 dark:border-gray-800 bg-gradient-to-r from-indigo-50 to-violet-50 dark:from-indigo-950/20 dark:to-violet-950/20">
              <h3 className="font-bold text-lg flex items-center gap-2 text-gray-900 dark:text-white">
                <div className="p-2 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg text-indigo-600 dark:text-indigo-400">
                  <Icons.CheckCircle className="w-5 h-5" />
                </div>{" "}
                Detailed Audit Report
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-400 mt-1 ml-11">
                {result.audits?.length || 0} checks performed
              </p>
            </div>
            <div className="divide-y divide-gray-100 dark:divide-gray-800">
              {(result.audits || []).map((audit, idx) => {
                const config = getAuditConfig(audit.status);
                return (
                  <div
                    key={idx}
                    className="p-6 flex items-start gap-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors group"
                  >
                    <div
                      className={`mt-0.5 ${config.color} group-hover:scale-110 transition-transform`}
                    >
                      {config.icon}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-start justify-between gap-4 mb-2">
                        <h4 className="font-bold text-sm text-gray-900 dark:text-white">
                          {audit.title || audit.name || "Untitled Check"}
                        </h4>
                        <span
                          className={`flex-shrink-0 px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide ${config.bg} ${config.color}`}
                        >
                          {audit.status || "unknown"}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
                        {audit.description ||
                          audit.explanation ||
                          "No description provided."}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Suggestions */}
          <div>
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="text-2xl font-bold text-gray-900 dark:text-white">
                  Smart Fix Suggestions
                </h3>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                  Copy and implement these optimizations
                </p>
              </div>
              <span className="px-3 py-1 bg-indigo-50 dark:bg-indigo-900/20 text-indigo-600 dark:text-indigo-400 rounded-full text-xs font-bold">
                {(result.suggestions || []).length} suggestions
              </span>
            </div>

            <div className="space-y-6">
              {(result.suggestions || []).map((sug, idx) => {
                const code = sug.code || sug.codeSnippet;
                const showCode = hasValidCode(code);

                return (
                  <div
                    key={idx}
                    className="bg-white dark:bg-gray-900 rounded-3xl border border-gray-200 dark:border-gray-800 overflow-hidden shadow-sm hover:shadow-lg hover:border-indigo-200 dark:hover:border-indigo-800 transition-all"
                  >
                    <div className="p-6 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-800/30">
                      <div className="flex items-center gap-3">
                        <span className="px-3 py-1 bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-lg text-xs uppercase tracking-wider font-bold">
                          {sug.type || "general"}
                        </span>
                        <h4 className="font-bold text-sm text-gray-900 dark:text-white">
                          {sug.title || "Optimization Suggestion"}
                        </h4>
                      </div>
                      {/* Only show Copy button if valid code exists */}
                      {showCode && (
                        <button
                          onClick={() => copyToClipboard(code, idx)}
                          className="p-2.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-all text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 active:scale-90 group relative"
                          title="Copy code"
                        >
                          {copiedIndex === idx ? (
                            <Icons.CheckCircle className="w-5 h-5 text-emerald-500" />
                          ) : (
                            <Icons.Copy className="w-5 h-5" />
                          )}
                          <span className="absolute -top-8 right-0 bg-gray-900 dark:bg-white text-white dark:text-gray-900 text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
                            {copiedIndex === idx ? "Copied!" : "Copy code"}
                          </span>
                        </button>
                      )}
                    </div>
                    <div className="p-6">
                      <p className="text-sm text-gray-600 dark:text-gray-400 mb-5 leading-relaxed flex items-start gap-2">
                        <Icons.AlertCircle className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                        <span className="italic">
                          {sug.explanation || sug.description}
                        </span>
                      </p>

                      {/* Render Code Block or Fallback Message */}
                      {showCode ? (
                        <div className="relative group/code">
                          <div className="absolute -top-3 left-4 px-2 bg-slate-50 dark:bg-gray-900 text-[10px] font-bold tracking-wider text-indigo-500 uppercase border border-indigo-100 dark:border-indigo-900/50 rounded z-10">
                            Code Suggestion
                          </div>
                          <pre className="p-6 bg-[#0d1117] text-gray-300 border border-gray-800 rounded-2xl overflow-x-auto text-xs font-mono leading-relaxed shadow-inner">
                            <code>{code}</code>
                          </pre>
                        </div>
                      ) : (
                        <div className="p-4 bg-gray-50 dark:bg-gray-800/50 rounded-xl border border-gray-100 dark:border-gray-800 text-xs text-gray-500 italic flex items-center gap-2">
                          <Icons.AlertCircle className="w-4 h-4" /> No code
                          snippet required for this fix. Check the explanation
                          above.
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

Results.propTypes = {
  result: PropTypes.object,
  onBack: PropTypes.func.isRequired,
};

export default Results;
