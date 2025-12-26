import { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { Icons } from "../constants";

const History = ({ onView, onBack }) => {
  const [reports, setReports] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [sortBy, setSortBy] = useState("date");
  const [filterType, setFilterType] = useState("all");
  const [isVisible, setIsVisible] = useState(false);
  const containerRef = useRef(null);

  // Backend Integration
  useEffect(() => {
    const fetchHistory = async () => {
      setIsLoading(true);
      try {
        const response = await fetch(
          `http://localhost:6060/api/v1/content/history?page=${currentPage}&size=12`
        );

        if (!response.ok) {
          throw new Error(`Server error: ${response.status}`);
        }

        const data = await response.json();

        // Backend returns Page object
        setReports(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } catch (err) {
        console.error("Error fetching history:", err);
        setError("Failed to load history. Is the backend running?");
      } finally {
        setIsLoading(false);
        setTimeout(() => setIsVisible(true), 100);
      }
    };

    fetchHistory();
  }, [currentPage]);

  // Delete logic
  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!globalThis.confirm("Are you sure you want to delete this report?"))
      return;

    try {
      const response = await fetch(
        `http://localhost:6060/api/v1/content/report/${id}`,
        { method: "DELETE" }
      );

      if (response.ok) {
        setReports((prev) => prev.filter((item) => item.id !== id));
        setTotalElements((prev) => prev - 1);
      } else {
        alert("Failed to delete report");
      }
    } catch (error) {
      console.error("Delete failed", error);
      alert("Failed to delete report. Check connection.");
    }
  };

  //Intersection Observer for Stats
  const [statsVisible, setStatsVisible] = useState(false);
  const statsRef = useRef(null);

  useEffect(() => {
    if (isLoading || reports.length === 0) return;
    const observer = new IntersectionObserver(
      (entries) =>
        entries.forEach((entry) => setStatsVisible(entry.isIntersecting)),
      { threshold: 0.1 }
    );
    if (statsRef.current) observer.observe(statsRef.current);
    return () => observer.disconnect();
  }, [reports.length, isLoading]);

  // Helpers
  const getScoreColor = (score) => {
    if (score >= 80) return "text-emerald-500 dark:text-emerald-400";
    if (score >= 50) return "text-amber-500 dark:text-amber-400";
    return "text-rose-500 dark:text-rose-400";
  };

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

  const formatDate = (dateString) => {
    if (!dateString) return "Unknown";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  // Filter & Sort Logic (Client Side for Current Page)
  const filteredHistory = reports
    .filter((item) => filterType === "all" || item.type === filterType)
    .sort((a, b) => {
      // Sort by Date
      if (sortBy === "date") {
        return (
          new Date(b.createdAt || b.timestamp) -
          new Date(a.createdAt || a.timestamp)
        );
      }
      // Sort by Score
      const scoreA = a.score?.total || 0;
      const scoreB = b.score?.total || 0;
      return scoreB - scoreA;
    });

  // Stats Calculation
  const stats = {
    total: totalElements,
    avgScore:
      reports.length > 0
        ? Math.round(
            reports.reduce((sum, item) => sum + (item.score?.total || 0), 0) /
              reports.length
          )
        : 0,
    urlCount: reports.filter((item) => item.type === "url").length,
    textCount: reports.filter((item) => item.type === "text").length,
  };

  const statsData = [
    {
      label: "Total Audits",
      value: stats.total,
      color: "text-gray-900 dark:text-white",
    },
    {
      label: "Avg Score",
      value: stats.avgScore,
      color: getScoreColor(stats.avgScore),
    },
    {
      label: "URL Audits",
      value: stats.urlCount,
      color: "text-indigo-600 dark:text-indigo-400",
    },
    {
      label: "Text Audits",
      value: stats.textCount,
      color: "text-violet-600 dark:text-violet-400",
    },
  ];

  // Loading & Error UI
  if (isLoading && reports.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-20 flex flex-col items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
        <p className="text-gray-500 dark:text-gray-400">
          Loading your audit history...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-20">
        <div className="bg-red-50 dark:bg-red-900/20 border-2 border-red-200 dark:border-red-800 rounded-2xl p-8 text-center">
          <p className="text-red-600 dark:text-red-400 font-medium mb-4">
            {error}
          </p>
          <button
            onClick={() => window.location.reload()}
            className="px-6 py-2.5 bg-red-600 text-white rounded-full hover:bg-red-700 transition-colors"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div ref={containerRef} className="max-w-7xl mx-auto px-4 py-10">
      {/* Header */}
      <div
        className={`flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4 transition-all duration-700 ${
          isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
        }`}
      >
        <div>
          <button
            onClick={onBack}
            className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors mb-3 group font-normal"
          >
            <span className="rotate-180 group-hover:-translate-x-1 transition-transform">
              <Icons.ArrowRight />
            </span>
            Back to Home
          </button>
          <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-2 tracking-tight">
            Audit History
          </h2>
          <p className="text-gray-600 dark:text-gray-400 font-normal">
            Review your previous content performance reports.
          </p>
        </div>
      </div>

      {reports.length === 0 ? (
        <div
          className={`bg-white dark:bg-gray-900 rounded-2xl border-2 border-dashed border-gray-200 dark:border-gray-700 p-16 text-center transition-all duration-700 ${
            isVisible ? "opacity-100 scale-100" : "opacity-0 scale-95"
          }`}
        >
          <div className="w-20 h-20 bg-gradient-to-br from-indigo-100 to-indigo-200 dark:from-indigo-900/40 dark:to-indigo-800/40 rounded-full flex items-center justify-center mx-auto mb-6 text-indigo-600 dark:text-indigo-400 shadow-lg shadow-indigo-200/50 dark:shadow-indigo-900/30 animate-in zoom-in-50 duration-500">
            <Icons.History className="w-10 h-10" />
          </div>
          <h3 className="text-2xl font-semibold mb-3 text-gray-900 dark:text-white">
            No Audits Yet
          </h3>
          <p className="text-gray-600 dark:text-gray-400 max-w-md mx-auto mb-8 leading-relaxed font-normal">
            Start your first audit!
          </p>
        </div>
      ) : (
        <>
          {/* Stats Overview */}
          <div
            ref={statsRef}
            className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8"
          >
            {statsData.map((stat, idx) => (
              <div
                key={`stat-${stat.label}`}
                className={`bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 p-6 shadow-sm hover:shadow-lg hover:scale-105 transition-all duration-300 ${
                  statsVisible
                    ? "opacity-100 translate-y-0"
                    : "opacity-0 translate-y-4"
                }`}
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-1 font-normal">
                  {stat.label}
                </p>
                <p className={`text-3xl font-semibold ${stat.color}`}>
                  {stat.value}
                </p>
              </div>
            ))}
          </div>

          {/* Filters and Sort */}
          <div
            className={`flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6 transition-all duration-700 ${
              isVisible
                ? "opacity-100 translate-y-0"
                : "opacity-0 translate-y-4"
            }`}
            style={{ transitionDelay: "200ms" }}
          >
            <div className="flex items-center gap-2">
              <button
                onClick={() => setFilterType("all")}
                className={`px-5 py-2.5 rounded-full text-sm font-medium transition-all hover:scale-105 active:scale-95 ${
                  filterType === "all"
                    ? "bg-indigo-600 text-white shadow-lg shadow-indigo-500/30"
                    : "bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700"
                }`}
              >
                All
              </button>
              <button
                onClick={() => setFilterType("url")}
                className={`px-5 py-2.5 rounded-full text-sm font-medium transition-all hover:scale-105 active:scale-95 ${
                  filterType === "url"
                    ? "bg-indigo-600 text-white shadow-lg shadow-indigo-500/30"
                    : "bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700"
                }`}
              >
                URLs
              </button>
              <button
                onClick={() => setFilterType("text")}
                className={`px-5 py-2.5 rounded-full text-sm font-medium transition-all hover:scale-105 active:scale-95 ${
                  filterType === "text"
                    ? "bg-indigo-600 text-white shadow-lg shadow-indigo-500/30"
                    : "bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700"
                }`}
              >
                Text
              </button>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-600 dark:text-gray-400 font-normal">
                Sort by:
              </span>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-4 py-2 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 rounded-full text-sm font-medium text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-indigo-500 cursor-pointer transition-all hover:border-indigo-300 dark:hover:border-indigo-600"
              >
                <option value="date">Latest First</option>
                <option value="score">Highest Score</option>
              </select>
            </div>
          </div>

          {/* History Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredHistory.map((item, idx) => {
              // Real Score Data
              const totalScore = item.score?.total || 0;
              const badge = getScoreBadge(totalScore);

              return (
                <div
                  key={item.id}
                  onClick={() => onView(item)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      onView(item);
                    }
                  }}
                  className={`relative bg-white dark:bg-gray-900 rounded-2xl border-2 border-gray-200 dark:border-gray-700 p-6 hover:shadow-2xl hover:border-indigo-300 dark:hover:border-indigo-600 transition-all cursor-pointer group hover:-translate-y-2 duration-300 ${
                    isVisible
                      ? "opacity-100 translate-y-0"
                      : "opacity-0 translate-y-8"
                  }`}
                  style={{ transitionDelay: `${300 + idx * 50}ms` }}
                >
                  {/* Delete Button */}
                  <button
                    onClick={(e) => handleDelete(e, item.id)}
                    className="absolute top-4 right-4 p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-full transition-colors z-10 opacity-0 group-hover:opacity-100"
                    title="Delete Report"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M3 6h18" />
                      <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
                      <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
                    </svg>
                  </button>

                  <div className="flex justify-between items-start mb-5">
                    <div
                      className={`p-3 rounded-2xl shadow-sm group-hover:scale-110 group-hover:rotate-3 transition-all ${
                        item.type === "url"
                          ? "bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400"
                          : "bg-violet-50 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400"
                      }`}
                    >
                      {item.type === "url" ? (
                        <Icons.Search className="w-5 h-5" />
                      ) : (
                        <Icons.FileText className="w-5 h-5" />
                      )}
                    </div>
                    <div className="text-right">
                      <div
                        className={`text-3xl font-semibold mb-1 ${getScoreColor(
                          totalScore
                        )} group-hover:scale-110 transition-transform`}
                      >
                        {totalScore}
                      </div>
                      <span
                        className={`text-xs font-medium px-3 py-1 rounded-full ${badge.bg} ${badge.text}`}
                      >
                        {badge.label}
                      </span>
                    </div>
                  </div>

                  <h3 className="font-semibold text-lg mb-3 line-clamp-2 text-gray-900 dark:text-white group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors leading-tight">
                    {item.title}
                  </h3>

                  <div className="flex gap-2 mb-4">
                    <div className="flex-1 bg-gray-50 dark:bg-gray-800 rounded-xl p-2.5">
                      <p className="text-xs text-gray-600 dark:text-gray-400 mb-0.5 font-normal">
                        Readability
                      </p>
                      <p
                        className={`text-sm font-semibold ${getScoreColor(
                          item.score?.readability || 0
                        )}`}
                      >
                        {item.score?.readability || 0}
                      </p>
                    </div>
                    <div className="flex-1 bg-gray-50 dark:bg-gray-800 rounded-xl p-2.5">
                      <p className="text-xs text-gray-600 dark:text-gray-400 mb-0.5 font-normal">
                        Structure
                      </p>
                      <p
                        className={`text-sm font-semibold ${getScoreColor(
                          item.score?.structure || 0
                        )}`}
                      >
                        {item.score?.structure || 0}
                      </p>
                    </div>
                    <div className="flex-1 bg-gray-50 dark:bg-gray-800 rounded-xl p-2.5">
                      <p className="text-xs text-gray-600 dark:text-gray-400 mb-0.5 font-normal">
                        Schema
                      </p>
                      <p
                        className={`text-sm font-semibold ${getScoreColor(
                          item.score?.schema || 0
                        )}`}
                      >
                        {item.score?.schema || 0}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-gray-800">
                    <span className="text-xs text-gray-500 dark:text-gray-400 font-normal">
                      {formatDate(item.createdAt)}
                    </span>
                    <span className="flex items-center gap-1.5 group-hover:translate-x-1 transition-transform font-medium text-indigo-600 dark:text-indigo-400 text-sm">
                      View Report <Icons.ArrowRight className="w-4 h-4" />
                    </span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <button
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0}
                className="px-4 py-2 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 rounded-full disabled:opacity-50 disabled:cursor-not-allowed hover:border-indigo-300 dark:hover:border-indigo-600 transition-all"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600 dark:text-gray-400">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() =>
                  setCurrentPage((p) => Math.min(totalPages - 1, p + 1))
                }
                disabled={currentPage >= totalPages - 1}
                className="px-4 py-2 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 rounded-full disabled:opacity-50 disabled:cursor-not-allowed hover:border-indigo-300 dark:hover:border-indigo-600 transition-all"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

History.propTypes = {
  onView: PropTypes.func.isRequired,
  onBack: PropTypes.func.isRequired,
};

export default History;
