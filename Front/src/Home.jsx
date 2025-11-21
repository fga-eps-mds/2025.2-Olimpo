import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/Home.module.css";
import Sidebar from "./components/Sidebar"; // Usando o componente Sidebar!

// Imagens e Ícones
import lupa from './assets/lupa.png';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

// Função auxiliar para limpar valores monetários (R$ 10.000 -> 10000)
const parseCurrency = (valueStr) => {
    if (!valueStr) return 0;
    return parseFloat(valueStr.replace(/[^\d,]/g, '').replace(',', '.'));
};

function PostCard({ data }) {
    return (
        <article className={styles.card}>
            <header className={styles.cardHeader}>
                <div className={styles.userBlock}>
                    <div className={styles.avatar}>
                        {data.avatarUrl ? (
                            <img
                                src={data.avatarUrl}
                                alt={data.userName}
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                        ) : (
                            <div style={{
                                width: '100%', height: '100%',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                fontSize: '16px', color: '#363940', fontWeight: 'bold'
                            }}>
                                {data.userName ? data.userName.charAt(0).toUpperCase() : '?'}
                            </div>
                        )}
                    </div>
                    <div className={styles.userInfo}>
                        <span className={styles.name}>{data.userName}</span>
                        <span className={styles.date}>{data.date}</span>
                    </div>
                </div>

                <div className={styles.headerActions}>
                    <div className={styles.segmentBadge}>{data.segment}</div>
                    <button className={styles.moreDots}>⋮</button>
                </div>
            </header>

            {data.mediaUrl && (
                <div className={styles.mediaBox}>
                    <img
                        src={data.mediaUrl}
                        alt="Mídia"
                        style={{ width: '100%', height: '100%', objectFit: 'contain' }}
                    />
                </div>
            )}

            <h3 className={styles.cardTitle}>{data.title}</h3>
            <p className={styles.cardText}>{data.description}</p>

            <div style={{ marginTop: "8px" }}>
                <span className={styles.invest}>Investimento: </span>
                <span>{data.investment}</span>
            </div>
        </article>
    );
}

