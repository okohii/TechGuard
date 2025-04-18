var database = require("../database/config");

function gerarIdChamado() {
    const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let id = '';
    for (let i = 0; i < 6; i++) {
        id += caracteres.charAt(Math.floor(Math.random() * caracteres.length));
    }
    return id;
}

function cadastrarChamado(descricao, prioridade, fk_usuario, tema, nome_usuario, email_usuario) {
    console.log("CHAMADO MODEL: Cadastrando chamado com os valores:", descricao, prioridade, fk_usuario, tema, nome_usuario, email_usuario);
    const idChamado = gerarIdChamado();

    var instrucaoSql = `
        INSERT INTO chamados (id, descricao, prioridade, fk_usuario, tema, data, nomeUsuario, emailUsuario)
        VALUES ('${idChamado}', '${descricao}', '${prioridade}', '${fk_usuario}', '${tema}', CURRENT_TIMESTAMP, '${nome_usuario}', '${email_usuario}');
    `;
    console.log("Executando a instrução SQL: \n" + instrucaoSql);
    return database.executar(instrucaoSql);
}

function listarChamados() {
    console.log("CHAMADO MODEL: Listando todos os chamados...");
    
    var instrucaoSql = `
        SELECT 
            id,
            nomeUsuario,
            emailUsuario, 
            descricao, 
            prioridade, 
            fk_usuario, 
            tema, 
            data
        FROM 
            chamados;
    `;
    console.log("Executando a instrução SQL: \n" + instrucaoSql);
    return database.executar(instrucaoSql);
}

function deletarChamado(idChamado) {
    console.log("ID do chamado para exclusão:", idChamado);
    
    var instrucaoSql = `DELETE FROM chamados WHERE id = '${idChamado}'`;  
    console.log("Executando a instrução SQL: \n" + instrucaoSql);
    return database.executar(instrucaoSql);
}

function identificarChamado(idChamado) {
    console.log("ID do chamado para busca:", idChamado);
    
    var instrucaoSql = `SELECT * FROM chamados WHERE id = '${idChamado}';`;
    return database.executar(instrucaoSql);
}

module.exports = {
    cadastrarChamado,
    listarChamados,
    deletarChamado,
    identificarChamado
};
