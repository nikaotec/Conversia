package com.avs.conversia.embedding_ai_engine.service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.avs.conversia.embedding_ai_engine.entity.Embedding;
import com.avs.conversia.embedding_ai_engine.entity.FileMetadata;
import com.avs.conversia.embedding_ai_engine.entity.UrlMetadata;
import com.avs.conversia.embedding_ai_engine.repository.EmbeddingRepository;
import com.avs.conversia.embedding_ai_engine.repository.FileMetadataRepository;
import com.avs.conversia.embedding_ai_engine.repository.UrlMetadataRepository;
import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.repository.TenantRepository;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {
   private static final Logger logger = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final UrlMetadataRepository urlMetadataRepository;
    private final EmbeddingRepository embeddingRepository;
    private final TenantRepository tenantRepository;
    private final GridFsTemplate gridFsTemplate;
    private final RestTemplate restTemplate;
    private final String huggingFaceApiKey;

    public EmbeddingServiceImpl(FileMetadataRepository fileMetadataRepository, UrlMetadataRepository urlMetadataRepository,
                                EmbeddingRepository embeddingRepository, TenantRepository tenantRepository,
                                GridFsTemplate gridFsTemplate, RestTemplate restTemplate,
                                @Value("${huggingface.api-key:}") String huggingFaceApiKey) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.urlMetadataRepository = urlMetadataRepository;
        this.embeddingRepository = embeddingRepository;
        this.tenantRepository = tenantRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.restTemplate = restTemplate;
        this.huggingFaceApiKey = huggingFaceApiKey;
    }

    @Override
    public FileMetadata uploadFile(Long botId, Long tenantId, MultipartFile file) throws IOException, TikaException {
        logger.info("Iniciando upload do arquivo {} para botId: {}, tenantId: {}", file.getOriginalFilename(), botId, tenantId);

        String gridFsId = storeFileInGridFS(file);
        String extractedText = extractText(file.getInputStream());

        FileMetadata fileMetadata = FileMetadata.builder()
                .botId(botId)
                .tenantId(tenantId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .gridFsId(gridFsId)
                .uploadedAt(LocalDateTime.now())
                .build();
        fileMetadata = fileMetadataRepository.save(fileMetadata);

        indexText(botId, tenantId, extractedText, fileMetadata.getId(), "file");

        logger.info("Arquivo {} processado com sucesso para botId: {}, tenantId: {}", file.getOriginalFilename(), botId, tenantId);
        return fileMetadata;
    }

    @Override
    public void indexUrl(Long botId, Long tenantId, String url) throws IOException {
        logger.info("Indexando URL {} para botId: {}, tenantId: {}", url, botId, tenantId);

        String extractedText = extractTextFromUrl(url);

        UrlMetadata urlMetadata = UrlMetadata.builder()
                .botId(botId)
                .tenantId(tenantId)
                .url(url)
                .indexedAt(LocalDateTime.now())
                .build();
        urlMetadata = urlMetadataRepository.save(urlMetadata);

        indexText(botId, tenantId, extractedText, urlMetadata.getId(), "url");

        logger.info("URL {} indexada com sucesso para botId: {}, tenantId: {}", url, botId, tenantId);
    }

    @Override
    public void indexDatabase(Long botId, Long tenantId) {
        logger.info("Indexando dados do banco para botId: {}, tenantId: {}", botId, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado: " + tenantId));
        String dataText = "Tenant: " + tenant.getName() + "\nDetails: " ;

        indexText(botId, tenantId, dataText, tenantId.toString(), "database");

        logger.info("Dados do banco indexados com sucesso para botId: {}, tenantId: {}", botId, tenantId);
    }

    @Override
    public String findRelevantContext(Long botId, Long tenantId, String query) {
        logger.info("Buscando contexto relevante para botId: {}, tenantId: {}, query: {}", botId, tenantId, query);

        float[] queryEmbedding = generateEmbedding(query, true);
        List<Embedding> embeddings = embeddingRepository.findByBotIdAndTenantId(botId, tenantId);
        Embedding mostRelevant = null;
        double maxSimilarity = -1;

        for (Embedding embedding : embeddings) {
            double similarity = cosineSimilarity(queryEmbedding, embedding.getVector());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostRelevant = embedding;
            }
        }

        String context = mostRelevant != null ? mostRelevant.getTextChunk() : "";
        logger.info("Contexto encontrado: {}", context);
        return context;
    }

    private void indexText(Long botId, Long tenantId, String text, String sourceId, String sourceType) {
        List<String> textChunks = splitTextIntoChunks(text, 512);
        for (String chunk : textChunks) {
            float[] embedding = generateEmbedding(chunk , false);
            Embedding embeddingEntity = Embedding.builder()
                    .botId(botId)
                    .tenantId(tenantId)
                    .fileMetadataId(sourceType.equals("file") ? sourceId : null)
                    .urlMetadataId(sourceType.equals("url") ? sourceId : null)
                    .dbRecordId(sourceType.equals("database") ? sourceId : null)
                    .textChunk(chunk)
                    .vector(embedding)
                    .build();
            embeddingRepository.save(embeddingEntity);
        }
    }

    private String storeFileInGridFS(MultipartFile file) throws IOException {
        return gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).toString();
    }

     private String extractText(InputStream inputStream) throws IOException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1); // Sem limite de tamanho
        Metadata metadata = new Metadata();
        try {
            parser.parse(inputStream, handler, metadata);
        } catch (org.xml.sax.SAXException e) {
            throw new IOException("Erro ao fazer parsing do arquivo", e);
        }

        // Limpar o texto extraído
        String text = handler.toString();
        // Remover caracteres de controle, novas linhas extras e espaços redundantes
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "") // Remove caracteres de controle, exceto \r, \n, \t
                   .replaceAll("\\s+", " ") // Normaliza espaços
                   .trim(); // Remove espaços no início e fim
        // Garantir codificação UTF-8
        text = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        logger.debug("Texto extraído e limpo: {}", text);
        return text;
    }

    private String extractTextFromUrl(String url) throws IOException {
        return Jsoup.connect(url).get().text();
    }

    private List<String> splitTextIntoChunks(String text, int maxTokens) {
        return Arrays.asList(text.split("\n\n"));
    }

private float[] generateEmbedding(String text, boolean isQuery) {
   String url = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + huggingFaceApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            List<?> response = restTemplate.postForObject(url, request, List.class);
            if (response != null && !response.isEmpty()) {
                List<?> embeddingList = (List<?>) response.get(0);
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = ((Number) embeddingList.get(i)).floatValue();
                }
                return embedding;
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar embedding: " + e.getMessage(), e);
        }
        return new float[0];
}


    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}