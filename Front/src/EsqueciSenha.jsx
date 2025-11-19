import React, { useState, useEffect } from 'react';
import styles from './styles/EsqueciSenha.module.css'
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
    <div className={styles.container}>
      <div className={styles.card}>
        {isDesktop && (
          <div className={styles['card-esquerdo']}>
            <div className={styles['logo-elipse']}>
              <img src={logo} alt="Logo" className={styles.logo} />
            </div>
            <div className={styles.lume}>LUME</div>
            <div className={styles['bem-vindo']}>Bem-vindo de volta!</div>
            <div className={styles.texto}>
              Insira seu e-mail para o envio do link de recuperação de senha
            </div>
          </div>
        )}
        <div className={styles['card-direito']}>
          <Link to="/" className={styles.voltar}><VscArrowLeft /> Voltar</Link>
          {(isTablet || isMobile) && (
            <>
              <div className={styles['logo-elipse']}>
                <img src={logo} alt="Logo" className={styles.logo} />
              </div>
              <div className={styles.lume}>LUME</div>
            </>
          )}
          <div className={styles['esqueci-senha']}>Esqueci minha senha</div>
          {(isTablet || isMobile) && (
            <div className={styles.texto}>
              Insira seu e-mail para o envio do link de recuperação de senha
            </div>
          )}
          
          <form className={styles.formulario} onSubmit={handleSubmit}>
            <label htmlFor="email" className={styles.email}>
              E-mail
            </label>
            <input
              type="email"
              id="email"
              className={styles.input}
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
            <button type="submit" className={styles['botao-enviar']}>Enviar</button>
          </form>
          <div className={styles.rodape}>
            Ainda não possui uma conta? <Link to="/cadastro-estudante" className={styles.link}>Crie aqui</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EsqueciSenha;