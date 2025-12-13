import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './styles/PostarIdeia.module.css';
import Sidebar from './components/Sidebar';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

export default function PostarIdeia() {
    const navigate = useNavigate();

    const [dropdownOpen, setDropdownOpen] = useState(false);

    const [selected, setSelected] = useState("");
    const [titulo, setTitulo] = useState("");
    const [investimento, setInvestimento] = useState("");
    const [descricao, setDescricao] = useState("");
    const [imagem, setImagem] = useState(null);

    const dropdownRef = useRef(null);

    useEffect(() => {
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

            if (response.ok) {
                alert("Ideia postada com sucesso!");
                navigate('/home');
            } else if (response.status === 403) {
                alert("Sessão expirada. Por favor, faça login novamente.");
                localStorage.removeItem('token');
                navigate('/');
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
            {/* Sidebar reutilizável */}
            <Sidebar />

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
                                        <button
                                            type="button"
                                            className={styles.selecionar}
                                            onClick={() => setDropdownOpen(!dropdownOpen)}
                                        >
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
                                                {["Educação", "Tecnologia", "Indústria alimentícia", "Indústria Cinematográfica", "Outros"].map((item) => (
                                                    <div
                                                        key={item}
                                                        className={styles['lista-itens']}
                                                        onClick={() => {
                                                            setSelected(item);
                                                            setDropdownOpen(false);
                                                        }}
                                                    >
                                                        {item}
                                                    </div>
                                                ))}
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