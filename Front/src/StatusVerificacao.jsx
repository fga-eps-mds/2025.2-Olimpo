import React from 'react';
import { useParams, Link } from 'react-router-dom';
import './styles/StatusVerificacao.css'; // Vamos criar este CSS
import logo from './assets/logo.png';
import { CiCircleCheck, CiCircleRemove } from "react-icons/ci"; // Ícones

function StatusVerificacao() {
  // Lê o parâmetro :status da URL (ex: "/verificacao/sucesso")
  const { status } = useParams();

  const isSuccess = status === 'sucesso';

  return (
    <div className="container-status">
      <div className="card-status">
        <div className="logo-elipse-status">
          <img src={logo} alt="Logo" className="logo-status" />
        </div>
        <div className="lume-status">LUME</div>
        
        {isSuccess ? (
          <CiCircleCheck size={80} color="#FDC700" style={{ marginBottom: 20 }} />
        ) : (
          <CiCircleRemove size={80} color="#ff8a8a" style={{ marginBottom: 20 }} />
        )}

        <h1 className={`titulo-status ${isSuccess ? 'sucesso' : 'erro'}`}>
          {isSuccess ? 'E-mail verificado com sucesso!' : 'Link inválido ou expirado!'}
        </h1>
        
        <p className="texto-status">
          {isSuccess 
            ? 'Sua conta está ativa. Agora você pode fazer o login.'
            : 'Por favor, tente solicitar um novo link de verificação.'}
        </p>

        <Link to={isSuccess ? "/" : "/"} className="botao-status">
          {isSuccess ? 'Ir para o Login' : 'Voltar ao Início'}
        </Link>
      </div>
    </div>
  );
}

export default StatusVerificacao;