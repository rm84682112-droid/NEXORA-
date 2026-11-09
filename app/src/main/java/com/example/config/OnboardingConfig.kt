package com.example.config

/**
 * Variáveis de configuração global do Onboarding da NEXORA.
 * Ao alterar para true/false, ativa ou desativa as funcionalidades sem apagar o código.
 */
object OnboardingConfig {
    /**
     * Controlo do Tour Inicial de 4 ecrãs para novos Donos de loja.
     * Quando false: Não mostra o diálogo do tour após o registo nem no primeiro acesso.
     */
    const val ONBOARDING_TOUR = false

    /**
     * Controlo do Cartão de Progresso "Primeiros Passos" no Painel (Dashboard).
     * Quando false: O cartão "Primeiros Passos" não é exibido no topo do Painel.
     */
    const val ONBOARDING_CHECKLIST = false

    /**
     * Controlo das Dicas Contextuais (tooltips no Stock, Fiado e Balcão/POS do Caixa).
     * Quando false: Nenhuma dica flutuante ou banner de dica é apresentado.
     */
    const val ONBOARDING_TIPS = false
}
