import { useState, useEffect } from "react";

const Navbar = ({ currentPage, onNavigate }) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [isVisible, setIsVisible] = useState(false);

  // Entry animation
  useEffect(() => {
    setTimeout(() => setIsVisible(true), 100);
  }, []);

  // Scroll effect
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const navItems = [
    { id: "landing", label: "Home" },
    { id: "analyzer", label: "Analyze" },
    { id: "tools", label: "Tools" },
    { id: "history", label: "History" },
    { id: "news", label: "News" },
  ];

  const getNavButtonClass = (pageId) => {
    if (currentPage === pageId) {
      return "px-6 py-2 bg-gray-900 dark:bg-white text-white dark:text-gray-900 rounded-full font-medium text-sm transition-all shadow-lg active:scale-95";
    }
    return "px-6 py-2 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-full font-medium text-sm transition-all active:scale-95";
  };

  return (
    <nav
      className={`sticky top-4 z-50 mx-4 sm:mx-8 lg:mx-auto lg:max-w-6xl transition-all duration-500 ${
        isVisible ? "translate-y-0 opacity-100" : "-translate-y-4 opacity-0"
      }`}
    >
      <div
        className={`bg-white/80 dark:bg-gray-900/80 backdrop-blur-xl rounded-full border border-gray-200 dark:border-gray-800 px-4 sm:px-6 h-16 flex items-center justify-between transition-all duration-300 ${
          isScrolled
            ? "shadow-2xl shadow-gray-200/50 dark:shadow-gray-900/50 scale-[0.98]"
            : "shadow-lg"
        }`}
      >
        {/* Logo */}
        <button
          onClick={() => {
            onNavigate("landing");
            setIsMobileMenuOpen(false);
          }}
          className="flex items-center gap-2 group focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded-full px-2 -ml-2 transition-transform hover:scale-105 active:scale-95"
          aria-label="Go to home page"
        >
          <div className="w-8 h-8 bg-gradient-to-br from-indigo-600 to-indigo-700 rounded-xl flex items-center justify-center text-white font-semibold shadow-lg shadow-indigo-500/30 group-hover:shadow-xl group-hover:shadow-indigo-500/40 transition-all">
            A
          </div>
          <span className="text-lg font-semibold bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent hidden sm:block">
            AEO Analyzer
          </span>
        </button>

        {/* Desktop Navigation - ONLY SHOWS ON md+ SCREENS */}
        <div className="hidden md:flex items-center gap-2 bg-gray-100/50 dark:bg-gray-800/50 rounded-full p-1.5">
          {navItems.map((item, index) => (
            <button
              key={item.id}
              onClick={() => onNavigate(item.id)}
              className={getNavButtonClass(item.id)}
              aria-current={currentPage === item.id ? "page" : undefined}
            >
              {item.label}
            </button>
          ))}
        </div>

        {/* Mobile Menu Button - ONLY SHOWS ON MOBILE */}
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="md:hidden p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500 active:scale-90"
          aria-label="Toggle menu"
          aria-expanded={isMobileMenuOpen}
        >
          <div className="relative w-6 h-6">
            <span
              className={`absolute top-1 left-0 w-6 h-0.5 bg-gray-900 dark:bg-white rounded-full transition-all duration-300 ${
                isMobileMenuOpen ? "rotate-45 top-3" : ""
              }`}
            />
            <span
              className={`absolute top-3 left-0 w-6 h-0.5 bg-gray-900 dark:bg-white rounded-full transition-all duration-300 ${
                isMobileMenuOpen ? "opacity-0" : ""
              }`}
            />
            <span
              className={`absolute top-5 left-0 w-6 h-0.5 bg-gray-900 dark:bg-white rounded-full transition-all duration-300 ${
                isMobileMenuOpen ? "-rotate-45 top-3" : ""
              }`}
            />
          </div>
        </button>
      </div>

      {/* Mobile Menu Dropdown - ONLY SHOWS ON MOBILE */}
      <div
        className={`md:hidden mt-2 bg-white/95 dark:bg-gray-900/95 backdrop-blur-xl rounded-2xl border border-gray-200 dark:border-gray-800 overflow-hidden transition-all duration-300 origin-top ${
          isMobileMenuOpen
            ? "shadow-2xl opacity-100 scale-100 max-h-96"
            : "shadow-none opacity-0 scale-95 max-h-0 pointer-events-none"
        }`}
      >
        <div className="p-3 flex flex-col gap-2">
          {navItems.map((item, index) => (
            <button
              key={item.id}
              onClick={() => {
                onNavigate(item.id);
                setIsMobileMenuOpen(false);
              }}
              className={`${
                currentPage === item.id
                  ? "bg-gray-900 dark:bg-white text-white dark:text-gray-900"
                  : "text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
              } px-6 py-3 rounded-xl font-medium text-sm transition-all text-left active:scale-95`}
              aria-current={currentPage === item.id ? "page" : undefined}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
