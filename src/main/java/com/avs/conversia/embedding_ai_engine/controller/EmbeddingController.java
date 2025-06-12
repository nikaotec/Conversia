package com.avs.conversia.embedding_ai_engine.controller;

import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.service.BotService;
import com.avs.conversia.embedding_ai_engine.entity.FileMetadata;
import com.avs.conversia.embedding_ai_engine.service.EmbeddingService;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {
    private final EmbeddingService embeddingService;
    private final BotService botService;

    public EmbeddingController(EmbeddingService embeddingService, BotService botService) {
        this.embeddingService = embeddingService;
        this.botService = botService;
    }

   @PostMapping(value = "/{botId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileMetadata> uploadFile(@PathVariable Long botId, @RequestPart("file") MultipartFile file)
            throws IOException, TikaException {
        Bot bot = botService.findBotById(botId);
        FileMetadata fileMetadata = embeddingService.uploadFile(botId, bot.getTenant().getId(), file);
        return ResponseEntity.ok(fileMetadata);
    }

    @PostMapping("/{botId}/index-url")
    public ResponseEntity<String> indexUrl(@PathVariable Long botId, @RequestBody String url) throws IOException {
        Bot bot = botService.findBotById(botId);
        embeddingService.indexUrl(botId, bot.getTenant().getId(), url);
        return ResponseEntity.ok("URL indexada com sucesso");
    }

    @PostMapping("/{botId}/index-database")
    public ResponseEntity<String> indexDatabase(@PathVariable Long botId) {
        Bot bot = botService.findBotById(botId);
        embeddingService.indexDatabase(botId, bot.getTenant().getId());
        return ResponseEntity.ok("Dados do banco indexados com sucesso");
    }
}