import { GoogleGenAI, Type } from "@google/genai";

const ai = new GoogleGenAI({ apiKey: import.meta.env.VITE_GEMINI_API_KEY || '' });

export const analyzeContent = async (content, type) => {
  const prompt = `
    Perform a comprehensive Answer Engine Optimization (AEO) audit on the following content.
    Content: "${content}"
    
    Evaluate based on:
    1. Directness of answers.
    2. Use of structured data (Schema.org).
    3. Clear Q&A formatting.
    4. Key takeaway summaries.
    5. Factual accuracy and citation readiness.

    Return a JSON object matching this schema:
    {
      "score": { "total": number, "schema": number, "structure": number, "readability": number },
      "audits": [ { "title": string, "status": "pass" | "fail" | "warning", "description": string } ],
      "suggestions": [ { "type": "schema" | "qa" | "summary", "title": string, "code": string, "explanation": string } ]
    }
  `;

  try {
    const response = await ai.models.generateContent({
      model: "gemini-3-flash-preview",
      contents: prompt,
      config: {
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            score: {
              type: Type.OBJECT,
              properties: {
                total: { type: Type.NUMBER },
                schema: { type: Type.NUMBER },
                structure: { type: Type.NUMBER },
                readability: { type: Type.NUMBER },
              },
              required: ["total", "schema", "structure", "readability"]
            },
            audits: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  title: { type: Type.STRING },
                  status: { type: Type.STRING },
                  description: { type: Type.STRING },
                },
                required: ["title", "status", "description"]
              }
            },
            suggestions: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  type: { type: Type.STRING },
                  title: { type: Type.STRING },
                  code: { type: Type.STRING },
                  explanation: { type: Type.STRING },
                },
                required: ["type", "title", "code", "explanation"]
              }
            }
          }
        }
      }
    });

    const data = JSON.parse(response.text || '{}');
    
    return {
      id: Math.random().toString(36).substr(2, 9),
      timestamp: new Date().toISOString(),
      title: type === 'url' ? content : (content.substring(0, 30) + "..."),
      type,
      content,
      ...data
    };
  } catch (error) {
    console.error("Gemini Analysis Error:", error);
    throw error;
  }
};