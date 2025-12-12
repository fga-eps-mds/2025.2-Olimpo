import React, { useState, useEffect } from 'react';
import styles from './styles/CadastroInvestidor.module.css';
import logo from './assets/logo.png';
import { VscArrowLeft } from "react-icons/vsc";
import { CiCircleRemove, CiCircleCheck } from "react-icons/ci";
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

const rules = [
  { label: 'Mínimo 8 caracteres', check: senha => senha.length >= 8 },
  { label: 'Pelo menos 1 número', check: senha => /[0-9]/.test(senha) },
  { label: 'Pelo menos 1 caractere especial', check: senha => /[!@#$%^&*(),.?":{}|<>]/.test(senha) },
  { label: 'Pelo menos 1 letra maiúscula', check: senha => /[A-Z]/.test(senha) },
  { label: 'Pelo menos 1 letra minúscula', check: senha => /[a-z]/.test(senha) }
];

export default function CadastroInvestidor() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;
  const navigate = useNavigate();

  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [cpf, setCpf] = useState('');
  const [telefone, setTelefone] = useState('');
  const [empresa, setEmpresa] = useState(null);
  const [cnpj, setCnpj] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (senha !== confirmaSenha) {
      setErrorMessage('As senhas não coincidem');
      return;
    }
    if (!rules.every(rule => rule.check(senha))) {
      setErrorMessage('A senha não cumpre todos os requisitos');
      return;
    }
    if (empresa === null) {
      setErrorMessage('Por favor, selecione se faz parte de uma empresa');
      return;
    }

    const payload = {
      name: nome,
      email: email,
      password: senha,
      role: "INVESTIDOR",
      docType: empresa ? "CNPJ" : "CPF",
      docNumber: empresa ? cnpj : cpf,
      phone: telefone
    };

    try {
      await axios.post('http://localhost:8080/auth/register', payload);
      alert('Cadastro de investidor realizado com sucesso!');
      navigate('/');
    } catch (error) {
      console.error('Erro ao cadastrar:', error);
      if (error.response) {
        console.error('Dados do erro:', error.response.data);
        setErrorMessage(`Erro do servidor: ${error.response.data.message || 'Verifique os dados.'}`);
      } else if (error.request) {
        setErrorMessage('Não foi possível se conectar ao servidor. O back-end está no ar?');
      } else {
        setErrorMessage(`Erro no front-end: ${error.message}`);
      }
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles["card-esquerdo"]}>
          <Link to="/" className={styles.voltar}><VscArrowLeft />{' Voltar'}</Link>
          <div className={styles["cadastro-titulo"]}>Cadastro Investidor</div>
          <form className={`${styles.formulario} ${styles["formulario-scroll"]}`} onSubmit={handleSubmit}>
            <label className={styles["cadastro-label"]}>Nome completo</label>
            <input type="text" className={styles.input} placeholder="Digite seu nome" autoComplete="name"
              value={nome} onChange={e => setNome(e.target.value)} required />
            <label className={styles["cadastro-label"]}>E-mail</label>
            <input type="email" className={styles.input} placeholder="Digite seu e-mail" autoComplete="email"
              value={email} onChange={e => setEmail(e.target.value)} required />

            <label className={styles["cadastro-label"]}>Telefone</label>
            <input type="text" className={styles.input} placeholder="Digite seu telefone" autoComplete="tel"
              value={telefone} onChange={e => setTelefone(e.target.value)} required />

            <label className={styles["cadastro-label"]}>Faz parte de uma empresa?</label>
            <div className={styles["empresa-radio-group"]}>
              <button
                type="button"
                className={empresa === true ? `${styles["input-radio"]} ${styles.selected}` : styles["input-radio"]}
                onClick={() => setEmpresa(true)}
              >Sim</button>
              <button
                type="button"
                className={empresa === false ? `${styles["input-radio"]} ${styles.selected}` : styles["input-radio"]}
                onClick={() => setEmpresa(false)}
              >Não</button>
            </div>

            {empresa === true && (
              <>
                <label className={styles["cadastro-label"]}>CNPJ</label>
                <input type="text" className={styles.input} placeholder="Digite o CNPJ" autoComplete="cnpj"
                  value={cnpj} onChange={e => setCnpj(e.target.value)} required />
              </>
            )}

            {empresa === false && (
              <>
                <label className={styles["cadastro-label"]}>CPF</label>
                <input type="text" className={styles.input} placeholder="Digite seu CPF" autoComplete="cpf"
                  value={cpf} onChange={e => setCpf(e.target.value)} required />
              </>
            )}

            <label className={styles["cadastro-label"]}>Senha</label>
            <input
              type="password"
              className={styles.input}
              placeholder="Senha"
              autoComplete="new-password"
              value={senha}
              onChange={e => setSenha(e.target.value)}
              required
            />
            <div className={styles["senha-requisitos"]} style={{ marginBottom: 15 }}>
              {rules.map(rule => {
                const passed = rule.check(senha);
                return (
                  <div key={rule.label} style={{ display: 'flex', alignItems: 'center', marginBottom: 5, marginLeft: 16 }}>
                    {passed ?
                      <CiCircleCheck size={18} color="#FDC700" /> :
                      <CiCircleRemove size={18} color="#fff" />
                    }
                    <span style={{ marginLeft: 8, color: passed ? '#FDC700' : '#fff', fontWeight: passed ? 'bold' : 'normal', fontSize: '0.85rem' }}>
                      {rule.label}
                    </span>
                  </div>
                );
              })}
            </div>

            <label className={styles["cadastro-label"]}>Confirmar senha</label>
            <input
              type="password"
              className={styles.input}
              placeholder="Confirmar senha"
              autoComplete="new-password"
              value={confirmaSenha}
              onChange={e => setConfirmaSenha(e.target.value)}
              required
            />

            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 15, marginLeft: 16 }}>
              {senha && confirmaSenha && senha === confirmaSenha ?
                <CiCircleCheck size={18} color="#FDC700" /> :
                <CiCircleRemove size={18} color="#fff" />
              }
              <span style={{ marginLeft: 8, color: senha && confirmaSenha && senha === confirmaSenha ? '#FDC700' : '#fff', fontWeight: senha && confirmaSenha && senha === confirmaSenha ? 'bold' : 'normal', fontSize: '0.85rem' }}>
                As senhas coincidem
              </span>
            </div>

            {errorMessage && (
              <p style={{ color: '#FDC700', fontSize: '0.9rem', textAlign: 'center' }}>
                {errorMessage}
              </p>
            )}
            <button type="submit" className={styles["botao-enviar"]}>Criar conta</button>
          </form>
        </div>

        <div className={styles["card-direito"]}>
          {isDesktop && (
            <>
              <div className={styles["logo-elipse"]}>
                <img src={logo} alt="Logo" className={styles.logo} />
              </div>
              <div className={styles.lume}>LUME</div>
              <div className={styles.texto}>
                Conectando ideias inovadoras a investidores que acreditam no potencial universitário
                <br /><br />
                Transforme sua ideia em realidade ou invista no futuro
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}