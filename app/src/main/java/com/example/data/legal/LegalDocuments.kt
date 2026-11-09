package com.example.data.legal

object LegalDocuments {
    const val DOCUMENT_VERSION = "1.0"
    const val LAST_UPDATED_DATE = "07/11/2026"
    const val LEGAL_NOTICE_DISCLAIMER = "NOTA LEGAL: Versão 1.0 ($LAST_UPDATED_DATE) - Este documento foi preparado em conformidade com a Constituição da República de Moçambique (Art. 71 - Protecção de Dados Pessoais) e a Lei n.º 3/2017 das Transacções Electrónicas. DEVE SER REVISTO POR UM ADVOGADO ANTES DO LANÇAMENTO OFICIAL."

    val PRIVACY_POLICY = """
        POLÍTICA DE PRIVACIDADE E TRATAMENTO DE DADOS PESSOAIS DA NEXORA
        Versão $DOCUMENT_VERSION - Data de Emissão: $LAST_UPDATED_DATE
        
        $LEGAL_NOTICE_DISCLAIMER
        
        1. ENTIDADE RESPONSÁVEL PELO TRATAMENTO
        A NEXORA é uma plataforma de gestão comercial para pequenas, médias e grandes empresas em Moçambique, operada pela NEXORA TECNOLOGIAS MOÇAMBIQUE, LDA. Contacto oficial de suporte via WhatsApp: +258 83 568 1467 e e-mail: suporte@nexora.co.mz.
        
        2. DADOS PESSOAIS RECOLHIDOS
        A NEXORA recolhe e processa as seguintes categorias de dados:
        a) Dados da Conta do Comerciante: Nome do proprietário/operador, número de telefone (+258), e-mail (se fornecido), nome da loja, localização comercial, moeda (MT) e PIN de acesso encriptado (SHA-256).
        b) Dados Operacionais do Negócio: Catálogo de produtos, preços de compra/venda, histórico de vendas, recibos emitidos, registos de despesas e compras a fornecedores.
        c) Dados dos Clientes do Comerciante: Nome, número de telefone e registos de vendas a fiado. A morada do cliente é um campo estritamente opcional.
        d) Dados de Utilização e Dispositivo: Endereço IP aproximado, modelo de dispositivo, versão da aplicação e registos de sessão para auditoria de segurança.
        
        3. FINALIDADES E BASE JURÍDICA DO TRATAMENTO
        Os dados são recolhidos e tratados para as seguintes finalidades:
        a) Prestação do serviço de gestão de ponto de venda (POS), stock e contabilidade simplificada (execução de contrato).
        b) Envio de recibos comerciais e comprovativos por WhatsApp ou SMS a pedido do utilizador.
        c) Geração de relatórios financeiros e análises inteligentes de negócio via IA.
        d) Cumprimento de obrigações legais, fiscais e contabilísticas aplicáveis na República de Moçambique.
        
        4. LOCALIZAÇÃO DO ARMAZENAMENTO E SEGURANÇA
        Os dados da aplicação são guardados em infra-estrutura de nuvem segura fornecida pelo Firebase / Google Cloud Platform, localizados em servidores de dados protegidos fora do território moçambicano (regiões da União Europeia e Estados Unidos da América). A transmissão é encriptada via protocolo HTTPS/TLS e protegida por Firebase App Check e regras de segurança Firestore.
        
        5. ANÁLISE DE INTELIGÊNCIA ARTIFICIAL (GEMINI API)
        Ao utilizar o Assistente Inteligente Nexora, a aplicação transmite apenas métricas consolidadas do negócio (totais de vendas em MT, volumes de artigos e categorias de despesas) ao modelo de IA Gemini da Google (Firebase AI Logic). NENHUM dado pessoal identificável dos clientes do comerciante (como nomes, telefones ou moradas) é transmitido à Inteligência Artificial.
        
        6. PRAZO DE CONSERVAÇÃO DOS DADOS
        a) Os dados da conta e catálogo mantêm-se activos durante a vigência da subscrição do utilizador.
        b) Os registos de vendas e transacções comerciais são conservados pelo período de 5 (cinco) anos, em conformidade com as obrigações fiscais e comerciais da legislação moçambicana.
        c) Em caso de eliminação da conta ("Direito ao Apagamento"), os dados pessoais da conta são eliminados imediatamente, mantendo-se exclusivamente os registos agregados exigidos por lei.
        
        7. DIREITOS DO UTILIZADOR
        Nos termos da legislação moçambicana aplicável, o utilizador tem os seguintes direitos:
        a) Direito de Acesso e Rectificação: Consultar e alterar os seus dados a qualquer momento em "Mais > Privacidade e Dados".
        b) Direito de Apagamento / Esquecimento: Solicitar a eliminação total da conta mediante validação por PIN.
        c) Direito de Oposição e Revogação: Retirar a qualquer momento o consentimento para análises de IA ou notificações.
        d) Direito de Portabilidade / Exportação: Descarregar a totalidade dos dados da empresa em formato JSON ou CSV.
        
        Para exercer qualquer um destes direitos ou esclarecer dúvidas, entre em contacto através do WhatsApp de suporte +258 83 568 1467 ou do e-mail suporte@nexora.co.mz.
    """.trimIndent()

