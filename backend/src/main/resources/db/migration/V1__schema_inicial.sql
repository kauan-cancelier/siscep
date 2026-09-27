-- SISCEP - modelo de dados conforme DER (secao 6 do Documento do Sistema)

CREATE TABLE usuario (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(120) NOT NULL,
    email             VARCHAR(160) NOT NULL,
    senha_hash        VARCHAR(100) NOT NULL,
    perfil            VARCHAR(20)  NOT NULL,
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    tentativas_falhas INTEGER      NOT NULL DEFAULT 0,
    bloqueado_ate     TIMESTAMP,
    ultimo_acesso     TIMESTAMP,
    criado_em         TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ADMINISTRADOR', 'ALMOXARIFE', 'OPERADOR', 'GESTOR'))
);

CREATE TABLE fabricante (
    id    BIGSERIAL PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL,
    pais  VARCHAR(60),
    site  VARCHAR(160),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_fabricante_nome UNIQUE (nome)
);

CREATE TABLE fornecedor (
    id                       BIGSERIAL PRIMARY KEY,
    razao_social             VARCHAR(160) NOT NULL,
    cnpj                     VARCHAR(14),
    contato                  VARCHAR(120),
    telefone                 VARCHAR(20),
    email                    VARCHAR(160),
    prazo_medio_entrega_dias INTEGER,
    ativo                    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_fornecedor_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_fornecedor_prazo CHECK (prazo_medio_entrega_dias IS NULL OR prazo_medio_entrega_dias >= 0)
);

CREATE TABLE pastilha (
    id                      BIGSERIAL PRIMARY KEY,
    codigo                  VARCHAR(40)  NOT NULL,
    descricao               VARCHAR(160) NOT NULL,
    especificacao           VARCHAR(500),
    unidade_medida          VARCHAR(10)  NOT NULL DEFAULT 'PC',
    fabricante_id           BIGINT REFERENCES fabricante (id),
    fornecedor_principal_id BIGINT REFERENCES fornecedor (id),
    estoque_minimo          INTEGER NOT NULL DEFAULT 0,
    saldo_atual             INTEGER NOT NULL DEFAULT 0,
    ativo                   BOOLEAN NOT NULL DEFAULT TRUE,
    versao                  BIGINT  NOT NULL DEFAULT 0,
    criado_em               TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_pastilha_codigo UNIQUE (codigo),
    CONSTRAINT ck_pastilha_saldo CHECK (saldo_atual >= 0),
    CONSTRAINT ck_pastilha_minimo CHECK (estoque_minimo >= 0)
);

CREATE TABLE pastilha_fornecedor (
    pastilha_id        BIGINT NOT NULL REFERENCES pastilha (id),
    fornecedor_id      BIGINT NOT NULL REFERENCES fornecedor (id),
    preco_referencia   NUMERIC(12, 2),
    prazo_entrega_dias INTEGER,
    PRIMARY KEY (pastilha_id, fornecedor_id),
    CONSTRAINT ck_pf_preco CHECK (preco_referencia IS NULL OR preco_referencia >= 0)
);

CREATE TABLE movimentacao (
    id                   BIGSERIAL PRIMARY KEY,
    pastilha_id          BIGINT      NOT NULL REFERENCES pastilha (id),
    usuario_id           BIGINT      NOT NULL REFERENCES usuario (id),
    tipo                 VARCHAR(10) NOT NULL,
    quantidade           INTEGER     NOT NULL,
    saldo_anterior       INTEGER     NOT NULL,
    saldo_resultante     INTEGER     NOT NULL,
    fornecedor_id        BIGINT REFERENCES fornecedor (id),
    documento_referencia VARCHAR(60),
    destino              VARCHAR(120),
    justificativa        VARCHAR(500),
    observacao           VARCHAR(500),
    data_hora            TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mov_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE')),
    CONSTRAINT ck_mov_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_mov_saldo CHECK (saldo_resultante >= 0)
);

CREATE TABLE alerta_estoque (
    id                       BIGSERIAL PRIMARY KEY,
    pastilha_id              BIGINT      NOT NULL REFERENCES pastilha (id),
    situacao                 VARCHAR(10) NOT NULL,
    saldo_no_alerta          INTEGER     NOT NULL,
    estoque_minimo_no_alerta INTEGER     NOT NULL,
    gerado_em                TIMESTAMP   NOT NULL DEFAULT NOW(),
    resolvido                BOOLEAN     NOT NULL DEFAULT FALSE,
    resolvido_em             TIMESTAMP
);

CREATE TABLE historico_estoque_minimo (
    id             BIGSERIAL PRIMARY KEY,
    pastilha_id    BIGINT    NOT NULL REFERENCES pastilha (id),
    usuario_id     BIGINT    NOT NULL REFERENCES usuario (id),
    valor_anterior INTEGER   NOT NULL,
    valor_novo     INTEGER   NOT NULL,
    alterado_em    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE registro_acesso (
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(160) NOT NULL,
    sucesso   BOOLEAN      NOT NULL,
    motivo    VARCHAR(120),
    ip        VARCHAR(45),
    data_hora TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pastilha_fabricante ON pastilha (fabricante_id);
CREATE INDEX idx_pastilha_fornecedor ON pastilha (fornecedor_principal_id);
CREATE INDEX idx_mov_pastilha_data ON movimentacao (pastilha_id, data_hora DESC);
CREATE INDEX idx_mov_data ON movimentacao (data_hora DESC);
CREATE INDEX idx_mov_usuario ON movimentacao (usuario_id);
CREATE INDEX idx_mov_tipo_data ON movimentacao (tipo, data_hora);
CREATE INDEX idx_alerta_aberto ON alerta_estoque (pastilha_id) WHERE resolvido = FALSE;
CREATE INDEX idx_registro_acesso_data ON registro_acesso (data_hora DESC);
