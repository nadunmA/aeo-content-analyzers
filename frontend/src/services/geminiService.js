const API_URL = "http://localhost:6060/api/content/analyze";

export const analyzeContent = async (inputValue, type) => {

  if(type == "url") {
    throw new Error("URL Analysis feature is coming soon! Please try 'Text Analyzer' instead.");
  }

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({ text: inputValue }),

    });

    if(!response.ok){
      throw new Error("Server Error: Could not connect to Backend");
    }

    const jsonString = await response.text();

    const data = JSON.parse(jsonString);

    return {
      id: Math.random().toString(36).substr(2, 9),
      timestamp: new Date().toISOString(),
      title: inputValue.substring(0, 30) + "...",
      type: type,
      content: inputValue,
   
      ...data
    };

  }catch (error) {
    console.error("API Service Error:", error);
    throw error;
  }

}