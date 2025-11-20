import React, {useState, useEffect, useRef} from 'react';
import filtrar from './assets/filtrar.png'
import lupa from './assets/lupa.png';
import styles from './styles/PesquisarIdeia.module.css';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

export default function PesquisarIdeia() {
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [segmentoOpen, setSegmentoOpen] = useState(false);
  const [investimentoOpen, setInvestimentoOpen] = useState(false);
  const [selectedSegmento, setSelectedSegmento] = useState("");
  const [selectedInvestimento, setSelectedInvestimento] = useState("");
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
        setSegmentoOpen(false);
        setInvestimentoOpen(false);
      }
    }
    
    if (dropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }
    
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dropdownOpen]);

  const toggleSegmento = () => {
    setSegmentoOpen(!segmentoOpen);
    setInvestimentoOpen(false);
  };

  const toggleInvestimento = () => {
    setInvestimentoOpen(!investimentoOpen);
    setSegmentoOpen(false);
  };

  const handleSegmentoSelect = (valor) => {
    setSelectedSegmento(valor);
    setSegmentoOpen(false);
  };

  const handleInvestimentoSelect = (valor) => {
    setSelectedInvestimento(valor);
    setInvestimentoOpen(false);
  };

  return (
    <div className={styles['container-root']}>
      <aside className={styles.sidebar}>
        <nav className={styles['menu-icons']}>
          <div className={styles.container}>
            <input 
              className={styles.input}
              type="text"
              placeholder="Pesquisar"
            />
            <img 
              className={styles.lupa}
              src={lupa}
              alt="lupa"
            />
          </div>
          
          <div ref={dropdownRef} style={{position: 'relative'}}>
            <button 
              className={styles['filtrar-botao']} 
              onClick={() => setDropdownOpen(!dropdownOpen)}
            >
              <img src={filtrar} alt="Filtrar" />
            </button>
            
            {dropdownOpen && (
              <div className={styles.filtro}>
                <div className={styles.label}>Segmento</div>
                
                <div className={styles['dropdown-container']}>
                  <button 
                    className={styles['dropdown-button']}
                    onClick={toggleSegmento}
                  >
                    <span className={styles['dropdown-text']}>
                      {selectedSegmento || "Selecione um segmento"}
                    </span>
                    <span>
                        <img
                            src={segmentoOpen ? setaCima : setaBaixo}
                            width={16}
                            height={8}
                        />        
                    </span>
                  </button>
                  
                  {segmentoOpen && (
                    <div className={styles['dropdown-menu']}>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleSegmentoSelect('Educação')}
                      >
                        Educação
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleSegmentoSelect('Tecnologia')}
                      >
                        Tecnologia
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleSegmentoSelect('Industria alimentícia')}
                      >
                        Industria alimentícia
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleSegmentoSelect('Indústria cinematográfica')}
                      >
                        Indústria cinematográfica
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleSegmentoSelect('Outros')}
                      >
                        Outros
                      </div>
                    </div>
                  )}
                </div>

                <div className={styles.label}>Investimento</div>
                
                <div className={styles['dropdown-container']}>
                  <button 
                    className={styles['dropdown-button']}
                    onClick={toggleInvestimento}
                  >
                    <span className={styles['dropdown-text']}>
                      {selectedInvestimento || "Selecione um investimento"}
                    </span>
                    <span>
                        <img
                        src={investimentoOpen ? setaCima : setaBaixo}
                        width={16}
                        height={8}
                        />
                    </span>
                  </button>
                  
                  {investimentoOpen && (
                    <div className={styles['dropdown-menu']}>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleInvestimentoSelect('Até R$ 10.000')}
                      >
                        Até R$ 10.000
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleInvestimentoSelect('R$ 10.000 - R$ 50.000')}
                      >
                        R$ 10.000 - R$ 50.000
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleInvestimentoSelect('R$ 50.000 - R$ 100.000')}
                      >
                        R$ 50.000 - R$ 100.000
                      </div>
                      <div 
                        className={styles['dropdown-item']} 
                        onClick={() => handleInvestimentoSelect('Acima de R$ 100.000')}
                      >
                        Acima de R$ 100.000
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}            
          </div>        
        </nav>
      </aside>
      
      <main className={styles['main-content']}>
      </main>
    </div>
  );
}