-- Cria o banco de dados e abre ela
CREATE DATABASE IF NOT EXISTS db_controle_residuos;
USE db_controle_residuos;

-- 1. Tabela de Usuários (Login e Registro)
CREATE TABLE Usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(32) NOT NULL UNIQUE,
    senha VARCHAR(16) NOT NULL
);

-- 2. Tabela de Estoque de Resíduos
CREATE TABLE Estoque_residuos (
    id_residuo INT AUTO_INCREMENT PRIMARY KEY,
    nome_residuo VARCHAR(100) NOT NULL,
    tipo_armazenagem VARCHAR(32) NOT NULL, -- Fardo, Rack, Tubo, Tambor, Volume
    quantidade INT DEFAULT 0,
    limite_medio INT NOT NULL,
    limite_alto INT NOT NULL
);

-- 3. Tabela de Agendamentos Pendentes/Ativos
CREATE TABLE Agendamentos (
    id_agendamento INT AUTO_INCREMENT PRIMARY KEY,
    data_agendada DATE NOT NULL,
    horario TIME NOT NULL, -- Ex: '08:30:00', '14:00:00'
    empresa_retirada VARCHAR(100) NOT NULL,
    status ENUM('Pendente', 'Concluido', 'Nao Concluido') DEFAULT 'Pendente',
    id_usuario_criou INT NOT NULL,
    FOREIGN KEY (id_usuario_criou) REFERENCES Usuarios(id_usuario)
);

-- 4. Tabela de Itens do Agendamento (Permite vários resíduos por carregamento)
CREATE TABLE Agendamentos_Itens (
    id_item INT AUTO_INCREMENT PRIMARY KEY,
    id_agendamento INT NOT NULL,
    id_residuo INT NOT NULL,
    FOREIGN KEY (id_agendamento) REFERENCES Agendamentos(id_agendamento) ON DELETE CASCADE,
    FOREIGN KEY (id_residuo) REFERENCES Estoque_residuos(id_residuo)
);

-- 5. Tabela de Histórico de Carregamentos
CREATE TABLE Historico_Carregamentos (
    id_historico INT AUTO_INCREMENT PRIMARY KEY,
    id_agendamento INT,
    data_conclusao DATE NOT NULL,
    mes INT NOT NULL, -- Extraído para facilitar relatórios de meses/anos anteriores
    ano INT NOT NULL, -- Extraído para facilitar relatórios
    empresa_retirada VARCHAR(100) NOT NULL,
    residuos_descricao TEXT NOT NULL, -- Salva o que foi levado em texto (Ex: "10 Fardos Papelão, 5 Racks") para o histórico não quebrar se um resíduo for excluído no futuro
    id_usuario_encerrou INT NOT NULL,
    FOREIGN KEY (id_usuario_encerrou) REFERENCES Usuarios(id_usuario)
);


-- Inserindo dados:
INSERT INTO Estoque_residuos (nome_residuo, tipo_armazenagem, limite_medio, limite_alto) VALUES
('Papelão', 'Rack', 10, 15),
('Casquinha', 'Rack', 20, 21),
('Massinha', 'Rack', 10, 13),
('Reciclagem doação', 'Rack', 20, 25),
('Papelão', 'Fardo', 35, 38),
('Plástico filme', 'Fardo', 35, 38),
('Doação', 'Fardo', 11, 16),
('Massa líquida', 'IBC', 2, 2), -- Limite médio igual ao alto para forçar o pulo direto de Baixa para Alta
('Plástico', 'Tambor', 40, 60),
('Ferro', 'Tambor', 40, 60),
('Pó fábrica 2', 'Colméia', 3, 4),
('Casquinha moída', 'Bag', 20, 21),
('Descarte moinho', 'Volume', 4, 5),
('Papelão', 'Tubo', 4, 5),
('Pallet', 'Volume Combo', 50, 100), -- Limite fictício (ajuste conforme necessário)
('Balde', 'Volume Combo', 50, 100), -- Limite fictício (ajuste conforme necessário)
('Bombonas', 'Volume Combo', 50, 100); -- Limite fictício (ajuste conforme necessário)

INSERT INTO usuários ( nome, login, senha ) VALUES ('teste','adm1','123654'), ('teste2','adm2','456321');