package com.lotus.lotusSPM.web;

import com.lotus.lotusSPM.ai.AiChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "Artificial Intelligence", description = "AI-powered features including chatbot, resume analysis, and job matching")
@CrossOrigin(origins = "*")
public class AiController {

    @Autowired
    private AiChatbotService aiService;

    @PostMapping("/chatbot")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chat with AI assistant", description = "Send a query to the AI chatbot and get a response")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String context = request.getOrDefault("context", "");

            String response = aiService.processQuery(query, context);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing chat: " + e.getMessage());
        }
    }

    @PostMapping("/resume/analyze")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'COORDINATOR')")
    @Operation(summary = "Analyze resume", description = "Upload and analyze a resume using AI")
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            byte[] resumeData = file.getBytes();
            Map<String, Object> analysis = aiService.analyzeResume(resumeData);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error analyzing resume: " + e.getMessage());
        }
    }

    @GetMapping("/jobs/match/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'COORDINATOR', 'CAREER_CENTER')")
    @Operation(summary = "Match student with jobs", description = "Get AI-powered job recommendations for a student")
    public ResponseEntity<?> matchJobs(@PathVariable Long studentId) {
        try {
            List<Map<String, Object>> matches = aiService.matchStudentWithJobs(studentId);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error matching jobs: " + e.getMessage());
        }
    }

    @GetMapping("/learning-path/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'COORDINATOR')")
    @Operation(summary = "Generate learning path", description = "Create personalized learning path for career goal")
    public ResponseEntity<?> generateLearningPath(
            @PathVariable Long studentId,
            @RequestParam String targetRole) {
        try {
            Map<String, Object> path = aiService.generateLearningPath(studentId, targetRole);
            return ResponseEntity.ok(path);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating learning path: " + e.getMessage());
        }
    }

    @PostMapping("/sentiment/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Analyze sentiment", description = "Analyze sentiment of text feedback")
    public ResponseEntity<?> analyzeSentiment(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            Map<String, Object> sentiment = aiService.analyzeSentiment(text);
            return ResponseEntity.ok(sentiment);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error analyzing sentiment: " + e.getMessage());
        }
    }
}
