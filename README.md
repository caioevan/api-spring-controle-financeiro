# API de um sistema de controle financeiro
------------------------------------------------------------------------------------------------------------
API que recebe e adiciona usuários e movimentações financeiras.
Cada usuário tem suas movimentações, seja de crédito ou débito, com categorias para cada movimentação.
Cada movimentação pertence a apenas 1 usuário.
------------------------------------------------------------------------------------------------------------
Endpoints dos usuários:
POST /usuario
-Adiciona um usuário;

GET /usuario
-Retorna uma lista de todos os usuários;

GET /usuario/saldo
-Retorna o saldo do usuário cujo o ID é passada na requisição;
------------------------------------------------------------------------------------------------------------
Endpoits das movimentações:
POST /movimentacoes
-Adiciona uma movimentação;

POST /movimentacoes/lote
-Adiciona um array de movimentações;

DELETE /movimentacoes
-Deleta uma movimentação;

GET /movimentacoes
-Busca as movimentações de um usuário;

GET /movimentacoes/busca-por-id
-Busca uma movimentação específica de um usuário;

GET /movimentacoes/busca-por-mes
-Busca as movimentações dentro de um mês de um usuário;

GET /movimentacoes/busca-por-ano
-Busca as movimentações dentro de um ano de um usuário;

GET /movimentacoes/busca-por-categoria
-Busca as movimentações de uma categoria específica de um usuário;

GET /movimentacoes/busca-personalizada
-Busca as movimentações dentro de um range de datas específicas de um usuário;
