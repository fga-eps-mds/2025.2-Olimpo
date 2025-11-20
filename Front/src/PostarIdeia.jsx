import React, {useState, useEffect, useRef} from 'react';
// ... (mantenha seus imports de imagens e CSS aqui)
import home from './assets/home.png';
import home_hover from './assets/home_hover.png';
import coracao from './assets/coracao.png';
import coracao_hover from './assets/coracao_hover.png';
import seta from './assets/seta.png';
import seta_hover from './assets/seta_hover.png';
import lupa from './assets/lupa.png';
import lupa_hover from './assets/lupa_hover.png';
import mais from './assets/mais.png';
import mais_hover from './assets/mais_hover.png';
import usuario from './assets/usuario.png';
import styles from './styles/PostarIdeia.module.css';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

export default function PostarIdeia() {

  const [hovered, setHovered] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  
  // Estados para os dados do formulário
  const [selected, setSelected] = useState(""); // Segmento (Keyword)
  const [titulo, setTitulo] = useState("");
  const [investimento, setInvestimento] = useState("");
  const [descricao, setDescricao] = useState("");
  const [imagem, setImagem] = useState(null);

  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    }
    if (dropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dropdownOpen]);

  // Função para enviar os dados para o Backend
  const handleSubmit = async (e) => {
    e.preventDefault();

    // 1. Validar dados básicos
    if (!titulo || !selected || !investimento) {
      alert("Por favor, preencha os campos obrigatórios.");
      return;
    }

    // 2. Criar o objeto FormData para envio de arquivos + dados
    const formData = new FormData();
    
    // Estrutura do objeto Idea (baseado no seu IdeaTests.java)
    const ideaData = {
      name: titulo,
      description: descricao,
      price: parseFloat(investimento), // Backend espera um número
      keywords: [selected] // Envia o segmento como uma lista de keywords
    };

    // Adiciona o JSON da ideia. O Backend deve esperar um @RequestPart("data") ou similar
    formData.append('data', new Blob([JSON.stringify(ideaData)], {
        type: 'application/json'
    }));

    // Adiciona o arquivo se existir
    if (imagem) {
      formData.append('file', imagem);
    }

    try {
      // Recupera o token salvo no login
      const token = localStorage.getItem('token'); 

      const response = await fetch('http://localhost:8080/api/ideas', { 
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}` 
          },
          body: formData
        });

      if (response.ok) {
        alert("Ideia postada com sucesso!");
        // Limpar formulário ou redirecionar
        setTitulo("");
        setDescricao("");
        setInvestimento("");
        setImagem(null);
        setSelected("");
      } else {
        const errorText = await response.text();
        alert("Erro ao postar ideia: " + errorText);
      }
    } catch (error) {
      console.error("Erro na requisição:", error);
      alert("Erro de conexão com o servidor.");
    }
  };

  return (
    <div className={styles['container-root']}>
      <aside 
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      className={styles.sidebar}>
        <nav className={styles['menu-icons']}>
          <button className={styles['icon-btn']}><img src={hovered ? home_hover : home} alt="Home" /><span>Início</span></button>
          <button className={styles['icon-btn']}><img src={hovered ? coracao_hover : coracao} alt="Coracao" /><span>Notificações</span></button>
          <button className={styles['icon-btn']}><img src={hovered ? seta_hover : seta} alt="Seta" /><span>Mensagens</span></button>
          <button className={styles['icon-btn']}><img src={hovered ? lupa_hover : lupa} alt="Lupa" /><span>Pesquisar</span></button>
          <button className={styles['icon-btn']}><img src={hovered ? mais_hover : mais} alt="Mais" /><span>Postar</span></button>
        </nav>
        <div className={styles.profile}>
          <button className={styles['profile-btn']}><img src={usuario} alt="Usuário" /><span>Perfil</span></button>
        </div>
      </aside>
      <main className={styles['main-content']}>
        <div className={styles['form-container']}>
          
          {/* Adicionado onSubmit */}
          <form className={styles['post-form']} onSubmit={handleSubmit}>
            
            <label className={styles.label}>Imagem</label>
            <input
              className={styles['input-imagem']}
              type="file"
              accept="image/*" // Restringe para imagens
              onChange={(e) => setImagem(e.target.files[0])} // Captura o arquivo
            />

            <label className={styles.label}>Título</label>
            <input
              className={styles.input}
              type="text"
              placeholder="Título"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)} // Captura o título
            />

            <div className={styles['input-row']}>
              <div>
                <label className={styles.label}>Segmento</label>
                <div ref={dropdownRef}>
                <div className={styles.menu}>
                <button type ="button" className={styles.selecionar} onClick={() => setDropdownOpen(!dropdownOpen)}>
                  {selected || "Selecione uma opção"}
                  <span className={styles.seta}>
                    <img
                      src={dropdownOpen ? setaCima : setaBaixo}
                      alt="seta"
                      width={14}
                      height={7}
                    />
                    </span>
                </button>
                {dropdownOpen && (
                  <div className={styles['lista-selecionar']}>
                    {/* As strings aqui devem bater com o nome das Keywords no Banco de Dados */}
                    <div className={styles['lista-itens']} onClick={() => {setSelected("Educação"); setDropdownOpen(false)}}>Educação</div>
                    <div className={styles['lista-itens']} onClick={() => {setSelected("Tecnologia"); setDropdownOpen(false)}}>Tecnologia</div>
                    <div className={styles['lista-itens']} onClick={() => {setSelected("Indústria alimentícia"); setDropdownOpen(false)}}>Indústria alimentícia</div>
                    <div className={styles['lista-itens']} onClick={() => {setSelected("Indústria Cinematográfica"); setDropdownOpen(false)}}>Indústria Cinematográfica</div>
                    <div className={styles['lista-itens']} onClick={() => {setSelected("Outros"); setDropdownOpen(false)}}>Outros</div>
                  </div>
                )}
              </div>
              </div>
              </div>
              <div>
                <label className={styles.label}>Investimento</label>
                <input
                  className={styles.input}
                  type="number" // Mudado para number para facilitar
                  placeholder="Investimento"
                  value={investimento}
                  onChange={(e) => setInvestimento(e.target.value)} // Captura investimento
                />
              </div>
            </div>
            <label className={styles.label}>Descrição</label>
            <textarea
              className={styles.textarea}
              placeholder="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)} // Captura descrição
            />
            <button className={styles['btn-postar']} type="submit">
              Postar
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}