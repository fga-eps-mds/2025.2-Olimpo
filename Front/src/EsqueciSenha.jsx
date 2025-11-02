import React, { useState, useEffect } from 'react';
import './styles/EsqueciSenha.css';
import logo from './assets/logo.png';

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
          <a href="#" className="voltar">&larr; Voltar</a>
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
          <form className="formulario">
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
            <button type="submit" className="botao-enviar">Enviar</button>
          </form>
          <div className="rodape">
            Ainda não possui uma conta? <a href="#" className="link">Crie aqui</a>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EsqueciSenha;