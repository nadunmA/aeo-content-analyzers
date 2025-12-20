import React, { useState, useEffect } from "react";
import Landing from "./pages/Landing";
import Analyzer from "./pages/Analyzer";
import Results from "./pages/Results";
import History from "./pages/History";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";

const App = () => {
  const [state, setState] = useState(() => {
    const savedHistory = localStorage.getItem("aeo_history");
    return {
      currentPage: "landing",
      currentResult: null,
      history: savedHistory ? JSON.parse(savedHistory) : [],
      isDark: window.matchMedia("(prefers-color-scheme: dark)").matches,
    };
  });

  useEffect(() => {
    localStorage.setItem("aeo_history", JSON.stringify(state.history));
  }, [state.history]);

  useEffect(() => {
    if (state.isDark) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [state.isDark]);

  const navigate = (page) =>
    setState((prev) => ({ ...prev, currentPage: page }));
  const toggleTheme = () =>
    setState((prev) => ({ ...prev, isDark: !prev.isDark }));

  const handleAnalysisComplete = (result) => {
    setState((prev) => ({
      ...prev,
      currentResult: result,
      history: [result, ...prev.history],
      currentPage: "results",
    }));
  };

  const viewHistoryResult = (result) => {
    setState((prev) => ({
      ...prev,
      currentResult: result,
      currentPage: "results",
    }));
  };

  const renderPage = () => {
    switch (state.currentPage) {
      case "landing":
        return <Landing onNavigate={navigate} />;
      case "analyzer":
        return <Analyzer onComplete={handleAnalysisComplete} />;
      case "results":
        return (
          <Results
            result={state.currentResult}
            onBack={() => navigate("analyzer")}
          />
        );
      case "history":
        return (
          <History
            history={state.history}
            onView={viewHistoryResult}
            onBack={() => navigate("landing")}
          />
        );
      default:
        return <Landing onNavigate={navigate} />;
    }
  };

  return (
    <div
      className={`min-h-screen transition-colors duration-300 ${
        state.isDark ? "bg-gray-950 text-gray-100" : "bg-gray-50 text-gray-900"
      }`}
    >
      <Navbar
        currentPage={state.currentPage}
        onNavigate={navigate}
        isDark={state.isDark}
        toggleTheme={toggleTheme}
      />
      <main className="container mx-auto px-4 py-8 max-w-7xl">
        {renderPage()}
      </main>
      <Footer />
    </div>
  );
};

export default App;
