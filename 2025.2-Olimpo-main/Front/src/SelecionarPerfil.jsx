import React from 'react';
import styles from './styles/SelecionarPerfil.module.css';
import { VscArrowLeft } from "react-icons/vsc";
import logo from './assets/logo.png';
import icone_investidor from './assets/icone_investidor.png';
import icone_estudante from './assets/icone_estudante.png';
import { useNavigate, Link } from 'react-router-dom';

function useWindowSize() {
  const [width, setWidth] = React.useState(window.innerWidth);
  React.useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);
  return width;
}

export default function SelecionarPerfil() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;
  const navigate = useNavigate();

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles['card-esquerdo']}>
          <Link to="/" className={styles.voltar}><VscArrowLeft />{' Voltar'}</Link>
          {!isDesktop && (
          <div className={styles['container-lume']}>
            <div className={styles['logo-elipse']}>
              <img src={logo} alt="Logo" className={styles.logo} />
            </div>
            <div className={styles.lume}>LUME</div>
          </div>
          )}
          <div className={styles['cadastro-titulo']}>
            Selecione o tipo de perfil
          </div>
          <div className={styles.perfis}>
            <div className={styles.perfil} onClick={() => navigate('/cadastro-investidor')}>
              <div className={styles.perfilIcone}>
                <img src={icone_investidor} alt="Investidor" />
              </div>
              <div className={styles.perfilTitulo}>Investidor</div>
              <div className={styles.perfilDesc}>
                Quero investir em ideias inovadoras e apoiar novos talentos
              </div>
            </div>
            <div className={styles.perfil} onClick={() => navigate('/cadastro-estudante')}>
              <div className={styles.perfilIcone}>
                <img src={icone_estudante} alt="Estudante Universitário" />
              </div>
              <div className={styles.perfilTitulo}>Estudante Universitário</div>
              <div className={styles.perfilDesc}>
                Tenho uma ideia empreendedora e busco investidores para torná-la realidade
              </div>
            </div>
          </div>
        </div>
        {isDesktop && (
          <div className={styles['card-direito']}>
            <div className={styles['logo-elipse']}>
              <img src={logo} alt="Logo" className={styles.logo} />
            </div>
            <div className={styles.lume}>LUME</div>
            <div className={styles.texto}>
              Conectando ideias inovadoras a investidores que acreditam no potencial universitário
              <br /><br />
              Transforme sua ideia em realidade ou invista no futuro
            </div>
          </div>
        )}
      </div>
    </div>
  );
}