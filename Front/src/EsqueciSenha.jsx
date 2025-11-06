import React, { useState, useEffect } from 'react';
import './styles/EsqueciSenha.css';
import logo from './assets/logo.png';
import { VscArrowLeft } from "react-icons/vsc";

import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

function useWindowSize() {
  const [width, setWidth] = useState(window.innerWidth);
  useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);
  return width;
}

function EsqueciSenha() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;
  const isTablet = width >= 800 && width < 1200;
  const isMobile = width < 800;
  
  const [email, setEmail] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    try {
      await axios.post('http://localhost:8080/api/password/forgot-password', {
        email: email
      });
      
      navigate('/link-enviado');

    } catch (error) {
      console.error('Erro ao enviar email:', error);
      setErrorMessage('E-mail não encontrado. Tente novamente.');
    }
  };

  return (
    <div className="container">
      <div className="card">
        {isDesktop && (
          <div className="card-esquerdo">
            <div className="logo-elipse">
              <img src={logo} alt="Logo" className="logo" />
            </div>
            <div className="lume">LUME</div>
            <div className="bem-vindo">Bem-vindo de volta!</div>
            <div className="texto">
              Insira seu e-mail para o envio do link de recuperação de senha
            </div>
          </div>
        )}
        <div className="card-direito">
          <Link to="/" className="voltar">&larr; Voltar</Link>
          {(isTablet || isMobile) && (
            <>
              <div className="logo-elipse">
                <img src={logo} alt="Logo" className="logo" />
              </div>
              <div className="lume">LUME</div>
            </>
          )}
          <div className="esqueci-senha">Esqueci minha senha</div>
          {(isTablet || isMobile) && (
            <div className="texto">
              Insira seu e-mail para o envio do link de recuperação de senha
            </div>
          )}
          
          <form className="formulario" onSubmit={handleSubmit}>
            <label htmlFor="email" className="reset-label">
              E-mail
            </label>
            <input
              type="email"
              id="email"
              className="input"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="Digite seu e-mail"
            />
            {errorMessage && (
              <p style={{ color: '#FDC700', fontSize: '0.9rem', textAlign: 'center' }}>
                {errorMessage}
              </p>
            )}
            <button type="submit" className="botao-enviar">Enviar</button>
          </form>
          <div className="rodape">
            Ainda não possui uma conta? <Link to="/cadastro-estudante" className="link">Crie aqui</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EsqueciSenha;