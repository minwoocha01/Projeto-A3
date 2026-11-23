package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndReliabilityAuditTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * AUDITORIA DE SEGURANÇA 1 (Regra SonarQube java:S4502 - CSRF Desabilitado)
     * Exige que requisições de escrita (POST) sem token CSRF sejam bloqueadas com HTTP 403.
     * Como o sistema desativa o CSRF na configuração de segurança, o teste expõe a falha.
     */
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDetectarFaltaDeProtecaoCsrf_Security() throws Exception {
        String novoOwnerJson = "{\"firstName\":\"Carlos\",\"lastName\":\"Silva\",\"address\":\"Rua A, 10\",\"city\":\"Sao Paulo\",\"telephone\":\"11999999999\"}";

        this.mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(novoOwnerJson))
                .andExpect(status().isForbidden());
    }

    /**
     * AUDITORIA DE SEGURANÇA 2 (Regra SonarQube java:S5122 - Política CORS Permissiva)
     * Verifica se a API rejeita origens externas não autorizadas.
     * Como o projeto usa Access-Control-Allow-Origin: *, o cabeçalho é exposto indevidamente.
     */
    @Test
    void testDetectarCorsPermissivo_Security() throws Exception {
        this.mockMvc.perform(options("/api/owners")
                .header("Origin", "https://dominio-nao-autorizado.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    /**
     * AUDITORIA DE FIABILIDADE (Regra SonarQube java:S2259 / Tratamento de Nulos)
     * Simula atualização com payload vazio/nulo para verificar se a aplicação trata
     * entradas incompletas sem lançar exceções internas não tratadas (HTTP 500).
     */
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDetectarFalhaTratamentoPayloadNulo_Reliability() throws Exception {
        this.mockMvc.perform(put("/api/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}