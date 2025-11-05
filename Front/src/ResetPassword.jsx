// src/ResetPassword.jsx

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import './styles/ResetPassword.css';
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

const rules = [
  { label: 'Mínimo 8 caracteres', check: senha => senha.length >= 8 },
  { label: 'Pelo menos 1 número', check: senha => /[0-9]/.test(senha) },
  { label: 'Pelo menos 1 caractere especial', check: senha => /[!@#$%^&*(),.?":{}|<>]/.test(senha) },
  { label: 'Pelo menos 1 letra maiúscula', check: senha => /[A-Z]/.test(senha) },
  { label: 'Pelo menos 1 letra minúscula', check: senha => /[a-z]/.test(senha) }
];

function ResetPassword() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;

  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isValidToken, setIsValidToken] = useState(null);

  useEffect(() => {
    if (!token) {
      setError('Token não encontrado.');
      setIsValidToken(false);
      return;
    }

    const validateToken = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/password/validate-token?token=${token}`);
        if (response.data.valid) {
          setIsValidToken(true);
        } else {
          setError('Token inválido ou expirado. Solicite um novo link.');
          setIsValidToken(false);
        }
      } catch (err) {
        setError('Erro ao validar o token.');
        setIsValidToken(false);
      }
    };
    validateToken();
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (password !== confirmPassword) {
      setError('As senhas não coincidem.');
      return;
    }
    if (password.length < 8) {
      setError('A senha deve ter pelo menos 8 caracteres.');
      return;
    }

    try {
      const payload = {
        token: token,
        newPassword: password,
        confirmPassword: confirmPassword
      };
      const response = await axios.post('http://localhost:8080/api/password/reset-password', payload);
      
      setMessage(response.data.message || 'Senha redefinida com sucesso!');
      
      setTimeout(() => {
        navigate('/');
      }, 3000);

    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao redefinir a senha.');
    }
  };

  const renderContent = () => {
    if (isValidToken === null) {
      return <div className="esqueci-senha">Validando token...</div>;
    }
    if (isValidToken === false) {
      return (
        <>
          <div className="esqueci-senha" style={{ color: '#FDC700' }}>Erro</div>
          <div className="texto" style={{ color: 'white', textAlign: 'center' }}>{error}</div>
          <br/>
          <Link to="/esqueci-senha" className="botao-enviar-link">Solicitar Novo Link</Link>
        </>
      );
    }
    if (isValidToken === true) {
      return (
        <>
          <div className="esqueci-senha">Redefinir Senha</div>
          <form className="formulario" onSubmit={handleSubmit}>
            <label htmlFor="password" className="reset-label">Nova Senha</label>
            <input
              type="password"
              id="password"
              className="input"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              placeholder="Digite sua nova senha"
            />
            <label htmlFor="confirmPassword" className="reset-label">Confirmar Nova Senha</label>
            <input
              type="password"
              id="confirmPassword"
              className="input"
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              required
              placeholder="Confirme sua nova senha"
            />
            {error && <p className="form-message error">{error}</p>}
            {message && <p className="form-message success">{message}</p>}
            <button type="submit" className="botao-enviar">Salvar Nova Senha</button>
          </form>
        </>
      );
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
            <div className="bem-vindo">Quase lá!</div>
            <div className="texto">
              Escolha uma nova senha forte para proteger sua conta.
            </div>
          </div>
        )}
        <div className="card-direito">
          <Link to="/" className="voltar">&larr; Voltar</Link>
          {(width < 1200) && (
            <>
              <div className="logo-elipse">
                <img src={logo} alt="Logo" className="logo" />
              </div>
              <div className="lume">LUME</div>
            </>
          )}
          {renderContent()}
        </div>
      </div>
    </div>
  );
}

export default ResetPassword;