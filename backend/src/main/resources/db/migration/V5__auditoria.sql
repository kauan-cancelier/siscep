CREATE TABLE auditoria (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NULL REFERENCES usuario (id),
    usuario_nome VARCHAR(150) NOT NULL,
    acao        VARCHAR(40) NOT NULL,
    entidade    VARCHAR(40) NOT NULL,
    entidade_id BIGINT      NULL,
    detalhes    VARCHAR(500) NULL,
    criado_em   TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_criado_em ON auditoria (criado_em DESC);
CREATE INDEX idx_auditoria_entidade ON auditoria (entidade, entidade_id);
