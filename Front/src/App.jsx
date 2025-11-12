import React, { useState, useEffect } from 'react';
import './styles/App.css';
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

function App() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;
  const isTablet = width >= 800 && width < 1200;
  const isMobile = width < 800;
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    try {
      await axios.post('http://localhost:8080/auth/login', {
        email: email,
        password: password
      });
      
      alert('Login realizado com sucesso!');
      navigate('/esqueci-senha');

    } catch (error) {
      console.error('Erro ao fazer login:', error);
      if (error.response) {
        if (error.response.status === 401) {
          setErrorMessage('Email ou senha inválidos.');
        } else if (error.response.status === 403) {
          setErrorMessage('Email não verificado. Por favor, verifique sua caixa de entrada.');
        } else {
          setErrorMessage('Erro ao tentar fazer login. Tente novamente.');
        }
      } else if (error.request) {
        setErrorMessage('Não foi possível se conectar ao servidor. O back-end está no ar?');
      } else {
        setErrorMessage('Ocorreu um erro. Tente novamente.');
      }
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
              Conectando ideias inovadoras a investidores que acreditam no potencial universitário
            </div>
          </div>
        )}
        <div className="card-direito">
          
          {(isTablet || isMobile) && (
            <>
              <div className="logo-elipse">
                <img src={logo} alt="Logo" className="logo" />
              </div>
              <div className="lume">LUME</div>
            </>
          )}
          
          <div className="login-titulo">Login</div>
          
          <form className="formulario" onSubmit={handleSubmit}>
            <label htmlFor="email" className="login-label">
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

            <label htmlFor="password" style={{marginTop: 10}} className="login-label">
              Senha
            </label>
            <input
              type="password"
              id="password"
              className="input"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              placeholder="Digite sua senha"
            />

            <div style={{width: '100%', textAlign: 'right', marginTop: -5, marginBottom: 15}}>
              <Link to="/esqueci-senha" className="link-esqueci">Esqueceu a senha?</Link>
            </div>
            
            {errorMessage && (
              <p style={{ color: '#FDC700', fontSize: '0.9rem', textAlign: 'center', marginTop: -10 }}>
                {errorMessage}
              </p>
            )}
            <button type="submit" className="botao-enviar">Entrar</button>
          </form>
          
          <div className="rodape">
            Ainda não possui uma conta? <Link to="/cadastro-estudante" className="link">Crie aqui</Link>
          </div>
          <div className="rodape" style={{marginTop: 5}}>
            É um investidor? <Link to="/cadastro-investidor" className="link">Cadastre-se aqui</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;