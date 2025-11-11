import React, { useState, useEffect } from 'react';
import './styles/CadastroEstudante.css';
import logo from './assets/logo.png';
import { VscArrowLeft } from "react-icons/vsc";
import { CiCircleRemove, CiCircleCheck } from "react-icons/ci";

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
  {
    label: 'Mínimo 8 caracteres',
    check: senha => senha.length >= 8
  },
  {
    label: 'Pelo menos 1 número',
    check: senha => /[0-9]/.test(senha)
  },
  {
    label: 'Pelo menos 1 caractere especial',
    check: senha => /[!@#$%^&*(),.?":{}|<>]/.test(senha)
  },
  {
    label: 'Pelo menos 1 letra maiúscula',
    check: senha => /[A-Z]/.test(senha)
  },
  {
    label: 'Pelo menos 1 letra minúscula',
    check: senha => /[a-z]/.test(senha)
  }
];

export default function CadastroInvestidor() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;

  const [senha, setSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');

  return (
    <div className="container">
      <div className="card">
        <div className="card-esquerdo">
          <a href="#" className="voltar"><VscArrowLeft />{' Voltar'}</a>
          <div className="cadastro-titulo">Cadastro Estudante</div>
          <form className="formulario formulario-scroll">
            <label className="cadastro-label">Nome completo</label>
            <input type="text" className="input" placeholder="Digite seu nome" autoComplete="name" required />
            
            <label className="cadastro-label">E-mail</label>
            <input type="email" className="input" placeholder="Digite seu e-mail" autoComplete="email" required />
            
            <label className="cadastro-label">CPF</label>
            <input type="text" className="input" placeholder="Digite seu CPF" autoComplete="cpf" required />
            
            <label className="cadastro-label">Telefone</label>
            <input type="text" className="input" placeholder="Digite seu telefone" autoComplete="tel" required />
            
            <label className="cadastro-label">Instituição de ensino</label>
            <input type="text" className="input" placeholder="Digite sua instituição de ensino" autoComplete="tel" required />
            
            <label className="cadastro-label">Curso</label>
            <input type="text" className="input" placeholder="Digite seu curso" autoComplete="tel" required />

            <label className="cadastro-label">Senha</label>
            <input 
              type="password" 
              className="input" 
              placeholder="Senha"
              autoComplete="new-password"
              value={senha}
              onChange={e => setSenha(e.target.value)}
              required 
            />
            {/* Checklist de requisitos da senha */}
            <div className="senha-requisitos" style={{marginBottom: 10, marginLeft: 16}}>
              {rules.map(rule => {
                const passed = rule.check(senha);
                return (
                  <div 
                    key={rule.label} 
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      marginBottom: 5,
                    }}>
                    {passed ? 
                      <CiCircleCheck size={18} color="#FDC700" /> : 
                      <CiCircleRemove size={18} color="#fff" />
                    }
                    <span style={{
                      marginLeft: 8, 
                      color: passed ? '#FDC700' : '#fff',
                      fontWeight: passed ? 'bold' : 'normal', 
                      fontSize: '14px'
                    }}>
                      {rule.label}
                    </span>
                  </div>
                );
              })}
            </div>

            <label className="cadastro-label">Confirmar senha</label>
            <input 
              type="password" 
              className="input" 
              placeholder="Confirmar senha"
              autoComplete="new-password"
              value={confirmaSenha}
              onChange={e => setConfirmaSenha(e.target.value)}
              required 
            />

            {/* Confirmação se as senhas coincidem */}
            <div style={{display: 'flex', alignItems: 'center', marginBottom: 15, marginLeft: 16}}>
              {senha && confirmaSenha && senha === confirmaSenha ? 
                <CiCircleCheck size={18} color="#FDC700" /> :
                <CiCircleRemove size={18} color="#fff" />
              }
              <span style={{marginLeft: 8, color: senha && confirmaSenha && senha === confirmaSenha ? '#FDC700' : '#fff', fontWeight: senha && confirmaSenha && senha === confirmaSenha ? 'bold' : 'normal', fontSize: 14}}>
                As senhas coincidem
              </span>
            </div>

            <button type="submit" className="botao-enviar">Criar conta</button>
          </form>
        </div>

        <div className="card-direito">
          {isDesktop && (
            <>
              <div className="logo-elipse">
                <img src={logo} alt="Logo" className="logo" />
              </div>
              <div className="lume">LUME</div>
              <div className="texto">
                Conectando ideias inovadoras a investidores que acreditam no potencial universitário
                <br/><br/>
                Transforme sua ideia em realidade ou invista no futuro
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}