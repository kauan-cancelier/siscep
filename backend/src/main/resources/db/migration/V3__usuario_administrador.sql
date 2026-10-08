-- Usuario administrador inicial (senha: Admin@123). Troque a senha no primeiro acesso.
INSERT INTO usuario (nome, email, senha_hash, perfil)
VALUES ('Administrador do Sistema', 'admin@siscep.com.br',
        '$2a$10$dSRYqoynu5AYjdNJ3.DZluShbP5xogLQqij3KEgNWovn7M79wQb6q', 'ADMINISTRADOR');
