import { useState, useEffect, useRef } from "react";
import { Icons } from "../constants";

const Footer = ({ onNavigate }) => {
  const currentYear = new Date().getFullYear();
  const [showScrollTop, setShowScrollTop] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const footerRef = useRef(null);

  // Show scroll to top button when user scrolls downnn
  useEffect(() => {
    const handleScroll = () => {
      setShowScrollTop(window.scrollY > 300);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Intersection Observer for footer animation - triggers every time
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setIsVisible(true);
          } else {
            setIsVisible(false); // Reset when leaving viewport
          }
        });
      },
      { threshold: 0.1 }
    );

    if (footerRef.current) {
      observer.observe(footerRef.current);
    }

    return () => observer.disconnect();
  }, []);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const socialLinks = [
    {
      name: "Twitter",
      icon: (
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
          <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
        </svg>
      ),
      href: "#",
      color:
        "hover:bg-sky-50 dark:hover:bg-sky-900/20 hover:text-sky-600 dark:hover:text-sky-400",
    },
    {
      name: "LinkedIn",
      icon: (
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
          <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
        </svg>
      ),
      href: "#",
      color:
        "hover:bg-blue-50 dark:hover:bg-blue-900/20 hover:text-blue-600 dark:hover:text-blue-400",
    },
    {
      name: "GitHub",
      icon: (
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
          <path
            fillRule="evenodd"
            d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
            clipRule="evenodd"
          />
        </svg>
      ),
      href: "#",
      color:
        "hover:bg-gray-100 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white",
    },
    {
      name: "Email",
      icon: (
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
            d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      ),
      href: "mailto:support@aeoanalyzer.com",
      color:
        "hover:bg-indigo-50 dark:hover:bg-indigo-900/20 hover:text-indigo-600 dark:hover:text-indigo-400",
    },
  ];

  return (
    <>
      <footer
        ref={footerRef}
        className="border-t border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 mt-20"
      >
        <div className="container mx-auto px-4 py-12">
          {/* Main Content */}
          <div className="max-w-4xl mx-auto">
            {/* Brand Section */}
            <div
              className={`text-center mb-8 transition-all duration-700 ${
                isVisible
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-8"
              }`}
            >
              <div
                className="inline-flex items-center gap-2 mb-4 group cursor-pointer"
                onClick={() => onNavigate?.("landing")}
              >
                <div className="w-10 h-10 bg-gradient-to-br from-indigo-600 to-indigo-700 rounded-2xl flex items-center justify-center text-white font-semibold shadow-lg shadow-indigo-500/30 group-hover:scale-105 group-hover:rotate-3 transition-all">
                  A
                </div>
                <span className="text-2xl font-semibold bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent group-hover:scale-105 transition-transform">
                  AEO Analyzer
                </span>
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400 max-w-md mx-auto mb-8 font-normal">
                Optimize your content for AI-powered search engines.
              </p>
            </div>

            {/* Navigation Buttons */}
            <div className="flex flex-wrap justify-center gap-3 mb-8">
              {["landing", "analyzer", "faq"].map((page, idx) => {
                const labels = {
                  landing: "Home",
                  analyzer: "Analyzer",
                  faq: "FAQ",
                };
                return (
                  <button
                    key={page}
                    onClick={() =>
                      page === "faq"
                        ? console.log("FAQ coming soon")
                        : onNavigate?.(page)
                    }
                    className={`px-6 py-2.5 bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 rounded-full font-medium text-sm text-gray-900 dark:text-white hover:border-indigo-300 dark:hover:border-indigo-600 hover:shadow-lg hover:scale-105 transition-all active:scale-95 ${
                      isVisible
                        ? "opacity-100 translate-y-0"
                        : "opacity-0 translate-y-4"
                    }`}
                    style={{
                      transitionDelay: `${200 + idx * 100}ms`,
                      transitionDuration: "700ms",
                    }}
                  >
                    {labels[page]}
                  </button>
                );
              })}
            </div>

            {/* Social Links */}
            <div className="flex items-center justify-center gap-3 mb-8">
              {socialLinks.map((social, idx) => (
                <a
                  key={social.name}
                  href={social.href}
                  target={social.href.startsWith("http") ? "_blank" : undefined}
                  rel={
                    social.href.startsWith("http")
                      ? "noopener noreferrer"
                      : undefined
                  }
                  className={`p-3 rounded-full bg-white dark:bg-gray-800 border-2 border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 ${
                    social.color
                  } transition-all hover:scale-110 hover:rotate-6 active:scale-95 hover:shadow-lg ${
                    isVisible ? "opacity-100 scale-100" : "opacity-0 scale-50"
                  }`}
                  style={{
                    transitionDelay: `${500 + idx * 80}ms`,
                    transitionDuration: "500ms",
                  }}
                  aria-label={social.name}
                >
                  {social.icon}
                </a>
              ))}
            </div>

            {/* Bottom Bar */}
            <div
              className={`pt-6 border-t border-gray-200 dark:border-gray-800 text-center transition-all duration-700 ${
                isVisible
                  ? "opacity-100 translate-y-0"
                  : "opacity-0 translate-y-4"
              }`}
              style={{ transitionDelay: "800ms" }}
            >
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-3 font-normal">
                © {currentYear} AEO Analyzer. All rights reserved.
              </p>
              <div className="flex flex-wrap justify-center items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
                {["Privacy Policy", "Terms of Service", "Cookie Policy"].map(
                  (link, idx, arr) => (
                    <span key={link} className="inline-flex items-center gap-4">
                      <a
                        href="#"
                        className="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors font-normal hover:underline"
                      >
                        {link}
                      </a>
                      {idx < arr.length - 1 && (
                        <span className="text-gray-300 dark:text-gray-700">
                          •
                        </span>
                      )}
                    </span>
                  )
                )}
              </div>
            </div>
          </div>
        </div>
      </footer>

      {/* Scroll to Top Button */}
      <button
        onClick={scrollToTop}
        className={`fixed bottom-8 right-8 p-4 bg-indigo-600 text-white rounded-full shadow-2xl shadow-indigo-500/40 hover:bg-indigo-700 hover:scale-110 active:scale-95 transition-all z-50 group ${
          showScrollTop
            ? "opacity-100 translate-y-0"
            : "opacity-0 translate-y-16 pointer-events-none"
        }`}
        style={{ transitionDuration: "300ms" }}
        aria-label="Scroll to top"
      >
        <svg
          className="w-5 h-5 group-hover:-translate-y-1 transition-transform"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M5 10l7-7m0 0l7 7m-7-7v18"
          />
        </svg>
      </button>
    </>
  );
};

export default Footer;
