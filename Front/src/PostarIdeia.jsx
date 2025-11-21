import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom'; // Importação para redirecionamento
import styles from './styles/PostarIdeia.module.css';

// ... Mantenha suas importações de imagens (home, seta, etc.) aqui ...
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
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

export default function PostarIdeia() {
    const navigate = useNavigate(); // Hook para navegação

    const [hovered, setHovered] = useState(false);
    const [dropdownOpen, setDropdownOpen] = useState(false);

    const [selected, setSelected] = useState("");
    const [titulo, setTitulo] = useState("");
    const [investimento, setInvestimento] = useState("");
    const [descricao, setDescricao] = useState("");
    const [imagem, setImagem] = useState(null);

    const dropdownRef = useRef(null);

    useEffect(() => {
        // Verifica se tem token ao abrir a página
        const token = localStorage.getItem('token');
        if (!token) {
            alert("Você precisa estar logado para postar.");
            navigate('/');
        }

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
    }, [dropdownOpen, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!titulo || !selected || !investimento) {
            alert("Por favor, preencha os campos obrigatórios.");
            return;
        }

        const formData = new FormData();

        const ideaData = {
            name: titulo,
            description: descricao,
            price: parseFloat(investimento),
            keywords: [selected]
        };

        formData.append('data', new Blob([JSON.stringify(ideaData)], {
            type: 'application/json'
        }));

        if (imagem) {
            formData.append('file', imagem);
        }

        try {
            const token = localStorage.getItem('token');

            const response = await fetch('http://localhost:8080/api/ideas', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            // SE DER SUCESSO (200 OK)
            if (response.ok) {
                alert("Ideia postada com sucesso!");
                navigate('/home'); // <--- AQUI ESTÁ O REDIRECIONAMENTO PARA A HOME
            }
            // SE O TOKEN FOR INVÁLIDO (403 FORBIDDEN)
            else if (response.status === 403) {
                alert("Sessão expirada. Por favor, faça login novamente.");
                localStorage.removeItem('token'); // Limpa o token podre
                navigate('/'); // Manda pro login
            }
            // OUTROS ERROS
            else {
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
                    {/* Adicionei navegação nos botões da sidebar também */}
                    <button onClick={() => navigate('/home')} className={styles['icon-btn']}><img src={hovered ? home_hover : home} alt="Home" /><span>Início</span></button>
                    <button className={styles['icon-btn']}><img src={hovered ? coracao_hover : coracao} alt="Coracao" /><span>Notificações</span></button>
                    <button className={styles['icon-btn']}><img src={hovered ? seta_hover : seta} alt="Seta" /><span>Mensagens</span></button>
                    <button className={styles['icon-btn']}><img src={hovered ? lupa_hover : lupa} alt="Lupa" /><span>Pesquisar</span></button>
                    <button onClick={() => navigate('/postar-ideia')} className={styles['icon-btn']}><img src={hovered ? mais_hover : mais} alt="Mais" /><span>Postar</span></button>
                </nav>
                <div className={styles.profile}>
                    <button className={styles['profile-btn']}><img src={usuario} alt="Usuário" /><span>Perfil</span></button>
                </div>
            </aside>
            <main className={styles['main-content']}>
                <div className={styles['form-container']}>

                    <form className={styles['post-form']} onSubmit={handleSubmit}>

                        <label className={styles.label}>Imagem</label>
                        <input
                            className={styles['input-imagem']}
                            type="file"
                            accept="image/*"
                            onChange={(e) => setImagem(e.target.files[0])}
                        />

                        <label className={styles.label}>Título</label>
                        <input
                            className={styles.input}
                            type="text"
                            placeholder="Título"
                            value={titulo}
                            onChange={(e) => setTitulo(e.target.value)}
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
                                    type="number"
                                    placeholder="Investimento"
                                    value={investimento}
                                    onChange={(e) => setInvestimento(e.target.value)}
                                />
                            </div>
                        </div>
                        <label className={styles.label}>Descrição</label>
                        <textarea
                            className={styles.textarea}
                            placeholder="Descrição"
                            value={descricao}
                            onChange={(e) => setDescricao(e.target.value)}
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