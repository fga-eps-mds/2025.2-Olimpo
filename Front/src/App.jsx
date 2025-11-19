import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';

import Login from './Login.jsx';
import EsqueciSenha from './EsqueciSenha.jsx';
import LinkEnviado from './LinkEnviado.jsx';
import CadastroEstudante from './CadastroEstudante.jsx';
import CadastroInvestidor from './CadastroInvestidor.jsx';
import StatusVerificacao from './StatusVerificacao.jsx';
import ResetarSenha from './ResetarSenha.jsx';
import PostarIdeia from './PostarIdeia.jsx';
import Home from './Home.jsx'

function App() {
  return (
      <Routes>

        <Route path="/" element={<Login />} />
        <Route path="/esqueci-senha" element={<EsqueciSenha />} />
        <Route path="/link-enviado" element={<LinkEnviado />} />
        <Route path="/cadastro-estudante" element={<CadastroEstudante />} />
        <Route path="/cadastro-investidor" element={<CadastroInvestidor />} />
        <Route path="/verificacao/:status" element={<StatusVerificacao />} />
        <Route path="/reset-password" element={<ResetarSenha />} />
        <Route path="/postar-ideia" element={<PostarIdeia />} />
        <Route path="/home" element={<Home />} />

        <Route path="*" element={
          <div>
            <h1>404 - Página Não Encontrada</h1>
            <Link to="/">Voltar para o Login</Link>
          </div>
        } />
      </Routes>
  );
}

export default App;