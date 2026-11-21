# SISCEP — Sistema de Controle de Estoque de Pastilhas

Aplicação web da **DDA Usinagem Industrial Ltda.** para substituir o controle de estoque de pastilhas
industriais feito em planilhas: cadastro de pastilhas, fabricantes e fornecedores, entradas, saídas e
ajustes de inventário com saldo consistente, histórico imutável, alertas de estoque mínimo, dashboard,
relatórios (PDF/CSV) e controle de acesso por perfil.

> Projeto Integrador — SENAI/SC Fraiburgo (AV3).

## Arquitetura

| Camada | Tecnologia |
|---|---|
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, Recharts, React Router |
| Backend | Java 17, Spring Boot 3.3 (Web, Data JPA, Security, Validation, Actuator) |
| Banco de dados | PostgreSQL 16 com migrações Flyway |
| Autenticação | JWT (expiração de 8 h) + BCrypt; RBAC com `@PreAuthorize` |
| Relatórios | OpenPDF (PDF) e CSV UTF-8 com `;` (abre direto no Excel) |
| Testes | JUnit 5, Mockito, Testcontainers, JaCoCo; Vitest + Testing Library |
| Infra | Docker Compose (db, api, web/nginx, backup diário), GitHub Actions |

```
frontend (nginx :80) ──/api──▶ api (Spring Boot :8080) ──▶ db (PostgreSQL :5432)
                                                     backup ──pg_dump diário──▶ ./backups (7 dias)
```

### Estrutura

```
backend/   API REST (domain, repository, service, controller, security, config, dto, exception)
frontend/  SPA React (pages, components, api, auth, utils)
infra/     script de backup do PostgreSQL
.github/   pipeline de CI
```

## Como executar (Docker)

Pré-requisitos: Docker 24+ com Docker Compose v2.

```bash
cp .env.example .env
# edite o .env e defina JWT_SECRET (ex.: openssl rand -base64 48) e DB_PASSWORD
docker compose up -d --build
docker compose ps        # aguarde db, api e web ficarem "healthy"
```

- Aplicação: http://localhost (porta `WEB_PORT`)
- API / Swagger: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

**Primeiro acesso (somente ambiente de desenvolvimento/avaliação):** `admin@siscep.com.br` / `Admin@123`.
Troque a senha em *Alterar senha* logo após entrar e cadastre os demais usuários em *Usuários*.

Para parar: `docker compose down` (os dados ficam no volume `dados-postgres`; `down -v` apaga).

### Backup

O serviço `backup` executa `pg_dump` ao subir e diariamente no horário `BACKUP_HORARIO` (padrão 02:00),
gravando `./backups/siscep_AAAAMMDD_HHMMSS.sql.gz` e removendo arquivos com mais de 7 dias.
Restauração: `gunzip -c backups/<arquivo>.sql.gz | docker compose exec -T db psql -U siscep -d siscep`.

## Execução local (desenvolvimento)

```bash
docker compose up -d db                        # apenas o PostgreSQL
cd backend && mvn spring-boot:run              # API em :8080 (JWT_SECRET padrão de desenvolvimento)
cd frontend && npm install && npm run dev      # SPA em :5173 com proxy para /api
```

## Testes e qualidade

```bash
cd backend && mvn verify      # unitários + integração (Testcontainers, requer Docker) + JaCoCo (mín. 60% no serviço/domínio)
cd frontend && npm run lint && npm run typecheck && npm test && npm run build
```

O relatório de cobertura fica em `backend/target/site/jacoco/index.html`. O CI (`.github/workflows/ci.yml`)
executa as mesmas etapas e o build das imagens Docker a cada push/PR.

## Perfis de acesso

| Funcionalidade | Administrador | Almoxarife | Operador | Gestor |
|---|:-:|:-:|:-:|:-:|
| Usuários e auditoria de acesso | ✔ | | | |
| Cadastros (pastilhas, fabricantes, fornecedores) e estoque mínimo | ✔ | ✔ | | |
| Entradas e ajustes de inventário | | ✔ | | |
| Saídas | | ✔ | ✔ | |
| Consulta de estoque e histórico | ✔ | ✔ | ✔ (só as próprias movimentações) | ✔ |
| Dashboard, relatórios e exportações | ✔ | ✔ | | ✔ |

## Regras de negócio principais

- Saldo nunca fica negativo; saídas acima do saldo são recusadas.
- Movimentações são imutáveis (trigger no PostgreSQL bloqueia `UPDATE`/`DELETE`) e registram saldo anterior e resultante.
- Concorrência: bloqueio pessimista da pastilha durante a movimentação + `@Version`.
- Ajuste exige justificativa e registra a diferença entre contagem física e saldo.
- Situação: **zerado** (saldo 0), **crítico** (0 < saldo ≤ mínimo), **normal** (saldo > mínimo); alertas abertos/resolvidos automaticamente.
- Código da pastilha é único e não pode ser alterado após a primeira movimentação.
- Conta bloqueada por 5 minutos após 3 tentativas de login inválidas; usuário inativado perde o acesso imediatamente.
- Reposição sugerida: `max(1, 2 × estoque mínimo − saldo)`.

## Importação inicial da planilha

Em *Pastilhas → Importar planilha (CSV)*, envie um CSV separado por `;` com o cabeçalho:

```
codigo;descricao;especificacao;unidade;fabricante;estoque_minimo;saldo_inicial
```

Fabricantes inexistentes são criados e o saldo inicial é lançado como ajuste de inventário. Linhas com erro são
reportadas sem interromper as demais (limite de 5.000 linhas).

## API

Documentação interativa em `/swagger-ui.html`. Principais recursos: `/api/auth`, `/api/usuarios`,
`/api/fabricantes`, `/api/fornecedores`, `/api/pastilhas`, `/api/movimentacoes`, `/api/dashboard`,
`/api/alertas`, `/api/relatorios`.

## Autor

Kauan Mello Cancelier