export default function Home() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // ESTADOS DE FILTRO
    const [searchTerm, setSearchTerm] = useState("");

    const [segmentoOpen, setSegmentoOpen] = useState(false);
    const [selectedSegmento, setSelectedSegmento] = useState("");

    const [investimentoOpen, setInvestimentoOpen] = useState(false);
    const [selectedInvestimento, setSelectedInvestimento] = useState("");

    const dropdownRef = useRef(null);

    // Fechar dropdowns ao clicar fora
    useEffect(() => {
        function handleClickOutside(event) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setSegmentoOpen(false);
                setInvestimentoOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // Buscar Ideias
    useEffect(() => {
        const fetchIdeas = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/');
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/api/ideas', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 403) {
                    localStorage.removeItem('token');
                    navigate('/');
                    return;
                }

                if (response.ok) {
                    const ideas = await response.json();

                    const mappedPosts = ideas.map(idea => ({
                        id: idea.id,
                        userName: idea.account.name,
                        avatarUrl: idea.account.pfp,
                        date: new Date(idea.time).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }),
                        segment: idea.keywords && idea.keywords.length > 0 ? idea.keywords[0].name : 'Geral',
                        title: idea.name,
                        description: idea.description,
                        priceRaw: idea.price, // Guardamos o valor numérico para filtrar
                        investment: new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(idea.price),
                        mediaUrl: idea.ideaFiles && idea.ideaFiles.length > 0 ? idea.ideaFiles[0].fileUrl : null
                    }));

                    setPosts(mappedPosts.reverse());
                }
            } catch (err) {
                console.error('Erro:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchIdeas();
    }, [navigate]);

    // LÓGICA DE FILTRAGEM
    const filteredPosts = posts.filter(post => {
        // 1. Filtro de Texto (Título ou Descrição)
        const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            post.description.toLowerCase().includes(searchTerm.toLowerCase());

        // 2. Filtro de Segmento
        const matchesSegment = selectedSegmento ? post.segment === selectedSegmento : true;

        // 3. Filtro de Investimento
        let matchesInvestment = true;
        if (selectedInvestimento) {
            const price = post.priceRaw;
            if (selectedInvestimento === 'Até R$ 10.000') matchesInvestment = price <= 10000;
            else if (selectedInvestimento === 'R$ 10.000 - R$ 50.000') matchesInvestment = price > 10000 && price <= 50000;
            else if (selectedInvestimento === 'R$ 50.000 - R$ 100.000') matchesInvestment = price > 50000 && price <= 100000;
            else if (selectedInvestimento === 'Acima de R$ 100.000') matchesInvestment = price > 100000;
        }

        return matchesSearch && matchesSegment && matchesInvestment;
    });

    return (
        <div className={styles.page}>
            {/* Sidebar Reutilizável */}
            <Sidebar />

            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>

                    {/* === ÁREA DE PESQUISA E FILTROS === */}
                    <div className={styles["search-section"]} ref={dropdownRef}>
                        <div className={styles["search-input-container"]}>
                            <input
                                className={styles["search-input"]}
                                type="text"
                                placeholder="Pesquisar ideias..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <img src={lupa} alt="Buscar" className={styles["search-icon"]} />
                        </div>

                        <div className={styles["filters-row"]}>
                            {/* Dropdown Segmento */}
                            <div className={styles["filter-dropdown"]}>
                                <button
                                    className={`${styles["filter-btn"]} ${selectedSegmento ? styles.active : ''}`}
                                    onClick={() => { setSegmentoOpen(!segmentoOpen); setInvestimentoOpen(false); }}
                                >
                                    {selectedSegmento || "Segmento"}
                                    <img src={segmentoOpen ? setaCima : setaBaixo} alt="seta" />
                                </button>

                                {segmentoOpen && (
                                    <div className={styles["dropdown-menu"]}>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSelectedSegmento(""); setSegmentoOpen(false); }}>Todos</div>
                                        {["Educação", "Tecnologia", "Indústria alimentícia", "Indústria Cinematográfica", "Outros"].map(opt => (
                                            <div key={opt} className={styles["dropdown-item"]} onClick={() => { setSelectedSegmento(opt); setSegmentoOpen(false); }}>{opt}</div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* Dropdown Investimento */}
                            <div className={styles["filter-dropdown"]}>
                                <button
                                    className={`${styles["filter-btn"]} ${selectedInvestimento ? styles.active : ''}`}
                                    onClick={() => { setInvestimentoOpen(!investimentoOpen); setSegmentoOpen(false); }}
                                >
                                    {selectedInvestimento || "Faixa de Preço"}
                                    <img src={investimentoOpen ? setaCima : setaBaixo} alt="seta" />
                                </button>

                                {investimentoOpen && (
                                    <div className={styles["dropdown-menu"]}>
                                        <div className={styles["dropdown-item"]} onClick={() => { setSelectedInvestimento(""); setInvestimentoOpen(false); }}>Qualquer valor</div>
                                        {['Até R$ 10.000', 'R$ 10.000 - R$ 50.000', 'R$ 50.000 - R$ 100.000', 'Acima de R$ 100.000'].map(opt => (
                                            <div key={opt} className={styles["dropdown-item"]} onClick={() => { setSelectedInvestimento(opt); setInvestimentoOpen(false); }}>{opt}</div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* Botão Limpar Filtros (aparece se algo estiver selecionado) */}
                            {(selectedSegmento || selectedInvestimento || searchTerm) && (
                                <button
                                    className={styles["filter-btn"]}
                                    style={{color: '#ff4b4b'}}
                                    onClick={() => { setSelectedSegmento(""); setSelectedInvestimento(""); setSearchTerm(""); }}
                                >
                                    Limpar Filtros ✕
                                </button>
                            )}
                        </div>
                    </div>

                    {/* === LISTA DE POSTS === */}
                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : filteredPosts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhum resultado encontrado</h3>
                            <p>Tente ajustar seus filtros de pesquisa.</p>
                        </div>
                    ) : (
                        <>
                            {filteredPosts.map((p) => <PostCard key={p.id} data={p} />)}
                            <div className={styles.endText}>Você viu todas as publicações</div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}