    val TERMS_OF_USE = """
        TERMOS E CONDIÇÕES DE USO DA PLATAFORMA NEXORA
        Versão $DOCUMENT_VERSION - Data de Emissão: $LAST_UPDATED_DATE
        
        $LEGAL_NOTICE_DISCLAIMER
        
        1. ACEITAÇÃO DOS TERMOS
        Ao criar uma conta ou utilizar a aplicação NEXORA, o comerciante ("Utilizador") concorda expressamente com os presentes Termos de Uso e com a Política de Privacidade. Caso não concorde, não deverá utilizar a aplicação.
        
        2. NATURA DA PLATAFORMA E ISENÇÃO DE GESTÃO FINANCEIRA DIRECTA
        a) A NEXORA é uma ferramenta estritamente de software para REGISTO e GESTÃO COMERCIAL.
        b) A NEXORA NÃO é uma instituição financeira, banco ou emissora de moeda electrónica. A aplicação NÃO movimenta, NÃO guarda, NÃO retém e NÃO cobra comissões sobre os fundos transaccionados por M-Pesa, e-Mola ou Dinheiro.
        c) Todos os pagamentos e transferências ocorrem directamente entre o cliente e o comerciante ou entre o comerciante e os operadores M-Pesa / e-Mola.
        
        3. SUBSCRIÇÃO, PLANOS E PAGAMENTOS DA APLICAÇÃO
        a) A NEXORA disponibiliza Planos Gratuito, Básico e Profissional com limites de produtos e funcionalidades especificadas na aplicação.
        b) As subscrições da aplicação são pagas manualmente via transferência M-Pesa ou e-Mola para os números oficiais fornecidos pela administração NEXORA, mediante envio de comprovativo com referência de transacção.
        c) A NEXORA reserva-se o direito de suspender o acesso a funcionalidades pagas em caso de expiração do plano ou não validação do comprovativo.
        
        4. RESPONSABILIDADES DO COMERCIANTE E EMISSÃO DE RECIBOS
        a) O comerciante é o único responsável pela veracidade dos dados introduzidos, preços praticados e cumprimento das suas obrigações fiscais perante a Autoridade Tributária de Moçambique.
        b) Os recibos gerados pela NEXORA são recibos comerciais de simples conferência interna e contêm o aviso expresso "Recibo comercial, sem valor fiscal", cabendo ao comerciante emitir facturas/recibos oficiais com NUIT nos termos da lei fiscal.
        
        5. RESPONSABILIDADE SOBRE DADOS DE TERCEIROS (CLIENTES)
        O comerciante actua como ÚNICO Responsável pelo Tratamento (*Data Controller*) dos dados pessoais dos seus clientes introduzidos na NEXORA. A NEXORA actua exclusivamente como Subcontratada (*Data Processor*) de armazenamento e processamento sob instruções do comerciante.
        
        6. LIMITAÇÃO DE RESPONSABILIDADE
        A NEXORA não será responsável por lucros cessantes, perdas indirectas, falhas de conectividade da rede móvel local ou erros decorrentes do uso indevido da aplicação por parte do comerciante ou dos seus operadores de caixa.
        
        7. LEI APLICÁVEL E FORO
        Estes Termos são regidos e interpretados em conformidade com as leis da República de Moçambique. Para a resolução de qualquer litígio, é competente o Foro do Tribunal Judicial da Cidade de Maputo, com renúncia expressa a qualquer outro.
    """.trimIndent()

    val CUSTOMER_DATA_PROCESSING_CLAUSE = """
        CLÁUSULA DE TRATAMENTO DE DADOS DE CLIENTES (DONO VS SUB-CONTRATADO)
        Versão $DOCUMENT_VERSION
        
        1. O Comerciante (Proprietário da Empresa) garante que recolheu os contactos dos seus clientes de forma lícita para efeitos de registo de vendas e gestão de fiado.
        2. A NEXORA limita-se a armazenar e disponibilizar os registos na nuvem Firebase protegida de forma encriptada.
        3. Os dados de clientes não são vendidos, alugados nem partilhados com terceiros ou com modelos de Inteligência Artificial.
    """.trimIndent()
}
