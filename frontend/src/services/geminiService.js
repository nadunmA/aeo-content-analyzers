const API_URL = "http://localhost:6060/api/v1/content/analyze";

export const analyzeContent = async (inputValue, type) => {
  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text: inputValue, type }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Backend error");
    }

    const jsonString = await response.text();
    const data = JSON.parse(jsonString);

    return {
      id: crypto.randomUUID(),
      timestamp: new Date().toISOString(),
      title: data.title ?? inputValue.slice(0, 30) + "...",
      type,
      content: inputValue,
      score: data.score ?? {},
      audits: data.audits ?? [],
      suggestions: data.suggestions ?? [],
      comparison: data.comparison ?? {},
    };

  } catch (error) {
    console.error("API Service Error:", error);
    throw error;
  }
};
