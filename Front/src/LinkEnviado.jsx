import React from 'react';
import styles from './styles/LinkEnviado.module.css';
import logo from './assets/logo.png';
import { VscArrowLeft } from "react-icons/vsc";

function useWindowSize() {
  const [width, setWidth] = React.useState(window.innerWidth);
  React.useEffect(() => {
    function handleResize() {
      setWidth(window.innerWidth);
    }
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);
  return width;
}

function LinkEnviado() {
  const width = useWindowSize();
  const isDesktop = width >= 1200;
  const isTablet = width >= 800 && width < 1200;
  const isMobile = width < 800;

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        {(isMobile || isTablet) && (
          <>
            <div className={styles["elipse-logo"]}>
              <img src={logo} alt="LUME Logo" className={styles.logo} />
            </div>
            <div className={styles.lume}>LUME</div>
          </>
        )}

        {isDesktop && (
          <div className={styles["card-esquerdo"]}>
            <div className={styles["elipse-logo"]}>
              <img src={logo} alt="LUME Logo" className={styles.logo} />
            </div>
            <div className={styles.lume}>LUME</div>
            <div className={styles["bem-vindo"]}>Bem-vindo de volta!</div>
            <div className={styles["texto-inicial"]}>
              Conectando ideias inovadoras a investidores<br />
              que acreditam no divotencial universitário
            </div>
          </div>
        )}

        <div className={styles["card-direito"]}>
          <a href="#" className={styles.voltar}><VscArrowLeft /> Voltar</a>
          <div className={styles["conteudo-card-direito"]}>
            <div className={styles["link-recuperacao"]}>Link de recuperação enviado!</div>
            <div className={styles['texto']}>Enviamos um link para recuperação de senha para o seu email</div>
            <div>
              Não recebeu o e-mail?
              <a href="#" className={styles["link-reenviar"]}> Clique aqui para reenviar</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LinkEnviado;