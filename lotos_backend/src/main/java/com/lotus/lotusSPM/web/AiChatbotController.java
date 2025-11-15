package com.lotus.lotusSPM.web;

import com.lotus.lotusSPM.ai.AiChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI-powered features
 * Provides endpoints for chatbot, resume analysis, job matching, and learning paths
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI & Innovation", description = "AI-powered student services and chatbot")
@CrossOrigin(origins = "*")
@Slf4j
public class AiChatbotController {

    @Autowired
    private AiChatbotService aiChatbotService;

    /**
     * Process chatbot query
     * @param request Query request with message and context
     * @return Chatbot response
     */
    @PostMapping("/chatbot/query")
    @Operation(summary = "Send message to AI chatbot", description = "Get AI-powered response to student queries")
    public ResponseEntity<Map<String, String>> processChatbotQuery(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String context = request.getOrDefault("context", "");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Query is required"));
            }

            String response = aiChatbotService.processQuery(query, context);

            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "response", response,
                    "source", "AI Chatbot"
            ));
        } catch (Exception ex) {
            log.error("Error processing chatbot query", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process query"));
        }
    }

    /**
     * Analyze student resume
     * @param file Resume file (PDF, DOC, DOCX)
     * @return Resume analysis with score and suggestions
     */
    @PostMapping("/resume/analyze")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Analyze resume", description = "AI-powered resume analysis with feedback")
    public ResponseEntity<Object> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.contains("pdf") &&
                !contentType.contains("word") && !contentType.contains("document"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only PDF, DOC, and DOCX files are supported"));
            }

            byte[] fileData = file.getBytes();
            Map<String, Object> analysis = aiChatbotService.analyzeResume(fileData);

            return ResponseEntity.ok(analysis);
        } catch (Exception ex) {
            log.error("Error analyzing resume", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze resume"));
        }
    }

    /**
     * Match student with job opportunities
     * @param studentId Student ID
     * @return List of matched jobs with scores
     */
    @GetMapping("/jobs/match/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'CAREER_CENTER')")
    @Operation(summary = "Match student with jobs", description = "AI-powered job matching based on skills and preferences")
    public ResponseEntity<Object> matchStudentWithJobs(@PathVariable Long studentId) {
        try {
            List<Map<String, Object>> matches = aiChatbotService.matchStudentWithJobs(studentId);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "matches", matches,
                    "matchedAt", java.time.LocalDateTime.now()
            ));
        } catch (Exception ex) {
            log.error("Error matching student with jobs: {}", studentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to match jobs"));
        }
    }

    /**
     * Generate personalized learning path
     * @param studentId Student ID
     * @param targetRole Target role/position
     * @return Learning path with modules and timeline
     */
    @GetMapping("/learning-path/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Generate learning path", description = "AI-generated personalized learning roadmap")
    public ResponseEntity<Object> generateLearningPath(
            @PathVariable Long studentId,
            @RequestParam String targetRole) {
        try {
            if (targetRole == null || targetRole.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Target role is required"));
            }

            Map<String, Object> learningPath = aiChatbotService.generateLearningPath(studentId, targetRole);

            return ResponseEntity.ok(learningPath);
        } catch (Exception ex) {
            log.error("Error generating learning path for student: {}", studentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate learning path"));
        }
    }

    /**
     * Analyze sentiment of feedback text
     * @param request Request with text to analyze
     * @return Sentiment analysis result
     */
    @PostMapping("/sentiment/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Analyze sentiment", description = "AI-powered sentiment analysis of student feedback")
    public ResponseEntity<Object> analyzeSentiment(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");

            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }

            Map<String, Object> sentiment = aiChatbotService.analyzeSentiment(text);

            return ResponseEntity.ok(sentiment);
        } catch (Exception ex) {
            log.error("Error analyzing sentiment", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze sentiment"));
        }
    }

    /**
     * Health check for AI services
     * @return Service status
     */
    @GetMapping("/health")
    @Operation(summary = "AI service health check", description = "Check if AI services are operational")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "operational",
                "service", "AI Chatbot Service",
                "version", "1.0.0",
                "features", List.of("chatbot", "resume-analysis", "job-matching", "learning-path", "sentiment-analysis"),
                "timestamp", java.time.LocalDateTime.now()
        ));
    }
}
