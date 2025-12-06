import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import axios from 'axios';
import './styles/ResetarSenha.css';
import logo from './assets/logo.png';

function ResetarSenha() {
  const [searchParams] = useSearchParams();

  const [token, setToken] = useState(null);
  const [status, setStatus] = useState('validando');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('Validando seu link...');

  useEffect(() => {
    const tokenDaURL = searchParams.get('token');
    if (!tokenDaURL) {
      setStatus('invalido');
      setMessage('Token não encontrado. O link pode estar quebrado.');
      return;
    }
    setToken(tokenDaURL);

    const validarToken = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/password/validate-token', {
          params: { token: tokenDaURL }
        });

        if (response.data.valid) {
          setStatus('valido');
          setMessage('Token válido! Por favor, defina sua nova senha.');
        } else {
          setStatus('invalido');
          setMessage('Token inválido ou expirado. Por favor, solicite um novo link.');
        }
      } catch {
        setStatus('invalido');
        setMessage('Erro ao validar o token. O servidor pode estar offline.');
      }
    };

    validarToken();
  }, [searchParams]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    if (newPassword !== confirmPassword) {
      setMessage('As senhas não coincidem!');
      return;
    }
    if (newPassword.length < 6) {
      setMessage('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/password/reset-password', {
        token: token,
        newPassword: newPassword,
        confirmPassword: confirmPassword
      });

      setStatus('sucesso');
      setMessage(response.data.message);

    } catch (error) {
      setMessage(error.response?.data?.message || 'Erro ao redefinir a senha.');
    }
  };

  const renderContent = () => {
    switch (status) {
      case 'validando':
        return <div className="spinner"></div>;

      case 'invalido':
        return (
          <>
            <h1 className="titulo-reset status-erro">{message}</h1>
            <Link to="/esqueci-senha" className="botao-reset">
              Solicitar novo link
            </Link>
          </>
        );

      case 'sucesso':
        return (
          <>
            <h1 className="titulo-reset status-sucesso">{message}</h1>
            <Link to="/" className="botao-reset">
              Ir para o Login
            </Link>
          </>
        );

      case 'valido':
        return (
          <form className="formulario-reset" onSubmit={handleSubmit}>
            <h1 className="titulo-reset">Redefinir Senha</h1>
            <label htmlFor="newPassword" className="label-reset">Nova Senha</label>
            <input
              type="password"
              id="newPassword"
              className="input-reset"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              placeholder="Digite sua nova senha"
            />
            <label htmlFor="confirmPassword" className="label-reset">Confirmar Nova Senha</label>
            <input
              type="password"
              id="confirmPassword"
              className="input-reset"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="Confirme sua nova senha"
            />
            {message && message !== 'Token válido! Por favor, defina sua nova senha.' && (
              <p className="mensagem-erro">{message}</p>
            )}
            <button type="submit" className="botao-reset">Redefinir Senha</button>
          </form>
        );

      default:
        return null;
    }
  };

  return (
    <div className="container-reset">
      <div className="card-reset">
        <div className="logo-elipse-reset">
          <img src={logo} alt="Logo" className="logo-reset" />
        </div>
        <div className="lume-reset">LUME</div>
        {renderContent()}
      </div>
    </div>
  );
}

export default ResetarSenha;