import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/Home.module.css";
import Sidebar from "./components/Sidebar";

// Imagens
import lupa from './assets/lupa.png';
import setaBaixo from './assets/setaBaixo.png';
import setaCima from './assets/setaCima.png';

// Função para decodificar o JWT e pegar o email do usuário logado
const parseJwt = (token) => {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
        return null;
    }
};

function PostCard({ data, currentUserEmail, onDelete, onEdit }) {
    const [menuOpen, setMenuOpen] = useState(false);
    // Verifica se o usuário logado é o dono do post
    const isOwner = data.userEmail === currentUserEmail;

    return (
        <article className={styles.card}>
            <header className={styles.cardHeader}>
                <div className={styles.userBlock}>
                    <div className={styles.avatar}>
                        {data.avatarUrl ? (
                            <img src={data.avatarUrl} alt={data.userName} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }} />
                        ) : (
                            <div style={{ width: '100%', height: '100%', borderRadius: '50%', backgroundColor: '#f5f6fa', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '16px', color: '#363940', fontWeight: 'bold' }}>
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

                    {/* SÓ MOSTRA O BOTÃO SE FOR O DONO */}
                    {isOwner && (
                        <div style={{ position: 'relative' }}>
                            <button
                                className={styles.moreDots}
                                onClick={() => setMenuOpen(!menuOpen)}
                            >
                                ⋮
                            </button>
                            {menuOpen && (
                                <div className={styles.dropdownMenuPost}> {/* CSS novo aqui */}
                                    <div className={styles.dropdownItemPost} onClick={() => onEdit(data)}>Editar</div>
                                    <div className={styles.dropdownItemPost} onClick={() => onDelete(data.id)} style={{ color: 'red' }}>Excluir</div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </header>

            {data.mediaUrl && (
                <div className={styles.mediaBox}>
                    <img src={data.mediaUrl} alt="Mídia" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
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
    const [currentUserEmail, setCurrentUserEmail] = useState("");
    const navigate = useNavigate();

    // Estados de Filtro
    const [searchTerm, setSearchTerm] = useState("");
    const [segmentoOpen, setSegmentoOpen] = useState(false);
    const [selectedSegmento, setSelectedSegmento] = useState("");
    const [investimentoOpen, setInvestimentoOpen] = useState(false);
    const [selectedInvestimento, setSelectedInvestimento] = useState("");
    const dropdownRef = useRef(null);

    // Função de Deletar
    const handleDelete = async (ideaId) => {
        if (!window.confirm("Tem certeza que deseja excluir esta ideia?")) return;

        const token = localStorage.getItem('token');
        try {
            const response = await fetch(`http://localhost:8080/api/ideas/${ideaId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                setPosts(posts.filter(p => p.id !== ideaId));
                alert("Ideia excluída com sucesso!");
            } else {
                alert("Erro ao excluir ideia.");
            }
        } catch (error) {
            console.error("Erro ao deletar:", error);
        }
    };

    // Função de Editar (Redireciona com dados)
    const handleEdit = (idea) => {
        navigate('/editar-ideia', { state: { idea } });
    };

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

    useEffect(() => {
        const fetchIdeas = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/');
                return;
            }

            // Pega o email do usuário logado
            const userData = parseJwt(token);
            if (userData) setCurrentUserEmail(userData.sub);

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
                        userEmail: idea.account.email, // CRUCIAL para identificar o dono
                        userName: idea.account.name,
                        avatarUrl: idea.account.pfp,
                        date: new Date(idea.time).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }),
                        segment: idea.keywords && idea.keywords.length > 0 ? idea.keywords[0].name : 'Geral',
                        title: idea.name,
                        description: idea.description,
                        priceRaw: idea.price,
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

    // Filtros
    const filteredPosts = posts.filter(post => {
        const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            post.description.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesSegment = selectedSegmento ? post.segment === selectedSegmento : true;

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
            <Sidebar />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>

                    {/* BARRA DE BUSCA */}
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

                            {(selectedSegmento || selectedInvestimento || searchTerm) && (
                                <button
                                    className={styles["filter-btn"]}
                                    style={{color: '#ff4b4b'}}
                                    onClick={() => { setSelectedSegmento(""); setSelectedInvestimento(""); setSearchTerm(""); }}
                                >
                                    Limpar ✕
                                </button>
                            )}
                        </div>
                    </div>

                    {/* LISTA DE POSTS */}
                    {loading ? (
                        <div className={styles.loading}>Carregando publicações...</div>
                    ) : filteredPosts.length === 0 ? (
                        <div className={styles.noPosts}>
                            <h3>Nenhum resultado encontrado</h3>
                            <p>Tente ajustar seus filtros de pesquisa.</p>
                        </div>
                    ) : (
                        <>
                            {filteredPosts.map((p) => (
                                <PostCard
                                    key={p.id}
                                    data={p}
                                    currentUserEmail={currentUserEmail} // Passamos o email para o card saber se é dono
                                    onDelete={handleDelete}
                                    onEdit={handleEdit}
                                />
                            ))}
                            <div className={styles.endText}>Você viu todas as publicações</div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}