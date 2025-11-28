import React, { useState, useEffect } from 'react';
import styles from './styles/Login.module.css';
import logo from './assets/logo.png';
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

function Login() {
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
            console.log("Enviando requisição de login...");
            const response = await axios.post('http://localhost:8080/auth/login', {
                email: email,
                password: password
            });

            console.log("Resposta do servidor:", response);
            console.log("Dados da resposta:", response.data);

            const token = response.data.token;
            console.log("Token extraído:", token);

            if (!token) {
                console.error("ERRO: O token veio vazio ou undefined!");
                setErrorMessage("Erro no sistema: Token não recebido.");
                return;
            }

            localStorage.setItem('token', token);
            console.log("Token salvo no localStorage!");

            alert('Login realizado com sucesso!');
            navigate('/home');

        } catch (error) {
            console.error('Erro ao fazer login:', error);
            if (error.response) {
                if (error.response.status === 401) {
                    setErrorMessage('Email ou senha inválidos.');
                } else if (error.response.status === 403) {
                    setErrorMessage('Email não verificado ou bloqueado.');
                } else {
                    setErrorMessage('Erro ao tentar fazer login. Tente novamente.');
                }
            } else if (error.request) {
                setErrorMessage('Não foi possível se conectar ao servidor. O back-end está no ar?');
            } else {
                setErrorMessage('Ocorreu um erro inesperado.');
            }
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
                            Insira seus dados para entrar na sua conta
                        </div>
                    </div>
                )}
                <div className={styles['card-direito']}>
                    {(isTablet || isMobile) && (
                        <>
                            <div className={styles['logo-elipse']}>
                                <img src={logo} alt="Logo" className={styles.logo} />
                            </div>
                            <div className={styles.lume}>LUME</div>
                        </>
                    )}
                    <div className={styles['login-titulo']}>Bem vindo de volta!</div>
                    {(isTablet || isMobile) && (
                        <div className={styles.texto}>
                        Insira seus dados para entrar na sua conta
                    </div>
                    )}
                    <form className={styles.formulario} onSubmit={handleSubmit}>
                        <label htmlFor="email" className={styles['login-label']}>
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
                        <label htmlFor="password" style={{ marginTop: 10 }} className={styles['login-label']}>
                            Senha
                        </label>
                        <input
                            type="password"
                            id="password"
                            className={styles.input}
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            autoComplete="current-password"
                            placeholder="Digite sua senha"
                        />
                        <div style={{ width: '100%', textAlign: 'left', marginTop: -5, marginBottom: 15 }}>
                            <Link to="/esqueci-senha" className={styles['link-esqueci']}>Esqueceu a senha?</Link>
                        </div>
                        {errorMessage && (
                            <p style={{ color: '#FDC700', fontSize: '0.9rem', textAlign: 'center', marginTop: -10 }}>
                                {errorMessage}
                            </p>
                        )}
                        <button type="submit" className={styles['botao-enviar']}>Entrar</button>
                    </form>
                    <div className={styles.rodape}>
                        Ainda não possui uma conta? <Link to="/selecionar-perfil" className={styles.link}>Crie aqui</Link>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Login;