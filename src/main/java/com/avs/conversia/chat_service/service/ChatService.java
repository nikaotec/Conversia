package com.avs.conversia.chat_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.repository.BotRepository;
import com.avs.conversia.chat_service.dto.ConversaDTO;
import com.avs.conversia.chat_service.entity.Conversa;
import com.avs.conversia.chat_service.repository.ConversaRepository;

import dev.langchain4j.chain.ConversationalChain;

@Service
public class ChatService {
    private final BotRepository botRepository;
    private final ConversaRepository conversaRepository;
    private final ConversationalChain conversationalChain;

    public ChatService(BotRepository botRepository, ConversaRepository conversaRepository, ConversationalChain conversationalChain) {
        this.botRepository = botRepository;
        this.conversaRepository = conversaRepository;
        this.conversationalChain = conversationalChain;
    }

    public ConversaDTO interagir(Long botId, Long tenantId, String mensagem) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot não encontrado"));
        if (!bot.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Bot não pertence ao tenant informado");
        }

        String resposta = conversationalChain.execute(mensagem);

        Conversa conversa = new Conversa(botId, tenantId, mensagem, resposta);
        conversa = conversaRepository.save(conversa);

        return new ConversaDTO(conversa.getId(), conversa.getBotId(), conversa.getTenantId(),
                conversa.getMensagemUsuario(), conversa.getRespostaBot(), conversa.getTimestamp());
    }

    public List<ConversaDTO> listarHistorico(Long botId, Long tenantId) {
        return conversaRepository.findByBotIdAndTenantId(botId, tenantId).stream()
                .map(c -> new ConversaDTO(c.getId(), c.getBotId(), c.getTenantId(),
                        c.getMensagemUsuario(), c.getRespostaBot(), c.getTimestamp()))
                .collect(Collectors.toList());
    }
}