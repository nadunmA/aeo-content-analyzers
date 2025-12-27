const API_URL = "http://localhost:6060/api/v1/content/analyze";

export const analyzeContent = async (inputValue, type) => {


  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        text: inputValue,
        type: type
      }),

    });

    if(!response.ok){
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Server Error: Could not connect to Backend");
    }

    const jsonString = await response.text();

    const data = JSON.parse(jsonString);

    return {
      id: Math.random().toString(36).substr(2, 9),
      timestamp: new Date().toISOString(),
      title: data.title || (inputValue.substring(0, 30) + "..."),
      type: type,
      content: inputValue,

      ...data
    };

  }catch (error) {
    console.error("API Service Error:", error);
    throw error;
  }

}
