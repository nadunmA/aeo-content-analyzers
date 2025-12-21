package com.aeo.analyzer.controller;

import com.aeo.analyzer.service.GeminiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*") //connect with frontend
public class ContentController {

    private final GeminiService geminiService;

    //service connect controller (dependency injection)
    public ContentController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze")
    public  String analyze(@RequestBody Map<String, String> payload) {
        //get text frontend
        String text = payload.get("text");

        //send gemini service and get results
        return  geminiService.analyzeContent(text);
    }


}
