-- RN10 / RNF11: movimentacoes nao podem ser editadas nem excluidas, nem mesmo direto no banco.
CREATE OR REPLACE FUNCTION fn_bloqueia_alteracao_movimentacao() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Movimentacoes sao imutaveis; registre um ajuste para corrigir o saldo.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimentacao_imutavel
    BEFORE UPDATE OR DELETE ON movimentacao
    FOR EACH ROW EXECUTE FUNCTION fn_bloqueia_alteracao_movimentacao();
