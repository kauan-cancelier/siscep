-- View de apoio aos relatorios e a integracoes futuras (RNF10).
CREATE VIEW vw_situacao_estoque AS
SELECT p.id,
       p.codigo,
       p.descricao,
       p.saldo_atual,
       p.estoque_minimo,
       CASE
           WHEN p.saldo_atual = 0 THEN 'ZERADO'
           WHEN p.saldo_atual <= p.estoque_minimo THEN 'CRITICO'
           ELSE 'NORMAL'
       END AS situacao,
       f.nome         AS fabricante,
       fo.razao_social AS fornecedor_principal
FROM pastilha p
         LEFT JOIN fabricante f ON f.id = p.fabricante_id
         LEFT JOIN fornecedor fo ON fo.id = p.fornecedor_principal_id
WHERE p.ativo = TRUE;
