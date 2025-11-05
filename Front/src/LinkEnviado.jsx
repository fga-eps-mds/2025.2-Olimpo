import React from 'react';
import './styles/LinkEnviado.css';
import logo from './assets/logo.png';

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
    <div className="container">
      <div className="card">
        {(isMobile || isTablet) && (
          <>
            <div className="elipse-logo">
              <img src={logo} alt="LUME Logo" className="logo" />
            </div>
            <div className="lume">LUME</div>
          </>
        )}

        {isDesktop && (
          <div className="card-esquerdo">
            <div className="elipse-logo">
              <img src={logo} alt="LUME Logo" className="logo" />
            </div>
            <div className="lume">LUME</div>
            <div className="bem-vindo">Bem-vindo de volta!</div>
            <div className="texto-inicial">
              Conectando ideias inovadoras a investidores<br />
              que acreditam no divotencial universitário
            </div>
          </div>
        )}

        <div className="card-direito">
          <a href="#" className="voltar">&larr; Voltar</a>
          <div className="conteudo-card-direito">
            <div className="link-recuperacao">Link de recuperação enviado!</div>
            <p>
              Enviamos um link para recuperação de senha para o seu email<br />
            </p>
            <div>
              Não recebeu o e-mail?
              <a href="#" className="link-reenviar"> Clique aqui para reenviar</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LinkEnviado;
