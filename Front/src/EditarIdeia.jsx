import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import styles from './styles/EditarIdeia.module.css';
import { SEGMENTS } from './constants';
import Sidebar from './components/Sidebar';

import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

export default function EditarIdeia() {
    const navigate = useNavigate();
    const location = useLocation();
    const ideaToEdit = location.state?.idea;

    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [selected, setSelected] = useState("");
    const [titulo, setTitulo] = useState("");
    const [investimento, setInvestimento] = useState("");
    const [descricao, setDescricao] = useState("");

    const [imagem, setImagem] = useState(null);

    const dropdownRef = useRef(null);

    useEffect(() => {
        if (ideaToEdit) {
            setTitulo(ideaToEdit.title);
            setDescricao(ideaToEdit.description);
            const valor = ideaToEdit.priceRaw || String(ideaToEdit.investment).replace(/\D/g, '') / 100;
            setInvestimento(valor);
            setSelected(ideaToEdit.segment);
        } else {
            alert("Nenhuma ideia selecionada.");
            navigate('/home');
        }
    }, [ideaToEdit, navigate]);

    useEffect(() => {
        function handleClickOutside(event) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setDropdownOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!titulo || !selected || !investimento) {
            alert("Preencha os campos obrigatórios");
            return;
        }

        const token = localStorage.getItem('token');

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
            const response = await fetch(`http://localhost:8080/api/ideas/${ideaToEdit.id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (response.ok) {
                alert("Ideia atualizada com sucesso!");
                navigate('/home');
            } else {
                const err = await response.text();
                alert("Erro ao atualizar: " + err);
            }
        } catch (error) {
            console.error("Erro:", error);
            alert("Erro de conexão.");
        }
    };

    return (
        <div className={styles['container-root']}>
            <Sidebar />
            <main className={styles['main-content']}>
                <div className={styles['form-container']}>

                    <form className={styles['post-form']} onSubmit={handleSubmit}>

                        <label className={styles.label}>Imagem</label>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                            <input
                                className={styles['input-imagem']}
                                type="file"
                                accept="image/*"
                                onChange={(e) => setImagem(e.target.files[0])}
                            />
                            {!imagem && ideaToEdit?.mediaUrl && (
                                <span style={{ fontSize: '12px', color: '#666', marginLeft: '20px' }}>
                                    Imagem atual mantida. Escolha outra para alterar.
                                </span>
                            )}
                        </div>

                        <label className={styles.label}>Título</label>
                        <input
                            className={styles.input}
                            type="text"
                            placeholder="Título da ideia"
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
                                            {selected || "Selecione"}
                                            <span className={styles.seta}>
                                                <img src={dropdownOpen ? setaCima : setaBaixo} alt="seta" width={14} />
                                            </span>
                                        </button>
                                        {dropdownOpen && (
                                            <div className={styles['lista-selecionar']}>
                                                {SEGMENTS.map(opt => (
                                                    <div key={opt} className={styles['lista-itens']} onClick={() => { setSelected(opt); setDropdownOpen(false) }}>{opt}</div>
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
                                    placeholder="R$ 0,00"
                                    value={investimento}
                                    onChange={(e) => setInvestimento(e.target.value)}
                                />
                            </div>
                        </div>

                        <label className={styles.label}>Descrição</label>
                        <textarea
                            className={styles.textarea}
                            placeholder="Descreva sua ideia..."
                            value={descricao}
                            onChange={(e) => setDescricao(e.target.value)}
                        />

                        <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                            <button
                                type="button"
                                className={styles['btn-postar']}
                                style={{ background: '#b0b8c9', color: '#333' }}
                                onClick={() => navigate('/home')}
                            >
                                Cancelar
                            </button>
                            <button className={styles['btn-postar']} type="submit">
                                Salvar Alterações
                            </button>
                        </div>

                    </form>
                </div>
            </main>
        </div>
    );
}